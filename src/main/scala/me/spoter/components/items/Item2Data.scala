package me.spoter.components.items

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Item2Data {

  private val component =
    ScalaComponent.builder.static("Item2")(<.div("This is Item2 Page ")).build

  def apply(): VdomElement = component().vdomElement
}
