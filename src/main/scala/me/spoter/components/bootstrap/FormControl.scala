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
    var as: String = js.native
    var defaultValue: String = js.native
    var readOnly: Option[Boolean] = js.native
    var plaintext: Option[Boolean] = js.native
    var rows: Int = js.native
  }

  private def props(as: String, defaultValue: String, readOnly: Boolean, plaintext: Boolean, rows: Int): Props = {
    val p = (new js.Object).asInstanceOf[Props]
    p.as = as
    p.defaultValue = defaultValue
    p.readOnly = if (readOnly) Some(readOnly) else None
    p.plaintext = if (plaintext) Some(plaintext) else None
    p.rows = rows
    p
  }

  val component = JsComponent[Props, Children.None, Null](RawComponent)

  /**
    *
    * @param as input | textarea | elementType
    * @param defaultValue
    * @param readOnly
    * @param plaintext
    * @param rows
    * @return
    */
  def apply(as: String = "input",
            defaultValue: String,
            readOnly: Boolean = false,
            plaintext: Boolean = false,
            rows: Int = 3): VdomElement =
    component(props(as, defaultValue, readOnly, plaintext, rows)).vdomElement
}
