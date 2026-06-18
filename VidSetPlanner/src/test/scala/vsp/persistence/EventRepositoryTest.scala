package vsp.persistence

import vsp.TestDatabaseSuite
import vsp.model.{CalendarEvent, City}

import java.time.LocalDateTime

class EventRepositoryTest extends TestDatabaseSuite {

  private def saveCity(name: String = "Warszawa"): City =
    CityRepository.save(City.create(name, "PL", 52.2297, 21.0122)).get

  private def event(
    title: String,
    city: City,
    start: LocalDateTime,
    weather: Option[String] = None,
    temperature: Option[Double] = None,
    lastWeatherUpdate: Option[LocalDateTime] = None
  ): CalendarEvent =
    CalendarEvent(
      id = 0,
      title = title,
      city = city,
      description = s"Description for $title",
      startTime = start,
      endTime = start.plusHours(2),
      weather = weather,
      temperature = temperature,
      lastWeatherUpdate = lastWeatherUpdate
    )

  test("save and findAll round-trip event with city") {
    val city = saveCity()
    val start = LocalDateTime.of(2030, 6, 1, 10, 0)

    EventRepository.save(event("Morning shoot", city, start)).get

    val events = EventRepository.findAll()
    assertEquals(events.length, 1)

    val loaded = events.head
    assert(loaded.id > 0)
    assertEquals(loaded.title, "Morning shoot")
    assertEquals(loaded.city, city)
    assertEquals(loaded.description, "Description for Morning shoot")
    assertEquals(loaded.startTime, start)
    assertEquals(loaded.endTime, start.plusHours(2))
    assertEquals(loaded.weather, None)
    assertEquals(loaded.temperature, None)
    assertEquals(loaded.lastWeatherUpdate, None)
  }

  test("save and findAll round-trip cached weather fields") {
    val city = saveCity()
    val start = LocalDateTime.of(2030, 6, 2, 12, 0)
    val fetchedAt = LocalDateTime.of(2030, 5, 31, 8, 30)

    EventRepository.save(
      event(
        title = "Weather shoot",
        city = city,
        start = start,
        weather = Some("Partly cloudy"),
        temperature = Some(21.5),
        lastWeatherUpdate = Some(fetchedAt)
      )
    ).get

    val loaded = EventRepository.findAll().head
    assertEquals(loaded.weather, Some("Partly cloudy"))
    assertEquals(loaded.temperature, Some(21.5))
    assertEquals(loaded.lastWeatherUpdate, Some(fetchedAt))
  }

  test("findAll returns events ordered by start time") {
    val city = saveCity()
    val later = LocalDateTime.of(2030, 6, 3, 15, 0)
    val earlier = LocalDateTime.of(2030, 6, 3, 9, 0)

    EventRepository.save(event("Later event", city, later)).get
    EventRepository.save(event("Earlier event", city, earlier)).get

    val titles = EventRepository.findAll().map(_.title)
    assertEquals(titles, List("Earlier event", "Later event"))
  }

  test("deleteById removes persisted event") {
    val city = saveCity()
    val start = LocalDateTime.of(2030, 6, 4, 10, 0)

    EventRepository.save(event("Event to delete", city, start)).get
    val saved = EventRepository.findAll().head

    EventRepository.deleteById(saved.id).get

    assertEquals(EventRepository.findAll(), List.empty[CalendarEvent])
  }
}
