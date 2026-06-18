package vsp.core

import vsp.TestDatabaseSuite
import vsp.model.{CalendarEvent, City}
import vsp.persistence.{CityRepository, EventRepository}

import java.time.LocalDateTime

class CalendarEventServiceTest extends TestDatabaseSuite {

  private def saveCity(): City =
    CityRepository.save(City.create("Warszawa", "PL", 52.2297, 21.0122)).get

  private def validEvent(title: String = "Valid event"): CalendarEvent = {
    val city = saveCity()
    CalendarEvent.create(
      title = title,
      city = city,
      description = "Service test description",
      startTime = LocalDateTime.now().plusDays(2),
      endTime = LocalDateTime.now().plusDays(2).plusHours(2)
    )
  }

  test("addEvent persists valid event") {
    val result = CalendarEventService.addEvent(validEvent())

    assertEquals(result, Right(()))
    assertEquals(EventRepository.findAll().map(_.title), List("Valid event"))
  }

  test("addEvent rejects invalid event and does not persist it") {
    val invalid = validEvent(title = "   ")

    val result = CalendarEventService.addEvent(invalid)

    assertEquals(result, Left("Tytuł nie może być pusty"))
    assertEquals(EventRepository.findAll(), List.empty[CalendarEvent])
  }

  test("removeEvent deletes existing event") {
    CalendarEventService.addEvent(validEvent("Delete me"))
    val saved = EventRepository.findAll().head

    val result = CalendarEventService.removeEvent(saved.id)

    assertEquals(result, Right(()))
    assertEquals(EventRepository.findAll(), List.empty[CalendarEvent])
  }
}
