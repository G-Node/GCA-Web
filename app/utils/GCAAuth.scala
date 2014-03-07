package utils

import play.api.mvc.{WrappedRequest, SimpleResult, Request, ActionBuilder}
import scala.concurrent.Future
import securesocial.core.{UserService, SecureSocial}
import models.Account
import javax.persistence.NoResultException

case class RequestWithAccount[A](user: Option[Account], request: Request[A]) extends WrappedRequest(request)


trait GCAAuth extends securesocial.core.SecureSocial {


  object AccountAwareAction extends ActionBuilder[RequestWithAccount] {
    protected def invokeBlock[A](request: Request[A],
                                 block: (RequestWithAccount[A]) => Future[SimpleResult]): Future[SimpleResult] =
    {
      val Account = getAccount(request)

      try {
        block(RequestWithAccount(Account, request))
      } catch exceptionHandler(asJson = true) andThen {
        result => Future.successful(result)
      }
    }
  }

  def getAccount[A](request: Request[A]) : Option[Account] = {
    implicit val req = request
    for {
      authenticator <- SecureSocial.authenticatorFromRequest
      user <- UserService.find(authenticator.identityId)
      account <- user match {case a: Account => Some(a); case _ => None}
    } yield {
      touch(authenticator)
      account
    }
  }


  def exHandlerHTML() : PartialFunction[Throwable, SimpleResult] = {
    case e: NoResultException => InternalServerError("No Result !\n" + e.getStackTraceString)
    case e: Exception => InternalServerError("Uh oh!\n" + e.getStackTraceString)
  }

  def exHandlerJSON() : PartialFunction[Throwable, SimpleResult] = {
    case e: NoResultException => InternalServerError("No Result !\n" + e.getStackTraceString)
    case e: Exception => InternalServerError("Uh oh!\n" + e.getStackTraceString)
  }

  def exceptionHandler(asJson: Boolean) = {
    asJson match {
      case true => exHandlerJSON()
      case _    => exHandlerHTML()
    }
  }

}
