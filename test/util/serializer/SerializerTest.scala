package util.serializer

import org.junit._
import org.scalatest.junit.JUnitSuite
import play.api.test.FakeApplication
import play.api.Play

import models._
import utils.serializer._
import utils.DefaultRoutesResolver._
/**
 * Tests for Serializer
 */
class SerializerTest extends JUnitSuite {

  val sampleConference: Conference = Conference(Option("someuuid"), Option("bar"), Some("XX"), Some("G"), Some("X"),
                                                None, None, Some(false), Some(true), Some(false), Some(true),
                                                None, None, None)
  val sampleAccount: Account = Account(Option("someuuid"), Option("example@gnode.org"))
  val sampleAuthor: Author = Author(Option("someuuid"), Option("email"), Option("first"),
    Option("middle"), Option("last"))
  val sampleAffiliation: Affiliation = Affiliation(Option("someuuid"), Option("address"), Option("country"),
    Option("department"), Option("section"))
  val sampleReference: Reference = Reference(Option("someuuid"), Option("authors, title"),
    Option("http://www.someink.com"), Option("doi"))
  val sampleFigure: Figure = Figure(Option("someuuid"), Option("caption"))
  val sampleAbstract = Abstract(Option("someuuid"), Option("title"), Option("topic"),
      Option("text"), Option("doi"), Option("conflictOfInterest"), Option("acknowledgements"), Option(true), Option("reason"),
        Some(0), Some(AbstractState.InPreparation), Option(sampleConference), Seq(sampleFigure), Nil, Nil, Seq(sampleAuthor),
          Seq(sampleAffiliation), Seq(sampleReference))

  @Test
  def testConference(): Unit = {
    val jsFormat = new ConferenceFormat()

    val original = Conference(Option("someuuid"), Option("bar"), Some("XX"), Some("G"), Some("X"),
                             None, None, Some(false), Some(false), None, None, None, None)
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

  @Test
  def testAccount(): Unit = {
    val jsFormat = new AccountFormat()

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
      invalid = {
        errors => throw new MatchError(
          errors.toString()
        )
      }
    )
  }

  @Test
  def testAffiliation(): Unit = {
    val jsFormat = new AffiliationFormat()

    val original = Affiliation(Option("someuuid"), Option("address"), Option("country"),
      Option("department"), Option("section"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

  @Test
  def testReference(): Unit = {
    val jsFormat = new ReferenceFormat()

    val original = Reference(Option("someuuid"), Option("authors, title"),
      None, Option("doi"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

  @Test
  def testFigure(): Unit = {
    val jsFormat = new FigureFormat()

    val original = Figure(Option("someuuid"), Option("caption"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.uuid == original.uuid)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }

  @Test
  def testAbstract(): Unit = {
    val jsFormat = new AbstractFormat()

    val json = jsFormat.writes(sampleAbstract)

    jsFormat.reads(json).fold(
      valid = { converted => {
        assert(converted.uuid == sampleAbstract.uuid)
        assert(converted.authors.iterator().next().uuid == sampleAuthor.uuid)
        assert(converted.affiliations.iterator().next().uuid == sampleAffiliation.uuid)
        assert(converted.references.iterator().next().uuid == sampleReference.uuid)
      }},
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
