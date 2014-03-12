package controller

import org.junit._
import play.api.test._
import play.api.Play
import play.api.test.Helpers._

import scala.Some
import play.api.test.FakeApplication
import play.api.libs.json.JsObject
import utils.serializer.ConferenceFormat
import play.api.mvc.Cookie


/**
 * Test
 */
class ConferenceCtrlTest extends BaseCtrlTest {

  val formatter = new ConferenceFormat("http://example.com")
  var cookie : Cookie = _

  @Before
  override def before() : Unit = {
    super.before()
    cookie = getCookie(assets.alice.identityId, "testtest")
  }

  @Test
  def testCreate(): Unit = {
    val body = formatter.writes(assets.conferences(0)).as[JsObject] - "uuid" - "abstracts"

    val createUnauth = FakeRequest(POST, "/conferences").withHeaders(
      ("Content-Type", "application/json")
    ).withJsonBody(body)
    val failed = route(ConferenceCtrlTest.app, createUnauth).get
    assert(status(failed) == UNAUTHORIZED)

    val createAuth = createUnauth.withCookies(cookie)
    val created = route(ConferenceCtrlTest.app, createAuth).get
    assert(status(created) == CREATED)
  }

  @Test
  def testList(): Unit = {
    val request = FakeRequest(GET, "/conferences")
    val confResult = route(ConferenceCtrlTest.app, request).get
    assert(status(confResult) == OK)
    assert(contentType(confResult) == Some("application/json"))

    val existingIds: Array[String] = for (c <- assets.conferences) yield c.uuid
    for (jconf <- contentAsJson(confResult).as[List[JsObject]])
      assert(existingIds.contains(formatter.reads(jconf).get.uuid))
  }

  @Test
  def testGet(): Unit = {
    val uuid = assets.conferences(0).uuid
    val request = FakeRequest(GET, s"/conferences/$uuid")
    val confResult = route(ConferenceCtrlTest.app, request).get

    assert(status(confResult) == OK)
    assert(contentType(confResult) == Some("application/json"))
    assert(formatter.reads(contentAsJson(confResult)).get.uuid == uuid)
  }

  @Test
  def testUpdate(): Unit = {
    val conf = assets.conferences(1)
    val uuid = conf.uuid
    val body = formatter.writes(conf).as[JsObject] - "abstracts" - "uuid"

    val aliceCookie = cookie
    val updateAuth = FakeRequest(PUT, s"/conferences/$uuid").withHeaders(
      ("Content-Type", "application/json")
    ).withJsonBody(body).withCookies(aliceCookie)
    val updated = route(ConferenceCtrlTest.app, updateAuth).get
    assert(status(updated) == OK)

    val bobCookie = getCookie(assets.bob.identityId, "testtest")
    val updateUnauth = updateAuth.withCookies(bobCookie)
    val failed = route(ConferenceCtrlTest.app, updateUnauth).get
    assert(status(failed) == FORBIDDEN)
  }

  @Test
  def testDelete(): Unit = {
    val good = FakeRequest(DELETE, "/conferences/" +
      assets.conferences(1).uuid).withCookies(cookie)
    val deleted = route(ConferenceCtrlTest.app, good).get
    assert(status(deleted) == OK)

    val bad = FakeRequest(DELETE, "/conferences/foo").withCookies(cookie)
    val failed = route(ConferenceCtrlTest.app, bad).get
    assert(status(failed) == NOT_FOUND)
  }
}


object ConferenceCtrlTest {

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
