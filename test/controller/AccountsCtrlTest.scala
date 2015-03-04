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
    val body = "password1=%s&password2=%s".format(newpass, newpass)
    val postR = FakeRequest(POST, "/password").withCookies(cookie)
      .withFormUrlEncodedBody("password1" -> newpass, "password2" -> newpass)

    response = route(AccountsCtrlTest.app, postR).get
    assert(status(response) == SEE_OTHER || status(response) == OK)

    val new_cookie = getCookie(assets.alice, newpass)

    val reqAuth = reqNoAuth.withCookies(new_cookie)
    response = route(AccountsCtrlTest.app, reqAuth).get
    assert(status(response) == OK)
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