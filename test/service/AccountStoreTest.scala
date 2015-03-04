package service

import javax.persistence.NoResultException

import com.mohiva.play.silhouette.contrib.utils.BCryptPasswordHasher
import com.mohiva.play.silhouette.core.LoginInfo
import models.{CredentialsLogin, Account}
import org.junit.{AfterClass, Before, BeforeClass, Test}
import org.scalatest.junit.JUnitSuite
import play.api.Play
import play.api.test.FakeApplication
import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class AccountStoreTest extends JUnitSuite {

  var assets: Assets = _
  var accountStore: AccountStore = _
  var credentialStore: CredentialsStore = _


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
    accountStore = new AccountStore(new BCryptPasswordHasher())
    credentialStore = new CredentialsStore()
  }


  @Test
  def testGet(): Unit = {
    for (account <- assets.accounts) {
      val result = accountStore.get(account.uuid)
      assert(result.uuid == account.uuid)
    }

    intercept[NoResultException](accountStore.get("doesNotExist"))
  }

  @Test
  def testFindByEmail(): Unit =  {
    for (id <- existingAccountList) {
      val account = accountStore.getByMail(id.providerKey)
      assert(account.mail.equalsIgnoreCase(id.providerKey))
    }

    intercept[NoResultException](accountStore.getByMail("notindb@forsure.com"))
  }

  @Test
  def testList(): Unit = {

    val l = accountStore.list()

    assert(l.size == assets.accounts.size)

    for(acc <- assets.accounts) {
      assert(l.contains(acc))
    }
  }

  @Test
  def testCreate(): Unit = {
    val accountNew = Account(null, "newuser@bar.com", "New", "User", None)
    val accountCreated = accountStore.create(accountNew, Some("testtest"))

    assert(accountCreated.uuid != null)
    assert(accountCreated.logins.size() == 1)
    assert(accountCreated.logins.forall {
        case l: CredentialsLogin => ! l.isActive
        case _ => true
      }
    )
    assert(accountNew.mail.equalsIgnoreCase(accountCreated.mail))

    accountCreated.logins.foreach {
      case l: CredentialsLogin => Await.result(credentialStore.activate(l.token), Duration.Inf)
    }

    var accountRetrieved = accountStore.get(accountCreated.uuid)

    assert(accountRetrieved.uuid != null)
    assert(accountNew.mail.equalsIgnoreCase(accountRetrieved.mail))

    accountRetrieved = accountStore.getByMail(accountNew.mail)

    assert(accountRetrieved.uuid != null)
    assert(accountNew.mail.equalsIgnoreCase(accountRetrieved.mail))
  }

  @Test
  def testUpdate(): Unit = {

    val alice = assets.alice

    alice.firstName = "Alice"
    alice.lastName = "Schwarzer"
    alice.fullName = "Alice Sophie Schwarzer"

    val aliceUpdated = accountStore.update(alice)

    assert(aliceUpdated.firstName == "Alice")
    assert(aliceUpdated.lastName == "Schwarzer")
    assert(aliceUpdated.fullName == "Alice Sophie Schwarzer")

    val accountNew = Account(null, "newuser@bar.com", "New", "User", None)

    intercept[NoResultException](accountStore.update(accountNew))
  }

  @Test
  def testDelete(): Unit = {
    accountStore.delete(assets.alice)
    intercept[NoResultException](accountStore.get(assets.alice.uuid))
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