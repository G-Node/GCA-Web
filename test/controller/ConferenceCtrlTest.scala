package controller

import org.scalatest.junit.JUnitSuite
import org.junit._
import play.api.test._
import play.api.Play
import play.api.test.Helpers._
import javax.persistence._
import service.util.DBUtil
import service.Assets
import utils.serializer.ConferenceFormat
import scala.Some
import play.api.test.FakeApplication
import play.api.libs.json.JsObject
import utils.DefaultRoutesResolver._

/**
 * Test
 */
class ConferenceCtrlTest extends JUnitSuite with DBUtil {

  var emf : EntityManagerFactory = _
  var assets : Assets = _
  val authenticate = securesocial.controllers.ProviderController.authenticateByPost _
  val formatter = new ConferenceFormat()
  val getCookie = (username: String, password: String, provider: String) => {
    val authRequest = FakeRequest().withFormUrlEncodedBody(
      "username" -> username,
      "password" -> password
    )
    val authResponse = authenticate(provider)(authRequest)
    cookies(authResponse).get("id").get
  }

  @Before
  def before() : Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    assets = new Assets(emf)
    assets.killDB()
    assets.fillDB()
  }

  @Test
  def testCreate(): Unit = {
    val body = formatter.writes(assets.conferences(0)).as[JsObject] - "uuid" - "abstracts"

    val createUnauth = FakeRequest(POST, "/api/conferences").withHeaders(
      ("Content-Type", "application/json")
    ).withJsonBody(body)
    val failed = route(ConferenceCtrlTest.app, createUnauth).get
    assert(status(failed) == UNAUTHORIZED)

    val aliceCookie = getCookie(assets.alice.mail, "testtest", assets.alice.provider)
    val createAuth = FakeRequest(POST, "/api/conferences").withHeaders(
        ("Content-Type", "application/json")
      ).withJsonBody(body).withCookies(aliceCookie)

    val created = route(ConferenceCtrlTest.app, createAuth).get
    assert(status(created) == CREATED)
  }

  @Test
  def testList(): Unit = {
    val request = FakeRequest(GET, "/api/conferences")
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
    val request = FakeRequest(GET, "/api/conferences/" + uuid)
    val confResult = route(ConferenceCtrlTest.app, request).get

    assert(status(confResult) == OK)
    assert(contentType(confResult) == Some("application/json"))
    assert(formatter.reads(contentAsJson(confResult)).get.uuid == uuid)
  }

  @Test
  def testUpdate(): Unit = {
    val conf = assets.conferences(1)
    val body = formatter.writes(conf).as[JsObject] - "abstracts" - "uuid"

    val aliceCookie = getCookie(assets.alice.mail, "testtest", assets.alice.provider)
    val updateAuth = FakeRequest(PUT, "/api/conferences/" + conf.uuid).withHeaders(
      ("Content-Type", "application/json")
    ).withJsonBody(body).withCookies(aliceCookie)
    val updated = route(ConferenceCtrlTest.app, updateAuth).get
    assert(status(updated) == OK)

    val bobCookie = getCookie(assets.bob.mail, "testtest", assets.bob.provider)
    val updateUnauth = FakeRequest(PUT, "/api/conferences/" + conf.uuid).withHeaders(
      ("Content-Type", "application/json")
    ).withJsonBody(body).withCookies(bobCookie)
    val failed = route(ConferenceCtrlTest.app, updateUnauth).get
    assert(status(failed) == FORBIDDEN)
  }

  @Test
  def testDelete(): Unit = {
    val aliceCookie = getCookie(assets.alice.mail, "testtest", assets.alice.provider)
    val good = FakeRequest(DELETE, "/api/conferences/" +
      assets.conferences(1).uuid).withCookies(aliceCookie)
    val deleted = route(ConferenceCtrlTest.app, good).get
    assert(status(deleted) == OK)

    val bad = FakeRequest(DELETE, "/api/conferences/" +
      "foo").withCookies(aliceCookie)
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
