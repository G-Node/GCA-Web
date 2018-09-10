package controller

import org.junit._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc.Cookie

import utils.serializer.AccountFormat
import scala.Some
import play.api.libs.json.JsObject
import utils.DefaultRoutesResolver._
import play.api.Play
import play.api.test.FakeApplication
import play.api.libs.json.JsObject
import play.api.mvc.Cookie
import scala.Some

/**
 * Test
 */
class AccountsCtrlTest extends BaseCtrlTest {

  val formatter = new AccountFormat()
  var cookie : Cookie = _

  @Before
  override def before() : Unit = {
    super.before()
    cookie = getCookie(assets.alice, "testtest")
  }

  @Test
  def testListByEmail(): Unit = {
    val request = FakeRequest(
      GET, "/api/users?email=" + assets.bob.mail
    ).withCookies(cookie)
    val result = route(AccountsCtrlTest.app, request).get
    assert(status(result) == OK)
    assert(contentType(result) == Some("application/json"))

    for (jconf <- contentAsJson(result).as[List[JsObject]])
      assert(assets.bob.uuid == formatter.reads(jconf).get.uuid)
  }


  @Test
  def testListAccounts() {
    val reqNoAuth = FakeRequest(GET, "/api/user/list")

    val reqNoAuthResult = route(AccountsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)

    val reqAuthResult = route(AccountsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)
    assert(contentType(reqAuthResult) == Some("application/json"))
  }

  @Test
  def testPasswordReset(): Unit = {
    val reqNoAuth = FakeRequest(GET, "/password")

    var response = route(AccountsCtrlTest.app, reqNoAuth).get
    assert(status(response) == UNAUTHORIZED)

    val newpass = "foofoofoo"
    val postR = FakeRequest(POST, "/password").withCookies(cookie)
      .withFormUrlEncodedBody("passwordold" -> "testtest","password1" -> newpass, "password2" -> newpass)

    response = route(AccountsCtrlTest.app, postR).get
    assert(status(response) == SEE_OTHER || status(response) == OK)

    val new_cookie = getCookie(assets.alice, newpass)

    val reqAuth = reqNoAuth.withCookies(new_cookie)
    response = route(AccountsCtrlTest.app, reqAuth).get
    assert(status(response) == OK)
  }

  @Test
  def testForgotPassword(): Unit = {
    val reqNoAuth = FakeRequest(GET, "/forgotpassword")

    var response = route(AccountsCtrlTest.app, reqNoAuth).get
    assert(status(response) == OK)

    val postR = FakeRequest(POST, "/forgotpassword")
      .withFormUrlEncodedBody("email" -> "alice@foo.com")

    response = route(AccountsCtrlTest.app, postR).get
    assert(status(response) == SEE_OTHER || status(response) == OK)

    try {
      val new_cookie = getCookie(assets.alice, "testtest")
      fail("Password was not reset properly")
    }
    catch {
      case _:RuntimeException => // Expected, continue
    }
  }
  
  @Test
  def testChangeMail(): Unit = {
    cookie = getCookie(assets.alice, "testtest")

    val req = FakeRequest(GET, "/mail").withCookies(cookie)

    var response = route(AccountsCtrlTest.app, req).get
    assert(status(response) == OK)

    val postR = FakeRequest(POST, "/mail")
      .withFormUrlEncodedBody("email1" -> "alice_new@foo.com", "email2" -> "alice_new@foo.com", "password"-> "testtest")
      .withCookies(cookie)

    response = route(AccountsCtrlTest.app, postR).get
    assert(status(response) == SEE_OTHER || status(response) == OK)

    try {
      val new_cookie = getCookie(assets.alice_new, "testtest")
      val postR = FakeRequest(POST, "/mail")
        .withFormUrlEncodedBody("email1" -> "alice@foo.com", "email2" -> "alice@foo.com", "password"-> "testtest")
        .withCookies(new_cookie)
        response = route(AccountsCtrlTest.app, postR).get
        assert(status(response) == SEE_OTHER || status(response) == OK)
    }
    catch {
      case _:RuntimeException => fail("Could not reset mail")
    }
  }
  
}




object AccountsCtrlTest {

  var app: FakeApplication = null

  @BeforeClass
  def beforeClass() = {
    app = new FakeApplication()
    Play.start(app)
  }

  @AfterClass
  def afterClass() = {
    Play.stop()
  }

}