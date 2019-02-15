package me.spoter.components

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Leaflet facade.
  */
@js.native
@JSImport("leaflet", JSImport.Default)
object Leaflet extends js.Object {
  def map(id: String): LeafletMap = js.native

  def latLng(lat: Double, lng: Double): js.Object = js.native

  def tileLayer(urlTemplate: String, ops: js.Dynamic = js.Dynamic.literal()): js.Dynamic = js.native
}

@js.native
@JSImport("leaflet", "Map")
class LeafletMap extends js.Object {
  def setView(center: js.Object, zoom: Int): LeafletMap = js.native
}