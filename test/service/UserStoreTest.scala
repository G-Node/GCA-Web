package service

import org.scalatest.junit.JUnitSuite
import service.util.DBUtil
import play.api.test.FakeApplication
import org.junit.{Test, Before, AfterClass, BeforeClass}
import play.api.Play
import javax.persistence.{Persistence, EntityManagerFactory}
import securesocial.core.IdentityId
import models.Account

class UserStoreTest extends JUnitSuite with DBUtil {

  var emf: EntityManagerFactory = _
  var store: UserStore = _
  var assets: Assets = _
  val existingAccountList = List (
    new IdentityId("alice@foo.com", "userpass"),
    new IdentityId("bob@bar.com", "userpass"),
    new IdentityId("eve@evil.com", "userpass"),
    new IdentityId("Alice@foo.com", "userpass"), //test if email matching is case insensitive
    new IdentityId("BOB@bar.com", "userpass"),
    new IdentityId("EVE@EVIL.COM", "userpass"))


  @Before
  def before() : Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    assets = new Assets(emf)
    assets.killDB()
    assets.fillDB()
    store = new UserStore(UserStoreTest.app)
  }

  @Test
  def testList() {

    val l = store.list()

    assert(l.size == assets.accounts.size)

    for(acc <- assets.accounts) {
      assert(l.contains(acc))
    }
  }

  @Test
  def testFindById() {

    for (id <- existingAccountList) {
      val account = store.find(id)
      assert(!account.isEmpty)
    }

    assert(store.find(new IdentityId("notindb@forsure.com", "userpass")).isEmpty)
  }

  @Test
  def testFindByEmailProvider() {
    for (id <- existingAccountList) {
      val account = store.findByEmailAndProvider(id.userId, id.providerId)
      assert(account.isDefined)
    }

    assert(store.findByEmailAndProvider("notindb@forsure.com", "userpass").isEmpty)
  }

  @Test
  def testCreate() {
    val acc1 = Account(Option("42"), Option("douglas@adams.org"))
    acc1.userid = "douglas@adams.org"
    acc1.provider = "thebrain"

    assert(store.find(acc1.identityId).isEmpty)
    store.save(acc1)
    assert(store.find(acc1.identityId).isDefined)
  }

  @Test
  def testUpdate() {
    val acc1Opt = store.findAccount(new IdentityId("alice@foo.com", "userpass"))
    assert(acc1Opt.isDefined)
    val acc1 = acc1Opt.get

    acc1.mail = "foo@bar.se"
    assert(acc1.mail == acc1.email.get)

    store.save(acc1)

    val retAcc = store.find(acc1.identityId)
    assert(retAcc.isDefined)

    val res = retAcc flatMap {a =>
      a.email map {
        e => e == acc1.mail
      }
    }

    assert(res.isDefined && res.get)
  }

}



object UserStoreTest {

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