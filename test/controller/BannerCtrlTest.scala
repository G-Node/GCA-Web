package controller

import java.io.File

import com.sksamuel.scrimage._
import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.junit._
import play.api.Play
import play.api.http.Writeable
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContentAsMultipartFormData, Cookie, _}
import play.api.test.Helpers._
import play.api.test._
import utils.DefaultRoutesResolver._
import utils.serializer.BannerFormat

import scala.io.Source.fromFile

class BannerCtrlTest extends BaseCtrlTest {

  val formatter = new BannerFormat()
  var cookie : Cookie = _

  @Before
  override def before() : Unit = {
    super.before()
    cookie = getCookie(assets.alice, "testtest")
  }

  implicit def writeableOf_AnyContentAsMultipartFormData(implicit codec: Codec):
  Writeable[AnyContentAsMultipartFormData] = {
    val boundary = "----Af456BGe4h"
    val charset = codec.charset

    Writeable(dataParts => {

      val text: Iterable[Array[Byte]] = for (dp <- dataParts.mdf.dataParts) yield {
        val header = ("Content-Disposition: form-data; name=\"" + dp._1 + "\"\r\n\r\n").getBytes(charset)
        val data = dp._2.mkString.getBytes(charset)
        val footer = "\r\n".getBytes(charset)

        header ++ data ++ footer
      }

      val data: Iterable[Array[Byte]] = for (file <- dataParts.mdf.files) yield {
        val header = ("Content-Disposition: form-data; name=\"" + file.key +
          "\"; filename=\"" + file.filename + "\"\r\n" + "Content-Type: " +
          file.contentType.getOrElse("image/jpeg") + "\r\n\r\n").getBytes(charset)

        val f2 = new File(file.ref+"new")
        f2.deleteOnExit()
        FileUtils.copyFile(file.ref.file, f2)
        val data = Image.fromFile(f2).bytes
        val footer = "\r\n".getBytes(charset)

        header ++ data ++ footer
      }

      val separator = ("--" + boundary + "\r\n").getBytes(charset)
      val body = (text ++ data).foldLeft(Array[Byte]())((r,c) => r ++ separator ++ c)

      //val debug = new String(body.map(_.toChar))
      body ++ separator ++ "--".getBytes(charset)

    }, Some(s"multipart/form-data; boundary=$boundary"))
  }

  // The Future in Banner upload is not properly resolved during the tests
  // and leads to a timeout exception on docker build tests where the test
  // assumably requires 33s while the timout exception is raised after 20s.
  @Test(timeout=40000)
  def testUpload(): Unit = {
    val formats = List("jpg", "png")
    for (format <- formats) {
      val banner = formatter.writes(assets.banner(0)).as[JsObject] - "uuid" - "bType"
      val file = new File("tmp")
      val pDir = new java.io.File(".").getCanonicalPath
      val data = new File(pDir + "/test/utils/BC_header_" + format + "." + format)
      FileUtils.copyFile(data, file)

      val requestBody = MultipartFormData(
        Map("banner" -> Seq(banner.toString())),
        Seq(MultipartFormData.FilePart(
          "file", "foo.bar",
          Some("image/jpeg"),
          TemporaryFile(file))
        ),
        Seq(),
        Seq()
      )

      val uuid = assets.conferences(0).uuid
      val request = FakeRequest(
        POST, s"/api/conferences/$uuid/banner"
      ).withMultipartFormDataBody(requestBody).withCookies(cookie)

      val result = route(BannerCtrlTest.app, request).get
      assert(status(result) == CREATED)
    }
  }

  @Test
  def testDownload(): Unit = {
    val uuid = assets.banner(0).uuid
    val request = FakeRequest(GET, s"/api/banner/$uuid/image")
    val result = route(BannerCtrlTest.app, request).get

    assert(status(result) == OK)
  }

  @Test
  def testDownloadMobile(): Unit = {
    val uuid = assets.banner(0).uuid
    val request = FakeRequest(GET, s"/api/banner/$uuid/imagemobile")
    val result = route(BannerCtrlTest.app, request).get

    assert(status(result) == OK)
  }

  @Test
  def testDelete(): Unit = {
    val uuid = assets.banner(1).uuid
    val request = FakeRequest(DELETE, s"/api/banner/$uuid").withCookies(cookie)
    val deleted = route(BannerCtrlTest.app, request).get
    assert(status(deleted) == OK)

    val id = "NOTEXISTANT"
    val bad = FakeRequest(DELETE, s"/api/banner/$id").withCookies(cookie)
    val failed = routeWithErrors(BannerCtrlTest.app, bad).get
    assert(status(failed) == NOT_FOUND)
  }
}

object BannerCtrlTest {

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
