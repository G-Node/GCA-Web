package utils

import java.net.URL

object URLHelper {

  /**
   * Helps to determine the base part of the URL (like "https://example.com:9000")
   * from the given full URL.
   *
   * @param uri full version of the URL (like "https://example.com:9000/foo/bar/?id=none")
   *
   * @return base URL part (like "https://example.com:9000")
   */
  def getBaseUrl(uri: String): String = {
    val fullUrl = new URL(uri)
    fullUrl.toString.replace(fullUrl.getPath, "")
  }

}
