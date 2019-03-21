package me.spoter.services

import me.spoter.models.{Address, Location}
import org.scalajs.dom.ext.Ajax

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

/**
  *
  */
object GeoCodingService {
  def locationFrom(address: Address): Future[Location] = {
    val searchUri = "https://nominatim.openstreetmap.org/search"
    val searchQuery = js.Dynamic.global.encodeURI(
      s"$searchUri?q=${address.streetAndNumber.value},+${address.postalCode.value},+${address.region.value}")
    val params = "&countrycodes=de&format=json&addressdetails=1"
    Ajax.get(
      url = s"$searchQuery$params",
      timeout = 10000,
      responseType = "json")
      .map {
        case res if res.status == 200 =>
          val placeOpt = res.response.asInstanceOf[js.Array[js.Dynamic]].headOption
          placeOpt.fold(Location())(p => Location(p.lat.toString.toDouble, p.lon.toString.toDouble))
      }
  }
}
