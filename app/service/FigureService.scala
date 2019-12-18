package service

import java.io.{File, FileNotFoundException}
import java.nio.file.{Paths, NoSuchFileException}

import javax.persistence._
import play.Play
import play.api.libs.Files.TemporaryFile
import models._
import plugins.DBUtil._
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.JpegWriter
import org.apache.commons.io.FileUtils
import com.drew.imaging.ImageProcessingException
import Math.sqrt

import scala.util.control.Breaks.{break, breakable}

/**
 * Service class for figures.
 */
class FigureService(figPath: String, figMobilePath: String) {

  /**
   * Get a figure by id.
   *
   * @param id      The id of the figure.
   *
   * @return The requested figure.
   *
   * @throws NoResultException If the conference was not found
   */
  def get(id: String) : Figure = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT f FROM Figure f
           LEFT JOIN FETCH f.abstr
           WHERE f.uuid = :uuid"""

      val query : TypedQuery[Figure] = em.createQuery(queryStr, classOf[Figure])
      query.setParameter("uuid", id)

      query.getSingleResult
    }
  }

  /**
   * Create a new figure.
   * This action is restricted to all accounts owning the abstract the
   * figure belongs to.
   *
   * @param fig     The figure object to create.
   * @param data    The actual image data of the respective figure.
   * @param abstr   The abstract the figure belongs to.
   * @param account The account uploading the figure.
   *
   * @return The created figure.
   *
   * @throws EntityNotFoundException If the account does not exist
   * @throws IllegalArgumentException If the abstract has no uuid
   */
  def create(fig: Figure, data: TemporaryFile, abstr: Abstract, account: Account) : Figure = {
    val figCreated = transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val abstractChecked = em.find(classOf[Abstract], abstr.uuid)
      if (abstractChecked == null)
        throw new EntityNotFoundException("Unable to find abstract with uuid = " + abstr.uuid)

      abstractChecked.figures.add(fig)
      fig.abstr = abstractChecked
      abstractChecked.touch()

      em.persist(fig)

      val file = new File(figPath, fig.uuid)
      val parent = file.getParentFile

      if (!parent.exists()) {
        parent.mkdirs()
      }

      data.moveTo(file, replace = true)

      fig
    }

    get(figCreated.uuid)
  }

  /**
    * Upload a new mobile image for already created figure.
    * This action is restricted to all accounts owning the abstract the
    * figure belongs to.
    *
    * @param fig     The figure object.
    * @param abstr   The abstract the figure belongs to.
    * @param account The account uploading the figure.
    *
    * @throws EntityNotFoundException If the account does not exist
    * @throws IllegalArgumentException If the abstract has no uuid
    */
  def uploadMobile(fig: Figure,  abstr: Abstract, account: Account) : Unit = {
    transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val abstractChecked = em.find(classOf[Abstract], abstr.uuid)
      if (abstractChecked == null)
        throw new EntityNotFoundException("Unable to find abstract with uuid = " + abstr.uuid)


      // In a docker environment '/target/universal/stage' is used when resolving '.'
      // in a relative path. 'java.io.File' seems to properly resolve the relative paths
      // even in a docker environment, but the third party library scrimage cannot work
      // with these file descriptors and requires file descriptors created with absolute paths.
      //   As a workaround the file not found exception is caught and the docker container
      // root path is used to create file descriptior with absolut paths appropriate for
      // the docker environment.
      var figure_file = new File(figPath, fig.uuid)
      var mobile_file = new File(figMobilePath, fig.uuid)

      try {
        var mobile_image = Image.fromFile(figure_file)
      } catch {
        case nofile: NoSuchFileException => {
          val figure_path = Paths.get("/srv", "gca", figPath, fig.uuid).normalize.toString
          val mobile_path = Paths.get("/srv", "gca", figMobilePath, fig.uuid).normalize.toString

          figure_file = new File(figure_path)
          mobile_file = new File(mobile_path)
        }
      }

      var mobile_image = Image.fromFile(figure_file)

      val mobile_parent = mobile_file.getParentFile
      if (!mobile_parent.exists()) {
        mobile_parent.mkdirs()
      }

      var current_size = mobile_image.bytes.length.toFloat
      var scaleFactor = 1.0

      breakable {
        for (i <- 1 to 10) {
          if (current_size > 2500000.0) {
            scaleFactor = sqrt(1250000.0 / current_size)
            mobile_image = mobile_image.scale(scaleFactor)
            current_size = mobile_image.bytes.length.toFloat
          } else {
            break
          }
        }
      }
      mobile_image.output(mobile_file)(JpegWriter().withCompression(25))
    }
  }

  /**
   * Update a figure, only name and caption can be updated not the image data.
   * This action is restricted to all accounts owning the abstract the
   * figure belongs to.
   *
   * @param fig     The figure to update.
   * @param account The account updating the figure.
   *
   * @return The created figure.
   */
  def update(fig: Figure, account: Account) : Figure = {
    val figUpdated = transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val figChecked = em.find(classOf[Figure], fig.uuid)
      if (figChecked == null)
        throw new EntityNotFoundException("Unable to find figure with uuid = " + account.uuid)

      val isOwner = figChecked.abstr.owners.contains(accountChecked)
      val isConfOwner = figChecked.abstr.conference.owners.contains(accountChecked)
      val isAdmin = accountChecked.isAdmin
      if (! (isOwner || isConfOwner || isAdmin))
        throw new IllegalAccessException("No permissions for figure with uuid = " + fig.uuid)

      fig.abstr.touch()

      em.merge(fig)
    }

    get(figUpdated.uuid)
  }

  /**
   * Delete an existing figure.
   * This action is restricted to all accounts owning the abstract the
   * figure belongs to.
   *
   * @param id      The id of the figure.
   * @param account The account that performs the delete.
   *
   * @return True if the figure was deleted, false otherwise.
   */
  def delete(id: String, account: Account) : Unit = {
    transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val figChecked = em.find(classOf[Figure], id)
      if (figChecked == null)
        throw new EntityNotFoundException("Unable to find figure with uuid = " + account.uuid)

      val isOwner = figChecked.abstr.owners.contains(accountChecked)
      val isConfOwner = figChecked.abstr.conference.owners.contains(accountChecked)
      val isAdmin = accountChecked.isAdmin
      if (! (isOwner || isConfOwner || isAdmin))
        throw new IllegalAccessException("No permissions for figure with uuid = " + id)

      val file = new File(figPath, figChecked.uuid)
      if (file.exists())
        file.delete()

      val mobile_file = new File(figMobilePath, figChecked.uuid)
      if (mobile_file.exists())
        mobile_file.delete()

      figChecked.abstr.figures.remove(figChecked)
      figChecked.abstr.touch()
      figChecked.abstr = null

      em.remove(figChecked)
    }
  }

  /**
   * Open the image file that belongs to the figure.
   *
   * @param fig The figure to open.
   *
   * @return A file handler to the respective image file.
   */
  def openFile(fig: Figure) : File = {
    if (fig.uuid == null)
      throw new IllegalArgumentException("Unable to open file for figure without uuid")

    val file = new File(figPath, fig.uuid)

    if (!file.exists || !file.canRead)
      throw new FileNotFoundException("Unable to open the file for reading: " + file.toString)

    file
  }

}

/**
 * FigureService companion object.
 */
object FigureService {

  /**
   * Create a figure service using a figure path stored in the configuration under "file.fig_path".
   * As default the relative path "./figures" will be used.
   * A mobile figure will be stored as well.
   * As default the relative path "./figures_mobile" will be used.
   *
   * @return A new figure service.
   */
  def apply[A]() : FigureService = {
    new FigureService(Play.application().configuration().getString("file.fig_path", "./figures"),
      Play.application().configuration().getString("file.fig_mobile_path", "./figures_mobile"))
  }

  def apply(figPath: String, figMobilePath: String) = {
    new FigureService(figPath, figMobilePath)
  }

}
