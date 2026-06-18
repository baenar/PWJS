package vsp.model

import munit.FunSuite

import java.time.LocalDateTime

class CalendarEventTest extends FunSuite {

  private val warsaw: City = City(
    id = 1,
    name = "Warszawa",
    country = "PL",
    latitude = 52.2297,
    longitude = 21.0122
  )

  test("CalendarEvent.create builds a new unsaved event with empty weather cache") {
    val start = LocalDateTime.now().plusDays(2).withNano(0)
    val event = CalendarEvent.create(
      title = "Sesja testowa",
      city = warsaw,
      description = "Opis",
      startTime = start,
      endTime = start.plusHours(2)
    )

    assertEquals(event.id, 0)
    assertEquals(event.title, "Sesja testowa")
    assertEquals(event.city, warsaw)
    assertEquals(event.description, "Opis")
    assertEquals(event.startTime, start)
    assertEquals(event.endTime, start.plusHours(2))
    assertEquals(event.weather, None)
    assertEquals(event.temperature, None)
    assertEquals(event.lastWeatherUpdate, None)
  }

  test("CalendarEvent.toString includes weather and temperature when cached") {
    val start = LocalDateTime.of(2026, 6, 18, 10, 0)
    val event = CalendarEvent(
      id = 7,
      title = "Nagranie reklamy",
      city = warsaw,
      description = "Opis",
      startTime = start,
      endTime = start.plusHours(2),
      weather = Some("Clear sky"),
      temperature = Some(23.456),
      lastWeatherUpdate = Some(start.minusHours(1))
    )

    val rendered = event.toString

    assert(rendered.contains("[7] Nagranie reklamy @ Warszawa"))
    assert(rendered.contains("pogoda: Clear sky"))
    assert(rendered.contains("23.5°C"))
  }
}
