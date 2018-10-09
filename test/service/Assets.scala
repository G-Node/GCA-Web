// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service

import java.io.File

import com.mohiva.play.silhouette.contrib.utils.BCryptPasswordHasher
import org.joda.time.DateTime
import play.Play
import models.Model._
import models._
import plugins.DBUtil._

import scala.collection.JavaConversions._
import scala.{Option => ?}
import org.joda.time.{DateTimeZone, DateTime}


class Assets() {

  val pwHasher = new BCryptPasswordHasher()

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
      state = ?(AbstractState.Accepted),
      authors = Seq(
        Author(None, ?("one@foo.bar"), ?("One"), ?("Middle"), ?("Name")),
        Author(None, ?("two@foo.bar"), ?("Two"), ?("Middle"), ?("Name")),
        Author(None, ?("three@foo.bar"), ?("The"), None, ?("Name"))
      ).addPosition(),
      affiliations = Seq(
        Affiliation(None, ?("New York"), ?("USA"), ?("Department One"), ?("Institute Foo")),
        Affiliation(None, ?("Munich"), ?("Germany"), ?("New Department"), ?("Institute Bar"))
      ).addPosition(),
      references = Seq(
        Reference(None, ?("Authorone et al. Title One"), None, None),
        Reference(None, ?("Authortwo et al. Title Two"), None, None),
        Reference(None, ?("Authortwo et al. Title Three"), None, None)
      ).addPosition()
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
        Affiliation(None, ?("Seatlle"), ?("USA"), ?("Department II"), ?("Institute of Bla")),
        Affiliation(None, ?("Berlin"), ?("Germany"), ?("Other Department"), ?("Institute III"))
      ).addPosition(),
      references = Seq(
        Reference(None, ?("Authorfour et al. Title Four"), None, None),
        Reference(None, ?("Authorfive et al. Title Five"), None, None),
        Reference(None, ?("Authorfive et al. Title Six"), None, None)
      ).addPosition()
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
      ?(AbstractState.InPreparation),
      authors = Seq(
        Author(None, ?("four@foo.bar"), ?("Four"), ?("Middle"), ?("Name")),
        Author(None, ?("five@foo.bar"), ?("Five"), ?("Middle"), ?("Name")),
        Author(None, ?("six@foo.bar"), ?("The"), None, ?("Name"))
      ).addPosition(),
      affiliations = Seq(
        Affiliation(None, ?("Los Angeles"), ?("USA"), ?("Department IV"), ?("Another Institute")),
        Affiliation(None, ?("Frankfurt"), ?("Germany"), ?("My Department"), ?("Institute of Nothing"))
      ).addPosition(),
      references = Seq(
        Reference(None, ?("Authorfour et al. Title Six"), None, None),
        Reference(None, ?("Authorfive et al. Title Seven"), None, None),
        Reference(None, ?("Authorfive et al. Title Nine"), None, None)
      ).addPosition()
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
        Affiliation(None, ?("Frankfurt"), ?("Germany"), ?("My Department"), ?("Institute of Nothing"))
      ).addPosition(),
      references = Seq(
        Reference(None, ?("Authorfour et al. Title Six"), None, None),
        Reference(None, ?("Authorfive et al. Title Seven"), None, None),
        Reference(None, ?("Authorfive et al. Title Nine"), None, None)
      ).addPosition()
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
        Affiliation(None, ?("New York"), ?("USA"), ?("New Department"), ?("Institute Foo"))
      ).addPosition(),
      references = Seq(
        Reference(None, ?("E. Albert et al. New is always better"), None, None)
      ).addPosition()
    )
  }

  var figures : Array[Figure] = Array(
    Figure(None, ?("This is the super nice figure one.")),
    Figure(None, ?("This is the super nice figure two.")),
    Figure(None, ?("This is the super nice figure three."))
  )

  var alice : Account = createAccount("Alice", "Goodchild", "alice@foo.com")

  var alice_new : Account = createAccount("Alice", "Goodchild", "alice_new@foo.com")


  var bob: Account = createAccount("Bob", "Trusty", "bob@bar.com")

  var eve: Account = createAccount("Eve", "Sarevok", "eve@evil.com")

  var admin: Account = createAccount("Thomas", "Anderson", "neo@matrix.com")

  var dave: Account = createAccount("Dave", "Bowman", "dave@hal9k.com")

  def createAccount(firstName: String, lastName: String, mail: String) = {

    val account = new Account()

    account.firstName = firstName
    account.lastName = lastName
    account.mail = mail

    account.logins = toJSet(Seq(CredentialsLogin(pwHasher.hash("testtest"), isActive = true, account)))
    account
  }

  def accounts : Array[Account] = {
    Array(alice, bob, eve, admin, dave)
  }

  def createTopics : Seq[Topic] = Seq(
    Topic("topic one", None),
    Topic("topic two", None),
    Topic("topic three", None)
  ).addPosition()

  var bcDesc = "The Bernstein Conference is the Bernstein Network's central forum that has developed over time into the biggest European Computational Neuroscience conference"

  var conferences : Array[Conference] = Array(
    Conference(None, ?("Bernstein Conference 2014"), ?("BC14"), ?("BCCN"),
      ?("The C1 Conf, Somewhere, Sometime"), ?("http://www.nncn.de/en/bernstein-conference/2014"),
      ?(bcDesc), ?(true), ?(true), ?(true), ?(true),
      ?(new DateTime(393415200000L)), ?(new DateTime(574423200000L)), ?(new DateTime(1321005600000L)),
      ?("https://portal.g-node.org/abstracts/bc18/BC18_header.jpg"),
      ?("https://portal.g-node.org/abstracts/bc14/BC14_icon.png"),
      ?("557712638"),
      Seq(AbstractGroup(None, ?(1), ?("Talk"), ?("T")),
        AbstractGroup(None, ?(2), ?("Poster"), ?("P"))),Nil,Nil,Nil,
      ?("""[{"ExtendedData": "","name": "Central Lecture Hall (ZHG)","description": "Main Conference and Workshops","point": {"lat": 51.542262,"long": 9.935886},"type": 0,"zoomto": true, "floorplans" : ["https://www.uni-muenchen.de/studium/beratung/beratung_service/beratung_lmu/beratungsstelle-barrierefrei/bilderbaukasten/Barrierefreiheit/geschwister-scholl-platz-1-eg.jpg"]},{"ExtendedData": "","name": "Alte Mensa","description": "Public Lecture and Conference Dinner","point": {"lat": 51.533442,"long": 9.937631},"type": 0,"zoomto": true},{"ExtendedData": "","name": "Alte Mensa","description": "Conference Dinner","point": {"lat": 51.533442,"long": 9.937631},"type": 5,"zoomto": true},{"ExtendedData": "","name": "Göttingen Hbf","description": "main station","point": {"lat": 51.536548,"long": 9.926891},"type": 4,"zoomto": true}]"""),
      ?("""{"schedule": "some schedule json"}"""), ?("# Some markdown text"),Some(5000)),
    Conference(None, ?("The second conference"), ?("C2"), ?("BCCN"),
      ?("The C2 Conf, Somewhere, Sometime"), ?(""), None, ?(false), ?(true), ?(false), ?(true),
      ?(new DateTime(126283320000L)), ?(new DateTime(149870520000L)), ?(new DateTime(1321005600000L)),
      ?("https://portal.g-node.org/abstracts/bc18/BC18_header.jpg"),
      ?("https://portal.g-node.org/abstracts/bc14/BC14_icon.png")),
    Conference(None, ?("The third conference"), ?("C3"), ?(""),
      ?("The C3 Conf, Somewhere, Sometime"), ?(""), ?(""), ?(false), ?(true), ?(false), ?(false),
      ?(new DateTime(126283320000L)), ?(new DateTime(149870520000L)), ?(new DateTime(1321005600000L)),
      ?("https://portal.g-node.org/abstracts/bc18/BC18_header.jpg"),
      ?("https://portal.g-node.org/abstracts/bc14/BC14_icon.png"))
  )

  def fillDB() : Unit = {
    transaction { (em, tx) =>
      // merge accounts
      alice = em.merge(alice)
      bob = em.merge(bob)
      eve = em.merge(eve)
      admin = em.merge(admin)
      dave = em.merge(dave)


      // add alice to conference owners and merge conferences
      conferences = conferences.map { conf =>
        conf.owners.add(alice)
        conf.owners.add(dave)

        conf.groups.foreach{
          group => group.conference = conf
        }

        conf.topics = toJSet(createTopics)
        conf.topics.foreach { topic => topic.conference = conf }

        conf.ctime = new DateTime(DateTimeZone.UTC)

        em.merge(conf)
      }

      // add alice and bob to abstract owners and merge abstracts
      // add all abstracts to conference one
      abstracts = abstracts.map { abstr =>
        abstr.conference = conferences(0)

        abstr.ctime = new DateTime(DateTimeZone.UTC)

        abstr.owners.add(alice)
        abstr.owners.add(bob)

        abstr.favUsers.add(bob)

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
        fig.position = i
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

    transaction { (em, tx) =>
      em.createQuery("DELETE FROM StateLogEntry").executeUpdate()      
      em.createQuery("DELETE FROM Affiliation").executeUpdate()
      em.createQuery("DELETE FROM Author").executeUpdate()
      em.createQuery("DELETE FROM Reference").executeUpdate()
      em.createQuery("DELETE FROM Figure").executeUpdate()
      em.createQuery("DELETE FROM Abstract").executeUpdate()
      em.createQuery("DELETE FROM Topic").executeUpdate()
      em.createQuery("DELETE FROM AbstractGroup").executeUpdate()
      em.createQuery("DELETE FROM Conference").executeUpdate()
      em.createQuery("DELETE FROM CredentialsLogin").executeUpdate()
      em.createQuery("DELETE FROM Account").executeUpdate()      
    }
  }

}
