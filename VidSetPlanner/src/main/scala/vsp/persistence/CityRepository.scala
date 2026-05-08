package vsp.persistence

import vsp.model.City

import java.sql.{Connection, DriverManager, PreparedStatement}
import scala.util.{Try, Using}

object CityRepository {

  private def connect(): Connection = DriverManager.getConnection(DbConfig.url)

  def findAll(): List[City] = {
    val result = Using(connect()) { conn =>
      val rs = conn.createStatement().executeQuery(
        "SELECT id, name, country FROM cities ORDER BY name"
      )
      var cities: List[City] = List()
      while (rs.next()) {
        cities = cities :+ City(
          id      = rs.getInt("id"),
          name    = rs.getString("name"),
          country = rs.getString("country")
        )
      }
      cities
    }
    result.getOrElse(List())
  }

  def save(city: City): Try[Unit] = {
    Using(connect()) { conn =>
      val stmt: PreparedStatement = conn.prepareStatement(
        "INSERT INTO cities (name, country) VALUES (?, ?)"
      )
      stmt.setString(1, city.name)
      stmt.setString(2, city.country)
      stmt.executeUpdate()
    }.map(_ => ())
  }
}
