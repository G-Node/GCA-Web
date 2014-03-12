package controller

import org.scalatest.junit.JUnitSuite
import service.util.DBUtil
import javax.persistence.{Persistence, EntityManagerFactory}
import service.{Assets, UserStore}
import play.api.test.{FakeRequest, FakeApplication}
import org.junit.{Test, Before, AfterClass, BeforeClass}
import play.api.Play
import play.api.test.Helpers._
import securesocial.controllers.ProviderController.authenticateByPost
import utils.serializer.AbstractFormat
import play.api.libs.json.Json
import securesocial.core.IdentityId
import models.Abstract
import play.api.mvc.Cookie

class AbstractsCtrlTest extends JUnitSuite with DBUtil {

  implicit val absFormat = new AbstractFormat("http://localhost:9000")

  var emf: EntityManagerFactory = _
  var store: UserStore = _
  var assets: Assets = _

  var cookie: Cookie = _

  @Before
  def before() : Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    assets = new Assets(emf)
    assets.killDB()
    assets.fillDB()

    //auth only once
    cookie = getCookie(assets.alice.identityId, "testtest")
  }


  // utility function
  def getCookie (id: IdentityId, password: String) = {
    val authRequest = FakeRequest().withFormUrlEncodedBody(
      "username" -> id.userId,
      "password" -> password
    )
    val authResponse = authenticateByPost(id.providerId)(authRequest)
    cookies(authResponse).get("id").getOrElse {
      throw new RuntimeException("Could not authenticate successfully")
    }
  }

  //test no auth


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
    assert(loadedAbs.length > 0)
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