package vsp.api

import munit.FunSuite

class WeatherCodeTest extends FunSuite {

  test("describe maps clear and cloudy weather codes") {
    assertEquals(WeatherCode.describe(0), "Clear sky")
    assertEquals(WeatherCode.describe(1), "Mainly clear")
    assertEquals(WeatherCode.describe(2), "Partly cloudy")
    assertEquals(WeatherCode.describe(3), "Overcast")
  }

  test("describe maps fog codes") {
    assertEquals(WeatherCode.describe(45), "Fog")
    assertEquals(WeatherCode.describe(48), "Fog")
  }

  test("describe maps drizzle and freezing drizzle codes") {
    List(51, 53, 55).foreach { code =>
      assertEquals(WeatherCode.describe(code), "Drizzle")
    }

    List(56, 57).foreach { code =>
      assertEquals(WeatherCode.describe(code), "Freezing drizzle")
    }
  }

  test("describe maps rain-related codes") {
    List(61, 63, 65).foreach { code =>
      assertEquals(WeatherCode.describe(code), "Rain")
    }

    List(66, 67).foreach { code =>
      assertEquals(WeatherCode.describe(code), "Freezing rain")
    }

    List(80, 81, 82).foreach { code =>
      assertEquals(WeatherCode.describe(code), "Rain showers")
    }
  }

  test("describe maps snow-related codes") {
    List(71, 73, 75).foreach { code =>
      assertEquals(WeatherCode.describe(code), "Snowfall")
    }

    assertEquals(WeatherCode.describe(77), "Snow grains")

    List(85, 86).foreach { code =>
      assertEquals(WeatherCode.describe(code), "Snow showers")
    }
  }

  test("describe maps thunderstorm codes") {
    assertEquals(WeatherCode.describe(95), "Thunderstorm")

    List(96, 99).foreach { code =>
      assertEquals(WeatherCode.describe(code), "Thunderstorm with hail")
    }
  }

  test("describe handles unknown weather codes") {
    assertEquals(WeatherCode.describe(12345), "Unknown weather code (12345)")
  }
}
