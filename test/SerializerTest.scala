package test

import org.junit._
import org.scalatest.junit.JUnitSuite
import play.api.test.FakeApplication
import play.api.Play
import play.api.libs.json._

import models._
import utils.serializer._

/**
 * Tests for Serializer
 */
class SerializerTest extends JUnitSuite {

  val baseUrl = "http://hostname:8000"

  val sampleConference: Conference = Conference(Option("someuuid"), Option("bar"))
  val sampleAccount: Account = Account(Option("someuuid"), Option("example@gnode.org"))
  val sampleAuthor: Author = Author(Option("someuuid"), Option("email"), Option("first"),
    Option("middle"), Option("last"))
  val sampleAffiliation: Affiliation = Affiliation(Option("someuuid"), Option("address"), Option("country"),
    Option("department"), Option("name"), Option("section"))
  val sampleReference: Reference = Reference(Option("someuuid"), Option("authors"), Option("title"),
    Option(2013), Option("doi"))
  val sampleFigure: Figure = Figure(Option("someuuid"), Option("name"), Option("caption"))
  val sampleAbstract = Abstract(Option("someuuid"), Option("title"), Option("topic"),
      Option("text"), Option("doi"), Option("conflictOfInterest"), Option("acknowledgements"),
        false, false, Option(sampleConference), Option(sampleFigure), Nil, Seq(sampleAuthor),
          Seq(sampleAffiliation), Seq(sampleReference))

  @Test
  def testConference(): Unit = {
    val jsFormat = new ConferenceFormat(baseUrl)

    val original = Conference(Option("someuuid"), Option("bar"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

  @Test
  def testAccount(): Unit = {
    val jsFormat = new AccountFormat(baseUrl)

    val original = Account(Option("someuuid"), Option("example@gnode.org"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

  @Test
  def testAuthor(): Unit = {
    val jsFormat = new AuthorFormat()

    val original = Author(Option("someuuid"), Option("email"), Option("first"),
                            Option("middle"), Option("last"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

  @Test
  def testAffiliation(): Unit = {
    val jsFormat = new AffiliationFormat()

    val original = Affiliation(Option("someuuid"), Option("address"), Option("country"),
      Option("department"), Option("name"), Option("section"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

  @Test
  def testReference(): Unit = {
    val jsFormat = new ReferenceFormat()

    val original = Reference(Option("someuuid"), Option("authors"), Option("title"),
      Option(2013), Option("doi"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

  @Test
  def testFigure(): Unit = {
    val jsFormat = new FigureFormat()

    val original = Figure(Option("someuuid"), Option("name"), Option("caption"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

}

object SerializerTest {

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
