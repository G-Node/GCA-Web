package utils

import java.net.URL

class RoutesResolver {
  lazy val conf = play.api.Play.current.configuration
  lazy val baseUrl = sys.env.get("GCA_BASEURL").orElse(conf.getString("baseurl")).getOrElse("http://localhost:9000")

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
    * Builds an URL to the related favourite abstracts from a given object ID.
    *
    * @param id an ID of a Conference object to insert into the URL
    *
    * @return URL for related abstracts, like "/conferences/HNOPSADMHV/favouriteabstracts"
    */
  def favUsersUrl(id: String) = {
    new URL(baseUrl + s"/api/abstracts/$id/favusers")
  }

  /**
   * Builds an URL to the related abstracts from a given object ID.
   *
   * @param id an ID of a Conference object to insert into the URL
   *
   * @return URL for related abstracts, like "/api/conferences/HNOPSADMHV/abstracts"
   */
  def abstractsUrl(id: String) = {
    new URL(baseUrl + s"/api/conferences/$id/abstracts")
  }

  /**
   * Builds an URL to all related abstracts from a given object ID.
   *
   * @param id an ID of a Conference object to insert into the URL
   *
   * @return URL for related abstracts, like "/api/conferences/HNOPSADMHV/allAbstracts"
   */
  def allAbstractsUrl(id: String) = {
    new URL(baseUrl + s"/api/conferences/$id/allAbstracts")
  }

  /**
    * Builds an URL to the related abstracts from a given object ID.
    *
    * @param id an ID of a User object to insert into the URL
    *
    * @return URL for related abstracts, like "/api/user/HNOPSADMHV/favouriteabstracts"
    */
  def favAbstractsUrl(id: String) = {
    new URL(baseUrl + s"/api/user/$id/favouriteabstracts")
  }


  /**
   * Builds an URL to figure file.
   *
   * @param id an ID of a Figure object to insert into the URL
   *
   * @return URL for file, like "/api/figures/HNOPSADMHV/image"
   */
  def figureFileUrl(id: String) = {
    new URL(baseUrl + s"/api/figures/$id/image")
  }

  /**
   * Builds an URL to state log of a given abstract.
   *
   * @param id an ID of an Abstract object
   *
   * @return URL for file, like " /api/abstracts/:id/stateLog"
   */
  def stateLogUrl(id: String) = {
    new URL(baseUrl + s"/api/abstracts/$id/stateLog")
  }

  /**
   * Builds an URL to the Conference page from a given conference UUID.
   *
   * @param id an UUID of a Conference object to insert into the URL
   *
   * @return URL for the conference, like "/api/conferences/HNOPSADMHV"
   */
  def conferenceUrl(id: String) = {
    new URL(baseUrl + s"/api/conferences/$id")
  }

  /**
   * Builds an URL to the the account activation
   *
   * @param token The token used to activate the account
   *
   * @return URL for the activation
   */
  def activationUrl(token: String) = {
    new URL(baseUrl + s"/activate/$token")
  }

  /**
    * Builds an URL to fetch or manipulate the geo entry of a conference for a given conference UUID.
    *
    * @param id A UUID of a Conference object to insert into the URL.
    *
    * @return URL to manipulate the geo entry of the conference, like "/api/conferences/HNOPSADMHV/geo".
    */
  def geoUrl(id: String) = {
    new URL(baseUrl + s"/api/conferences/$id/geo")
  }

  /**
    * Builds an URL to fetch or manipulate the schedule entry of a conference for a given conference UUID.
    *
    * @param id A UUID of a Conference object to insert into the URL.
    *
    * @return URL to manipulate the schedule entry of the conference, like "/api/conferences/HNOPSADMHV/schedule".
    */
  def scheduleUrl(id: String) = {
    new URL(baseUrl + s"/api/conferences/$id/schedule")
  }

  /**
    * Builds an URL to fetch or manipulate the info entry of a conference for a given conference UUID.
    *
    * @param id A UUID of a Conference object to insert into the URL.
    *
    * @return URL to manipulate the info entry of the conference, like "/api/conferences/HNOPSADMHV/info".
    */
  def infoUrl(id: String) = {
    new URL(baseUrl + s"/api/conferences/$id/info")
  }

}

object DefaultRoutesResolver {

  implicit val resolver = new RoutesResolver()

}
