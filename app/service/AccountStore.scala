package service

import java.util.{List => JList}
import javax.persistence.TypedQuery

import anorm.SqlParser._
import anorm._
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.services.IdentityService
import org.joda.time.DateTime
import play.api.Play.current
import play.api.db.DB
import play.api.{Application, Logger}
import utils.AnormExtension._
import plugins.DBUtil._
import models.Account

import scala.collection.JavaConversions._
import scala.concurrent.Future


class AccountStore extends IdentityService[Account]  {
  override def retrieve(loginInfo: LoginInfo): Future[Option[Account]] = ???
}

