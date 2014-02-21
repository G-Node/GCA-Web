/**
 * Created by sobolev on 2/21/14.
 */

package test

import org.scalatest.junit.JUnitSuite
import org.junit._
import play.api.test.FakeApplication
import play.api.Play
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._
import utils.serializer._

/**
 * Tests for Serializer
 */
class SerializerTest extends JUnitSuite {

  val baseUrl = "http://hostname:port"

  @Test
  def testConference(): Unit = {
    val jsFormat = new ConferenceFormat(baseUrl)

    val c1 = JsObject(Seq("uuid" -> JsString("foo"), "name" -> JsString("bar")))

    //jsFormat.reads(c1).fold(
    //  valid = { conf => println(conf) } //assert(conf.name == "foo")
    //  invalid = { errors => errors.toString }
    //)
    //assert(list.size == 2)
  }

}

object ConferenceServiceTest {

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
