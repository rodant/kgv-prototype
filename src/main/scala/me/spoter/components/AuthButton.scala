package me.spoter.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.VdomElement

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  * Wrapper for Solid Login React component.
  */
object AuthButton {

  @JSImport("@solid/react", "AuthButton")
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait Props extends js.Object {
    var popup: String = js.native
  }

  private def props(popup: String): Props = {
    val p = (new js.Object).asInstanceOf[Props]
    p.popup = popup
    p
  }

  val component = JsComponent[Props, Children.None, Null](RawComponent)

  def apply(popup: String): VdomElement = component(props(popup)).vdomElement
}
