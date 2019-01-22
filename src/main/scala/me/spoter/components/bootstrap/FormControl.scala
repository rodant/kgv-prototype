package me.spoter.components.bootstrap

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{Children, JsComponent}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  *
  */

object FormControl {

  @JSImport("react-bootstrap", "Form.Control")
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait Props extends js.Object {
    var defaultValue: String = js.native
    var readOnly: Option[Boolean] = js.native
    var plaintext: Option[Boolean] = js.native
  }

  private def props(defaultValue: String, readOnly: Boolean, plaintext: Boolean): Props = {
    val p = (new js.Object).asInstanceOf[Props]
    p.defaultValue = defaultValue
    p.readOnly = if (readOnly) Some(readOnly) else None
    p.plaintext = if (plaintext) Some(plaintext) else None
    p
  }

  val component = JsComponent[Props, Children.None, Null](RawComponent)

  def apply(defaultValue: String, readOnly: Boolean = false, plaintext: Boolean = false): VdomElement =
    component(props(defaultValue, readOnly, plaintext)).vdomElement
}
