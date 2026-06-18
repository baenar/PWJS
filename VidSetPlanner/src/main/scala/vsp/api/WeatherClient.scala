package vsp.api

import org.json.JSONObject
import vsp.model.City

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import scala.util.{Failure, Success, Try}

object WeatherClient {

  private val ForecastUrl     = "https://api.open-meteo.com/v1/forecast"
  private val httpClient      = HttpClient.newHttpClient()
  private val MaxForecastDays = 7  // dalej niż tydzień API nie pozwala

  // Pobiera pogodę dla miasta i daty, ale tylko, gdy to uzasadnione:
  //  - data w przeszłości             -> SkippedPastDate
  //  - data dalej niż tydzień         -> SkippedTooFar
  //  - odświeżano mniej niż 24h temu  -> SkippedFresh
  //  - błąd sieci/parsowania          -> Error
  // W pozostałych przypadkach realnie odpytuje Open-Meteo.
  def getWeatherByCityAndDate(
    city: City,
    date: LocalDateTime,
    lastUpdate: Option[LocalDateTime]
  ): WeatherResult = {
    val today      = LocalDate.now()
    val targetDate = date.toLocalDate

    if (targetDate.isBefore(today)) {
      WeatherResult.SkippedPastDate
    } else if (targetDate.isAfter(today.plusDays(MaxForecastDays))) {
      WeatherResult.SkippedTooFar
    } else if (recentlyUpdated(lastUpdate)) {
      WeatherResult.SkippedFresh
    } else {
      fetchForecast(city, targetDate)
    }
  }

  private def recentlyUpdated(lastUpdate: Option[LocalDateTime]): Boolean = {
    lastUpdate.exists(ts => ts.isAfter(LocalDateTime.now().minusHours(24)))
  }

  private def fetchForecast(city: City, date: LocalDate): WeatherResult = {
    val day = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val url = s"$ForecastUrl?latitude=${city.latitude}&longitude=${city.longitude}" +
      s"&daily=weathercode,temperature_2m_max&timezone=auto&start_date=$day&end_date=$day"

    val attempt = Try {
      val request  = HttpRequest.newBuilder().uri(URI.create(url)).GET().build()
      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
      parseForecast(response.body())
    }

    attempt match {
      case Success(Some((weather, temp))) =>
        WeatherResult.Fetched(weather, temp, LocalDateTime.now())
      case Success(None) =>
        WeatherResult.Error("Brak danych pogodowych w odpowiedzi API")
      case Failure(ex) =>
        WeatherResult.Error(s"Błąd pobierania pogody: ${ex.getMessage}")
    }
  }

  // Zwraca (opis, temperatura) albo None, gdy odpowiedź nie zawiera danych.
  private def parseForecast(body: String): Option[(String, Double)] = {
    val json = new JSONObject(body)
    if (!json.has("daily")) {
      return None
    }
    val daily = json.getJSONObject("daily")
    val codes = daily.getJSONArray("weathercode")
    val temps = daily.getJSONArray("temperature_2m_max")
    if (codes.length() == 0 || temps.length() == 0) {
      None
    } else {
      val code = codes.getInt(0)
      val temp = temps.getDouble(0)
      Some((WeatherCode.describe(code), temp))
    }
  }
}
