package vsp.api

import munit.FunSuite
import vsp.model.City

import java.time.LocalDateTime

class WeatherClientPolicyTest extends FunSuite {

  private val warsaw: City = City(
    id = 1,
    name = "Warszawa",
    country = "PL",
    latitude = 52.2297,
    longitude = 21.0122
  )

  test("getWeatherByCityAndDate skips dates in the past without calling the API") {
    val result = WeatherClient.getWeatherByCityAndDate(
      city = warsaw,
      date = LocalDateTime.now().minusDays(1),
      lastUpdate = None
    )

    assertEquals(result, WeatherResult.SkippedPastDate)
  }

  test("getWeatherByCityAndDate skips dates farther than supported forecast range") {
    val result = WeatherClient.getWeatherByCityAndDate(
      city = warsaw,
      date = LocalDateTime.now().plusDays(8),
      lastUpdate = None
    )

    assertEquals(result, WeatherResult.SkippedTooFar)
  }

  test("getWeatherByCityAndDate skips refresh when cached weather is fresh") {
    val result = WeatherClient.getWeatherByCityAndDate(
      city = warsaw,
      date = LocalDateTime.now().plusDays(1),
      lastUpdate = Some(LocalDateTime.now().minusHours(1))
    )

    assertEquals(result, WeatherResult.SkippedFresh)
  }
}
