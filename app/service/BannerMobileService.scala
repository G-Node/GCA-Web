package service

import java.io.{File, FileNotFoundException}

import javax.persistence._
import models._
import play.Play
import plugins.DBUtil._

/**
 * Service class for mobile banners.
 */
class BannerMobileService(banPath: String) {

  /**
   * Get a banner by id.
   *
   * @param id The id of the banner.
   *
   * @return The requested banner.
   *
   * @throws NoResultException If the conference was not found
   */
  def get(id: String) : Banner = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT b FROM Banner b
           LEFT JOIN FETCH b.conference
           WHERE b.uuid = :uuid"""

      val query : TypedQuery[Banner] = em.createQuery(queryStr, classOf[Banner])
      query.setParameter("uuid", id)

      query.getSingleResult
    }
  }

  /**
   * Open the mobile image file that belongs to the banner;
   * if the mobile banner cannot be found, fallback to the
   * original banner.
   *
   * @param ban The banner to open.
   *
   * @return A file handler to the respective image file.
   */
  def openFile(ban: Banner) : File = {
    if (ban.uuid == null)
      throw new IllegalArgumentException("Unable to open file for banner without uuid")

    var file = new File(banPath, ban.uuid)

    if (!file.exists || !file.canRead)
      // If a low resolution file cannot be found, get back to the original file
      file = new File(Play.application().configuration().getString("file.ban_mobile_path", "./banner_mobile"), ban.uuid)
      if (!file.exists || !file.canRead)
        throw new FileNotFoundException("Unable to open the file for reading: " + file.toString)

    file
  }

}

/**
  * BannerMobileService companion object.
  */
object BannerMobileService {

  /**
    * Create a banner service using a banner path stored in the configuration under "file.ban_mobile_path".
    * As default the relative path "./banner_mobile" will be used.
    *
    * @return A new banner service.
    */
  def apply[A]() : BannerMobileService = {
    new BannerMobileService(Play.application().configuration().getString("file.ban_mobile_path", "./banner_mobile"))
  }

  def apply(banPath: String) = {
    new BannerMobileService(banPath)
  }

}


