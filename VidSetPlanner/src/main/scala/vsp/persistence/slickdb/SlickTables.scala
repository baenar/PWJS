package vsp.persistence.slickdb

import slick.jdbc.SQLiteProfile.api._

class CitiesTable(tag: Tag) extends Table[CityRow](tag, "cities") {
  def id        = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name      = column[String]("name")
  def country   = column[String]("country")
  def latitude  = column[Double]("latitude")
  def longitude = column[Double]("longitude")

  def * = (id, name, country, latitude, longitude).mapTo[CityRow]
}

class CalendarEventsTable(tag: Tag) extends Table[CalendarEventRow](tag, "calendar_events") {
  def id                = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def title             = column[String]("title")
  def cityId            = column[Int]("city_id")
  def description       = column[String]("description")
  def startTime         = column[String]("start_time")
  def endTime           = column[String]("end_time")
  def weather           = column[Option[String]]("weather")
  def temperature       = column[Option[Double]]("temperature")
  def lastWeatherUpdate = column[Option[String]]("last_weather_update")

  def city = foreignKey("fk_calendar_events_city", cityId, SlickTables.cities)(_.id)

  def * = (
    id,
    title,
    cityId,
    description,
    startTime,
    endTime,
    weather,
    temperature,
    lastWeatherUpdate
  ).mapTo[CalendarEventRow]
}

object SlickTables {
  val cities = TableQuery[CitiesTable]
  val calendarEvents = TableQuery[CalendarEventsTable]
}
