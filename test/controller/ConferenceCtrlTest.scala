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

    val newShort = assets.conferences(0).short + "01"
    val body = formatter.writes(assets.conferences(0))
                  .as[JsObject] - "uuid" - "abstracts" ++ Json.obj("short" -> newShort)

    val createUnauth = FakeRequest(POST, "/api/conferences")
                        .withHeaders(("Content-Type", "application/json"))
                        .withJsonBody(body)
    val failed = route(ConferenceCtrlTest.app, createUnauth).get
    assert(status(failed) == UNAUTHORIZED)

    val createAuth = createUnauth.withCookies(cookie)
    val created = route(ConferenceCtrlTest.app, createAuth).get
    assert(status(created) == CREATED)
    assert(header(ETAG, created).isDefined)

    val bodyNameNull = formatter.writes(assets.conferences(0)).as[JsObject] - "uuid" - "abstracts" - "name"
    val reqNameNull = FakeRequest(POST, "/api/conferences")
      .withHeaders(("Content-Type", "application/json"))
      .withJsonBody(bodyNameNull)
      .withCookies(cookie)
    val respNameNull = routeWithErrors(ConferenceCtrlTest.app, reqNameNull).get
    assert(status(respNameNull) == INTERNAL_SERVER_ERROR)

    val bodyShortNull = formatter.writes(assets.conferences(0))
                          .as[JsObject] - "uuid" - "abstracts" - "short"
    val reqShortNull = FakeRequest(POST, "/api/conferences")
                        .withHeaders(("Content-Type", "application/json"))
                        .withJsonBody(bodyShortNull)
                        .withCookies(cookie)
    val respShortNull = routeWithErrors(ConferenceCtrlTest.app, reqShortNull).get
    assert(status(respShortNull) == INTERNAL_SERVER_ERROR)

  }

  @Test
  def testList(): Unit = {
    val request = FakeRequest(GET, "/api/conferences")
    val confResult = route(ConferenceCtrlTest.app, request).get
    assert(status(confResult) == OK)
    assert(contentType(confResult) == Some("application/json"))

    val etag = header("etag", confResult)
    assert(etag.isDefined)

    val existingIds: Array[String] = for (c <- assets.conferences) yield c.uuid
    for (jconf <- contentAsJson(confResult).as[List[JsObject]])
      assert(existingIds.contains(formatter.reads(jconf).get.uuid))

    val eTagReq = FakeRequest(GET, "/api/conferences").withHeaders(
      "If-None-Match" -> etag.get
    )

    val eTagRes = route(ConferenceCtrlTest.app, eTagReq).get
    assert(status(eTagRes) == NOT_MODIFIED)

  }

  @Test
  def testListWithGroup(): Unit = {
    val request = FakeRequest(GET, "/api/conferences?group=BCCN")
    val result  = route(ConferenceCtrlTest.app, request).get
    assert(status(result) == OK)
    assert(contentType(result) == Some("application/json"))
    assert(header(ETAG, result).isDefined)


    val existingIds: Array[String] = for (c <- assets.conferences if c.group == "BCCN")  yield c.uuid
    for (c <- contentAsJson(result).as[List[JsObject]])
      assert(existingIds.contains(formatter.reads(c).get.uuid))
  }

  @Test
  def testGet(): Unit = {
    val uuid = assets.conferences(0).uuid
    val request = FakeRequest(GET, s"/api/conferences/$uuid")
    val confResult = route(ConferenceCtrlTest.app, request).get

    assert(status(confResult) == OK)
    assert(contentType(confResult) == Some("application/json"))
    assert(formatter.reads(contentAsJson(confResult)).get.uuid == uuid)

    val etag = header("etag", confResult)
    assert(etag.isDefined)

    val etagRequest = FakeRequest(GET, s"/api/conferences/$uuid").withHeaders(
      "If-None-Match" -> etag.get)
    val etagResult = route(ConferenceCtrlTest.app, etagRequest).get
    assert(status(etagResult) == NOT_MODIFIED)

    val wrongEtagRequest = FakeRequest(GET, s"/api/conferences/$uuid").withHeaders(
      "If-None-Match" -> "MyEtagIsOverTheOcean")
    val wrongEtagResult = route(ConferenceCtrlTest.app, wrongEtagRequest).get
    assert(status(wrongEtagResult) == OK)

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
    assert(header(ETAG, updated).isDefined && header(ETAG, updated).get != conf.eTag)

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

    assert(status(response) == OK)
  }

  @Test
  def testGetGeo(): Unit = {
    val existingGeo = assets.conferences(0)
    val uuid = existingGeo.uuid
    val mainUrl = "api/conferences"
    val urlCap = "geo"

    val req = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap")
    val responseNoUser = route(ConferenceCtrlTest.app, req).get

    assert(status(responseNoUser) == OK)

    val eveCookie = getCookie(assets.eve, "testtest")
    val reqEve = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(eveCookie)
    val responseEve = route(ConferenceCtrlTest.app, reqEve).get

    assert(status(responseEve) == OK)

    val adminCookie = getCookie(assets.admin, "testtest")
    val request = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie)
    val responseAdmin = route(ConferenceCtrlTest.app, request).get

    assert(status(responseAdmin) == OK)
    assert(contentAsJson(responseAdmin).equals(Json.parse(existingGeo.geo)))

    val emptyGeoUuid = assets.conferences(1).uuid
    val reqEmpty = FakeRequest(GET, s"/$mainUrl/$emptyGeoUuid/$urlCap").withCookies(adminCookie)
    val response = route(ConferenceCtrlTest.app, reqEmpty).get

    assert(status(response) == NOT_FOUND)
    assert(contentAsJson(response).asInstanceOf[JsObject]
      .values.head.asInstanceOf[JsString].value.equals("Geo entry not found."))
  }

  @Test
  def testSetGeo(): Unit = {
    val uuid = assets.conferences(2).uuid
    val mainUrl = "api/conferences"
    val urlCap = "geo"

    val validJson = Json.toJson("""{"entryOne": 1, "entryTwo": 2}""")

    val reqNoUser = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withJsonBody(validJson)
    val responseNoUser = route(ConferenceCtrlTest.app, reqNoUser).get

    assert(status(responseNoUser) == UNAUTHORIZED)

    val eveCookie = getCookie(assets.eve, "testtest")
    val reqNoAccess = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(eveCookie).withJsonBody(validJson)
    val responseNoAccess = routeWithErrors(ConferenceCtrlTest.app, reqNoAccess).get

    assert(status(responseNoAccess) == FORBIDDEN)

    val adminCookie = getCookie(assets.admin, "testtest")
    val req = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie).withJsonBody(validJson)
    val response = route(ConferenceCtrlTest.app, req).get

    assert(status(response) == OK)

    val reqCheck = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie)
    val getValidResponse = route(ConferenceCtrlTest.app, reqCheck).get

    assert(contentAsJson(getValidResponse).equals(validJson))

    val invalidJson = """entryOne: 1, "entryTwo": 2}"""
    val reqInvalid = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie).withBody(invalidJson)
    val responseInvalid = route(ConferenceCtrlTest.app, reqInvalid).get

    assert(status(responseInvalid) == BAD_REQUEST)

    val reqInvalidCheck = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie)
    val getInvalidResponse = route(ConferenceCtrlTest.app, reqInvalidCheck).get

    assert(contentAsJson(getInvalidResponse).equals(validJson))

    val empty = ""
    val reqEmpty = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie).withBody(empty)
    val responseEmpty = route(ConferenceCtrlTest.app, reqEmpty).get

    assert(status(responseEmpty) == BAD_REQUEST)
  }

  @Test
  def testGetSchedule(): Unit = {
    val existing = assets.conferences(0)
    val uuid = existing.uuid
    val mainUrl = "api/conferences"
    val urlCap = "schedule"
    val req = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap")
    val responseNoUser = route(ConferenceCtrlTest.app, req).get

    assert(status(responseNoUser) == OK)

    val eveCookie = getCookie(assets.eve, "testtest")
    val reqEve = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(eveCookie)
    val responseEve = route(ConferenceCtrlTest.app, reqEve).get

    assert(status(responseEve) == OK)

    val adminCookie = getCookie(assets.admin, "testtest")
    val request = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie)
    val responseAdmin = route(ConferenceCtrlTest.app, request).get

    assert(status(responseAdmin) == OK)
    assert(contentAsJson(responseAdmin).equals(Json.parse(existing.schedule)))

    val emptyScheduleID = assets.conferences(1).uuid
    val reqEmpty = FakeRequest(GET, s"/$mainUrl/$emptyScheduleID/$urlCap").withCookies(adminCookie)
    val response = route(ConferenceCtrlTest.app, reqEmpty).get

    assert(status(response) == NOT_FOUND)
    assert(contentAsJson(response).asInstanceOf[JsObject]
      .values.head.asInstanceOf[JsString].value.equals("Schedule entry not found."))
  }

  @Test
  def testSetSchedule(): Unit = {
    val uuid = assets.conferences(2).uuid
    val mainUrl = "api/conferences"
    val urlCap = "schedule"

    val validJson = Json.toJson("""{"entryOne": 1, "entryTwo": 2}""")

    val reqNoUser = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withJsonBody(validJson)
    val responseNoUser = route(ConferenceCtrlTest.app, reqNoUser).get

    assert(status(responseNoUser) == UNAUTHORIZED)

    val eveCookie = getCookie(assets.eve, "testtest")
    val reqNoAccess = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(eveCookie).withJsonBody(validJson)
    val responseNoAccess = routeWithErrors(ConferenceCtrlTest.app, reqNoAccess).get

    assert(status(responseNoAccess) == FORBIDDEN)

    val adminCookie = getCookie(assets.admin, "testtest")
    val req = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie).withJsonBody(validJson)
    val response = route(ConferenceCtrlTest.app, req).get

    assert(status(response) == OK)

    val reqCheck = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie)
    val getValidResponse = route(ConferenceCtrlTest.app, reqCheck).get

    assert(contentAsJson(getValidResponse).equals(validJson))

    val invalidJson = """entryOne: 1, "entryTwo": 2}"""
    val reqInvalid = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie).withBody(invalidJson)
    val responseInvalid = route(ConferenceCtrlTest.app, reqInvalid).get

    assert(status(responseInvalid) == BAD_REQUEST)

    val reqInvalidCheck = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie)
    val getInvalidResponse = route(ConferenceCtrlTest.app, reqInvalidCheck).get

    assert(contentAsJson(getInvalidResponse).equals(validJson))

    val empty = ""
    val reqEmpty = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie).withBody(empty)
    val responseEmpty = route(ConferenceCtrlTest.app, reqEmpty).get

    assert(status(responseEmpty) == BAD_REQUEST)
  }

  @Test
  def testGetInfo(): Unit = {
    val existing = assets.conferences(0)
    val uuid = existing.uuid
    val mainUrl = "api/conferences"
    val urlCap = "info"
    val req = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap")
    val responseNoUser = route(ConferenceCtrlTest.app, req).get

    assert(status(responseNoUser) == OK)

    val eveCookie = getCookie(assets.eve, "testtest")
    val reqEve = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(eveCookie)
    val responseEve = route(ConferenceCtrlTest.app, reqEve).get

    assert(status(responseEve) == OK)

    val adminCookie = getCookie(assets.admin, "testtest")
    val request = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie)
    val responseAdmin = route(ConferenceCtrlTest.app, request).get

    assert(status(responseAdmin) == OK)
    assert( contentAsString(responseAdmin).equals(existing.info))

    val emptyInfoUuid = assets.conferences(1).uuid
    val reqEmpty = FakeRequest(GET, s"/$mainUrl/$emptyInfoUuid/$urlCap").withCookies(adminCookie)
    val response = route(ConferenceCtrlTest.app, reqEmpty).get

    assert(status(response) == NOT_FOUND)
    assert(contentAsJson(response).asInstanceOf[JsObject]
      .values.head.asInstanceOf[JsString].value.equals("Info entry not found."))
  }

  @Test
  def testSetInfo(): Unit = {
    val uuid = assets.conferences(2).uuid
    val mainUrl = "api/conferences"
    val urlCap = "info"

    val infoContent = "> Markdown entry"

    val reqNoUser = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withTextBody(infoContent)
    val responseNoUser = route(ConferenceCtrlTest.app, reqNoUser).get

    assert(status(responseNoUser) == UNAUTHORIZED)

    val eveCookie = getCookie(assets.eve, "testtest")
    val reqNoAccess = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(eveCookie).withTextBody(infoContent)
    val responseNoAccess = routeWithErrors(ConferenceCtrlTest.app, reqNoAccess).get

    assert(status(responseNoAccess) == FORBIDDEN)

    val adminCookie = getCookie(assets.admin, "testtest")
    val req = FakeRequest(PUT, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie).withTextBody(infoContent)
    val response = route(ConferenceCtrlTest.app, req).get

    assert(status(response) == OK)

    val reqCheck = FakeRequest(GET, s"/$mainUrl/$uuid/$urlCap").withCookies(adminCookie)
    val getValidResponse = route(ConferenceCtrlTest.app, reqCheck).get

    assert(contentAsString(getValidResponse).equals(infoContent))
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
