package vsp.persistence.slickdb

case class CityRow(
  id: Int,
  name: String,
  country: String,
  latitude: Double,
  longitude: Double
)

case class CalendarEventRow(
  id: Int,
  title: String,
  cityId: Int,
  description: String,
  startTime: String,
  endTime: String,
  weather: Option[String],
  temperature: Option[Double],
  lastWeatherUpdate: Option[String]
)
