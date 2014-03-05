// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import models.Model._
import java.util.{Set => JSet, TreeSet => JTreeSet}
import javax.persistence._
import org.eclipse.persistence.annotations.CascadeOnDelete

/**
 * A model class for abstracts
 */
@Entity
class Abstract extends Model {

  var title: String = _
  var topic: String = _
  var text:  String = _
  var doi:   String = _
  var conflictOfInterest: String = _
  var acknowledgements: String = _

  var approved: Boolean = false
  var published: Boolean = false

  @ManyToOne
  var conference : Conference = _
  @OneToOne(orphanRemoval = true)
  @CascadeOnDelete
  var figure: Figure = _

  @ManyToMany
  @JoinTable(name = "abstract_owners")
  var owners:  JSet[Account] = new JTreeSet[Account]()
  @OneToMany(mappedBy = "abstr")
  var authors: JSet[Author] = new JTreeSet[Author]()
  @OneToMany(mappedBy = "abstr")
  var affiliations: JSet[Affiliation] = new JTreeSet[Affiliation]()
  @OneToMany(mappedBy = "abstr")
  var references: JSet[Reference] = new JTreeSet[Reference]()

}


object Abstract {

  def apply(uuid: Option[String],
            title: Option[String],
            topic: Option[String],
            text: Option[String],
            doi: Option[String],
            conflictOfInterest: Option[String],
            acknowledgements: Option[String],
            approved: Boolean,
            published: Boolean,
            conference: Option[Conference] = None,
            figure: Option[Figure] = None,
            owners:  Seq[Account] = Nil,
            authors: Seq[Author] = Nil,
            affiliations: Seq[Affiliation] = Nil,
            references: Seq[Reference] = Nil) : Abstract = {

    val abstr = new Abstract()

    abstr.uuid        = unwrapRef(uuid)
    abstr.title       = unwrapRef(title)
    abstr.topic       = unwrapRef(topic)
    abstr.text        = unwrapRef(text)
    abstr.doi         = unwrapRef(doi)
    abstr.conflictOfInterest = unwrapRef(conflictOfInterest)
    abstr.acknowledgements   = unwrapRef(acknowledgements)
    abstr.approved    = approved
    abstr.published   = published

    abstr.conference  = unwrapRef(conference)
    abstr.figure      = unwrapRef(figure)
    abstr.owners      = toJSet(owners)
    abstr.authors     = toJSet(authors)
    abstr.affiliations = toJSet(affiliations)
    abstr.references  = toJSet(references)

    abstr
  }

}
