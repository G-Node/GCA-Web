package service

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import models._


/**
 * Test for FigureService
 */
@RunWith(classOf[JUnitRunner])
class FigureServiceSpec extends Specification {

  var srv = new FigureService("/tmp/figures")
  var account = Account(Some(Model.makeUUID()), Some("foo@foo"))
  var abstr = Abstract(Some(Model.makeUUID()), Some("title"), Some("topic"), Some("text"), Some("doi"),
                       Some("coi"), Some("ack"), approved=true, published=true)
  abstr.owners.add(account)

  var fig = Figure(Some(Model.makeUUID()), Some("foo_fig"), Some("caption"), Some(abstr))

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
