package vsp.persistence

import vsp.model.{CalendarEvent, City}

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}
import java.time.LocalDateTime
import scala.util.{Try, Using}

object EventRepository {

  private def connect(): Connection = DriverManager.getConnection(DbConfig.url)

  def findAll(): List[CalendarEvent] = {
    val sql =
      """SELECT e.id, e.title, e.description, e.start_time, e.end_time,
        |       c.id AS city_id, c.name AS city_name, c.country AS city_country
        |FROM calendar_events e
        |JOIN cities c ON e.city_id = c.id
        |ORDER BY e.start_time""".stripMargin

    val result = Using(connect()) { conn =>
      val rs: ResultSet = conn.createStatement().executeQuery(sql)
      var events: List[CalendarEvent] = List()
      while (rs.next()) {
        val city = City(
          id      = rs.getInt("city_id"),
          name    = rs.getString("city_name"),
          country = rs.getString("city_country")
        )
        val event = CalendarEvent(
          id          = rs.getInt("id"),
          title       = rs.getString("title"),
          city        = city,
          description = Option(rs.getString("description")).getOrElse(""),
          startTime   = LocalDateTime.parse(rs.getString("start_time")),
          endTime     = LocalDateTime.parse(rs.getString("end_time"))
        )
        events = events :+ event
      }
      events
    }
    result.getOrElse(List())
  }

  def save(event: CalendarEvent): Try[Unit] = {
    val sql =
      "INSERT INTO calendar_events (title, city_id, description, start_time, end_time) VALUES (?, ?, ?, ?, ?)"
    Using(connect()) { conn =>
      val stmt: PreparedStatement = conn.prepareStatement(sql)
      stmt.setString(1, event.title)
      stmt.setInt(2, event.city.id)
      stmt.setString(3, event.description)
      stmt.setString(4, event.startTime.toString)
      stmt.setString(5, event.endTime.toString)
      stmt.executeUpdate()
    }.map(_ => ())
  }

  def deleteById(id: Int): Try[Unit] = {
    Using(connect()) { conn =>
      val stmt: PreparedStatement = conn.prepareStatement(
        "DELETE FROM calendar_events WHERE id = ?"
      )
      stmt.setInt(1, id)
      stmt.executeUpdate()
    }.map(_ => ())
  }
}
