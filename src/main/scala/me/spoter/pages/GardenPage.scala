package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, ScalaComponent}
import me.spoter.models.AllotmentGarden

/**
  * A page showing the data of an allotment garden.
  */
object GardenPage {

  case class Props(uri: URI)

  class Backend(bs: BackendScope[Props, AllotmentGarden]) {
    def render(garden: AllotmentGarden): VdomElement = {
      <.div("Yay!!!")
    }
  }

  private val component = ScalaComponent
    .builder[Props]("OfferingPage")
    .initialState(AllotmentGarden())
    .renderBackend[Backend]
    .build

  def apply(uri: String): VdomElement = component(Props(new URI(uri))).vdomElement
}
