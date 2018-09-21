package service

import java.io.{File, FileNotFoundException}
import javax.persistence._

import play.Play
import play.api.libs.Files.TemporaryFile
import models._
import plugins.DBUtil._

/**
 * Service class for mobile figures.
 */
class FigureMobileService(figPath: String) {

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
   * Open the mobile image file that belongs to the figure;
   * if the mobile figure cannot be found, fallback to the
   * original figure.
   *
   * @param fig The figure to open.
   *
   * @return A file handler to the respective image file.
   */
  def openFile(fig: Figure) : File = {
    if (fig.uuid == null)
      throw new IllegalArgumentException("Unable to open file for figure without uuid")

    var file = new File(figPath, fig.uuid)

    if (!file.exists || !file.canRead)
      // If a lowres file cannot be found, get back to the original file
      file = new File(Play.application().configuration().getString("file.fig_path", "./figures"), fig.uuid)
      if (!file.exists || !file.canRead)
        throw new FileNotFoundException("Unable to open the file for reading: " + file.toString)

    file
  }

}

/**
  * FigureMobileService companion object.
  */
object FigureMobileService {

  /**
    * Create a figure service using a figure path stored in the configuration under "file.fig_mobile_path".
    * As default the relative path "./figures_mobile" will be used.
    *
    * @return A new figure service.
    */
  def apply[A]() : FigureMobileService = {
    new FigureMobileService(Play.application().configuration().getString("file.fig_mobile_path", "./figures_mobile"))
  }

  def apply(figPath: String) = {
    new FigureMobileService(figPath)
  }

}
