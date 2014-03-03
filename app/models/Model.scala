// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import collection.JavaConversions.asJavaCollection
import java.util.{Set => JSet, TreeSet => JTreeSet, UUID}
import javax.persistence.{Transient, PrePersist, Id, MappedSuperclass}

/**
 * Trait that defines stuff that is common for all models.
 * This trait may be extended with some other properties like timestamps
 * etc. when needed.
 */
@MappedSuperclass
class Model extends Ordered[Model] {

  /**
   * The primary key of the model.
   */
  @Id
  var uuid: String = _


  @PrePersist
  protected def beforePersist() : Unit = {
    if (uuid == null)
      uuid = Model.makeUUID()
  }

  @Transient
  override def compare(that: Model): Int = {
    if (uuid != null && that.uuid != null)
      uuid.compare(that.uuid)
    else
      hashCode.compare(that.hashCode)
  }
}

object Model {

  def makeUUID() : String = {
    UUID.randomUUID().toString
  }

  /**
   * Unwrap an optional value.
   *
   * @param value The optional value.
   *
   * @return The value wrapped by Some or 0
   */
  def unwrapVal[T: Numeric](value: Option[T]) = {
    value match {
      case Some(x) => x
      case _ => implicitly[Numeric[T]].zero
    }
  }

  /**
   * Unwrap an optional reference.
   *
   * @param value An optional value
   *
   * @return The value wrapped by Some or null
   */
  def unwrapRef[T >: Null](value: Option[T]) : T = {
    value match {
      case Some(x) => x
      case _ => null
    }
  }

  /**
   *
   * @param seq The sequence to convert.
   *
   * @return A list
   */
  def toJSet[T](seq : Seq[T]) : JSet[T] = {
    seq match {
      case Nil => new JTreeSet[T]()
      case _ => new JTreeSet[T](asJavaCollection(seq))
    }
  }


}
