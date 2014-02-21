package test.service

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import service._
import models._

/**
 * Test for AbstractService
 */
@RunWith(classOf[JUnitRunner])
class AbstractServiceSpec extends Specification {

  var srv = new AbstractService()
  var conf = Conference(Some(Model.makeUUID()), Some("foo"))
  var abstr = Abstract(Some(Model.makeUUID()), Some("title"), Some("topic"), Some("text"), Some("doi"),
              Some("coi"), Some("ack"), approved=true, published=true, conference=Some(conf))
  conf.abstracts.add(abstr)
  var account = Account(Some(Model.makeUUID()), Some("foo@foo"))

  "service.AbstractService" should {

    "throw NotImplementedError for unimplemented methods" in {

      srv.list(conf) must throwA[NotImplementedError]
      srv.listOwn(account) must throwA[NotImplementedError]

      srv.get(abstr.uuid) must throwA[NotImplementedError]
      srv.getOwn(abstr.uuid, account) must throwA[NotImplementedError]

      srv.create(abstr, conf, account) must throwA[NotImplementedError]
      srv.update(abstr, account) must throwA[NotImplementedError]

      srv.delete(abstr.uuid, account) must throwA[NotImplementedError]
    }

  }

}
