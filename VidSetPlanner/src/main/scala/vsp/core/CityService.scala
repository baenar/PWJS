package vsp.core

import vsp.api.GeocodingClient
import vsp.model.City
import vsp.persistence.CityRepository

// Ustala miasto wraz ze współrzędnymi.
// Cache: najpierw tabela cities
// Gdy pudło -> geokodowanie i zapis do cache.
object CityService {

  def resolveCity(name: String): Either[String, City] = {
    CityRepository.findByName(name) match {
      case Some(cached) => Right(cached)
      case None =>
        GeocodingClient.geocode(name) match {
          case Right(geocoded) => cacheCity(geocoded)
          case Left(err)       => Left(err)
        }
    }
  }

  // Dla pełnych adresów ( z Google Calendar) - najpierw geokodujemy
  // adres przez Nominatim, dostajemy samo miasto, a potem trzymamy się cache po nazwie.
  def resolveCityFromAddress(address: String): Either[String, City] = {
    GeocodingClient.geocodeAddress(address) match {
      case Right(geocoded) =>
        CityRepository.findByName(geocoded.name) match {
          case Some(cached) => Right(cached)
          case None         => cacheCity(geocoded)
        }
      case Left(err) => Left(err)
    }
  }

  private def cacheCity(city: City): Either[String, City] = {
    CityRepository.save(city).toEither match {
      case Right(saved) => Right(saved)
      case Left(ex)     => Left(s"Nie udało się zapisać miasta: ${ex.getMessage}")
    }
  }
}
