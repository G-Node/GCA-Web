package controller

import org.junit.Before
import org.scalatest.junit.JUnitSuite
import play.api.test.FakeRequest
import play.api.test.Helpers._
import securesocial.controllers.ProviderController.authenticateByPost
import securesocial.core.IdentityId
import service.Assets


trait BaseCtrlTest extends JUnitSuite {

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
    assets = new Assets()
    assets.killDB()
    assets.fillDB()
  }
}
