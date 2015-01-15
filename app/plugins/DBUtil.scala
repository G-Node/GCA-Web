package plugins

import javax.persistence.{EntityTransaction, EntityManager, Persistence}

import play.api._

/**
 * Plugin that provides an configured entity manager factory and
 * thread local entity managers.
 */
class DBUtil(implicit app: Application) extends Plugin {

  private lazy val emf = Persistence.createEntityManagerFactory(DBUtil.DEFAULT_UNIT)
  private lazy val tlem = new ThreadLocalEM

  /**
   * Get a thread local entity manager.
   *
   * @return An entity manager that is always the same for each thread.
   */
  def threadLocalEM : EntityManager = {
    tlem.get()
  }

  /**
   * Get a new entity manager.
   *
   * @return A newly created entity manager.
   */
  def createEM : EntityManager = {
    emf.createEntityManager()
  }


  /**
   * ThreadLocal implementation for EntityManager
   */
  private class ThreadLocalEM extends ThreadLocal[EntityManager] {

    override def initialValue(): EntityManager = {
      emf.createEntityManager()
    }

    override def set(value: EntityManager): Unit = {
      val curr = get()
      if (curr.isOpen)
        curr.close()

      super.set(value)
    }

    override def remove(): Unit = {
      val curr = get()
      if (curr.isOpen)
        curr.close()

      super.remove()
    }
  }

}

object DBUtil {

  val DEFAULT_UNIT = "defaultPersistenceUnit"

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

    val em = instance.threadLocalEM
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
    func(instance.threadLocalEM)
  }

}


