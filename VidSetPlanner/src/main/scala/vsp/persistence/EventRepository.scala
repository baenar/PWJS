package vsp.persistence

import vsp.model.{CalendarEvent, City}

import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, Types}
import java.time.LocalDateTime
import scala.util.{Try, Using}

object EventRepository {

  private def connect(): Connection = DriverManager.getConnection(DbConfig.url)

  def findAll(): List[CalendarEvent] = {
    val sql =
      """SELECT e.id, e.title, e.description, e.start_time, e.end_time,
        |       e.weather, e.temperature, e.last_weather_update,
        |       c.id AS city_id, c.name AS city_name, c.country AS city_country,
        |       c.latitude AS city_lat, c.longitude AS city_lon
        |FROM calendar_events e
        |JOIN cities c ON e.city_id = c.id
        |ORDER BY e.start_time""".stripMargin

    val result = Using(connect()) { conn =>
      val rs: ResultSet = conn.createStatement().executeQuery(sql)
      var events: List[CalendarEvent] = List()
      while (rs.next()) {
        val city = City(
          id        = rs.getInt("city_id"),
          name      = rs.getString("city_name"),
          country   = rs.getString("city_country"),
          latitude  = rs.getDouble("city_lat"),
          longitude = rs.getDouble("city_lon")
        )

        // temperature jest nullable - getDouble zwraca 0.0 dla NULL, więc sprawdzamy wasNull
        val tempRaw = rs.getDouble("temperature")
        val temperature = if (rs.wasNull()) None else Some(tempRaw)

        val event = CalendarEvent(
          id                = rs.getInt("id"),
          title             = rs.getString("title"),
          city              = city,
          description       = Option(rs.getString("description")).getOrElse(""),
          startTime         = LocalDateTime.parse(rs.getString("start_time")),
          endTime           = LocalDateTime.parse(rs.getString("end_time")),
          weather           = Option(rs.getString("weather")),
          temperature       = temperature,
          lastWeatherUpdate = Option(rs.getString("last_weather_update")).map(LocalDateTime.parse)
        )
        events = events :+ event
      }
      events
    }
    result.getOrElse(List())
  }

  def save(event: CalendarEvent): Try[Unit] = {
    val sql =
      "INSERT INTO calendar_events " +
        "(title, city_id, description, start_time, end_time, weather, temperature, last_weather_update) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    Using(connect()) { conn =>
      val stmt: PreparedStatement = conn.prepareStatement(sql)
      stmt.setString(1, event.title)
      stmt.setInt(2, event.city.id)
      stmt.setString(3, event.description)
      stmt.setString(4, event.startTime.toString)
      stmt.setString(5, event.endTime.toString)

      event.weather match {
        case Some(w) => stmt.setString(6, w)
        case None    => stmt.setNull(6, Types.VARCHAR)
      }
      event.temperature match {
        case Some(t) => stmt.setDouble(7, t)
        case None    => stmt.setNull(7, Types.REAL)
      }
      event.lastWeatherUpdate match {
        case Some(ts) => stmt.setString(8, ts.toString)
        case None     => stmt.setNull(8, Types.VARCHAR)
      }

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

  def update(event: CalendarEvent): Try[Unit] = {
    val sql =
      """UPDATE calendar_events 
        |SET title = ?, city_id = ?, description = ?, start_time = ?, end_time = ? 
        |WHERE id = ?""".stripMargin

    Using(connect()) { conn =>
      val stmt: PreparedStatement = conn.prepareStatement(sql)
      stmt.setString(1, event.title)
      stmt.setInt(2, event.city.id)
      stmt.setString(3, event.description)
      stmt.setString(4, event.startTime.toString)
      stmt.setString(5, event.endTime.toString)
      stmt.setInt(6, event.id) // To ID mówi bazie, który konkretnie wiersz nadpisać
      stmt.executeUpdate()
    }.map(_ => ())
  }
}
