//package utils //put right name

import java.sql.{Connection, DriverManager}
import java.util.UUID.randomUUID

import scala.collection.mutable.ListBuffer

/**
  * Extract old db values like "description" and put them into table ConfText
  */
object SwapConfTexts {

  def main(args: Array[String]) {

    //insert right data
    //postgres
    /*val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql://<host>:<port>/<db>"
    val ctTableName = "conftext"*/
    //h2
    val driver = "org.h2.Driver"
    val url = "jdbc:h2:gca-web"
    val ctTableName = "CONFTEXT"

    val username = ""
    val password = ""

    var connection:Connection = null

    try {
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)

      val statement = connection.createStatement()

      val md = connection.getMetaData()
      val rs = md.getTables(null, null, ctTableName, null)
      if (!rs.next()) {
        println(rs)
        statement.executeUpdate("CREATE TABLE conftext (" +
          "uuid varchar(255), " +
          "ctType varchar(255), " +
          "text varchar(255), " +
          "conference_uuid varchar(255))"
        )
      }

      val confResultSet = statement.executeQuery("SELECT * FROM Conference c")

      var descExists = false
      var logoExists = false
      var thnExists = false

      if (md.getColumns(null, null,
        "conference", "description").next()) {
        descExists = true
      }
      if (md.getColumns(null, null,
        "conference", "logo").next()) {
        logoExists = true
      }
      if (md.getColumns(null, null,
        "conference", "thumbnail").next()) {
        thnExists = true
      }

      val cuuid = new ListBuffer[String]()
      val name = new ListBuffer[String]()
      val desc = new ListBuffer[String]()
      val logo = new ListBuffer[String]()
      val thn = new ListBuffer[String]()

      while ( confResultSet.next() ) {
        cuuid += confResultSet.getString("uuid")
        name += confResultSet.getString("name")

        println("CONFERENCE")
        println("uuid = " + confResultSet.getString("uuid"))
        println("name = " + confResultSet.getString("name"))
        if (descExists) {
          desc += confResultSet.getString("description")
          println("desc = " + confResultSet.getString("description"))
        } else {
          desc += null
        }
        if (logoExists) {
          logo += confResultSet.getString("logo")
          println("logo = " + confResultSet.getString("logo"))

        } else {
          logo += null
        }
        if (thnExists) {
          thn += confResultSet.getString("thumbnail")
          println("thn = " + confResultSet.getString("thumbnail"))
        } else {
          thn += null
        }

        println()
      }

      val cuuidList = cuuid.toList
      val logoList = logo.toList
      val thnList = thn.toList
      val descList = desc.toList
      
      //add confTexts
      for ( i <- 0 to (cuuidList.length - 1)) {

        //keep already made entries in confText table
        val rSDesc = statement.executeQuery("SELECT * FROM ConfText " +
          "WHERE ctType = 'description' " +
          "AND conference_uuid = '" + cuuidList(i) + "'")

        //TODO: Check for current description == null?
        if (!rSDesc.next() && descList(i) != null) {
          val confTextUuid = randomUUID().toString
          val updStrCT = "INSERT INTO confText (uuid, ctType, text, conference_uuid)" +
            "VALUES ('" + confTextUuid + "', 'description', '" + descList(i) + "', '" + cuuidList(i) + "')"
          statement.executeUpdate(updStrCT)
        }

        //keep already made entries in confText table
        val rSLogo = statement.executeQuery("SELECT * FROM ConfText " +
          "WHERE ctType = 'logo' " +
          "AND conference_uuid = '" + cuuidList(i) + "'")

        //TODO: Check for current logo == null?
        if (!rSLogo.next() && logoList(i) != null) {
          val cUuid = randomUUID().toString
          val updStrCT = "INSERT INTO confText (uuid, ctType, text, conference_uuid)" +
            "VALUES (" + cUuid + ", 'logo', '" + logoList(i) + "', '" + cuuidList(i) + "')"
          statement.executeUpdate(updStrCT)
        }

        //keep already made entries in confText table
        val rSThn = statement.executeQuery("SELECT * FROM ConfText " +
          "WHERE ctType = 'thumbnail' " +
          "AND conference_uuid = '" + cuuidList(i) + "'")

        //TODO: Check for current description == null?
        if (!rSThn.next() && thnList(i) != null) {
          val cUuid = randomUUID().toString
          val updStrCT = "INSERT INTO confText (uuid, ctType, text, conference_uuid)" +
            "VALUES (" + cUuid + ", 'thumbnail', '" + thnList(i) + "', '" + cuuidList(i) + "')"
          statement.executeUpdate(updStrCT)
        }
      }

      val ctResultSet = statement.executeQuery("SELECT * FROM ConfText")

      //keep this to assure everything worked
      while ( ctResultSet.next() ) {
        println("CONFTEXT")
        println("uuid: " + ctResultSet.getString("uuid"))
        println("ctType: " + ctResultSet.getString("ctType"))
        println("text: " + ctResultSet.getString("text"))
        println("conf: " + ctResultSet.getString("conference_uuid"))
        println()
      }

      //delete columns if wanted
      //statement.executeQuery("ALTER TABLE 'Conference' DROP 'description'")
      //statement.executeQuery("ALTER TABLE 'Conference' DROP 'logo'")
      //statement.executeQuery("ALTER TABLE 'Conference' DROP 'thumbnail'")

    } catch {
      case e => e.printStackTrace
    }
    connection.close()
  }

}
