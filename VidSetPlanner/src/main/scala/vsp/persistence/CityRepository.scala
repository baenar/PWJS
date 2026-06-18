package vsp.persistence

import vsp.model.City

import java.sql.{Connection, DriverManager, PreparedStatement, Statement}
import scala.util.{Try, Using}

object CityRepository {

  private def connect(): Connection = DriverManager.getConnection(DbConfig.url)

  def findAll(): List[City] = {
    val result = Using(connect()) { conn =>
      val rs = conn.createStatement().executeQuery(
        "SELECT id, name, country, latitude, longitude FROM cities ORDER BY name"
      )
      var cities: List[City] = List()
      while (rs.next()) {
        cities = cities :+ City(
          id        = rs.getInt("id"),
          name      = rs.getString("name"),
          country   = rs.getString("country"),
          latitude  = rs.getDouble("latitude"),
          longitude = rs.getDouble("longitude")
        )
      }
      cities
    }
    result.getOrElse(List())
  }

  // Szuka w cache po nazwie. Bierzemy tylko wpisy z współrzędnymi -
  // dzięki temu ewentualne stare rekordy bez nich traktujemy jak brak.
  def findByName(name: String): Option[City] = {
    Using(connect()) { conn =>
      val stmt: PreparedStatement = conn.prepareStatement(
        "SELECT id, name, country, latitude, longitude FROM cities " +
          "WHERE name = ? AND latitude IS NOT NULL LIMIT 1"
      )
      stmt.setString(1, name)
      val rs = stmt.executeQuery()
      if (rs.next()) {
        Some(City(
          id        = rs.getInt("id"),
          name      = rs.getString("name"),
          country   = rs.getString("country"),
          latitude  = rs.getDouble("latitude"),
          longitude = rs.getDouble("longitude")
        ))
      } else {
        None
      }
    }.toOption.flatten
  }

  // Zapisuje miasto i zwraca je z nadanym id.
  def save(city: City): Try[City] = {
    Using(connect()) { conn =>
      val stmt: PreparedStatement = conn.prepareStatement(
        "INSERT INTO cities (name, country, latitude, longitude) VALUES (?, ?, ?, ?)",
        Statement.RETURN_GENERATED_KEYS
      )
      stmt.setString(1, city.name)
      stmt.setString(2, city.country)
      stmt.setDouble(3, city.latitude)
      stmt.setDouble(4, city.longitude)
      stmt.executeUpdate()

      val keys  = stmt.getGeneratedKeys
      val newId = if (keys.next()) keys.getInt(1) else 0
      city.copy(id = newId)
    }
  }
}
