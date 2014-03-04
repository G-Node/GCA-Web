package service.util

import javax.persistence.{EntityTransaction, EntityManagerFactory, EntityManager}

/**
 * Helper for classes using JPA
 */
trait DBUtil {

  protected def emf: EntityManagerFactory

  /**
   * Handles a function call inside a transaction and passes an entity manager to
   * it. The called function is expected to return a value of type A.
   *
   * @param func   The function that is invoked inside the transaction.
   * @tparam A  The return type of func.
   *
   * @return The value returned by func.
   */
  def dbTransaction[A](func: (EntityManager, EntityTransaction) => A) : A = {

    synchronized {

      var em: EntityManager = null
      var tx: EntityTransaction = null

      try {

        em = emf.createEntityManager()
        tx = em.getTransaction
        tx.begin()

        func(em, tx)

      } catch {

        case ex: Exception =>
          if (tx != null && tx.isActive) tx.rollback()
          throw ex

      } finally {

        if (tx != null && tx.isActive) tx.commit()
        if (em != null && em.isOpen) em.close()

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
  def dbQuery[A](func: (EntityManager) => A) : A = {

    synchronized {

      var em: EntityManager = null

      try {
        em = emf.createEntityManager()

        func(em)

      } finally {
        if (em != null && em.isOpen) em.close()
      }
    }
  }


}

