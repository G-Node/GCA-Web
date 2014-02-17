// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import java.util.{List => JList, LinkedList => JLinkedList}
import javax.persistence._

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
  @OneToOne(mappedBy = "abstr", optional = true)
  var figure: Figure = _

  @ManyToMany
  @JoinTable(name = "abstract_owners")
  var owners:  JList[Account] = new JLinkedList[Account]()
  @OneToMany(mappedBy = "abstr")
  var authors: JList[Author] = new JLinkedList[Author]()
  @OneToMany(mappedBy = "abstr")
  var affiliations: JList[Affiliation] = new JLinkedList[Affiliation]()
  @OneToMany(mappedBy = "abstr")
  var references: JList[Reference] = new JLinkedList[Reference]()

}


object Abstract {

  def apply() : Abstract = new Abstract()


  def apply(uuid: String,
            title: String,
            topic: String,
            text: String,
            doi: String,
            conflictOfInterest: String,
            acknowledgements: String,
            approved: Boolean,
            published: Boolean,
            conference: Conference,
            figure: Figure = null,
            owners:  JList[Account] = null,
            authors: JList[Author] = null,
            affiliations: JList[Affiliation] = null,
            references: JList[Reference] = null) : Abstract = {

    val abstr = new Abstract()

    abstr.uuid = uuid
    abstr.title = title
    abstr.topic = topic
    abstr.text = text
    abstr.doi = doi
    abstr.conflictOfInterest = conflictOfInterest
    abstr.acknowledgements = acknowledgements
    abstr.approved = approved
    abstr.published = published
    abstr.conference = conference
    abstr.figure = figure

    if (owners != null)
      abstr.owners = owners

    if (authors != null)
      abstr.authors = authors

    if (affiliations != null)
      abstr.affiliations = affiliations

    if (affiliations != null)
      abstr.references = references

    abstr
  }

}
