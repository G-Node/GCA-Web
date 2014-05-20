package utils

import play.api.mvc._
import scala.concurrent.Future
import securesocial.core.{Authenticator, IdentityProvider, UserService, SecureSocial}
import models.Account
import javax.persistence.{Persistence, EntityManager, EntityNotFoundException, NoResultException}
import play.api.libs.json._
import securesocial.core.providers.utils.RoutesHelper
import play.api.i18n.Messages
import scala.language.higherKinds
import scala.Some
import play.api.mvc.SimpleResult
import play.api.libs.json.JsResultException
import service.UserStore
import service.util.EntityManagerProvider
import service.util.EMPImplicits.EMPFromEntityManager

//custom request classes

case class RequestWithAccount[A](entityManager: EntityManager,
                                 user: Option[Account],
                                 req: Request[A]) extends WrappedRequest(req) with EntityManagerProvider

case class RequestAuthenticated[A](entityManager: EntityManager,
                                   user: Account,
                                   req: Request[A]) extends WrappedRequest(req) with EntityManagerProvider

//custom actions
trait GCAAuth extends securesocial.core.SecureSocial {

  trait SafeActionBuilder[R[_]] extends ActionBuilder[R] {

    var isAjaxCall = false

    def invokeBlockSafe[A](resultAsRest: Boolean)(request: R[A],
                                                block: (R[A]) => Future[SimpleResult]): Future[SimpleResult] = {
      try {
        block(request)
      } catch (if(resultAsRest) {
        exHandlerJSON()
      } else {
        exHandlerHTML()
      }) andThen {
        result => Future.successful(result)
      }

    }

    final def apply(isREST: Boolean)(block: R[AnyContent] => Result): Action[AnyContent] = {
      isAjaxCall = isREST
      apply(BodyParsers.parse.anyContent)(block)
    }

    final def apply[P](bodyParser: BodyParser[P], isREST: Boolean)(block: R[P] => Result): Action[P] = {
      isAjaxCall = isREST
      apply(bodyParser)(block)
    }

    def createDefaultEntityManger() = {
      val emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
      emf.createEntityManager()
    }
  }

  object AccountAwareAction extends SafeActionBuilder[RequestWithAccount] {
    protected def invokeBlock[A](request: Request[A],
                                 block: (RequestWithAccount[A]) => Future[SimpleResult]): Future[SimpleResult] = {

      implicit val em = createDefaultEntityManger()
      val Account = getAccount(request)
      val res = invokeBlockSafe(resultAsRest = isAjaxCall)(RequestWithAccount(em, Account, request), block)
      if (em.isOpen) {
        em.close()
      }

      res
    }
  }

  object AuthenticatedAction extends SafeActionBuilder[RequestAuthenticated] {

    protected def invokeBlock[A](request: Request[A],
                                 block: (RequestAuthenticated[A]) => Future[SimpleResult]): Future[SimpleResult] = {
      implicit val em = createDefaultEntityManger()
      val Account = getAccount(request)

      val res = Account.fold({
        authFailedResponse(request)
      })({ account =>
        invokeBlockSafe(resultAsRest = isAjaxCall)(RequestAuthenticated(em, account, request), block)
      })

      if (em.isOpen) {
        em.close()
      }

      res
    }

    def authFailedResponse[A](implicit request: Request[A]) : Future[SimpleResult] = {
      val response = if (isAjaxCall) {
        Unauthorized(Json.toJson(Map("error"->"Credentials required"))).as(JSON)
      } else {
        Redirect(RoutesHelper.login().absoluteURL(IdentityProvider.sslEnabled))
          .flashing("error" -> Messages("securesocial.loginRequired"))
          .withSession(session + (SecureSocial.OriginalUrlKey -> request.uri))

      }

      Future.successful(response.discardingCookies(Authenticator.discardingCookie))
    }
  }

  def getUserStore = {
    UserService.delegate match {
      case Some(u: UserStore) => u
      case _ => throw new RuntimeException("Could not obtain User Store")
    }
  }

  def getAccount[A](request: Request[A])(implicit emp:EntityManagerProvider) : Option[Account] = {
    implicit val req = request
    for {
      authenticator <- SecureSocial.authenticatorFromRequest
      user <- getUserStore.findAccount(authenticator.identityId)
      account <- user match {case a: Account => Some(a); case _ => None}
    } yield {
      touch(authenticator)
      account
    }
  }

  def exHandlerHTML() : PartialFunction[Throwable, SimpleResult] = {
    case e: NoResultException => NotFound(views.html.error.NotFound())
    case e: Exception => InternalServerError(views.html.error.InternalServerError(e))
  }

  def exHandlerJSON() : PartialFunction[Throwable, SimpleResult] = {
    case e: NoResultException => NotFound(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
    case e: EntityNotFoundException => NotFound(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
    case e: IllegalArgumentException => BadRequest(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
    case e: JsResultException => BadRequest(Json.obj("error" -> true, "causes" -> JsError.toFlatJson(e.errors)))
    case e: IllegalAccessException => Forbidden(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
    case e: Exception => InternalServerError(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
  }
}
