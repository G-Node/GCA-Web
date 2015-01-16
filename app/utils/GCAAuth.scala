package utils

import models.Account
import play.api.i18n.Messages
import play.api.libs.json._
import play.api.mvc.{SimpleResult, _}
import securesocial.core.providers.utils.RoutesHelper
import securesocial.core.{Authenticator, IdentityProvider, SecureSocial, UserService}
import service.UserStore

import scala.concurrent.Future
import scala.language.higherKinds

//custom request classes

case class RequestWithAccount[A](user: Option[Account],
                                 req: Request[A]) extends WrappedRequest(req)

case class RequestAuthenticated[A](user: Account,
                                   req: Request[A]) extends WrappedRequest(req)

//custom actions
trait GCAAuth extends securesocial.core.SecureSocial {

  trait SafeActionBuilder[R[_]] extends ActionBuilder[R] {

    var isAjaxCall = false

    final def apply(isREST: Boolean)(block: R[AnyContent] => Result): Action[AnyContent] = {
      isAjaxCall = isREST
      apply(BodyParsers.parse.anyContent)(block)
    }

    final def apply[P](bodyParser: BodyParser[P], isREST: Boolean)(block: R[P] => Result): Action[P] = {
      isAjaxCall = isREST
      apply(bodyParser)(block)
    }
  }

  object AccountAwareAction extends SafeActionBuilder[RequestWithAccount] {
    protected def invokeBlock[A](request: Request[A],
                                 block: (RequestWithAccount[A]) => Future[SimpleResult]): Future[SimpleResult] = {

      val Account = getAccount(request)
      val requestAcc = RequestWithAccount(Account, request)
      block(requestAcc)
    }
  }

  object AuthenticatedAction extends SafeActionBuilder[RequestAuthenticated] {

    protected def invokeBlock[A](request: Request[A],
                                 block: (RequestAuthenticated[A]) => Future[SimpleResult]): Future[SimpleResult] = {
      val Account = getAccount(request)

      val res = Account.fold({
        authFailedResponse(request)
      })({ account =>
        val requestAuth = RequestAuthenticated(account, request)
        block(requestAuth)
      })

      res
    }

    def authFailedResponse[A](implicit request: Request[A]): Future[SimpleResult] = {
      val response = if (isAjaxCall) {
        Unauthorized(Json.toJson(Map("error" -> "Credentials required"))).as(JSON)
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

  def getAccount[A](request: Request[A]): Option[Account] = {
    implicit val req = request
    for {
      authenticator <- SecureSocial.authenticatorFromRequest
      user <- getUserStore.findAccount(authenticator.identityId)
      account <- user match {
        case a: Account => Some(a);
        case _ => None
      }
    } yield {
      touch(authenticator)
      account
    }
  }
  
}
