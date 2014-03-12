package controller

import javax.persistence.{Persistence, EntityManagerFactory}
import service.Assets
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.junit.Before
import org.scalatest.junit.JUnitSuite
import service.util.DBUtil
import securesocial.core.IdentityId
import securesocial.controllers.ProviderController.authenticateByPost


trait BaseCtrlTest extends JUnitSuite with DBUtil  {

  var emf : EntityManagerFactory = _
  var assets : Assets = _

  /**
   * Utility function to get an authenticated Cookie
    */
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

  @Before
  def before() : Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    assets = new Assets(emf)
    assets.killDB()
    assets.fillDB()
  }
}
