package controller

import org.junit._
import play.api.Play
import play.api.libs.json._
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._
import utils.DefaultRoutesResolver._
import utils.serializer.{AccountFormat, ConferenceFormat}


/**
 * Test
 */
class ConferenceCtrlTest extends BaseCtrlTest {

  val formatter = new ConferenceFormat()
  var cookie : Cookie = _

  @Before
  override def before() : Unit = {
    super.before()
    cookie = getCookie(assets.alice, "testtest")
  }

  @Test
  def testCreate(): Unit = {
    val body = formatter.writes(assets.conferences(0)).as[JsObject] - "uuid" - "abstracts" - "short"

    val createUnauth = FakeRequest(POST, "/api/conferences").withHeaders(
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
    val request = FakeRequest(GET, s"/api/conferences/$uuid")
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
    val updateAuth = FakeRequest(PUT, s"/api/conferences/$uuid").withHeaders(
      ("Content-Type", "application/json")
    ).withJsonBody(body).withCookies(aliceCookie)
    val updated = route(ConferenceCtrlTest.app, updateAuth).get
    assert(status(updated) == OK)

    val bobCookie = getCookie(assets.bob, "testtest")
    val updateUnauth = updateAuth.withCookies(bobCookie)
    val failed = routeWithErrors(ConferenceCtrlTest.app, updateUnauth).get
    assert(status(failed) == FORBIDDEN)
  }

  @Test
  def testDelete(): Unit = {
    val good = FakeRequest(DELETE, "/api/conferences/" +
      assets.conferences(1).uuid).withCookies(cookie)
    val deleted = route(ConferenceCtrlTest.app, good).get
    assert(status(deleted) == OK)

    val id = "NOTEXISTANT"
    val bad = FakeRequest(DELETE, s"/api/conferences/$id").withCookies(cookie)
    val failed = routeWithErrors(ConferenceCtrlTest.app, bad).get
    assert(status(failed) == NOT_FOUND)
  }

  @Test
  def testPermissions(): Unit = {

    val accountFormat = new AccountFormat()

    def parseOwners = {json: JsValue =>
      for (acc <- json.as[List[JsObject]])
        yield accountFormat.reads(acc).get.uuid
    }

    val confid = assets.conferences(0).uuid

    var getR = FakeRequest(GET, s"/api/conferences/$confid/owners").withCookies(cookie)
    var response = route(ConferenceCtrlTest.app, getR).get

    assert(status(response) == OK)
    assert(parseOwners(contentAsJson(response)).contains(assets.alice.uuid))

    val body = JsArray(for (acc <- List(assets.bob, assets.eve)) yield accountFormat.writes(acc))
    var postR = FakeRequest(PUT, s"/api/conferences/$confid/owners").withCookies(cookie).withJsonBody(body)
    response = route(ConferenceCtrlTest.app, postR).get

    assert(status(response) == OK)
    assert(parseOwners(contentAsJson(response)).contains(assets.eve.uuid))

    val bobCookie = getCookie(assets.bob, "testtest")
    getR = FakeRequest(GET, s"/api/conferences/$confid/owners").withCookies(bobCookie)
    response = route(ConferenceCtrlTest.app, getR).get

    assert(status(response) == OK)
    assert(!parseOwners(contentAsJson(response)).contains(assets.alice.uuid))

    val adminCookie = getCookie(assets.admin, "testtest")
    getR = FakeRequest(GET, s"/api/conferences/$confid/owners").withCookies(adminCookie)
    response = route(ConferenceCtrlTest.app, getR).get

    assert(status(response) == OK)

    postR = FakeRequest(PUT, s"/api/conferences/$confid/owners").withCookies(adminCookie).withJsonBody(body)
    response = routeWithErrors(ConferenceCtrlTest.app, postR).get

    assert(status(response) == FORBIDDEN)
  }

  @Test
  def testGetGeo(): Unit = {

    val existingGeo = assets.conferences(0)
    val uuid = existingGeo.uuid
    val req = FakeRequest(GET, s"/api/conferences/$uuid/geo")
    val responseNoUser = route(ConferenceCtrlTest.app, req).get

    assert(status(responseNoUser) == UNAUTHORIZED)

    val eveCookie = getCookie(assets.eve, "testtest")
    val reqEve = FakeRequest(GET, s"/api/conferences/$uuid/geo").withCookies(eveCookie)
    val responseEve = route(ConferenceCtrlTest.app, reqEve).get

    assert(status(responseEve) == OK)

    val adminCookie = getCookie(assets.admin, "testtest")
    val request = FakeRequest(GET, s"/api/conferences/$uuid/geo").withCookies(adminCookie)
    val responseAdmin = route(ConferenceCtrlTest.app, request).get

    assert(status(responseAdmin) == OK)
    assert(contentAsJson(responseAdmin).equals(Json.parse(existingGeo.geo)))

    val emptyGeo = assets.conferences(1)
    val emptyGeoUuid = emptyGeo.uuid
    val reqEmpty = FakeRequest(GET, s"/api/conferences/$emptyGeoUuid/geo").withCookies(adminCookie)
    val response = route(ConferenceCtrlTest.app, reqEmpty).get

    assert(status(response) == NOT_FOUND)
    assert(contentAsJson(response).asInstanceOf[JsObject].values.head.asInstanceOf[JsBoolean].value.equals(true))
  }

  @Test
  def testSetGeo(): Unit = {
    val emptyGeo = assets.conferences(2)
    val uuid = emptyGeo.uuid

    val validJson = Json.toJson("""{"entryOne": 1, "entryTwo": 2}""")

    val reqNoUser = FakeRequest(PUT, s"/api/conferences/$uuid/geo").withJsonBody(validJson)
    val responseNoUser = route(ConferenceCtrlTest.app, reqNoUser).get

    assert(status(responseNoUser) == UNAUTHORIZED)

    val eveCookie = getCookie(assets.eve, "testtest")
    val reqNoAccess = FakeRequest(PUT, s"/api/conferences/$uuid/geo").withCookies(eveCookie).withJsonBody(validJson)
    val responseNoAccess = routeWithErrors(ConferenceCtrlTest.app, reqNoAccess).get

    assert(status(responseNoAccess) == FORBIDDEN)

    val adminCookie = getCookie(assets.admin, "testtest")
    val req = FakeRequest(PUT, s"/api/conferences/$uuid/geo").withCookies(adminCookie).withJsonBody(validJson)
    val response = route(ConferenceCtrlTest.app, req).get

    assert(status(response) == OK)

    val reqCheck = FakeRequest(GET, s"/api/conferences/$uuid/geo").withCookies(adminCookie)
    val getValidResponse = route(ConferenceCtrlTest.app, reqCheck).get

    assert(contentAsJson(getValidResponse).equals(validJson))

    val invalidJson = """entryOne: 1, "entryTwo": 2}"""
    val reqInvalid = FakeRequest(PUT, s"/api/conferences/$uuid/geo").withCookies(adminCookie).withBody(invalidJson)
    val responseInvalid = route(ConferenceCtrlTest.app, reqInvalid).get

    assert(status(responseInvalid) == BAD_REQUEST)

    val reqInvalidCheck = FakeRequest(GET, s"/api/conferences/$uuid/geo").withCookies(adminCookie)
    val getInvalidResponse = route(ConferenceCtrlTest.app, reqInvalidCheck).get

    assert(!contentAsJson(getInvalidResponse).equals(invalidJson))
    assert(contentAsJson(getInvalidResponse).equals(validJson))

    val empty = ""
    val reqEmpty = FakeRequest(PUT, s"/api/conferences/$uuid/geo").withCookies(adminCookie).withBody(empty)
    val responseEmpty = route(ConferenceCtrlTest.app, reqEmpty).get

    assert(status(responseEmpty) == BAD_REQUEST)
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
