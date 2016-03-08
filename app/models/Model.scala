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
import javax.persistence.{PrePersist, Id, MappedSuperclass}
import scala.reflect.runtime.universe._

/**
 * Trait that defines stuff that is common for all models.
 * This trait may be extended with some that properties like timestamps
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

  override def compare(that: Model): Int = {
    if (uuid != null && that.uuid != null)
      uuid.compareTo(that.uuid)
    else
      hashCode().compareTo(that.hashCode())
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Model]

  override def equals(other: Any): Boolean = other match {
    case that: Model =>
      (that canEqual this) &&
        uuid == that.uuid
    case _ => false
  }

  override def hashCode(): Int = {
    if (uuid != null) uuid.hashCode else super.hashCode()
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
   * @return A list
   */
  def toJSet[T](seq : Seq[T]) : JSet[T] = {
    seq match {
      case Nil => new JTreeSet[T]()
      case _ => new JTreeSet[T](asJavaCollection(seq))
    }
  }

  def membersAnnotations[T: TypeTag]: Map[String, Map[String, Map[String, Any]]] = {
    typeOf[T].members.withFilter {
      _.annotations.length > 0
    }.map { m =>
      m.name.toString.trim -> m.annotations.map { a =>
        a.tree.tpe.typeSymbol.name.toString.trim -> a.tree.children.withFilter {
          _.productPrefix eq "AssignOrNamedArg"
        }.map { tree =>
          tree.productElement(0).toString.trim -> tree.productElement(1)
        }.toMap
      }.toMap
    }.toMap
  }

  def getLimit[T :TypeTag](name: String): Int = {
    membersAnnotations[T].get(name).flatMap(_.get("Column")).flatMap(_.get("length")).map {
      x => x.asInstanceOf[Literal].value.value.asInstanceOf[Int]
    }.getOrElse(255)
  }

}

/**
 * A base class that defines a pos field for sorting
 */
@MappedSuperclass
class PositionedModel extends Model {

  var position: Int = _

  override def compare(that: Model): Int = {
    that match {
      case sm: PositionedModel => this.position - sm.position
      case _ => super.compare(that)
    }
  }
}
