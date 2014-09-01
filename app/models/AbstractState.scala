package models

import javax.persistence.{Converter, AttributeConverter}

object AbstractState extends Enumeration {
  type State = Value
  val InPreparation, Submitted, InReview, Accepted, Rejected, Withdrawn, InRevision = Value


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

  implicit class StateMethods(from: Value) {
    def canTransitionTo(to: Value, isAdmin: Boolean, isOpen: Boolean): Boolean = {

      val transitionMap = (isAdmin, isOpen) match {
        case (false, true)  => ownerStates.get("isOpen")
        case (false, false) => ownerStates.get("isClosed")
        case (true,  _)     => Some(adminStates)
      }

      val canTransition = for {
        theMap <- transitionMap
        possibleStates <- theMap.get(from)
      } yield possibleStates.contains(to)

      canTransition.getOrElse(false)
    }
  }
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
