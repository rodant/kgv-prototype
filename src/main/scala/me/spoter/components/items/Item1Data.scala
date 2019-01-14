package me.spoter.components.items

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Item1Data {

  private val component =
    ScalaComponent.builder.static("Item1")(<.div("This is Item1 Page ")).build

  def apply(): VdomElement = component().vdomElement
}
