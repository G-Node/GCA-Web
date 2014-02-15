package test.service

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import java.util.{LinkedList => JLinkedList}

import service._
import models._

/**
 * Test for AbstractService
 */
@RunWith(classOf[JUnitRunner])
class AbstractServiceSpec extends Specification {

  var srv = new AbstractService()
  var conf = Conference("foo", new JLinkedList[Abstract]())
  var abstr = Abstract("title", "topic", "text", "doi", "coi", "ack", approved=true, published=true, conference=conf)
  conf.abstracts.add(abstr)
  var account = Account("foo@foo", new JLinkedList[Abstract](conf.abstracts))

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
