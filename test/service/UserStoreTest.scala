package service

import org.junit.{AfterClass, Before, BeforeClass, Test}
import org.scalatest.junit.JUnitSuite
import play.api.Play
import play.api.test.FakeApplication
import models.Account
import com.mohiva.play.silhouette.core.{LoginInfo, Identity}

class UserStoreTest extends JUnitSuite {

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
    store = new AccountStore()
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
  def testFindByEmail() {
    for (id <- existingAccountList) {
      val account = store.findByEmail(id.providerKey)
      assert(account.nonEmpty)
    }

    assert(store.findByEmail("notindb@forsure.com").isEmpty)
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