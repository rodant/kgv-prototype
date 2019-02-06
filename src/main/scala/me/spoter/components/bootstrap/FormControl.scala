package me.spoter.components.bootstrap

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{Callback, Children, JsComponent}

import scala.scalajs.js
import scala.scalajs.js.UndefOr
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
    var defaultValue: js.UndefOr[String] = js.native
    var value: js.UndefOr[String] = js.native
    var readOnly: Boolean = js.native
    var plaintext: Boolean = js.native
    var rows: Int = js.native
    var onChange: js.UndefOr[js.Function1[js.Dynamic, Callback]] = js.native
  }

  private def props(as: String, defaultValue: String, value: String, readOnly: Boolean, plaintext: Boolean, rows: Int, onChange: js.Dynamic => Callback): Props = {
    import js.JSConverters._
    val p = (new js.Object).asInstanceOf[Props]
    p.as = as
    p.defaultValue = Option(defaultValue).orUndefined
    p.value = Option(value).orUndefined
    p.readOnly = readOnly
    p.plaintext = plaintext
    p.rows = rows
    p.onChange = UndefOr.any2undefOrA(onChange)
    p
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

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
            defaultValue: String = null,
            value: String = null,
            readOnly: Boolean = false,
            plaintext: Boolean = false,
            rows: Int = 3,
            onChange: js.Dynamic => Callback = _ => Callback())
           (children: ChildArg*): VdomElement =
    component(props(as, defaultValue, value, readOnly, plaintext, rows, onChange))(children: _*).vdomElement
}
