package utils

import java.net.URL
import collection.JavaConversions._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.data.validation.ValidationError

package object serializer {

  private implicit val urlWrites = new Writes[URL] {
    def writes(url: URL) = {
      if (url == null) {
        JsNull
      } else {
        Json.toJson(url.toString)
      }
    }
  }

  private implicit val stateWrites = new Format[AbstractState.State] {
    override def writes(s: AbstractState.State): JsValue = JsString(s.toString)

    override def reads(json: JsValue): JsResult[AbstractState.State] = json match {
      case JsString(str) => JsSuccess(AbstractState.withName(str))
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.string"))))
    }
  }

  private implicit val dateFormat = new Format[DateTime] {
    private val datePattern = "MM/dd/yyyy HH:mm a"
    private val dateFormatter = DateTimeFormat.forPattern(datePattern)
    
    def writes(date: DateTime) = {
      Json.toJson(dateFormatter.print(date))
    }

    def reads(json: JsValue): JsResult[DateTime] = {
      json match {
        case JsString(s) => JsSuccess(dateFormatter.parseDateTime(s))
        case JsNumber(n) => JsSuccess(new DateTime(n.toLong))
        case _ => JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.jodadate.format", datePattern))))
      }
    }

  }

  class AbstractGroupFormat() extends Format[AbstractGroup] {
    override def reads(json: JsValue): JsResult[AbstractGroup] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "prefix").readNullable[Int] and
      (__ \ "name").readNullable[String] and
      (__ \ "short").readNullable[String]
      )(AbstractGroup(_, _, _, _)).reads(json)

    override def writes(g: AbstractGroup): JsValue = {
      Json.obj(
        "uuid" -> g.uuid,
        "prefix" -> g.prefix,
        "name" -> g.name,
        "short" -> g.short
      )
    }
  }

  class TopicFormat extends Format[Topic] {

    override def writes(o: Topic): JsValue = JsString(o.topic)

    override def reads(json: JsValue): JsResult[Topic] = JsSuccess(Topic(json.as[String], None))

  }

  /**url
   * Conference serializer.
   *
   * @param routesResolver a RoutesResolver to resolve urls
   */
  class ConferenceFormat(implicit routesResolver: RoutesResolver) extends Format[Conference] {

    implicit val agf = new AbstractGroupFormat()
    implicit val tf = new TopicFormat()

    override def reads(json: JsValue): JsResult[Conference] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "short").readNullable[String] and
      (__ \ "cite").readNullable[String] and
      (__ \ "link").readNullable[String] and
      (__ \ "isOpen").readNullable[Boolean] and
      (__ \ "start").readNullable[DateTime] and
      (__ \ "end").readNullable[DateTime] and
      (__ \ "deadline").readNullable[DateTime] and
      (__ \ "logo").readNullable[String] and
      (__ \ "thumbnail").readNullable[String] and

      (__ \ "groups").read[List[AbstractGroup]] and
      (__ \ "topics").read[List[Topic]]
    )(Conference(_, _, _, _, _, _, _, _, _, _, _, _, Nil, Nil, _)).reads(json)

    override def writes(c: Conference): JsValue = {
      val groups: Seq[AbstractGroup] = asScalaSet(c.groups).toSeq
      Json.obj(
        "name" -> c.name,
        "uuid" -> c.uuid,
        "short" -> c.short,
        "cite" -> c.cite,
        "link" -> c.link,
        "isOpen" -> c.isOpen,
        "groups" ->  groups,
        "start" -> c.startDate,
        "end" -> c.endDate,
        "deadline" -> c.deadline,
        "logo" -> c.logo,
        "thumbnail" -> c.thumbnail,
        "abstracts" -> routesResolver.abstractsUrl(c.uuid),
        "topics" -> JsArray(for (topic <- c.topics.toSeq) yield tf.writes(topic))
      )
    }
  }

  /**
   * Account serializer.
   *
   * @param routesResolver a RoutesResolver to resolve urls
   */
  class AccountFormat(implicit routesResolver: RoutesResolver) extends Format[Account] {

    override def reads(json: JsValue): JsResult[Account] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "mail").readNullable[String]
    )(Account(_, _)).reads(json)

    override def writes(account: Account): JsValue = {
      Json.obj(
        "uuid" -> account.uuid,
        "email" -> account.mail,
        "abstracts" -> routesResolver.abstractsUrl(account.uuid)
      )
    }
  }

  /**
   * Author serializer.
   */
  class AuthorFormat extends Format[Author] {

    override def reads(json: JsValue): JsResult[Author] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "mail").readNullable[String] and
      (__ \ "firstName").readNullable[String] and
      (__ \ "middleName").readNullable[String] and
      (__ \ "lastName").readNullable[String] and
      (__ \ "position").readNullable[Int] and
      (__ \ "affiliations").read[Seq[Int]]
    )(Author(_, _, _, _, _, _, None, Nil, _)).reads(json)

    override def writes(a: Author): JsValue = {
      Json.obj(
        "uuid" -> a.uuid,
        "email" -> a.mail,
        "firstName" -> a.firstName,
        "middleName" -> a.middleName,
        "lastName" -> a.lastName,
        "position" -> a.position,
        "affiliations" -> (for (affiliation <- a.affiliations) yield affiliation.position)
      )
    }
  }

  /**
   * Affiliation serializer.
   */
  class AffiliationFormat extends Format[Affiliation] {

    override def reads(json: JsValue): JsResult[Affiliation] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "address").readNullable[String] and
      (__ \ "country").readNullable[String] and
      (__ \ "department").readNullable[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "section").readNullable[String] and
      (__ \ "position").readNullable[Int]
    )(Affiliation(_, _, _, _, _, _, _)).reads(json)

    override def writes(a: Affiliation): JsValue = {
      Json.obj(
        "uuid" -> a.uuid,
        "address" -> a.address,
        "country" -> a.country,
        "department" -> a.department,
        "name" -> a.name,
        "section" -> a.section,
        "position" -> a.position
      )
    }
  }

  /**
   * Reference serializer.
   */
  class ReferenceFormat extends Format[Reference] {

    override def reads(json: JsValue): JsResult[Reference] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "authors").readNullable[String] and
      (__ \ "title").readNullable[String] and
      (__ \ "year").readNullable[Int] and
      (__ \ "doi").readNullable[String]
    )(Reference(_, _, _, _, _)).reads(json)

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
   * Figure serializer.
   */
  class FigureFormat extends Format[Figure] {

    override def reads(json: JsValue): JsResult[Figure] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "name").readNullable[String] and
      (__ \ "caption").readNullable[String]
    )(Figure(_, _, _)).reads(json)

    override def writes(a: Figure): JsValue = {
      if (a == null) {
        JsNull
      } else {
        Json.obj(
          "uuid" -> a.uuid,
          "name" -> a.name,
          "caption" -> a.caption,
          "URL" -> a.uuid  // TODO: build URL
        )
      }
    }
  }

  /**
   * Abstract serializer.
   *
   * @param routesResolver a RoutesResolver to resolve urls
   */
  class AbstractFormat(implicit routesResolver: RoutesResolver) extends Format[Abstract] with ConstraintReads {

    val authorF = new AuthorFormat()
    val affiliationF = new AffiliationFormat()
    val referenceF = new ReferenceFormat()
    val figureF = new FigureFormat()

    override def reads(json: JsValue): JsResult[Abstract] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "title").readNullable[String] and
      (__ \ "topic").readNullable[String] and
      (__ \ "text").readNullable[String] and
      (__ \ "doi").readNullable[String] and
      (__ \ "conflictOfInterest").readNullable[String] and
      (__ \ "acknowledgements").readNullable[String] and
      (__ \ "isTalk").readNullable[Boolean] and
      (__ \ "reasonForTalk").readNullable[String] and
      (__ \ "sortId").readNullable[Int] and
      (__ \ "state").readNullable[AbstractState.State] and
      (__ \ "authors").lazyRead( list[Author](authorF) ) and
      (__ \ "affiliations").lazyRead( list[Affiliation](affiliationF) ) and
      (__ \ "references").lazyRead( list[Reference](referenceF) )
    )(Abstract(_, _, _, _, _, _, _, _, _, _, _, None, Nil, Nil, _, _, _)).reads(json)

    override def writes(a: Abstract): JsValue = {
      val figures: Seq[Figure] = asScalaSet(a.figures).toSeq
      val authors: Seq[Author] = asScalaSet(a.authors).toSeq
      val affiliations: Seq[Affiliation] = asScalaSet(a.affiliations).toSeq
      val references: Seq[Reference] = asScalaSet(a.references).toSeq
      Json.obj(
        "uuid" -> a.uuid,
        "title" -> a.title,
        "topic" -> a.topic,
        "text" -> a.text,
        "doi" -> a.doi,
        "conflictOfInterest" -> a.conflictOfInterest,
        "acknowledgements" -> a.acknowledgements,
        "isTalk" -> a.isTalk,
        "reasonForTalk" -> a.reasonForTalk,
        "sortId" -> a.sortId,
        "state" -> a.state,
        "conference" -> a.conference.uuid,
        "figures" -> JsArray( for (auth <- figures) yield figureF.writes(auth) ),
        "owners" -> routesResolver.ownersUrl(a.uuid),
        "authors" -> JsArray( for (auth <- authors) yield authorF.writes(auth) ),
        "affiliations" -> JsArray( for (auth <- affiliations) yield affiliationF.writes(auth) ),
        "references" -> JsArray( for (auth <- references) yield referenceF.writes(auth) )
      )
    }
  }

}
