package models

import javax.persistence.{Convert, ManyToOne, Entity}
import models.util.DateTimeConverter
import org.joda.time.{DateTimeZone, DateTime}


/**
 * Model for state log changes
 */
@Entity
class StateLogEntry extends Model {

  @Convert(converter = classOf[AbstractStateConverter])
  var state: AbstractState.State = AbstractState.InPreparation

  @Convert(converter = classOf[DateTimeConverter])
  var timestamp: DateTime = _

  var note: String = _
  var editor: String = _

  @ManyToOne
  var abstr: Abstract = _
}

object StateLogEntry {

  def apply(abstr: Abstract,
            state: AbstractState.State,
            editor: Account,
            note: Option[String] = None,
            timestamp: Option[DateTime] = None) = {

    val log = new StateLogEntry()

    log.abstr     = abstr
    log.state     = state
    log.timestamp = timestamp.getOrElse(new DateTime(DateTimeZone.UTC))
    log.note      = Model.unwrapRef(note)
    log.editor    = if (editor.fullName != null) { editor.fullName } else { editor.firstName + " " + editor.lastName }

    log
  }
}