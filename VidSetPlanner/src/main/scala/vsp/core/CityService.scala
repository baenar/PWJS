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
          case Right(geocoded) =>
            CityRepository.save(geocoded).toEither match {
              case Right(saved) => Right(saved)
              case Left(ex)     => Left(s"Nie udało się zapisać miasta: ${ex.getMessage}")
            }
          case Left(err) => Left(err)
        }
    }
  }
}
