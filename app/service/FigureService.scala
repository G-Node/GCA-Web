package service

import models._
import java.io.File
import play.api.libs.Files.TemporaryFile
import play.Play
import javax.persistence.{Persistence, EntityManagerFactory}
import service.util.DBUtil

/**
 * Service class for figures. It implements all
 */
class FigureService(val emf: EntityManagerFactory, figPath: String) extends DBUtil {

  /**
   * Get a figure by id.
   *
   * @param id      The id of the figure.
   *
   * @return The requested figure.
   */
  def get(id: String) : Figure = {
    throw new NotImplementedError()
  }

  /**
   * Create a new figure.
   * This action is restricted to all accounts owning the abstract the
   * figure belongs to.
   *
   * @param fig     The figure object to create.
   * @param data    The actual image data of the respective figure.
   * @param abstr   The abstract the figure belongs to.
   * @param account The account uploading the figure.
   *
   * @return The created figure.
   */
  def create(fig: Figure, data: TemporaryFile, abstr: Abstract, account: Account) : Figure = {
    throw new NotImplementedError()
  }

  /**
   * Update a figure, only name and caption can be updated not the image data.
   * This action is restricted to all accounts owning the abstract the
   * figure belongs to.
   *
   * @param fig     The figure to update.
   * @param account The account updating the figure.
   *
   * @return The created figure.
   */
  def update(fig: Figure, account: Account) : Figure = {
    throw new NotImplementedError()
  }

  /**
   * Delete an existing figure.
   * This action is restricted to all accounts owning the abstract the
   * figure belongs to.
   *
   * @param id      The id of the figure.
   * @param account The account that performs the delete.
   *
   * @return True if the figure was deleted, false otherwise.
   */
  def delete(id: String, account: Account) : Boolean = {
    throw new NotImplementedError()
  }

  /**
   * Open the image file that belongs to the figure.
   *
   * @param fig The figure to open.
   *
   * @return A file handler to the respective image file.
   */
  def openFile(fig: Figure) : File = {
    throw new NotImplementedError()
  }

}

/**
 * FigureService companion object.
 */
object FigureService {

  /**
   * Create a figure service using a figure path stored in the configuration under "file.fig_path".
   * As default the relative path "./figures" will be used.
   *
   * @return A new figure service.
   */
  def apply() : FigureService = {
    new FigureService(
      Persistence.createEntityManagerFactory("defaultPersistenceUnit"),
      Play.application().configuration().getString("file.fig_path", "./figures")
    )
  }

}
