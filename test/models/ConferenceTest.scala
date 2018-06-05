package models

import org.junit._
import org.scalatest.junit.JUnitSuite

import scala.util.Random

/**
 * Test
 */
class ConferenceTest extends JUnitSuite {

  @Test
  def testConvertMarkdownToHTML() : Unit = {
    val r : Random = new Random()
    // test nulls throwing a NullPointerException
    intercept[NullPointerException] {
      Conference.convertMarkdownToHTML(null)
    }
    // test empty markdown returning empty html
    assert(Conference.convertMarkdownToHTML("") == "")
    // perform random tests for correct markdown parsing
    var testingString : String = ""
    for (i <- 0 to 10000) {
      testingString = ConferenceTest.generateRandomString(r.nextInt(200)+1)
      // normal text
      assert(Conference.convertMarkdownToHTML(testingString) == "<p>"+testingString+"</p>\n")
      // line break
      assert(Conference.convertMarkdownToHTML(testingString+"\\\n"+testingString)
        == "<p>"+testingString+"<br />\n"+testingString+"</p>\n")
      assert(Conference.convertMarkdownToHTML(testingString+"  \n"+testingString)
        == "<p>"+testingString+"<br />\n"+testingString+"</p>\n")
      assert(Conference.convertMarkdownToHTML(testingString+"\\"+testingString) // new line is needed for line break
        == "<p>"+testingString+"\\"+testingString+"</p>\n")
      assert(Conference.convertMarkdownToHTML(testingString+"  "+testingString)
        == "<p>"+testingString+"  "+testingString+"</p>\n")
      assert(Conference.convertMarkdownToHTML(testingString+"\\\n") // followup data is needed for line break
        == "<p>"+testingString+"\\</p>\n")
      // TODO: The parser trims whitespace.
      assert(Conference.convertMarkdownToHTML(testingString+"  \n")
        == "<p>"+testingString+"</p>\n")
      // font
      assert(Conference.convertMarkdownToHTML("_"+testingString+"_") == "<p><em>"+testingString+"</em></p>\n")
      assert(Conference.convertMarkdownToHTML("*"+testingString+"*") == "<p><em>"+testingString+"</em></p>\n")
      assert(Conference.convertMarkdownToHTML("__"+testingString+"__") == "<p><strong>"+testingString+"</strong></p>\n")
      assert(Conference.convertMarkdownToHTML("**"+testingString+"**") == "<p><strong>"+testingString+"</strong></p>\n")
      assert(Conference.convertMarkdownToHTML("___"+testingString+"___") == "<p><em><strong>"+testingString+"</strong></em></p>\n"
      || Conference.convertMarkdownToHTML("___"+testingString+"___") == "<p><strong><em>"+testingString+"</em></strong></p>\n")
      assert(Conference.convertMarkdownToHTML("***"+testingString+"***") == "<p><em><strong>"+testingString+"</strong></em></p>\n"
        || Conference.convertMarkdownToHTML("***"+testingString+"***") == "<p><strong><em>"+testingString+"</em></strong></p>\n")
      // heading
      assert(Conference.convertMarkdownToHTML("# "+testingString) == "<h1>"+testingString+"</h1>\n")
      assert(Conference.convertMarkdownToHTML("## "+testingString) == "<h2>"+testingString+"</h2>\n")
      assert(Conference.convertMarkdownToHTML("### "+testingString) == "<h3>"+testingString+"</h3>\n")
      assert(Conference.convertMarkdownToHTML("#### "+testingString) == "<h4>"+testingString+"</h4>\n")
      assert(Conference.convertMarkdownToHTML("##### "+testingString) == "<h5>"+testingString+"</h5>\n")
      assert(Conference.convertMarkdownToHTML("###### "+testingString) == "<h6>"+testingString+"</h6>\n")
      assert(Conference.convertMarkdownToHTML("####### "+testingString) == "<p>####### "+testingString+"</p>\n") // only six headers allowed
      assert(Conference.convertMarkdownToHTML("#"+testingString) == "<p>#"+testingString+"</p>\n") // whitespace needed
      assert(Conference.convertMarkdownToHTML(testingString + "\n=") == "<h1>"+testingString+"</h1>\n")
      assert(Conference.convertMarkdownToHTML(testingString + "\n-") == "<h2>"+testingString+"</h2>\n")
      // blockquote
      assert(Conference.convertMarkdownToHTML("> "+testingString) == "<blockquote>\n<p>"+testingString+"</p>\n</blockquote>\n")
      assert(Conference.convertMarkdownToHTML(">"+testingString) == "<blockquote>\n<p>"+testingString+"</p>\n</blockquote>\n")
      // lists
      assert(Conference.convertMarkdownToHTML("- "+testingString) == "<ul>\n<li>"+testingString+"</li>\n</ul>\n")
      assert(Conference.convertMarkdownToHTML("+ "+testingString) == "<ul>\n<li>"+testingString+"</li>\n</ul>\n")
      assert(Conference.convertMarkdownToHTML("* "+testingString) == "<ul>\n<li>"+testingString+"</li>\n</ul>\n")
      assert(Conference.convertMarkdownToHTML("-"+testingString) == "<p>-"+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("+"+testingString) == "<p>+"+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("*"+testingString) == "<p>*"+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("- "+testingString+"\n- "+testingString)
        == "<ul>\n<li>"+testingString+"</li>\n<li>"+testingString+"</li>\n</ul>\n")
      assert(Conference.convertMarkdownToHTML("1. "+testingString) == "<ol>\n<li>"+testingString+"</li>\n</ol>\n")
      assert(Conference.convertMarkdownToHTML("1."+testingString) == "<p>1."+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("2)"+testingString) == "<p>2)"+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("1. "+testingString+"\n2. "+testingString)
        == "<ol>\n<li>"+testingString+"</li>\n<li>"+testingString+"</li>\n</ol>\n")
      // links
      val urlTestingString : String = "http:"+testingString
      assert(Conference.convertMarkdownToHTML("<"+urlTestingString+">")
        == "<p><a href=\""+urlTestingString+"\">"+urlTestingString+"</a></p>\n")
      assert(Conference.convertMarkdownToHTML("["+testingString+"]"+"("+urlTestingString+")")
        == "<p><a href=\""+urlTestingString+"\">"+testingString+"</a></p>\n")
      // code
      assert(Conference.convertMarkdownToHTML("`"+testingString+"`") == "<p><code>"+testingString+"</code></p>\n")
      assert(Conference.convertMarkdownToHTML("    "+testingString) == "<pre><code>"+testingString+"\n</code></pre>\n")
      assert(Conference.convertMarkdownToHTML("```\n"+testingString+"\n```") == "<pre><code>"+testingString+"\n</code></pre>\n")
    }
  }

  @Test
  def testGetInfoAsHTML() : Unit = {
    /*
     * Mostly a placeholder for potential future changes, which need to be tested differently.
     */
    val r : Random = new Random()
    // test empty markdown returning empty html
    var testingInfo : String = ""
    var testingConf : Conference = null
    testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
      None, None, Some(false), Some(true), Some(false), Some(true), None, None, None, info = Some(testingInfo))
    assert(testingConf.getInfoAsHTML() == Conference.convertMarkdownToHTML(testingInfo))
    assert(testingConf.getInfoAsHTML() == "")
    // random tests
    for (i <- 0 to 10000) {
      testingInfo = ConferenceTest.generateRandomString(r.nextInt(200))
      testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
        None, None, Some(false), Some(true), Some(false), Some(true), None, None, None, info = Some(testingInfo))
      assert(testingConf.getInfoAsHTML() == Conference.convertMarkdownToHTML(testingInfo))
    }
    // test null yielding an empty string
    testingConf = Conference(Some("uuid"), Some("wrongconf"), Some("XX"), Some("G"), Some("X"),
      None, None, Some(false), Some(true), Some(false), Some(true), None, None, None, info = null)
    assert(testingConf.getInfoAsHTML() == "")
  }

  @Test
  def testGetDescriptionAsHTML() : Unit = {
    /*
     * Mostly a placeholder for potential future changes, which need to be tested differently.
     */
    val r : Random = new Random()
    // test empty markdown returning empty html
    var testingDescription : String = ""
    var testingConf : Conference = null
    testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
      None, Some(testingDescription), Some(false), Some(true), Some(false), Some(true), None, None, None)
    assert(testingConf.getDescriptionAsHTML() == Conference.convertMarkdownToHTML(testingDescription))
    assert(testingConf.getDescriptionAsHTML() == "")
    // random tests
    for (i <- 0 to 10000) {
      testingDescription = ConferenceTest.generateRandomString(r.nextInt(200))
      testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
        None, Some(testingDescription), Some(false), Some(true), Some(false), Some(true), None, None, None)
      assert(testingConf.getDescriptionAsHTML() == Conference.convertMarkdownToHTML(testingDescription))
    }
    // test null yielding an empty string
    testingConf = Conference(Some("uuid"), Some("wrongconf"), Some("XX"), Some("G"), Some("X"),
      None, null, Some(false), Some(true), Some(false), Some(true), None, None, None)
    assert(testingConf.getDescriptionAsHTML() == "")
  }

}

object ConferenceTest {

  val STATIC_RANDOM : Random = new Random()
  /*
   * This is an alphabet containing only characters suited for testing Markdown to
   * HTML conversion.
   */
  val RANDOM_STRING_CHARACTERS : String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZäöüÄÖÜ0123456789"

  def generateRandomString (length : Int) : String = {
    Stream.continually(STATIC_RANDOM.nextInt(RANDOM_STRING_CHARACTERS.length())).map(RANDOM_STRING_CHARACTERS).take(length).mkString
  }

}
