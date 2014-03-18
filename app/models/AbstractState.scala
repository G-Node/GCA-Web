package models

import javax.persistence.{Converter, AttributeConverter}

object AbstractState extends Enumeration {
  type State = Value
  val InPreparation, Submitted, InReview, Accepted, Rejected, Published, Withdrawn, Deleted = Value
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
