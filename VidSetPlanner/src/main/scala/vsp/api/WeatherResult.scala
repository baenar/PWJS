package vsp.api

import java.time.LocalDateTime

// Wynik próby pobrania pogody.
// Celowe pomijanie to decyzja odciążająca API pogodowe
// Error to faktyczna awaria.
sealed trait WeatherResult

object WeatherResult {
  case class Fetched(weather: String, temperature: Double, fetchedAt: LocalDateTime) extends WeatherResult
  case object SkippedPastDate extends WeatherResult  // data w przeszłości
  case object SkippedTooFar   extends WeatherResult  // dalej niż tydzień w przyszłość
  case object SkippedFresh    extends WeatherResult  // odświeżano mniej niż 24h temu
  case class Error(message: String) extends WeatherResult
}
