package vsp.core

import munit.FunSuite
import vsp.model.{CalendarEvent, City}

import java.time.LocalDateTime

class ValidatorTest extends FunSuite {

  private val warsaw: City = City(
    id = 1,
    name = "Warszawa",
    country = "PL",
    latitude = 52.2297,
    longitude = 21.0122
  )

  private def validEvent(): CalendarEvent = {
    val start = LocalDateTime.now().plusDays(3).withNano(0)
    CalendarEvent.create(
      title = "Sesja zdjęciowa - Park Saski",
      city = warsaw,
      description = "Plener, złota godzina",
      startTime = start,
      endTime = start.plusHours(2)
    )
  }

  test("validate accepts a correct future event") {
    val event = validEvent()

    assertEquals(Validator.validate(event), Right(event))
  }

  test("validate rejects an empty title") {
    val event = validEvent().copy(title = "")

    assertEquals(
      Validator.validate(event),
      Left("Tytuł nie może być pusty")
    )
  }

  test("validate rejects a whitespace-only title") {
    val event = validEvent().copy(title = "   \t  ")

    assertEquals(
      Validator.validate(event),
      Left("Tytuł nie może być pusty")
    )
  }

  test("validate rejects an event without a selected city") {
    val missingCity = warsaw.copy(id = 0)
    val event = validEvent().copy(city = missingCity)

    assertEquals(
      Validator.validate(event),
      Left("Miasto musi być wybrane")
    )
  }

  test("validate rejects an event starting in the past") {
    val start = LocalDateTime.now().minusDays(1).withNano(0)
    val event = validEvent().copy(
      startTime = start,
      endTime = start.plusHours(2)
    )

    assertEquals(
      Validator.validate(event),
      Left("Data rozpoczęcia nie może być w przeszłości")
    )
  }

  test("validate rejects an event whose end time equals start time") {
    val start = LocalDateTime.now().plusDays(3).withNano(0)
    val event = validEvent().copy(
      startTime = start,
      endTime = start
    )

    assertEquals(
      Validator.validate(event),
      Left("Czas zakończenia musi być po czasie rozpoczęcia")
    )
  }

  test("validate rejects an event whose end time is before start time") {
    val start = LocalDateTime.now().plusDays(3).withNano(0)
    val event = validEvent().copy(
      startTime = start,
      endTime = start.minusMinutes(1)
    )

    assertEquals(
      Validator.validate(event),
      Left("Czas zakończenia musi być po czasie rozpoczęcia")
    )
  }
}
