package utils

import java.net.URL

class RoutesResolver {
  lazy val conf = play.api.Play.current.configuration
  lazy val baseUrl = conf.getString("baseurl").getOrElse("http://localhost:9000")

  /**
   * Builds an URL to the related abstracts from a given object ID.
   *
   * @param id an ID of a Conference object to insert into the URL
   *
   * @return URL for related abstracts, like "/conferences/HNOPSADMHV/abstracts"
   */
  def ownersUrl(id: String) = {
    new URL(baseUrl + s"/api/abstracts/$id/owners")
  }

  /**
   * Builds an URL to the related abstracts from a given object ID.
   *
   * @param id an ID of a Conference object to insert into the URL
   *
   * @return URL for related abstracts, like "/api/conferences/HNOPSADMHV/abstracts"
   */
  def abstractsUrl(id: String) = {
    new URL(baseUrl + s"/api/account/$id/abstracts")
  }
}

object DefaultRoutesResolver {

  implicit val resolver = new RoutesResolver()

}