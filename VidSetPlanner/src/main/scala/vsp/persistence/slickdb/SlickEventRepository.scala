package vsp.persistence.slickdb

import slick.jdbc.SQLiteProfile.api._
import vsp.model.{CalendarEvent, City}

import java.time.LocalDateTime
import scala.util.Try

object SlickEventRepository {
  private val events = SlickTables.calendarEvents
  private val cities = SlickTables.cities

  private def toCity(row: CityRow): City =
    City(
      id = row.id,
      name = row.name,
      country = row.country,
      latitude = row.latitude,
      longitude = row.longitude
    )

  private def toEvent(eventRow: CalendarEventRow, cityRow: CityRow): CalendarEvent =
    CalendarEvent(
      id = eventRow.id,
      title = eventRow.title,
      city = toCity(cityRow),
      description = eventRow.description,
      startTime = LocalDateTime.parse(eventRow.startTime),
      endTime = LocalDateTime.parse(eventRow.endTime),
      weather = eventRow.weather,
      temperature = eventRow.temperature,
      lastWeatherUpdate = eventRow.lastWeatherUpdate.map(LocalDateTime.parse)
    )

  private def toRow(event: CalendarEvent): CalendarEventRow =
    CalendarEventRow(
      id = event.id,
      title = event.title,
      cityId = event.city.id,
      description = event.description,
      startTime = event.startTime.toString,
      endTime = event.endTime.toString,
      weather = event.weather,
      temperature = event.temperature,
      lastWeatherUpdate = event.lastWeatherUpdate.map(_.toString)
    )

  private def joinedQuery =
    for {
      event <- events
      city <- cities if event.cityId === city.id
    } yield (event, city)

  def findAll(): Try[List[CalendarEvent]] =
    SlickDb.run(joinedQuery.sortBy(_._1.startTime).result)
      .map(_.map((eventRow, cityRow) => toEvent(eventRow, cityRow)).toList)

  def findById(id: Int): Try[Option[CalendarEvent]] =
    SlickDb.run(joinedQuery.filter(_._1.id === id).result.headOption)
      .map(_.map((eventRow, cityRow) => toEvent(eventRow, cityRow)))

  def findBetween(from: LocalDateTime, to: LocalDateTime): Try[List[CalendarEvent]] =
    SlickDb.run(
      joinedQuery
        .filter { case (event, _) =>
          event.startTime >= from.toString && event.startTime < to.toString
        }
        .sortBy(_._1.startTime)
        .result
    ).map(_.map((eventRow, cityRow) => toEvent(eventRow, cityRow)).toList)

  def save(event: CalendarEvent): Try[CalendarEvent] = {
    val insert = (events returning events.map(_.id)) += toRow(event.copy(id = 0))
    SlickDb.run(insert).map(newId => event.copy(id = newId))
  }

  def update(event: CalendarEvent): Try[Boolean] = {
    val row = toRow(event)
    val updateAction = events
      .filter(_.id === event.id)
      .map(e => (
        e.title,
        e.cityId,
        e.description,
        e.startTime,
        e.endTime,
        e.weather,
        e.temperature,
        e.lastWeatherUpdate
      ))
      .update((
        row.title,
        row.cityId,
        row.description,
        row.startTime,
        row.endTime,
        row.weather,
        row.temperature,
        row.lastWeatherUpdate
      ))

    SlickDb.run(updateAction).map(_ > 0)
  }

  def deleteById(id: Int): Try[Boolean] =
    SlickDb.run(events.filter(_.id === id).delete)
      .map(_ > 0)
}
