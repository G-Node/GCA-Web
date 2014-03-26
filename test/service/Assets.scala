// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service

import scala.{Option => ?}
import collection.JavaConversions._
import models._
import models.Model._
import service.util.DBUtil
import javax.persistence.EntityManagerFactory
import play.Play
import java.io.File
import org.joda.time.DateTime

class Assets(val emf: EntityManagerFactory) extends DBUtil {

  implicit class PositionedLSeq[A <: PositionedModel](l: Seq[A]) {
    def addPosition():Seq[A] = {
      for { (element, i) <- l.zipWithIndex } yield {
        element.position = i; element
      }
    }
  }

  val figPath = Play.application().configuration().getString("file.fig_path", "./figures")

  def makeSortId(group: Int, seqid: Int) : Option[Int] = {
    val sortId : Int = group << 16 | seqid
    Some(sortId)
  }

  var abstracts : Array[Abstract] = Array(
    Abstract(
      None,
      ?("Title of abstract one"),
      ?("topic one"),
      ?("text one"),
      ?("doi one"),
      ?("coi one"),
      ?("acc one"),
      ?(true),
      ?("very good reason"),
      makeSortId(1, 1),
      state = ?(AbstractState.Published),
      authors = Seq(
        Author(None, ?("one@foo.bar"), ?("One"), ?("Middle"), ?("Name")),
        Author(None, ?("two@foo.bar"), ?("Two"), ?("Middle"), ?("Name")),
        Author(None, ?("three@foo.bar"), ?("The"), None, ?("Name"))
      ).addPosition(),
      affiliations = Seq(
        Affiliation(None, ?("One address"), ?("Andorra"), ?("One department"), None,None, ?(0)),
        Affiliation(None, ?("Two address"), ?("Andorra"), ?("Two department"), None, None, ?(1))
      ),
      references = Seq(
        Reference(None, ?("Authorone et al."), ?("Title One"), ?(2000), None),
        Reference(None, ?("Authortwo et al."), ?("Title Two"), ?(1999), None),
        Reference(None, ?("Authortwo et al."), ?("Title Three"), ?(2006), None)
      )
    ),
    Abstract(
      None,
      ?("Title of abstract two"),
      ?("topic two"),
      ?("text two"),
      ?("doi two"),
      ?("coi two"),
      ?("acc two"),
      ?(true),
      ?("very good reason"),
      makeSortId(1, 2),
      ?(AbstractState.InReview),
      authors = Seq(
        Author(None, ?("four@foo.bar"), ?("Four"), ?("Middle"), ?("Name")),
        Author(None, ?("five@foo.bar"), ?("Five"), ?("Middle"), ?("Name")),
        Author(None, ?("six@foo.bar"), ?("The"), None, ?("Name"))
      ).addPosition(),
      affiliations = Seq(
        Affiliation(None, ?("Four address"), ?("Andorra"), ?("Four department"), None, None, ?(0)),
        Affiliation(None, ?("Five address"), ?("Andorra"), ?("Five department"), None, None, ?(1))
      ),
      references = Seq(
        Reference(None, ?("Authorfour et al."), ?("Title Six"), ?(2000), None),
        Reference(None, ?("Authorfive et al."), ?("Title Seven"), ?(1998), None),
        Reference(None, ?("Authorfive et al."), ?("Title Nine"), ?(2009), None)
      )
    ),
    Abstract(
      None,
      ?("Title of abstract three"),
      ?("topic three"),
      ?("text three"),
      ?("doi three"),
      ?("coi three"),
      ?("acc three"),
      ?(false),
      None,
      makeSortId(2, 1),
      ?(AbstractState.Submitted),
      authors = Seq(
        Author(None, ?("four@foo.bar"), ?("Four"), ?("Middle"), ?("Name")),
        Author(None, ?("five@foo.bar"), ?("Five"), ?("Middle"), ?("Name")),
        Author(None, ?("six@foo.bar"), ?("The"), None, ?("Name"))
      ).addPosition(),
      affiliations = Seq(
        Affiliation(None, ?("Four address"), ?("Andorra"), ?("Four department"), None, None, ?(0)),
        Affiliation(None, ?("Five address"), ?("Andorra"), ?("Five department"), None, None, ?(1))
      ),
      references = Seq(
        Reference(None, ?("Authorfour et al."), ?("Title Six"), ?(2000), None),
        Reference(None, ?("Authorfive et al."), ?("Title Seven"), ?(1998), None),
        Reference(None, ?("Authorfive et al."), ?("Title Nine"), ?(2009), None)
      )
    ),
    Abstract(
      None,
      ?("Title of abstract four"),
      ?("topic four"),
      ?("text four"),
      ?("doi four"),
      ?("coi four"),
      ?("acc four"),
      ?(false),
      None,
      makeSortId(2, 2),
      ?(AbstractState.InPreparation),
      authors = Seq(
        Author(None, ?("four@foo.bar"), ?("Four"), ?("Middle"), ?("Name")),
        Author(None, ?("five@foo.bar"), ?("Five"), ?("Middle"), ?("Name")),
        Author(None, ?("six@foo.bar"), ?("The"), None, ?("Name"))
      ).addPosition(),
      affiliations = Seq(
        Affiliation(None, ?("Four address"), ?("Andorra"), ?("Four department"), None, None, ?(0)),
        Affiliation(None, ?("Five address"), ?("Andorra"), ?("Five department"), None, None, ?(1))
      ),
      references = Seq(
        Reference(None, ?("Authorfour et al."), ?("Title Six"), ?(2000), None),
        Reference(None, ?("Authorfive et al."), ?("Title Seven"), ?(1998), None),
        Reference(None, ?("Authorfive et al."), ?("Title Nine"), ?(2009), None)
      )
    )
  )

  def createAbstract() : Abstract = {
    Abstract(
      None,
      ?("Title of new abstract"),
      ?("Completely new topic"),
      ?("Cool new text"),
      ?("new doi"),
      ?("No conflict at all"),
      ?("Thanks for all the fish!"),
      ?(false),
      None,
      makeSortId(1, 42),
      ?(AbstractState.InReview),
      authors = Seq(
        Author(None, ?("new@mail.bar"), ?("New"), ?("Cool"), ?("Author")),
        Author(None, ?("foo@mail.bar"), ?("Second"), None, ?("Author"))
      ).addPosition(),
      affiliations = Seq(
        Affiliation(None, ?("New Street 5"), ?("New York"), ?("New Department"), None, None, ?(0))
      ),
      references = Seq(
        Reference(None, ?("E. Albert et al."), ?("New is always better"), ?(2000), None)
      )
    )
  }

  var figures : Array[Figure] = Array(
    Figure(None, ?("This is the super nice figure one.")),
    Figure(None, ?("This is the super nice figure two.")),
    Figure(None, ?("This is the super nice figure three."))
  )

  var alice : Account = createAccount("Alice", "Goodchild", "alice@foo.com")

  var bob: Account = createAccount("Bob", "Trusty", "bob@bar.com")

  var eve: Account = createAccount("Eve", "Sarevok", "eve@evil.com")

  def createAccount(firstName: String, lastName: String, mail: String) = {
    val account = new Account()

    account.firstName = firstName
    account.lastName = lastName
    account.mail = mail
    account.userid = mail
    account.authenticationMethod = "userPassword"
    account.provider = "userpass"
    account.pwInfo = PwInfo(
      "bcrypt",
      "$2a$10$iMoFsVr468/5JJkq0YLRruEMpleTNXMo/rdkm5aOqnuq83t5DwUvW",
      None
    )

    account
  }

  def accounts : Array[Account] = {
    Array(alice, bob, eve)
  }

  def createTopics : Array[Topic] = Array(
    Topic("topic one", None),
    Topic("topic two", None),
    Topic("topic three", None)
  )

  var bcDesc = "The Bernstein Conference is the Bernstein Network's central forum that has developed over time into the biggest European Computational Neuroscience conference"

  var conferences : Array[Conference] = Array(
    Conference(None, ?("Bernstein Conference 2014"), ?("BC14"),
      ?("The C1 Conf, Somewhere, Sometime"), ?("http://www.nncn.de/en/bernstein-conference/2014"),
      ?(bcDesc), ?(true), ?(true),
      ?(new DateTime(393415200000L)), ?(new DateTime(574423200000L)), ?(new DateTime(1321005600000L)),
      ?("http://www.nncn.de/header/header-conference.jpg"),
      ?("http://www.nncn.de/bilder/bc14_kurzlogo"),
      Seq(AbstractGroup(None, ?(1), ?("Talk"), ?("T")),
          AbstractGroup(None, ?(2), ?("Poster"), ?("P")))),
    Conference(None, ?("The second conference"), ?("C2"),
      ?("The C2 Conf, Somewhere, Sometime"), ?(""), None, ?(false), ?(true),
      ?(new DateTime(126283320000L)), ?(new DateTime(149870520000L))),
    Conference(None, ?("The third conference"), ?("C3"),
      ?("The C3 Conf, Somewhere, Sometime"), ?(""), ?(""), ?(false), ?(true),
      ?(new DateTime(126283320000L)), ?(new DateTime(149870520000L)))
  )

  def fillDB() : Unit = {
    dbTransaction { (em, tx) =>
      // merge accounts
      alice = em.merge(alice)
      bob = em.merge(bob)
      eve = em.merge(eve)

      // add alice to conference owners and merge conferences
      conferences = conferences.map { conf =>
        conf.owners.add(alice)

        conf.groups.foreach{
          group => group.conference = conf
        }

        conf.topics = toJSet(createTopics)
        conf.topics.foreach { topic => topic.conference = conf }

        em.merge(conf)
      }

      // add alice and bob to abstract owners and merge abstracts
      // add all abstracts to conference one
      abstracts = abstracts.map { abstr =>
        abstr.conference = conferences(0)

        abstr.owners.add(alice)
        abstr.owners.add(bob)

        abstr.authors.foreach { author =>
          author.abstr = abstr
        }

        abstr.affiliations.foreach { affiliation =>
          affiliation.abstr = abstr
        }

        abstr.references.foreach { reference =>
          reference.abstr = abstr
        }

        abstr.stateLog.add(StateLogEntry(abstr, AbstractState.InPreparation,
          alice, ?("Initial Creation"), ?(new DateTime(1394898814000L))))
        if(abstr.state != AbstractState.InPreparation) {
          abstr.stateLog.add(StateLogEntry(abstr, AbstractState.Submitted,
            bob, None, ?(new DateTime(1395146554000L))))
          if (abstr.state != AbstractState.Submitted) {
            abstr.stateLog.add(StateLogEntry(abstr, AbstractState.InReview, alice,
              None,  ?(new DateTime(1395753822000L))))
          }
        }

        em.merge(abstr)
      }

      figures = 0.until(figures.length).toArray.map { i: Int =>
        val fig = figures(i)
        val abstr = abstracts(i)
        fig.abstr = abstr
        abstr.figures.add(fig)

        em.persist(fig)

        val file = new File(figPath, fig.uuid)
        if (!file.getParentFile.exists())
          file.getParentFile.mkdirs()

        file.createNewFile()

        fig
      }
    }
  }

  def killDB() : Unit = {
    val dir = new File(figPath)
    if (dir.exists() && dir.isDirectory) {
      dir.listFiles().foreach {file =>
        file.delete()
      }
    }

    dbTransaction { (em, tx) =>
      em.createQuery("DELETE FROM StateLogEntry").executeUpdate()
      em.createQuery("DELETE FROM AbstractGroup").executeUpdate()
      em.createQuery("DELETE FROM Affiliation").executeUpdate()
      em.createQuery("DELETE FROM Author").executeUpdate()
      em.createQuery("DELETE FROM Reference").executeUpdate()
      em.createQuery("DELETE FROM Figure").executeUpdate()
      em.createQuery("DELETE FROM Abstract").executeUpdate()
      em.createQuery("DELETE FROM Topic").executeUpdate()
      em.createQuery("DELETE FROM Conference").executeUpdate()
      em.createQuery("DELETE FROM Account").executeUpdate()
    }
  }

}
