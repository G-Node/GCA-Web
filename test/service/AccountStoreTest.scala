package service

import javax.persistence.NoResultException

import com.mohiva.play.silhouette.contrib.utils.BCryptPasswordHasher
import com.mohiva.play.silhouette.core.LoginInfo
import models.Account
import org.junit.{AfterClass, Before, BeforeClass, Test}
import org.scalatest.junit.JUnitSuite
import play.api.Play
import play.api.test.FakeApplication

class AccountStoreTest extends JUnitSuite {

  var assets: Assets = _
  var store: AccountStore = _


  val existingAccountList = List (
    new LoginInfo("credentials", "alice@foo.com"),
    new LoginInfo("credentials", "bob@bar.com"),
    new LoginInfo("credentials", "eve@evil.com"),
    new LoginInfo("credentials", "Alice@foo.com"), //test if email matching is case insensitive
    new LoginInfo("credentials", "BOB@bar.com"),
    new LoginInfo("credentials", "EVE@EVIL.COM"))

  @Before
  def before() : Unit = {
    assets = new Assets()
    assets.killDB()
    assets.fillDB()
    store = new AccountStore(new BCryptPasswordHasher())
  }


  @Test
  def testGet(): Unit = {
    for (account <- assets.accounts) {
      val result = store.get(account.uuid)
      assert(result.uuid == account.uuid)
    }

    intercept[NoResultException](store.get("doesNotExist"))
  }

  @Test
  def testFindByEmail(): Unit =  {
    for (id <- existingAccountList) {
      val account = store.getByMail(id.providerKey)
      assert(account.mail.equalsIgnoreCase(id.providerKey))
    }

    intercept[NoResultException](store.getByMail("notindb@forsure.com"))
  }

  @Test
  def testList(): Unit = {

    val l = store.list()

    assert(l.size == assets.accounts.size)

    for(acc <- assets.accounts) {
      assert(l.contains(acc))
    }
  }

  @Test
  def testCreate(): Unit = {

    val accountNew = Account(null, "newuser@bar.com", "New", "User", None)
    val accountCreated = store.create(accountNew)

    assert(accountCreated.uuid != null)
    assert(accountNew.mail.equalsIgnoreCase(accountCreated.mail))

    var accountRetrieved = store.get(accountCreated.uuid)

    assert(accountRetrieved.uuid != null)
    assert(accountNew.mail.equalsIgnoreCase(accountRetrieved.mail))

    accountRetrieved = store.getByMail(accountNew.mail)

    assert(accountRetrieved.uuid != null)
    assert(accountNew.mail.equalsIgnoreCase(accountRetrieved.mail))
  }

  @Test
  def testUpdate(): Unit = {

    val alice = assets.alice

    alice.firstName = "Alice"
    alice.lastName = "Schwarzer"
    alice.fullName = "Alice Sophie Schwarzer"

    val aliceUpdated = store.update(alice)

    assert(aliceUpdated.firstName == "Alice")
    assert(aliceUpdated.lastName == "Schwarzer")
    assert(aliceUpdated.fullName == "Alice Sophie Schwarzer")

    val accountNew = Account(null, "newuser@bar.com", "New", "User", None)

    intercept[NoResultException](store.update(accountNew))
  }

  @Test
  def testDelete(): Unit = {
    store.delete(assets.alice)
    intercept[NoResultException](store.get(assets.alice.uuid))
  }

}



object AccountStoreTest {

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