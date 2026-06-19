package vsp.persistence

import vsp.model.City
import vsp.persistence.slickdb.SlickCityRepository

import scala.util.Try

object CityRepository {

  def findAll(): List[City] = {
    SlickCityRepository.findAll().getOrElse(List())
  }

  def findByName(name: String): Option[City] = {
    SlickCityRepository.findByName(name).toOption.flatten
  }

  def save(city: City): Try[City] = {
    SlickCityRepository.save(city)
  }
}
