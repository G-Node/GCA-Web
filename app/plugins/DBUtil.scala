package plugins

import javax.persistence.{EntityTransaction, EntityManager, Persistence}

import play.api._
import play.api.Logger._

/**
 * Plugin that provides an configured entity manager factory and
 * thread local entity managers.
 */
class DBUtil(implicit app: Application) extends Plugin {

  private lazy val emf = Persistence.createEntityManagerFactory(DBUtil.DEFAULT_UNIT)

  override def onStart(): Unit = {
    info("DBUtil: activating  plugin")
  }


  override def onStop(): Unit = {
    emf.close()
    info("DBUtil: stopping  plugin")
  }

  /**
   * Get a new entity manager.
   *
   * @return A newly created entity manager.
   */
  def createEM : EntityManager = {
    emf.createEntityManager()
  }

}

object DBUtil {

  val DEFAULT_UNIT = Play.current.configuration.getString("jpa.default").getOrElse("defaultPersistenceUnit")

  /**
   * Get the plugin instance from the play runtime
   *
   * @return The plugin instance
   */
  def instance : DBUtil = {
    Play.current.plugin[DBUtil]
        .getOrElse(throw new RuntimeException("JPAHelper: plugin not loaded"))
  }

  /**
   * Execute a function encapsulated in a JPA transaction. The called function
   * will be provided with a thread local entity manager and the transaction object.
   *
   * @param func  The function to invoke
   *
   * @tparam A The return type of the function
   * @return The result of the invoked function
   */
  def transaction[A](func : (EntityManager, EntityTransaction) => A) : A = {

    val em = instance.createEM
    val tx = em.getTransaction

    try {

      tx.begin()
      val res = func(em, tx)
      tx.commit()

      res
    } catch {

      case ex : Exception =>
        if (tx.isActive)
          tx.rollback()
        throw ex

    }

  }

  /**
   * Executing a function while providing a thread local entity manager.
   *
   * @param func  The function to invoke.
   *
   * @tparam A The return type of the
   * @return The result of the invoked function.
   */
  def query[A](func : (EntityManager) => A) : A = {
    func(instance.createEM)
  }

}


