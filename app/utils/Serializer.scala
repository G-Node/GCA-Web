/**
 * Created by sobolev on 2/19/14.
 */
package utils

import java.net.URL
import java.util.{LinkedList => JLinkedList}
import collection.JavaConversions.asJavaCollection
import collection.JavaConversions._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._

/**
 * Conference serializer.
 *
 * @param baseUrl base URL from the configuration, like "http://hostname:port"
 * @param abstractsUrlTemplate URL template for abstracts, like "/conferences/<id>/abstracts"
 */
class ConferenceFormat(baseUrl: String, abstractsUrlTemplate: String) extends Format[Conference] {

  /**
   * Builds an URL to the related abstracts from a given object ID.
   *
   * @param id an ID of a Conference object to insert into the URL
   *
   * @return URL for related abstracts, like "/conferences/HNOPSADMHV/abstracts"
   */
  private def abstractsUrl(id: String) = {
    // builds java.net.URL to somehow verify it's an URL
    // maybe add more validation here
    new URL(baseUrl + abstractsUrlTemplate.replace("<id>", id)).toString
  }

  /**
   * A Reader for the Conference type.
   */
  implicit def R: Reads[Conference] = {
    // this function basically specifies how to
    // build an object from parsed JSON
    def build(uuid: Option[String], name: String): Conference = {
      Conference(uuid.toString, name)
    }
    (
      (__ \ "uuid").readNullable[String] and
      (__ \ "name").read[String]
    )(build _)
  }

  override def reads(json: JsValue): JsResult[Conference] = R.reads(json)

  override def writes(c: Conference): JsValue = {
    Json.obj(
      "name" -> c.name,
      "uuid" -> c.uuid,
      "abstracts" -> abstractsUrl(c.uuid)
    )
  }
}

/**
 * Account serializer.
 *
 * @param baseUrl base URL from the configuration, like "http://hostname:port"
 * @param abstractsUrlTemplate URL template for abstracts, like "/conferences/<id>/abstracts"
 */
class AccountFormat(baseUrl: String, abstractsUrlTemplate: String) extends Format[Account] {

  /**
   * Builds an URL to the related abstracts from a given object ID.
   *
   * @param id an ID of an Account object to insert into the URL
   *
   * @return URL for related abstracts, like "/conferences/HNOPSADMHV/abstracts"
   */
  private def abstractsUrl(id: String) = {
    // builds java.net.URL to somehow verify it's an URL
    // maybe add more validation here
    new URL(baseUrl + abstractsUrlTemplate.replace("<id>", id)).toString
  }

  /**
   * A Reader for the Account type.
   */
  implicit def R: Reads[Account] = {
    // this function basically specifies how to
    // build an object from parsed JSON
    def build(uuid: Option[String], mail: String): Account = {
      Account(uuid.toString, mail)
    }
    (
      (__ \ "uuid").readNullable[String] and
      (__ \ "mail").read[String]
    )(build _)
  }

  override def reads(json: JsValue): JsResult[Account] = R.reads(json)

  override def writes(account: Account): JsValue = {
    Json.obj(
      "uuid" -> account.uuid,
      "email" -> account.mail,
      "abstracts" -> abstractsUrl(account.uuid)
    )
  }
}

/**
 * Author serializer.
 */
class AuthorFormat extends Format[Author] {

  /**
   * A Reader for the Author type.
   */
  implicit def R: Reads[Author] = {
    // this function basically specifies how to
    // build an object from parsed JSON
    def build(uuid: Option[String],
              mail: String,
              firstName: String,
              middleName: String,
              lastName: String): Author = {
      Author(uuid.toString, mail, firstName, middleName, lastName, new Abstract())
    }
    (
      (__ \ "uuid").readNullable[String] and
      (__ \ "mail").read[String] and
      (__ \ "firstName").read[String] and
      (__ \ "middleName").read[String] and
      (__ \ "lastName").read[String]
    )(build _)
  }

  override def reads(json: JsValue): JsResult[Author] = R.reads(json)

  override def writes(a: Author): JsValue = {
    Json.obj(
      "uuid" -> a.uuid,
      "email" -> a.mail,
      "firstName" -> a.firstName,
      "middleName" -> a.middleName,
      "lastName" -> a.lastName
    )
  }
}

/**
 * Affiliation serializer.
 */
class AffiliationFormat extends Format[Affiliation] {

  /**
   * A Reader for the Affiliation type.
   */
  implicit def R: Reads[Affiliation] = {
    // this function basically specifies how to
    // build an object from parsed JSON
    def build(uuid: Option[String],
              address: String,
              country: String,
              department: String,
              name: String,
              section: String): Affiliation = {
      Affiliation(uuid.toString, address, country, department, name, section, new Abstract())
    }
    (
      (__ \ "uuid").readNullable[String] and
      (__ \ "address").read[String] and
      (__ \ "country").read[String] and
      (__ \ "department").read[String] and
      (__ \ "name").read[String] and
      (__ \ "section").read[String]
    )(build _)
  }

  override def reads(json: JsValue): JsResult[Affiliation] = R.reads(json)

  override def writes(a: Affiliation): JsValue = {
    Json.obj(
      "uuid" -> a.uuid,
      "address" -> a.address,
      "country" -> a.country,
      "department" -> a.department,
      "name" -> a.name,
      "section" -> a.section
    )
  }
}


/**
* Reference serializer.
 */
class ReferenceFormat extends Format[Reference] {

  /**
   * A Reader for the Reference type.
   */
  implicit def R: Reads[Reference] = {
    // this function basically specifies how to
    // build an object from parsed JSON
    def build(uuid: Option[String],
              authors: String,
              title: String,
              year: Int,
              doi: String): Reference = {
      Reference(uuid.toString, authors, title, year, doi, new Abstract())
    }
    (
      (__ \ "uuid").readNullable[String] and
      (__ \ "authors").read[String] and
      (__ \ "title").read[String] and
      (__ \ "year").read[Int] and
      (__ \ "doi").read[String]
    )(build _)
  }

  override def reads(json: JsValue): JsResult[Reference] = R.reads(json)

  override def writes(a: Reference): JsValue = {
    Json.obj(
      "uuid" -> a.uuid,
      "authors" -> a.authors,
      "title" -> a.title,
      "year" -> a.year,
      "doi" -> a.doi
    )
  }
}

/**
 * Abstract serializer.
 *
 * @param baseUrl base URL from the configuration, like "http://hostname:port"
 * @param ownersUrlTemplate URL template for owners, like "/abstracts/<id>/owners"
 */
class AbstractFormat(baseUrl: String, ownersUrlTemplate: String) extends Format[Abstract] with ConstraintReads {

  val authorF = new AuthorFormat()
  val affiliationF = new AffiliationFormat()
  val referenceF = new ReferenceFormat()

  /**
   * Builds an URL to the related abstracts from a given object ID.
   *
   * @param id an ID of a Conference object to insert into the URL
   *
   * @return URL for related abstracts, like "/conferences/HNOPSADMHV/abstracts"
   */
  private def ownersUrl(id: String) = {
    // builds java.net.URL to somehow verify it's an URL
    // maybe add more validation here
    new URL(baseUrl + ownersUrlTemplate.replace("<id>", id)).toString
  }

  /**
   * A Reader for the Conference type.
   */
  implicit def R: Reads[Abstract] = {
    // this function basically specifies how to
    // build an object from parsed JSON
    def build(uuid: Option[String],
              title: String,
              topic: String,
              text: String,
              doi: String,
              conflictOfInterest: String,
              acknowledgements: String,
              approved: Boolean,
              published: Boolean,
              authors: List[Author],
              affiliations: List[Affiliation],
              references: List[Reference]): Abstract = {
      Abstract(uuid.toString, title, topic, text, doi, conflictOfInterest, acknowledgements,
        approved, published, new Conference(), null, null, new JLinkedList(asJavaCollection(authors)),
        new JLinkedList(asJavaCollection(affiliations)), new JLinkedList(asJavaCollection(references)))
    }
    (
      (__ \ "uuid").readNullable[String] and
      (__ \ "title").read[String] and
      (__ \ "topic").read[String] and
      (__ \ "text").read[String] and
      (__ \ "doi").read[String] and
      (__ \ "conflictOfInterest").read[String] and
      (__ \ "acknowledgements").read[String] and
      (__ \ "approved").read[Boolean] and
      (__ \ "published").read[Boolean] and
      (__ \ "authors").lazyRead( list[Author](authorF.R) ) and
      (__ \ "affiliations").lazyRead( list[Affiliation](affiliationF.R) ) and
      (__ \ "references").lazyRead( list[Reference](referenceF.R) )
    )(build _)
  }

  override def reads(json: JsValue): JsResult[Abstract] = R.reads(json)

  override def writes(a: Abstract): JsValue = {
    val authors: Seq[Author] = a.authors
    val affiliations: Seq[Affiliation] = a.affiliations
    val references: Seq[Reference] = a.references
    Json.obj(
      "uuid" -> a.uuid,
      "title" -> a.title,
      "topic" -> a.topic,
      "text" -> a.text,
      "doi" -> a.doi,
      "conflictOfInterest" -> a.conflictOfInterest,
      "acknowledgements" -> a.acknowledgements,
      "approved" -> a.approved,
      "published" -> a.published,
      "conference" -> a.conference.uuid,
      "figure" -> a.figure.uuid, // TODO: build URL
      "owners" -> ownersUrl(a.uuid),
      "authors" -> JsArray( for (auth <- authors) yield authorF.writes(auth) ),
      "affiliations" -> JsArray( for (auth <- affiliations) yield affiliationF.writes(auth) ),
      "references" -> JsArray( for (auth <- references) yield referenceF.writes(auth) )
    )
  }
}
