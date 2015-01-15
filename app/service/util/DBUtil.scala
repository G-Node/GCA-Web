package service.util

import javax.persistence.{EntityManager, EntityManagerFactory, EntityTransaction, Persistence}

import play.api.mvc.Request
import plugins.DBUtil._

@Deprecated
trait EntityManagerProvider {
  def entityManager() : EntityManager
}

@Deprecated
object EntityManagerProvider {

  def fromEntityManager(em: EntityManager) : EntityManagerProvider = new EntityManagerProvider {
    override def entityManager(): EntityManager = em
  }

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

@Deprecated
object EMPImplicits {

  implicit def EMPFromEntityManager(implicit em: EntityManager) = EntityManagerProvider.fromEntityManager(em)
  implicit def EMPFromRequest[A](implicit req: Request[A]) = EntityManagerProvider.fromRequest(req)

}

/**
 * Helper for classes using JPA
 */
@Deprecated
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
  def dbTransaction[A](func: (EntityManager, EntityTransaction) => A) = transaction(func)

  /**
   * Calls a function and provides an entity manager that can be used inside the function.
   * The entity manager is closed before this method returns.
   *
   * @param func    The function that is called.
   * @tparam A      The return type of func
   *
   * @return The value returned by func.
   */
  def dbQuery[A](func: (EntityManager) => A) = query(func)


}

