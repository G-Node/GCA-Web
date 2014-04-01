package models

import javax.persistence.{Converter, AttributeConverter}

object AbstractState extends Enumeration {
  type State = Value
  val InPreparation, Submitted, InReview, Accepted, Rejected, Published, Withdrawn, InRevision = Value


  /* **************************************************************************************************************** *
   * State transition checking                                                                                        *
   *                                                                                                                  *
   *                        ,----- Conference.isOpen                                                                  *
   *                       \/                                                                                         */
  val ownerStates = Map("isOpen"  -> Map(InPreparation -> (Submitted :: Nil),
                                         Submitted     -> (Withdrawn :: Nil),
                                         Withdrawn     -> (InPreparation :: Nil),
                                         InRevision    -> (Submitted :: Nil)),
                       "isClosed" -> Map(InRevision    -> (Submitted :: Nil)))

  val adminStates = Map(Submitted  -> (InReview :: Nil),
                        InReview   -> (Accepted :: Rejected :: InRevision :: Withdrawn :: Nil),
                        Accepted   -> (InRevision :: Withdrawn :: Nil),
                        Rejected   -> (InRevision :: Withdrawn :: Nil),
                        InRevision -> (InReview :: Nil))
}

@Converter
class AbstractStateConverter extends AttributeConverter[AbstractState.State, String] {

  override def convertToDatabaseColumn(state: AbstractState.State): String = {
     state.toString
  }

  override def convertToEntityAttribute(state: String): AbstractState.State = {
    AbstractState.withName(state)
  }
}
