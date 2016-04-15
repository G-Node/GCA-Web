package service

import java.io.{File, FileNotFoundException}
import javax.persistence._

import play.Play
import play.api.libs.Files.TemporaryFile
import models._
import plugins.DBUtil._

/**
 * Service class for figures.
 */
class FigureService(figPath: String) {

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

      data.moveTo(file, replace = false)

      fig
    }

    get(figCreated.uuid)
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

      if (!figChecked.abstr.owners.contains(accountChecked))
        throw new IllegalAccessException("No permissions for figure with uuid = " + id)

      val file = new File(figPath, figChecked.uuid)
      if (file.exists())
        file.delete()

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
   *
   * @return A new figure service.
   */
  def apply[A]() : FigureService = {
    new FigureService(Play.application().configuration().getString("file.fig_path", "./figures"))
  }

  def apply(figPath: String) = {
    new FigureService(figPath)
  }

}
