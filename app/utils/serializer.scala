package utils

import java.net.URL
import collection.JavaConversions._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.data.validation.ValidationError

package object serializer {

  implicit class PositionedListReads[A <: PositionedModel](r: Reads[List[A]]) {
    def addPosition(): Reads[List[A]] = { r.map { l =>
        for { (element, i) <- l.zipWithIndex } yield {
          element.position = i; element
        }
      }
    }
  }

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
    private val dateFormatter = ISODateTimeFormat.dateTime().withZoneUTC()
    
    def writes(date: DateTime) = {
      Json.toJson(dateFormatter.print(date))
    }

    def reads(json: JsValue): JsResult[DateTime] = {
      json match {
        case JsString(s) => JsSuccess(dateFormatter.parseDateTime(s))
        case JsNumber(n) => JsSuccess(new DateTime(n.toLong))
        case _ => JsError(Seq(JsPath() -> Seq(ValidationError("error.expected.jodadate.format.iso8601"))))
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
      (__ \ "group").readNullable[String] and
      (__ \ "cite").readNullable[String] and
      (__ \ "link").readNullable[String] and
      (__ \ "description").readNullable[String] and
      (__ \ "isOpen").readNullable[Boolean] and
      (__ \ "isPublished").readNullable[Boolean] and
      (__ \ "isActive").readNullable[Boolean] and
      (__ \ "hasPresentationPrefs").readNullable[Boolean] and
      (__ \ "start").readNullable[DateTime] and
      (__ \ "end").readNullable[DateTime] and
      (__ \ "deadline").readNullable[DateTime] and
      (__ \ "logo").readNullable[String] and
      (__ \ "thumbnail").readNullable[String] and
      (__ \ "iOSApp").readNullable[String] and
      (__ \ "groups").read[List[AbstractGroup]] and
      (__ \ "topics").read[List[Topic]].addPosition and
        (__ \ "mAbsLeng").readNullable[Int] and
        (__ \ "mFigs").readNullable[Int]
    )(Conference(_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, Nil, Nil, _,
      null,null,null,_,_)).reads(json)

    override def writes(c: Conference): JsValue = {
      val groups: Seq[AbstractGroup] = asScalaSet(c.groups).toSeq.sortBy(x => x.prefix)
      Json.obj(
        "name" -> c.name,
        "uuid" -> c.uuid,
        "short" -> c.short,
        "group" -> c.group,
        "cite" -> c.cite,
        "link" -> c.link,
        "description" -> c.description,
        "isOpen" -> c.isOpen,
        "isPublished" -> c.isPublished,
        "isActive" -> c.isActive,
        "hasPresentationPrefs" -> c.hasPresentationPrefs,
        "groups" ->  groups,
        "start" -> c.startDate,
        "end" -> c.endDate,
        "deadline" -> c.deadline,
        "logo" -> c.logo,
        "thumbnail" -> c.thumbnail,
        "iOSApp" -> c.iOSApp,
        "abstracts" -> routesResolver.abstractsUrl(c.uuid),
        "allAbstracts" -> routesResolver.allAbstractsUrl(c.uuid),
        "topics" -> c.topics.toSeq.sorted[Model],
        "geo" -> routesResolver.geoUrl(c.uuid),
        "schedule" -> routesResolver.scheduleUrl(c.uuid),
        "info" -> routesResolver.infoUrl(c.uuid),
        "mAbsLeng" -> c.abstractMaxLength,
        "mFigs" -> c.abstractMaxFigures
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
        "mail" -> account.mail,
        "fullName" -> account.fullName,
        "ctime" -> account.ctime,
        "abstracts" -> routesResolver.abstractsUrl(account.uuid),
        "favAbstracts" -> routesResolver.favAbstractsUrl(account.uuid)
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
      (__ \ "affiliations").read[Seq[Int]]
    )(Author(_, _, _, _, _, None, Nil, _)).reads(json)

    override def writes(a: Author): JsValue = {
      Json.obj(
        "uuid" -> a.uuid,
        "mail" -> a.mail,
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
      (__ \ "section").readNullable[String]
    )(Affiliation(_, _, _, _, _)).reads(json)

    override def writes(a: Affiliation): JsValue = {
      Json.obj(
        "uuid" -> a.uuid,
        "address" -> a.address,
        "country" -> a.country,
        "department" -> a.department,
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
      (__ \ "text").readNullable[String] and
      (__ \ "link").readNullable[String] and
      (__ \ "doi").readNullable[String]
    )(Reference(_, _, _, _)).reads(json)

    override def writes(a: Reference): JsValue = {
      Json.obj(
        "uuid" -> a.uuid,
        "text" -> a.text,
        "link" -> a.link,
        "doi" -> a.doi
      )
    }
  }

  /**
   * Figure serializer.
   */
  class FigureFormat(implicit routesResolver: RoutesResolver) extends Format[Figure] {

    override def reads(json: JsValue): JsResult[Figure] = (
      (__ \ "uuid").readNullable[String] and
      (__ \ "caption").readNullable[String] and
      (__ \ "position").readNullable[Int]
    )(Figure(_, _, _)).reads(json)

    override def writes(a: Figure): JsValue = {
      if (a == null) {
        JsNull
      } else {
        Json.obj(
          "uuid" -> a.uuid,
          "caption" -> a.caption,
          "position" -> a.position,
          "URL" -> routesResolver.figureFileUrl(a.uuid)
        )
      }
    }
  }

  class StateLogWrites extends Writes[StateLogEntry] {

    override def writes(l: StateLogEntry): JsValue = {
      Json.obj(
        "timestamp" -> l.timestamp,
        "state" -> l.state,
        "editor" -> l.editor,
        "note" -> l.note
      )
    }
  }

  /**
   * Abstract serializer.
   *
   * @param routesResolver a RoutesResolver to resolve urls
   */
  class AbstractFormat(implicit routesResolver: RoutesResolver) extends Format[Abstract] with ConstraintReads {

    implicit val authorF = new AuthorFormat()
    implicit val affiliationF = new AffiliationFormat()
    implicit val referenceF = new ReferenceFormat()
    implicit val figureF = new FigureFormat()
    implicit val abtsrTyepF = new AbstractGroupFormat()

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
      (__ \ "authors").read[List[Author]].addPosition and
      (__ \ "affiliations").read[List[Affiliation]].addPosition and
      (__ \ "references").read[List[Reference]].addPosition and
        (__ \ "abstrTypes").read[List[AbstractGroup]]
    )(Abstract(_, _, _, _, _, _, _, _, _, _, _, None, Nil, Nil, Nil, _, _, _,_)).reads(json)

    override def writes(a: Abstract): JsValue = {

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
        "mtime" -> a.mtime,
        "conference" -> routesResolver.conferenceUrl(a.conference.uuid),
        "figures" -> asScalaSet(a.figures).toSeq.sorted[Model],
        "owners" -> routesResolver.ownersUrl(a.uuid),
        "favUsers" -> routesResolver.favUsersUrl(a.uuid),
        "authors" -> asScalaSet(a.authors).toSeq.sorted[Model],
        "affiliations" -> asScalaSet(a.affiliations).toSeq.sorted[Model],
        "references" -> asScalaSet(a.references).toSeq.sorted[Model],
        "abstrTypes" -> asScalaSet(a.abstrTypes).toSeq,
        "stateLog" -> routesResolver.stateLogUrl(a.uuid))
    }
  }
}
