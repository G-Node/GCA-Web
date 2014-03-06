import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.mvc.Http.HeaderNames
//import service.UserStore


/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class ConferenceSpec extends PlaySpecification {

  "respond with the list of Conferences" in new WithApplication {
    val result = controllers.Conferences.list()(FakeRequest())

    status(result) must equalTo(OK)
    contentType(result) must equalTo(Some("application/json"))
  }

  "respond with the list of Conferences" in new WithApplication {
    val authRQ = FakeRequest(POST, "/login").withFormUrlEncodedBody(
      "username" -> "alice@foo.com", "password" -> "testtest"
    )
    val authRP = route(authRQ)
    authRP.isDefined must beTrue
    val cookies = header(HeaderNames.SET_COOKIE, authRP.get)
    cookies.isDefined must beTrue

    /*
    alternative to fetch a user via a service like that

    val us = new UserStore(implicitApp)
    val alice = us.findByEmailAndProvider("alice@foo.com", "userpass")
     */

    val create = FakeRequest().withHeaders(HeaderNames.COOKIE -> cookies.get)
    val result = controllers.Conferences.create()(create)

    //status(result) must equalTo(OK)
    //contentType(result) must equalTo(Some("application/json"))
  }
}