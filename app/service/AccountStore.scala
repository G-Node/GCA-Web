package service

import java.util.{List => JList}
import javax.persistence.TypedQuery

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.PasswordInfo
import com.mohiva.play.silhouette.core.services.IdentityService
import plugins.DBUtil._
import models.{CredentialsLogin, Account, Login}

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.{Success, Failure, Try}

class AccountStore {
  def findByEmail(mail: String): Seq[Account] = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Account a
           WHERE LOWER(a.mail) = LOWER(:email)"""

      val query: TypedQuery[Account] = em.createQuery(queryStr, classOf[Account])
      query.setParameter("email", mail)

      asScalaBuffer(query.getResultList)
    }
  }

  def list(): Seq[Account] = {
    query { em =>
      val builder = em.getCriteriaBuilder
      val criteria = builder.createQuery(classOf[Account])
      val query = em.createQuery(criteria)

      asScalaBuffer(query.getResultList)
    }

  }
}

class LoginStore extends IdentityService[Login] {
  override def retrieve(loginInfo: LoginInfo): Future[Option[Login]] = {

    val login = query { em =>
      val queryStr =
        """SELECT DISTINCT l FROM Login l
           LEFT JOIN FETCH l.account a
           WHERE LOWER(a.mail) = LOWER(:email)"""

      val query: TypedQuery[CredentialsLogin] = em.createQuery(queryStr, classOf[CredentialsLogin])
      query.setParameter("email", loginInfo.providerKey)

      Try(query.getSingleResult)
    } match {
      case Success(l) => Some(l)
      case Failure(e) => None
    }

    Future.successful(login)
  }
}


class CredentialsStore extends DelegableAuthInfoDAO[PasswordInfo] {
  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {

    val info = query { em =>
      val queryStr =
        """SELECT DISTINCT l FROM Login l
           LEFT JOIN FETCH l.account a
           WHERE LOWER(a.mail) = LOWER(:email)"""

      val query : TypedQuery[CredentialsLogin] = em.createQuery(queryStr, classOf[CredentialsLogin])
      query.setParameter("email", loginInfo.providerKey)

      Try(query.getSingleResult)
    } match {
      case Success(login) =>
        Some(PasswordInfo(login.hasher, login.password, None))
      case Failure(e) => None
    }

    Future.successful(info)
  }
}
