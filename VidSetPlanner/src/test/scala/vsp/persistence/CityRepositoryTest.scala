package vsp.persistence

import vsp.TestDatabaseSuite
import vsp.model.City

class CityRepositoryTest extends TestDatabaseSuite {

  test("findAll returns empty list after current migrations") {
    // V3 migration intentionally clears old seed cities, because cities are now
    // a geocoding cache with latitude/longitude.
    assertEquals(CityRepository.findAll(), List.empty[City])
  }

  test("save persists city and returns generated id") {
    val city = City.create(
      name = "Testowo",
      country = "PL",
      latitude = 52.1,
      longitude = 21.2
    )

    val saved = CityRepository.save(city).get

    assert(saved.id > 0)
    assertEquals(saved.name, "Testowo")
    assertEquals(saved.country, "PL")
    assertEquals(saved.latitude, 52.1)
    assertEquals(saved.longitude, 21.2)

    val all = CityRepository.findAll()
    assertEquals(all, List(saved))
  }

  test("findByName returns city with coordinates") {
    val saved = CityRepository.save(
      City.create("Warszawa", "PL", 52.2297, 21.0122)
    ).get

    assertEquals(CityRepository.findByName("Warszawa"), Some(saved))
    assertEquals(CityRepository.findByName("NonExistingCity"), None)
  }
}
