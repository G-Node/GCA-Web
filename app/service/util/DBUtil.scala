package service.util

import javax.persistence.{EntityTransaction, EntityManagerFactory, EntityManager, Persistence}
import play.api.mvc.Request

trait EntityManagerProvider {
  def entityManager() : EntityManager
}

object EntityManagerProvider {

  def fromFactory(emf: EntityManagerFactory) : EntityManagerProvider = new EntityManagerProvider {
    override def entityManager(): EntityManager = emf.createEntityManager()
  }

  def fromPersistenceUnit(pu: String) : EntityManagerProvider = fromFactory(Persistence.createEntityManagerFactory(pu))

  def fromDefaultPersistenceUnit() : EntityManagerProvider = fromPersistenceUnit("defaultPersistenceUnit")

  def fromRequest[A](req: Request[A]) : EntityManagerProvider = {
    req match {
      case emp: EntityManagerProvider => emp
      case _ => fromDefaultPersistenceUnit()
    }
  }
}

/**
 * Helper for classes using JPA
 */
trait DBUtil {

  /**
   * Handles a function call inside a transaction and passes an entity manager to
   * it. The called function is expected to return a value of type A.
   *
   * @param func   The function that is invoked inside the transaction.
   * @tparam A  The return type of func.
   *
   * @return The value returned by func.
   */
  def dbTransaction[A](func: (EntityManager, EntityTransaction) => A)(implicit emp: EntityManagerProvider) : A = {

    synchronized {

      var em: EntityManager = null
      var tx: EntityTransaction = null

      try {

        em = emp.entityManager()
        tx = em.getTransaction
        tx.begin()

        func(em, tx)

      } catch {

        case ex: Exception =>
          if (tx != null && tx.isActive) tx.rollback()
          throw ex

      } finally {

        if (tx != null && tx.isActive) tx.commit()

      }
    }
  }

  /**
   * Calls a function and provides an entity manager that can be used inside the function.
   * The entity manager is closed before this method returns.
   *
   * @param func    The function that is called.
   * @tparam A      The return type of func
   *
   * @return The value returned by func.
   */
  def dbQuery[A](func: (EntityManager) => A)(implicit emp: EntityManagerProvider) : A = {

    synchronized {

      var em: EntityManager = null

      try {
        em = emp.entityManager()

        func(em)

      } finally {
        //no-op
      }
    }
  }


}

