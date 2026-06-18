package vsp.persistence.slickdb

import slick.jdbc.SQLiteProfile.api._
import vsp.model.City

import scala.util.Try

object SlickCityRepository {
  private val cities = SlickTables.cities

  private def toModel(row: CityRow): City =
    City(
      id = row.id,
      name = row.name,
      country = row.country,
      latitude = row.latitude,
      longitude = row.longitude
    )

  private def toRow(city: City): CityRow =
    CityRow(
      id = city.id,
      name = city.name,
      country = city.country,
      latitude = city.latitude,
      longitude = city.longitude
    )

  def findAll(): Try[List[City]] =
    SlickDb.run(cities.sortBy(_.name).result)
      .map(_.map(toModel).toList)

  def findByName(name: String): Try[Option[City]] =
    SlickDb.run(
      cities
        .filter(_.name === name)
        .sortBy(_.name)
        .take(1)
        .result
        .headOption
    ).map(_.map(toModel))

  def save(city: City): Try[City] = {
    val insert = (cities returning cities.map(_.id)) += toRow(city.copy(id = 0))
    SlickDb.run(insert).map(newId => city.copy(id = newId))
  }
}
