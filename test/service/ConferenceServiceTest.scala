// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package test.service

import org.scalatest.junit.JUnitSuite
import org.junit._
import play.api.test.FakeApplication
import service.ConferenceService
import play.api.Play
import javax.persistence.{EntityManagerFactory, Persistence}
import models.{Conference, Account}

/**
 * Test
 */
class ConferenceServiceTest extends JUnitSuite {

  var service : ConferenceService = null
  var account : Account = null
  var emf : EntityManagerFactory = null

  @Before
  def before() : Unit = {
    println("BEFORE")
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")

    val em = emf.createEntityManager()
    em.getTransaction.begin()

    val conf = em.merge(Conference(null, "conf1"))
    em.merge(Conference(null, "conf2"))

    account = em.merge(Account(null, "mail"))

    conf.owners.add(account)
    em.merge(conf)

    em.getTransaction.commit()
    em.close()
    service = new ConferenceService(emf)
  }

  @After
  def after() : Unit = {
    val em = emf.createEntityManager()
    em.getTransaction.begin()

    em.createQuery("DELETE FROM Conference").executeUpdate()
    em.createQuery("DELETE FROM Account").executeUpdate()

    em.getTransaction.commit()
    em.close()
  }

  @Test
  def testList() : Unit = {
    println("testList")
    val list = service.list()
  }

  @Test
  def testListOwn() : Unit = {
    println("testListOwn")
    val list = service.listOwn(Account(uuid = "uuid", mail = "mail"))
  }

  @Test
  def testGet() : Unit = {
    intercept[NotImplementedError] {
      service.get("uuid")
    }
  }

  @Test
  def testCreate() : Unit = {
    intercept[NotImplementedError] {
      service.create(Conference(), account)
    }
  }

  @Test
  def testUpdate() : Unit = {
    intercept[NotImplementedError] {
      service.update(Conference(), account)
    }
  }

  @Test
  def testDelete() : Unit = {
    intercept[NotImplementedError] {
      service.delete("uuid", account)
    }
  }

}

object ConferenceServiceTest {

  var app: FakeApplication = null

  @BeforeClass
  def beforeClass() = {
    println("BEFORE CLASS")
    app = new FakeApplication()
    Play.start(app)
  }

  @AfterClass
  def afterClass() = {
    println("AFTER CLASS")
    Play.stop()
  }

}
