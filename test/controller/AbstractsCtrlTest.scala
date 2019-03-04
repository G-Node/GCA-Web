package controller

import play.api.test.{FakeRequest, FakeApplication}
import org.junit.{Test, Before, AfterClass, BeforeClass}
import play.api.Play
import play.api.test.Helpers._
import utils.serializer.{AccountFormat, AbstractFormat}
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import models.{AbstractState, Abstract}
import play.api.mvc.Cookie
import utils.DefaultRoutesResolver._

class AbstractsCtrlTest extends BaseCtrlTest {

  implicit val absFormat = new AbstractFormat()
  var cookie: Cookie = _

  @Before
  override def before() : Unit = {
    super.before()

    //auth only once
    cookie = getCookie(assets.alice, "testtest")
  }

  @Test
  def testGet() {

    var id = "NOTEXISTANT"
    var req = FakeRequest(GET, s"/api/abstracts/$id")

    var result = routeWithErrors(AbstractsCtrlTest.app, req).get
    assert(status(result) == NOT_FOUND)

    id = assets.abstracts(0).uuid //is published
    req = FakeRequest(GET, s"/api/abstracts/$id")

    result = route(AbstractsCtrlTest.app, req).get
    assert(status(result) == OK)

    val eTagHeader = header(ETAG, result)
    assert(eTagHeader.isDefined)
    val eTag = eTagHeader.get

    req = FakeRequest(GET, s"/api/abstracts/$id").withHeaders("If-None-Match" -> eTag)
    result = route(AbstractsCtrlTest.app, req).get
    assert(status(result) == NOT_MODIFIED)

    req = FakeRequest(GET, s"/api/abstracts/$id").withHeaders("If-None-Match" -> "42")
    result = route(AbstractsCtrlTest.app, req).get
    assert(status(result) == OK)
    assert(header(ETAG, result).get == eTag)


    id = assets.abstracts(2).uuid //approved == false, published == false
    req = FakeRequest(GET, s"/api/abstracts/$id").withCookies(cookie) //but we auth as the owner

    result = route(AbstractsCtrlTest.app, req).get
    assert(status(result) == OK)
  }

  @Test
  def testCreate() {

    val oldAbstract = assets.createAbstract()
    oldAbstract.title = "Cool new Method to do cool new stuff"
    oldAbstract.conference = assets.conferences(0)

    val body = Json.toJson(oldAbstract)
    val confId = assets.conferences(0).uuid
    val reqNoAuth = FakeRequest(POST, s"/api/conferences/$confId/abstracts").withJsonBody(body)

    val reqNoAuthResult = routeWithErrors(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)

    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == CREATED)
    assert(header(ETAG, reqAuthResult).isDefined)

    val loadedAbs = contentAsJson(reqAuthResult).as[Abstract]
    assert(loadedAbs.title == oldAbstract.title)

    //make sure we cannot create an abstract if conf is closed

    val bobCookie =  getCookie(assets.bob, "testtest")
    val confIdClosed = assets.conferences(1).uuid
    val rq = FakeRequest(POST, s"/api/conferences/$confIdClosed/abstracts").withJsonBody(body).withCookies(bobCookie)
    val rqResult = routeWithErrors(AbstractsCtrlTest.app, rq).get
    assert(status(rqResult) == FORBIDDEN)
  }

  @Test
  def testUpdate() {

    val original = assets.abstracts(0)

    original.title = "Cool new title"
    original.affiliations.clear()

    val absUUID = original.uuid

    val body = Json.toJson(original)
    val reqNoAuth = FakeRequest(PUT, s"/api/abstracts/$absUUID").withJsonBody(body)

    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)

    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)
    assert(header(ETAG, reqAuthResult).isDefined)

    val loadedAbs = contentAsJson(reqAuthResult).as[Abstract]
    assert(loadedAbs.title == original.title)
  }

  @Test
  def testUpdateState() {

    val original = assets.abstracts(0)

    original.state = AbstractState.Withdrawn
    val absUUID = original.uuid

    val body = Json.toJson(original)
    val reqNoAuth = FakeRequest(PUT, s"/api/abstracts/$absUUID").withJsonBody(body)

    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)

    val reqAuthResult = routeWithErrors(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == FORBIDDEN)
  }

  @Test
  def testListByAccount() {

    val uid = assets.alice.uuid
    val reqNoAuth = FakeRequest(GET, s"/api/user/$uid/abstracts")
    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)

    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)

    val loadedAbs = contentAsJson(reqAuthResult).as[Seq[Abstract]]
    assert(loadedAbs.length > 0)

  }

  @Test
  def testListFavByAccount() {

    val bobCookie = getCookie(assets.bob, "testtest")
    val uid = assets.bob.uuid
    val reqNoAuth = FakeRequest(GET, s"/api/user/$uid/favouriteabstracts")
    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(bobCookie)

    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)

    val loadedAbs = contentAsJson(reqAuthResult).as[Seq[Abstract]]
    assert(loadedAbs.nonEmpty)
  }

  @Test
  def testListByConference() {

    val cid = assets.conferences(0).uuid
    val reqNoAuth = FakeRequest(GET, s"/api/conferences/$cid/abstracts")

    val reqAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqAuthResult) == OK)
    assert(header(ETAG, reqAuthResult).isDefined)

    val loadedAbs = contentAsJson(reqAuthResult).as[Seq[Abstract]]

    //Assure we have at least one, but none that is not published
    assert(loadedAbs.length > 0 && loadedAbs.count{ _.state != AbstractState.Accepted } == 0)

    val eTag = header(ETAG, reqAuthResult).get
    val reqETag = FakeRequest(GET, s"/api/conferences/$cid/abstracts").withHeaders(
      "If-None-Match" -> eTag
    )

    val resETag = route(AbstractsCtrlTest.app, reqETag).get
    assert(status(resETag) ==  NOT_MODIFIED)
  }

  @Test
  def testListFavByConf() {

    val cid = assets.conferences(0).uuid
    val reqNoAuth = FakeRequest(GET, s"/api/user/self/conferences/$cid/favouriteabstracts")

    val reqNoAuthResult = routeWithErrors(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)
    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)
    assert(header(ETAG, reqAuthResult).isDefined)

    val loadedAbsAlice = contentAsJson(reqAuthResult).as[Seq[Abstract]]

    assert(loadedAbsAlice.isEmpty)

    val bobCookie = getCookie(assets.bob, "testtest")
    val reqBob = reqNoAuth.withCookies(bobCookie)
    val reqBobResult = route(AbstractsCtrlTest.app, reqBob).get
    assert(status(reqBobResult) == OK)

    val loadedAbs = contentAsJson(reqBobResult).as[Seq[Abstract]]
    assert(loadedAbs.length == assets.abstracts.size)
  }

  @Test
  def testListFavUuidByConf() {

    val cid = assets.conferences(0).uuid
    val reqNoAuth = FakeRequest(GET, s"/api/user/self/conferences/$cid/favabstractuuids")

    val reqNoAuthResult = routeWithErrors(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)
    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)

    val loadedJSONAlice = contentAsJson(reqAuthResult).as[Array[String]]
    assert(loadedJSONAlice.length == 0)

    val bobCookie = getCookie(assets.bob, "testtest")
    val reqBob = reqNoAuth.withCookies(bobCookie)
    val reqBobResult = route(AbstractsCtrlTest.app, reqBob).get
    assert(status(reqAuthResult) == OK)

    val loadedJSONBob = contentAsJson(reqBobResult).as[Array[String]]
    assert(loadedJSONBob.length == assets.abstracts.size)
  }

  @Test
  def testAddFavUser() {
    //Add
    val absUUID = assets.abstracts(0).uuid
    val reqNoAuth = FakeRequest(PUT, s"/api/abstracts/$absUUID/addfavuser")
    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)

    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)

    val cid = assets.conferences(0).uuid
    val reqList = FakeRequest(GET, s"/api/user/self/conferences/$cid/favabstractuuids").withCookies(cookie)

    val reqListResult = route(AbstractsCtrlTest.app, reqList).get
    val loadedJSONAlice = contentAsJson(reqListResult).as[Array[String]]
    assert(loadedJSONAlice.length > 0)
  }

  @Test
  def testDeleteFavUser() {
    //Add
    val absUUID = assets.abstracts(0).uuid
    val reqNoAuth = FakeRequest(DELETE, s"/api/abstracts/$absUUID/removefavuser")
    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)

    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)

    val cid = assets.conferences(0).uuid
    val reqList = FakeRequest(GET, s"/api/user/self/conferences/$cid/favabstractuuids").withCookies(cookie)

    val reqListResult = route(AbstractsCtrlTest.app, reqList).get
    val loadedJSONAlice = contentAsJson(reqListResult).as[Array[String]]
    assert(loadedJSONAlice.length == 0)
  }

  @Test
  def testDelete() {
    val absUUID = assets.abstracts(0).uuid
    val reqNoAuth = FakeRequest(DELETE, s"/api/abstracts/$absUUID")
    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)

    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)
  }

  @Test
  def testPermissions(): Unit = {

    val accountFormat = new AccountFormat()

    def parseOwners = {json: JsValue =>
      for (acc <- json.as[List[JsObject]])
        yield accountFormat.reads(acc).get.uuid
    }

    val abstrid = assets.abstracts(0).uuid

    var getR = FakeRequest(GET, s"/api/abstracts/$abstrid/owners").withCookies(cookie)
    var response = route(AbstractsCtrlTest.app, getR).get

    assert(status(response) == OK)
    assert(parseOwners(contentAsJson(response)).contains(assets.alice.uuid))

    val body = JsArray(for (acc <- List(assets.bob, assets.eve)) yield accountFormat.writes(acc))
    var postR = FakeRequest(PUT, s"/api/abstracts/$abstrid/owners").withCookies(cookie).withJsonBody(body)
    response = route(AbstractsCtrlTest.app, postR).get

    assert(status(response) == OK)
    assert(parseOwners(contentAsJson(response)).contains(assets.eve.uuid))

    val bobCookie = getCookie(assets.bob, "testtest")
    getR = FakeRequest(GET, s"/api/abstracts/$abstrid/owners").withCookies(bobCookie)
    response = route(AbstractsCtrlTest.app, getR).get

    assert(status(response) == OK)
    assert(!parseOwners(contentAsJson(response)).contains(assets.alice.uuid))

    val adminCookie = getCookie(assets.admin, "testtest")
    getR = FakeRequest(GET, s"/api/abstracts/$abstrid/owners").withCookies(adminCookie)
    response = route(AbstractsCtrlTest.app, getR).get

    assert(status(response) == OK)

    postR = FakeRequest(PUT, s"/api/abstracts/$abstrid/owners").withCookies(adminCookie).withJsonBody(body)
    response = routeWithErrors(AbstractsCtrlTest.app, postR).get

    assert(status(response) == FORBIDDEN)
  }

  @Test
  def testSetState() {

    val abstr = assets.abstracts(2)

    val absUUID = abstr.uuid

    var stateChange = Json.obj("state" -> "Submitted", "note" -> "")
    val reqNoAuth = FakeRequest(PUT, s"/api/abstracts/$absUUID/state").withJsonBody(stateChange)

    val reqNoAuthResult = routeWithErrors(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    //try as bob, who is one of the owners, so should be OK
    val bobCookie = getCookie(assets.bob, "testtest")

    var reqAuth = reqNoAuth.withCookies(bobCookie)
    var reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)

    val getReq = FakeRequest(GET, s"/api/abstracts/$absUUID").withCookies(bobCookie)
    val getRes = route(AbstractsCtrlTest.app, getReq).get
    assert(status(getRes) == OK)
    val etag = header("etag", getRes)
    assert(etag.isDefined && etag.get != abstr.eTag) // setting the state must update the etag


    //try to change a state that we are not allowed to as owner
    stateChange = Json.obj("state" -> "InReview", "note" -> "")
    reqAuth = reqAuth.withJsonBody(stateChange)
    reqAuthResult = routeWithErrors(AbstractsCtrlTest.app, reqAuth).get

    assert(status(reqAuthResult) == FORBIDDEN)

    //now try as alice (who is conference admin)
    reqAuth = reqAuth.withCookies(cookie)
    reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)
  }

  @Test
  def testPatchAbstract() {
    val abstr = assets.abstracts(1)
    val patch = Json.arr(Json.obj("op" -> "add", "path" -> "/sortId", "value" -> 2),
                         Json.obj("op" -> "add", "path" -> "/doi", "value" -> "10.12751/nncn.test.0042"))
    val absUUID = abstr.uuid
    val reqNoAuth = FakeRequest("PATCH", s"/api/abstracts/$absUUID").withJsonBody(patch)

    val reqNoAuthResult = routeWithErrors(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    //bob should not be allowed to do this
    val bobCookie = getCookie(assets.bob, "testtest")
    var reqAuth = reqNoAuth.withCookies(bobCookie)
    var reqAuthResult = routeWithErrors(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == FORBIDDEN)

    //now try as alice (who is conference admin)
    reqAuth = reqAuth.withCookies(cookie)
    reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)

    val loadedAbs = contentAsJson(reqAuthResult).as[Abstract]
    assert(loadedAbs.sortId == 2)
    assert(loadedAbs.doi == "10.12751/nncn.test.0042")
  }

}

object AbstractsCtrlTest {

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