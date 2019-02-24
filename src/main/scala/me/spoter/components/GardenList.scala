package me.spoter.components

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.vdom.html_<^._

/**
  *
  */
object GardenList {

  case class Props()

  private val component = ScalaComponent
    .builder[Props]("GardenList")
    .render_(<.div("Mein Gerten"))
    .build

  def apply(): VdomElement = component(Props()).vdomElement

}
