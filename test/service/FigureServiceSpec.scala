package test.service

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import java.util.{LinkedList => JLinkedList}

import service._
import models._


/**
 * Test for FigureService
 */
@RunWith(classOf[JUnitRunner])
class FigureServiceSpec extends Specification {

  var srv = new FigureService("/tmp/figures")
  var account = Account("foo@foo")
  var abstr = Abstract("title", "topic", "text", "doi", "coi", "ack",
                        approved=true, published=true)
  abstr.owners = new JLinkedList[Account]()
  abstr.owners.add(account)

  var fig = Figure(Model.makeUUID(), "foo_fig", "caption", abstr)

  "service.FigureService" should {

    "throw NotImplementedError for unimplemented methods" in {

      srv.get(fig.uuid) must throwA[NotImplementedError]

      srv.create(fig, null, abstr, account) must throwA[NotImplementedError]
      srv.update(fig, account)  must throwA[NotImplementedError]

      srv.delete(fig.uuid, account) must throwA[NotImplementedError]

      srv.openFile(fig) must throwA[NotImplementedError]
    }

  }

}
