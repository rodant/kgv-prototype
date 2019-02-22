package me.spoter.components

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}

import scala.scalajs.js

/**
  * Component display a Leaflet map.
  */
object SpoterMap {

  case class Props(lat: Double, lng: Double)

  private val component = ScalaComponent
    .builder[Props]("SpoterMap")
    .render_(<.div(^.id := "map", ^.height := 100.pct))
    .componentDidUpdate(c => renderMap(c.currentProps.lat, c.currentProps.lng))
    .build

  def apply(lat: Double, lng: Double): VdomElement = component(Props(lat, lng)).vdomElement

  def renderMap(lat: Double, lng: Double): Callback = Callback {
    val credits = "Map data &copy; <a href=\"https://www.openstreetmap.org/\">OpenStreetMap</a> " +
      "contributors, <a href=\"https://creativecommons.org/licenses/by-sa/2.0/\">CC-BY-SA</a>, " +
      "Imagery © <a href=\"https://www.mapbox.com/\">Mapbox</a>"

    val center = Leaflet.latLng(lat, lng)
    val map = Leaflet.map("map").setView(center, 16)
    Leaflet.tileLayer(
      "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}",
      js.Dynamic.literal(
        attribution = credits,
        maxZoom = 20,
        id = "mapbox.streets",
        accessToken = "pk.eyJ1Ijoicm9kYW50NjgiLCJhIjoiY2pzNXdmMHBkMDN1NzQzcWNjZWprOG0xMyJ9.I7FPD7O6HS03uDeh5v1vqg"
      )
    ).addTo(map)

    Leaflet.marker(center).addTo(map)
  }
}
