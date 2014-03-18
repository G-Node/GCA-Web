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

/**
 * Test
 */
class AccountsCtrlTest extends BaseCtrlTest {

  val formatter = new AccountFormat()
  var cookie : Cookie = _

  @Before
  override def before() : Unit = {
    super.before()
    cookie = getCookie(assets.alice.identityId, "testtest")
  }

  @Test
  def testListByEmail(): Unit = {
    val request = FakeRequest(
      GET, "/api/users?email=" + assets.bob.email.get
    ).withCookies(cookie)
    val result = route(AccountsCtrlTest.app, request).get
    assert(status(result) == OK)
    assert(contentType(result) == Some("application/json"))

    for (jconf <- contentAsJson(result).as[List[JsObject]])
      assert(assets.bob.uuid == formatter.reads(jconf).get.uuid)
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