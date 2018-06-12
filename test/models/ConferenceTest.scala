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
      assert(Conference.convertMarkdownToHTML(testingString) == "<p class=\"paragraph-small\">"+testingString+"</p>\n")
      // line break
      assert(Conference.convertMarkdownToHTML(testingString+"\\\n"+testingString)
        == "<p class=\"paragraph-small\">"+testingString+"<br />\n"+testingString+"</p>\n")
      assert(Conference.convertMarkdownToHTML(testingString+"  \n"+testingString)
        == "<p class=\"paragraph-small\">"+testingString+"<br />\n"+testingString+"</p>\n")
      assert(Conference.convertMarkdownToHTML(testingString+"\\"+testingString) // new line is needed for line break
        == "<p class=\"paragraph-small\">"+testingString+"\\"+testingString+"</p>\n")
      assert(Conference.convertMarkdownToHTML(testingString+"  "+testingString)
        == "<p class=\"paragraph-small\">"+testingString+"  "+testingString+"</p>\n")
      assert(Conference.convertMarkdownToHTML(testingString+"\\\n") // followup data is needed for line break
        == "<p class=\"paragraph-small\">"+testingString+"\\</p>\n")
      // TODO: The parser trims whitespace.
      assert(Conference.convertMarkdownToHTML(testingString+"  \n")
        == "<p class=\"paragraph-small\">"+testingString+"</p>\n")
      // font
      assert(Conference.convertMarkdownToHTML("_"+testingString+"_") == "<p class=\"paragraph-small\"><em>"+testingString+"</em></p>\n")
      assert(Conference.convertMarkdownToHTML("*"+testingString+"*") == "<p class=\"paragraph-small\"><em>"+testingString+"</em></p>\n")
      assert(Conference.convertMarkdownToHTML("__"+testingString+"__") == "<p class=\"paragraph-small\"><strong>"+testingString+"</strong></p>\n")
      assert(Conference.convertMarkdownToHTML("**"+testingString+"**") == "<p class=\"paragraph-small\"><strong>"+testingString+"</strong></p>\n")
      assert(Conference.convertMarkdownToHTML("___"+testingString+"___") == "<p class=\"paragraph-small\"><em><strong>"+testingString+"</strong></em></p>\n"
      || Conference.convertMarkdownToHTML("___"+testingString+"___") == "<p class=\"paragraph-small\"><strong><em>"+testingString+"</em></strong></p>\n")
      assert(Conference.convertMarkdownToHTML("***"+testingString+"***") == "<p class=\"paragraph-small\"><em><strong>"+testingString+"</strong></em></p>\n"
        || Conference.convertMarkdownToHTML("***"+testingString+"***") == "<p class=\"paragraph-small\"><strong><em>"+testingString+"</em></strong></p>\n")
      // heading
      assert(Conference.convertMarkdownToHTML("# "+testingString) == "<h1>"+testingString+"</h1>\n")
      assert(Conference.convertMarkdownToHTML("## "+testingString) == "<h2>"+testingString+"</h2>\n")
      assert(Conference.convertMarkdownToHTML("### "+testingString) == "<h3>"+testingString+"</h3>\n")
      assert(Conference.convertMarkdownToHTML("#### "+testingString) == "<h4>"+testingString+"</h4>\n")
      assert(Conference.convertMarkdownToHTML("##### "+testingString) == "<h5>"+testingString+"</h5>\n")
      assert(Conference.convertMarkdownToHTML("###### "+testingString) == "<h6>"+testingString+"</h6>\n")
      assert(Conference.convertMarkdownToHTML("####### "+testingString) == "<p class=\"paragraph-small\">####### "+testingString+"</p>\n") // only six headers allowed
      assert(Conference.convertMarkdownToHTML("#"+testingString) == "<p class=\"paragraph-small\">#"+testingString+"</p>\n") // whitespace needed
      assert(Conference.convertMarkdownToHTML(testingString + "\n=") == "<h1>"+testingString+"</h1>\n")
      assert(Conference.convertMarkdownToHTML(testingString + "\n-") == "<h2>"+testingString+"</h2>\n")
      // blockquote
      assert(Conference.convertMarkdownToHTML("> "+testingString) == "<blockquote>\n<p class=\"paragraph-small\">"+testingString+"</p>\n</blockquote>\n")
      assert(Conference.convertMarkdownToHTML(">"+testingString) == "<blockquote>\n<p class=\"paragraph-small\">"+testingString+"</p>\n</blockquote>\n")
      // lists
      assert(Conference.convertMarkdownToHTML("- "+testingString) == "<ul>\n<li>"+testingString+"</li>\n</ul>\n")
      assert(Conference.convertMarkdownToHTML("+ "+testingString) == "<ul>\n<li>"+testingString+"</li>\n</ul>\n")
      assert(Conference.convertMarkdownToHTML("* "+testingString) == "<ul>\n<li>"+testingString+"</li>\n</ul>\n")
      assert(Conference.convertMarkdownToHTML("-"+testingString) == "<p class=\"paragraph-small\">-"+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("+"+testingString) == "<p class=\"paragraph-small\">+"+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("*"+testingString) == "<p class=\"paragraph-small\">*"+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("- "+testingString+"\n- "+testingString)
        == "<ul>\n<li>"+testingString+"</li>\n<li>"+testingString+"</li>\n</ul>\n")
      assert(Conference.convertMarkdownToHTML("1. "+testingString) == "<ol>\n<li>"+testingString+"</li>\n</ol>\n")
      assert(Conference.convertMarkdownToHTML("1."+testingString) == "<p class=\"paragraph-small\">1."+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("2)"+testingString) == "<p class=\"paragraph-small\">2)"+testingString+"</p>\n") // space needed
      assert(Conference.convertMarkdownToHTML("1. "+testingString+"\n2. "+testingString)
        == "<ol>\n<li>"+testingString+"</li>\n<li>"+testingString+"</li>\n</ol>\n")
      // links
      val urlTestingString : String = "http:"+testingString
      assert(Conference.convertMarkdownToHTML("<"+urlTestingString+">")
        == "<p class=\"paragraph-small\"><a href=\""+urlTestingString+"\">"+urlTestingString+"</a></p>\n")
      assert(Conference.convertMarkdownToHTML("["+testingString+"]"+"("+urlTestingString+")")
        == "<p class=\"paragraph-small\"><a href=\""+urlTestingString+"\">"+testingString+"</a></p>\n")
      // code
      assert(Conference.convertMarkdownToHTML("`"+testingString+"`") == "<p class=\"paragraph-small\"><code>"+testingString+"</code></p>\n")
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
    // test sanitising
    testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
      None, None, Some(false), Some(true), Some(false), Some(true), None, None, None,
      info = Some("<h1 class=\"paragraph-small\">Some text</h1>"))
    assert(testingConf.getInfoAsHTML() == "<h1>Some text</h1>\n") // attributes are not allowed
    testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
      None, None, Some(false), Some(true), Some(false), Some(true), None, None, None,
      info = Some("Some text"))
    assert(testingConf.getInfoAsHTML() == "<p class=\"paragraph-small\">Some text</p>\n") // except classes on paragraphs
    testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
      None, None, Some(false), Some(true), Some(false), Some(true), None, None, None,
      info = Some("<script type=\"text/javascript\">alert(\"Cross-Site-Scripting\");</script>"))
    assert(testingConf.getInfoAsHTML() == "\n") // everything else should also be disallowed
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
    // test sanitising
    testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
      None, Some("<h1 class=\"paragraph-small\">Some text</h1>"), Some(false), Some(true), Some(false), Some(true), None, None, None)
    assert(testingConf.getDescriptionAsHTML() == "<h1>Some text</h1>\n") // attributes are not allowed
    testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
      None, Some("Some text"), Some(false), Some(true), Some(false), Some(true), None, None, None)
    assert(testingConf.getDescriptionAsHTML() == "<p class=\"paragraph-small\">Some text</p>\n") // except classes on paragraphs
    testingConf = Conference(Some("uuid"), Some("someconf"), Some("XX"), Some("G"), Some("X"),
      None, Some("<script type=\"text/javascript\">alert(\"Cross-Site-Scripting\");</script>"), Some(false), Some(true), Some(false), Some(true), None, None, None)
    assert(testingConf.getDescriptionAsHTML() == "\n") // everything else should also be disallowed
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
