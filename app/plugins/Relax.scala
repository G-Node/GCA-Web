package plugins

import play.api.{Logger, Application, Plugin}

class Relax(app: Application) extends Plugin {

  override def enabled = true

  override def onStart() {
    val configuration = app.configuration
    Logger.info("Relax has started")
  }

  override def onStop() {
    Logger.info("Relax has stopped")
  }

}
