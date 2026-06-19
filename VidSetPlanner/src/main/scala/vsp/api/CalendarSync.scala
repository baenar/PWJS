package vsp.api

import vsp.core.{CalendarEventService, CityService}
import vsp.environment.{EnvKeys, EnvLoader}
import vsp.model.CalendarEvent

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import scala.util.{Failure, Success}

// Dwukierunkowa synchronizacja lokalnej bazy z Google Calendar.
// Okno czasowe: od teraz do miesiąca w przód.
//  - czego nie ma lokalnie, a jest na Google -> zapisujemy do bazy,
//  - czego nie ma na Google, a jest lokalnie -> wysyłamy do kalendarza.
object CalendarSync {

  // Model nie trzyma googleId, więc dopasowujemy eventy po naturalnym kluczu:
  // tytuł + czas rozpoczęcia z dokładnością do minuty.
  private def keyOf(event: CalendarEvent): (String, LocalDateTime) = {
    (event.title.trim, event.startTime.truncatedTo(ChronoUnit.MINUTES))
  }

  def syncOnStartup(): Unit = {
    val credentialsPath = EnvLoader.get(EnvKeys.GoogleCalendarKey)
    val calendarAccount = EnvLoader.get(EnvKeys.GoogleCalendarEmail)

    if (credentialsPath == null || calendarAccount == null) {
      println("Synchronizacja Google Calendar pominięta: brak konfiguracji (.env).")
      return
    }

    val from = LocalDateTime.now()
    val to   = from.plusMonths(1)

    GoogleCalendarClient.fetchEvents(credentialsPath, calendarAccount, from, to) match {
      case Failure(ex) =>
        println(s"Synchronizacja Google Calendar nieudana: ${ex.getMessage}")
      case Success(googleEvents) =>
        // Bierzemy tylko lokalne eventy z tego samego okna czasowego.
        val localEvents = CalendarEventService.getAllEvents()
          .filter(e => !e.startTime.isBefore(from) && e.startTime.isBefore(to))

        downloadMissing(googleEvents, localEvents)
        uploadMissing(localEvents, googleEvents, credentialsPath, calendarAccount)
        println("Synchronizacja Google Calendar zakończona.")
    }
  }

  // Google -> lokalna baza
  private def downloadMissing(
    googleEvents: List[CalendarEvent],
    localEvents: List[CalendarEvent]
  ): Unit = {
    val localKeys = localEvents.map(keyOf).toSet
    val missing   = googleEvents.filterNot(g => localKeys.contains(keyOf(g)))

    missing.foreach { event =>
      CityService.resolveCityFromAddress(event.city.name) match {
        case Right(city) =>
          CalendarEventService.addEvent(event.copy(city = city)) match {
            case Right(_)  => println(s"Pobrano z Google: ${event.title}")
            case Left(err) => println(s"Nie zapisano '${event.title}': $err")
          }
        case Left(err) =>
          println(s"Pominięto '${event.title}' (miasto): $err")
      }
    }
  }

  // Lokalna baza -> Google
  private def uploadMissing(
    localEvents: List[CalendarEvent],
    googleEvents: List[CalendarEvent],
    credentialsPath: String,
    calendarAccount: String
  ): Unit = {
    val googleKeys = googleEvents.map(keyOf).toSet
    val missing    = localEvents.filterNot(l => googleKeys.contains(keyOf(l)))

    if (missing.nonEmpty) {
      val results = GoogleCalendarClient.addEvents(credentialsPath, calendarAccount, missing)
      missing.zip(results).foreach {
        case (event, Success(_))  => println(s"Wysłano do Google: ${event.title}")
        case (event, Failure(ex)) => println(s"Nie wysłano '${event.title}': ${ex.getMessage}")
      }
    }
  }
}
