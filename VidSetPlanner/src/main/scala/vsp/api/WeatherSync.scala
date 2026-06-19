package vsp.api

import vsp.core.CalendarEventService

import java.time.LocalDateTime

// Odświeżanie pogody przy starcie aplikacji.
// Bierzemy wydarzenia z najbliższego tygodnia i aktualizujemy, jeśli
// albo pogoda jest pusta, albo była aktualizowana ponad 24h temu.
object WeatherSync {

  def syncOnStartup(): Unit = {
    val now       = LocalDateTime.now()
    val weekAhead = now.plusDays(7)

    val events = CalendarEventService.getAllEvents()
      .filter(e => !e.startTime.isBefore(now) && e.startTime.isBefore(weekAhead))

    if (events.isEmpty) {
      println("Synchronizacja pogody: brak wydarzeń w nadchodzącym tygodniu.")
      return
    }

    events.foreach { event =>
      WeatherClient.getWeatherByCityAndDate(event.city, event.startTime, event.lastWeatherUpdate) match {
        case WeatherResult.Fetched(weather, temp, at) =>
          val updated = event.copy(
            weather           = Some(weather),
            temperature       = Some(temp),
            lastWeatherUpdate = Some(at)
          )
          CalendarEventService.updateWeather(updated) match {
            case Right(_)  => println(f"Pogoda zaktualizowana: ${event.title} -> $weather, $temp%.1f°C")
            case Left(err) => println(s"Nie zapisano pogody '${event.title}': $err")
          }
        case WeatherResult.SkippedFresh =>
          println(s"Pogoda aktualna (mniej niż 24h): ${event.title}")
        case WeatherResult.SkippedTooFar =>
          println(s"Pogoda pominięta (dalej niż tydzień): ${event.title}")
        case WeatherResult.SkippedPastDate =>
          println(s"Pogoda pominięta (data w przeszłości): ${event.title}")
        case WeatherResult.Error(msg) =>
          println(s"Błąd pogody '${event.title}': $msg")
      }
    }

    println("Synchronizacja pogody zakończona.")
  }
}
