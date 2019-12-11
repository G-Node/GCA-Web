// Copyright © 2019, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service

import java.io.{File, FileNotFoundException}

import javax.persistence._
import models._
import play.Play
import play.api.libs.Files.TemporaryFile
import plugins.DBUtil._
import com.sksamuel.scrimage._
import com.drew.imaging.ImageProcessingException
import Math.sqrt

/**
  * Service class for banners.
  */
class BannerService(banPath: String, banMobilePath: String) {

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
    * @throws EntityNotFoundException If the conference has no uuid
    * @throws IllegalAccessException If the user is not a conference owner or admin.
    */
  def create(ban: Banner, data: TemporaryFile, conference: Conference, account: Account) : Banner = {
    val banCreated = transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val conferenceChecked = em.find(classOf[Conference], conference.uuid)
      if (conferenceChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + conference.uuid)

      if (!(conferenceChecked.isOwner(account) || account.isAdmin))
        throw new IllegalAccessException("No permissions for conference with uuid = " + conferenceChecked.uuid)

      conferenceChecked.banner.add(ban)
      ban.conference = conferenceChecked
      conferenceChecked.touch()

      em.persist(ban)

      val file = new File(banPath, ban.uuid)
      val parent = file.getParentFile

      if (!parent.exists()) {
        parent.mkdirs()
      }

      data.moveTo(file, replace = true)

      try {
        val image_jpeg = Image.fromFile(file)
        image_jpeg.output(file)(nio.JpegWriter())
      } catch {
        case ipe: ImageProcessingException => println(ipe + ". Could not convert image.")
      }

      ban
    }

    get(banCreated.uuid)
  }

  /**
    * Upload mobile file for a banner.
    * This action is restricted to all accounts owning the conference the
    * banner belongs to.
    *
    * @param ban     The banner object.
    * @param conference   The conference the banner belongs to.
    * @param account The account uploading the banner.
    *
    * @throws EntityNotFoundException If the account does not exist
    * @throws EntityNotFoundException If the conference has no uuid
    * @throws IllegalAccessException If the user is not a conference owner or admin.
    */
  def uploadMobile(ban: Banner, conference: Conference, account: Account) : Unit = {
    transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val conferenceChecked = em.find(classOf[Conference], conference.uuid)
      if (conferenceChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + conference.uuid)

      if (!(conferenceChecked.isOwner(account) || account.isAdmin))
        throw new IllegalAccessException("No permissions for conference with uuid = " + conferenceChecked.uuid)

      val file = new File(banPath, ban.uuid)

      val mobile_file = new File(banMobilePath, ban.uuid)
      val mobile_parent = mobile_file.getParentFile

      if (!mobile_parent.exists()) {
        mobile_parent.mkdirs()
      }

      try {
        val image_jpeg = Image.fromFile(file)
        image_jpeg.output(mobile_file)(nio.JpegWriter())
      } catch {
        case ipe: ImageProcessingException => println(ipe + ". Could not convert image.")
      }

      val mobile_image_jpeg = Image.fromFile(mobile_file)

      try {
        while (mobile_file.length().toFloat > 200000.0) {
          val scaleFactor = sqrt(100000.0/mobile_file.length().toFloat)
          mobile_image_jpeg.scale(scaleFactor).output(mobile_file)(nio.JpegWriter())
        }
      } catch {
        case ipe: ImageProcessingException => println(ipe + " Could not resize mobile image.")
      }
    }
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
    *
    * @throws EntityNotFoundException If the account does not exist.
    * @throws EntityNotFoundException If the banner does not exist.
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

      val mobile_file = new File(banMobilePath, banChecked.uuid)
      if (mobile_file.exists())
        mobile_file.delete()

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
    *
    * @throws IllegalArgumentException If the banner has no uuid.
    * @throws EntityNotFoundException If the file does not exist or cannot be read.
    */
  def openFile(ban: Banner) : File = {
    if (ban.uuid == null)
      throw new IllegalArgumentException("Unable to open file for banner without uuid")

    val file = new File(banPath, ban.uuid)

    if (!file.exists || !file.canRead)
      throw new FileNotFoundException("Unable to open the file for reading: " + file.toString)

    file
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
  def openMobileFile(ban: Banner) : File = {
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
  * BannerService companion object.
  */
object BannerService {

  /**
    * Create a banner service using a banner path stored in the configuration under "file.ban_path".
    * As default the relative path "./banner" will be used.
    *
    * @return A new banner service.
    */
  def apply[A]() : BannerService = {
    new BannerService(Play.application().configuration().getString("file.ban_path", "./banner"),
      Play.application().configuration().getString("file.ban_mobile_path", "./banner_mobile"))
  }

  def apply(banPath: String, banMobilePath: String) = {
    new BannerService(banPath, banMobilePath)
  }

}

