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
    cookie = getCookie(assets.alice.identityId, "testtest")
  }

  @Test
  def testGet() {

    var id = "NOTEXISTANT"
    var req = FakeRequest(GET, s"/api/abstracts/$id")

    var result = route(AbstractsCtrlTest.app, req).get
    assert(status(result) == NOT_FOUND)

    id = assets.abstracts(0).uuid //is published
    req = FakeRequest(GET, s"/api/abstracts/$id")

    result = route(AbstractsCtrlTest.app, req).get
    assert(status(result) == OK)

    id = assets.abstracts(2).uuid //approved == false, published == false
    req = FakeRequest(GET, s"/api/abstracts/$id").withCookies(cookie) //but we auth as the owner

    result = route(AbstractsCtrlTest.app, req).get
    assert(status(result) == OK)
  }

  @Test
  def testCreate() {

    val oldAbstract = assets.abstracts(0)
    oldAbstract.title = "Cool new Method to do cool new stuff"
    oldAbstract.uuid = null //we want a new one

    val body = Json.toJson(oldAbstract)
    val confId = assets.conferences(0).uuid
    val reqNoAuth = FakeRequest(POST, s"/api/conferences/$confId/abstracts").withJsonBody(body)

    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    val reqAuth = reqNoAuth.withCookies(cookie)

    val reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == CREATED)

    val loadedAbs = contentAsJson(reqAuthResult).as[Abstract]
    assert(loadedAbs.title == oldAbstract.title)

    //make sure we cannot create an abstract if conf is closed

    val bobCookie =  getCookie(assets.bob.identityId, "testtest")
    val confIdClosed = assets.conferences(1).uuid
    val rq = FakeRequest(POST, s"/api/conferences/$confIdClosed/abstracts").withJsonBody(body).withCookies(bobCookie)
    val rqResult = route(AbstractsCtrlTest.app, rq).get
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

    val loadedAbs = contentAsJson(reqAuthResult).as[Abstract]
    assert(loadedAbs.title == original.title)
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
  def testListByConference() {

    val cid = assets.conferences(0).uuid
    val reqNoAuth = FakeRequest(GET, s"/api/conferences/$cid/abstracts")

    val reqAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqAuthResult) == OK)

    val loadedAbs = contentAsJson(reqAuthResult).as[Seq[Abstract]]

    //Assure we have at least one, but none that is not published
    assert(loadedAbs.length > 0 && loadedAbs.count{ _.state != AbstractState.Published } == 0)
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

    val bobCookie = getCookie(assets.bob.identityId, "testtest")
    getR = FakeRequest(GET, s"/api/abstracts/$abstrid/owners").withCookies(bobCookie)
    response = route(AbstractsCtrlTest.app, getR).get

    assert(status(response) == OK)
    assert(!parseOwners(contentAsJson(response)).contains(assets.alice.uuid))

    val adminCookie = getCookie(assets.admin.identityId, "testtest")
    getR = FakeRequest(GET, s"/api/abstracts/$abstrid/owners").withCookies(adminCookie)
    response = route(AbstractsCtrlTest.app, getR).get

    assert(status(response) == OK)

    postR = FakeRequest(PUT, s"/api/abstracts/$abstrid/owners").withCookies(adminCookie).withJsonBody(body)
    response = route(AbstractsCtrlTest.app, postR).get

    assert(status(response) == FORBIDDEN)
  }

  @Test
  def testSetState() {

    val abstr = assets.abstracts(2)

    val absUUID = abstr.uuid

    var stateChange = Json.obj("state" -> "Submitted", "note" -> "")
    val reqNoAuth = FakeRequest(PUT, s"/api/abstracts/$absUUID/state").withJsonBody(stateChange)

    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    //try as bob, who is one of the owners, so should be OK
    val bobCookie = getCookie(assets.bob.identityId, "testtest")

    var reqAuth = reqNoAuth.withCookies(bobCookie)
    var reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)

    //try to change a state that we are not allowed to as owner
    stateChange = Json.obj("state" -> "InReview", "note" -> "")
    reqAuth = reqAuth.withJsonBody(stateChange)
    reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get

    assert(status(reqAuthResult) == FORBIDDEN)

    //now try as alice (who is conference admin)
    reqAuth = reqAuth.withCookies(cookie)
    reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)
  }

  @Test
  def testPatchAbstract(): Unit = {
    val abstr = assets.abstracts(1)
    val patch = Json.arr(Json.obj("op" -> "add", "path" -> "/sortId", "value" -> 2))
    val absUUID = abstr.uuid
    val reqNoAuth = FakeRequest("PATCH", s"/api/abstracts/$absUUID").withJsonBody(patch)

    val reqNoAuthResult = route(AbstractsCtrlTest.app, reqNoAuth).get
    assert(status(reqNoAuthResult) == UNAUTHORIZED)

    //bob should not be allowed to do this
    val bobCookie = getCookie(assets.bob.identityId, "testtest")
    var reqAuth = reqNoAuth.withCookies(bobCookie)
    var reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == FORBIDDEN)

    //now try as alice (who is conference admin)
    reqAuth = reqAuth.withCookies(cookie)
    reqAuthResult = route(AbstractsCtrlTest.app, reqAuth).get
    assert(status(reqAuthResult) == OK)
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