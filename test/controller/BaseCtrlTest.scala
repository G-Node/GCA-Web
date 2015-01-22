package controller

import conf.Global
import org.junit.Before
import org.scalatest.junit.JUnitSuite
import play.api.Application
import play.api.http.Writeable
import play.api.mvc.{Request, SimpleResult}
import play.api.test.Helpers._
import play.api.test._
import models.Account
import service.Assets
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits._

import scala.concurrent.Future


trait BaseCtrlTest extends JUnitSuite {

  var assets : Assets = _

  /**
   * Utility function to get an authenticated Cookie
    */
  def getCookie (id: Account, password: String) = {
    throw new RuntimeException("Could not authenticate successfully")
  }

  /**
   * Call route and handle errors with Global.onError().
   */
  def routeWithErrors[T](app: Application, req: Request[T])(implicit w: Writeable[T]): Option[Future[SimpleResult]] = {
    route(req).map { result =>
      result.recoverWith {
        case t: Throwable => Global.onError(req, t)
      }
    }
  }

  @Before
  def before() : Unit = {
    assets = new Assets()
    assets.killDB()
    assets.fillDB()
  }
}
