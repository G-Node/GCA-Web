package service

import java.util.{List => JList}
import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.IdentityId
import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import org.joda.time.DateTime
import utils.AnormExtension._
import service.util.{EntityManagerProvider, DBUtil}
import javax.persistence.TypedQuery
import models.Account
import collection.JavaConversions._


class UserStore(application: Application) extends UserServicePlugin(application) with DBUtil {

  def resultToAccount(result: JList[Account]) : Option[Account] = {
    result.size() match {
      case 0 => None
      case 1 => Some(result.get(0))
      case _ => throw new RuntimeException("42. 31337. Should not happen!")
    }
  }

  def list() : Seq[Account] = {

    implicit val emp = EntityManagerProvider.fromDefaultPersistenceUnit()

    dbQuery { em =>
      val builder = em.getCriteriaBuilder
      val criteria = builder.createQuery(classOf[Account])
      val query = em.createQuery(criteria)

      asScalaBuffer(query.getResultList)
    }
  }

  def findAccount(id: IdentityId) : Option[Account] = {

    implicit val emp = EntityManagerProvider.fromDefaultPersistenceUnit()

    val user: Option[Account] = dbTransaction { (em, tx) =>

      //for the userpass provider we want case insensitive lookup
      val queryStr = id.providerId match {
        case "userpass" =>
          """SELECT a from Account a WHERE
             LOWER(a.userid) = LOWER(:uid) AND a.provider = :provider"""
        case _ =>
          """SELECT a from Account a
             WHERE a.userid = :uid AND a.provider = :provider"""
      }

      val query : TypedQuery[Account] = em.createQuery(queryStr, classOf[Account])
      query.setParameter("uid", id.userId)
      query.setParameter("provider", id.providerId)
      resultToAccount(query.getResultList)
    }

    user
  }

  def find(id: IdentityId): Option[Identity] = {

    implicit val emp = EntityManagerProvider.fromDefaultPersistenceUnit()

    Logger.debug("find")
    val account = findAccount(id)
    Logger.debug("found:" + account.toString)
    account
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {

    implicit val emp = EntityManagerProvider.fromDefaultPersistenceUnit()

    Logger.debug("findByEmailAndProvider $email, $providerId")

    dbTransaction { (em, tx) =>
      val queryStr =
        """SELECT a from Account a
           WHERE LOWER(a.mail) = LOWER(:mail) AND a.provider = :provider"""

      val query : TypedQuery[Account] = em.createQuery(queryStr, classOf[Account])
      query.setParameter("mail", email)
      query.setParameter("provider", providerId)
      resultToAccount(query.getResultList)
    }
  }

  def findByEmail(email: String): List[Account] = {

    implicit val emp = EntityManagerProvider.fromDefaultPersistenceUnit()

    Logger.debug("findByEmail $email")

    dbTransaction { (em, tx) =>
      val queryStr =
        """SELECT a from Account a
           WHERE LOWER(a.mail) = LOWER(:mail)"""

      val query : TypedQuery[Account] = em.createQuery(queryStr, classOf[Account])
      query.setParameter("mail", email)
      query.getResultList.toSet.toList
    }
  }

  def save(user: Identity): Identity = {

    implicit val emp = EntityManagerProvider.fromDefaultPersistenceUnit()

    val dbUser: Option[Account] = findAccount(user.identityId)

    dbTransaction { (em, tx) =>

      Logger.debug(dbUser.toString)

      dbUser match {

        case Some(account) =>
          Logger.debug("Have user already in the db!")
          account.updateFromIdentity(user)
          em.merge(account)
          account

        case None =>
          val account = Account(user)
          em.persist(account)
          Logger.debug("New user " + account.toString)
          account
      }
    }
  }

  private val tokenParser : anorm.RowParser[Token] = {
      get[String]("id") ~
      get[String]("email") ~
      get[DateTime]("creationTime") ~
      get[DateTime]("expirationTime") ~
      get[Boolean]("isSignUp") map {
      case i ~ e ~ ctime ~ etime ~ s => Token(i, e, ctime, etime, s)
    }
  }

  def save(token: Token) {

    DB.withConnection { implicit c =>
      SQL("""insert into Tokens(id, email, creationTime, expirationTime, isSignUp)
            |values({id}, {email}, {creationTime}, {expirationTime}, {isSignUp})
          """.stripMargin).onParams(token.uuid, token.email, token.creationTime,
                                    token.expirationTime, token.isSignUp).execute()
    }
  }

  def findToken(token: String): Option[Token] = {
    DB.withConnection{ implicit c =>
      SQL("select * from Tokens t where t.id = {uuid}").onParams(token).as(tokenParser.singleOpt)
    }
  }

  def deleteToken(uuid: String) {
    DB.withConnection{ implicit c =>
      SQL("delete from Tokens t where t.id = {uuid}").onParams(uuid).execute()
    }
  }

  def deleteTokens() {
    DB.withConnection{ implicit c =>
      SQL("delete from Tokens").execute()
    }
  }

  def deleteExpiredTokens() {
    DB.withConnection{ implicit c =>
      SQL("delete from Tokens t where t.expirationTime < now()").execute()
    }
  }
}