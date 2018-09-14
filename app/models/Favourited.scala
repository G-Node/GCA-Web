package models

import java.util.{Set => JSet}

import scala.collection.JavaConversions._

/**
 * Interface that defines favouriting by user.
 */
trait Favourited {

  var uuid: String
  var favs: JSet[Account]

  def isFav(account: Account): Boolean = {
    val favList: Seq[Account] = asScalaSet(favs).toSeq
    favList.contains(account)
  }

}
