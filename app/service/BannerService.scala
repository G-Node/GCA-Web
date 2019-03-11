package service

import java.io.{File, FileNotFoundException}

import javax.persistence._
import models._
import play.Play
import play.api.libs.Files.TemporaryFile
import plugins.DBUtil._

/**
  * Service class for banners.
  */
class BannerService(banPath: String) {

  /**
    * Get a banner by id.
    *
    * @param id      The id of the banner.
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
    * Create a new banner.
    * This action is restricted to all accounts owning the conference the
    * banner belongs to.
    *
    * @param ban     The banner object to create.
    * @param data    The actual image data of the respective banner.
    * @param conference   The conference the banner belongs to.
    * @param account The account uploading the banner.
    *
    * @return The created banner.
    *
    * @throws EntityNotFoundException If the account does not exist
    * @throws IllegalArgumentException If the conference has no uuid
    */
  def create(ban: Banner, data: TemporaryFile, conference: Conference, account: Account) : Banner = {
    val banCreated = transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val conferenceChecked = em.find(classOf[Conference], conference.uuid)
      if (conferenceChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + conference.uuid)

      conferenceChecked.banner.add(ban)
      ban.conference = conferenceChecked
      conferenceChecked.touch()

      em.persist(ban)

      val file = new File(banPath, ban.uuid)
      val parent = file.getParentFile

      if (!parent.exists()) {
        parent.mkdirs()
      }

      data.moveTo(file, replace = false)

      ban
    }

    get(banCreated.uuid)
  }

  /**
    * Delete an existing banner.
    * This action is restricted to all accounts owning the conference the
    * banner belongs to.
    *
    * @param id      The id of the banner.
    * @param account The account that performs the delete.
    *
    * @return True if the banner was deleted, false otherwise.
    */
  def delete(id: String, account: Account) : Unit = {
    transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val banChecked = em.find(classOf[Banner], id)
      if (banChecked == null)
        throw new EntityNotFoundException("Unable to find banner with uuid = " + id)

      val isConfOwner = banChecked.conference.owners.contains(accountChecked)
      val isAdmin = accountChecked.isAdmin
      if (! (isConfOwner || isAdmin))
        throw new IllegalAccessException("No permissions for banner with uuid = " + id)

      val file = new File(banPath, banChecked.uuid)
      if (file.exists())
        file.delete()

      banChecked.conference.banner.remove(banChecked)
      banChecked.conference.touch()
      banChecked.conference = null

      em.remove(banChecked)
    }
  }

  /**
    * Open the image file that belongs to the banner.
    *
    * @param ban The figure to open.
    *
    * @return A file handler to the respective image file.
    */
  def openFile(ban: Banner) : File = {
    if (ban.uuid == null)
      throw new IllegalArgumentException("Unable to open file for banner without uuid")

    val file = new File(banPath, ban.uuid)

    if (!file.exists || !file.canRead)
      throw new FileNotFoundException("Unable to open the file for reading: " + file.toString)

    file
  }

}

/**
  * FigureService companion object.
  */
object BannerService {

  /**
    * Create a banner service using a banner path stored in the configuration under "file.ban_path".
    * As default the relative path "./banner" will be used.
    *
    * @return A new banner service.
    */
  def apply[A]() : BannerService = {
    new BannerService(Play.application().configuration().getString("file.ban_path", "./banner"))
  }

  def apply(banPath: String) = {
    new BannerService(banPath)
  }

}

