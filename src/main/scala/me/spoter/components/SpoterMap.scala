package me.spoter.components

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ScalaComponent}

import scala.scalajs.js

/**
  * Component display a Leaflet map.
  */
object SpoterMap {

  case class Props(lat: Double, lng: Double)

  class Backend($: BackendScope[Props, Unit]) {
    private lazy val map = Leaflet.map("map")
    private lazy val marker = Leaflet.marker(Leaflet.latLng(0, 0)).addTo(map)

    def centerMap(props: Props): Callback = Callback {
      val center = Leaflet.latLng(props.lat, props.lng)
      map.setView(center, 16)
      marker.setLatLng(center)
    }

    def render(props: Props): VdomElement = {
      <.div(^.id := "map", ^.height := 100.pct)
    }

    def renderMap(props: Props): Callback = Callback {
      centerMap(props)

      val credits = "Map data &copy; <a href=\"https://www.openstreetmap.org/\">OpenStreetMap</a> " +
        "contributors, <a href=\"https://creativecommons.org/licenses/by-sa/2.0/\">CC-BY-SA</a>, " +
        "Imagery © <a href=\"https://www.mapbox.com/\">Mapbox</a>"

      Leaflet.tileLayer(
        "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}",
        js.Dynamic.literal(
          attribution = credits,
          maxZoom = 20,
          id = "mapbox.streets",
          accessToken = "pk.eyJ1Ijoicm9kYW50NjgiLCJhIjoiY2pzNXdmMHBkMDN1NzQzcWNjZWprOG0xMyJ9.I7FPD7O6HS03uDeh5v1vqg"
        )
      ).addTo(map)
    }
  }

  private val component = ScalaComponent
    .builder[Props]("SpoterMap")
    .renderBackend[Backend]
    .componentDidMount(c => c.backend.renderMap(c.props))
    .componentWillReceiveProps(c => c.backend.centerMap(c.nextProps))
    .build

  def apply(lat: Double, lng: Double): VdomElement = component(Props(lat, lng)).vdomElement
}
