package me.spoter.components.bootstrap

import japgolly.scalajs.react.CtorType.ChildArg
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.{Children, JsComponent}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
  *
  */

object Col {

  @JSImport("react-bootstrap", "Col")
  @js.native
  object RawComponent extends js.Object

  @js.native
  trait Props extends js.Object {
    var xl: js.UndefOr[String] = js.native
    var lg: js.UndefOr[String] = js.native
    var md: js.UndefOr[String] = js.native
    var sm: js.UndefOr[String] = js.native
    var xs: js.UndefOr[String] = js.native
  }

  private def props(xl: String, lg: String, md: String, sm: String, xs: String): Props = {
    val p = (new js.Object).asInstanceOf[Props]
    p.xl = if (xl == "") js.undefined else xl
    p.lg = if (lg == "") js.undefined else lg
    p.md = if (md == "") js.undefined else md
    p.sm = if (sm == "") js.undefined else sm
    p.xs = if (xs == "") js.undefined else xs
    p
  }

  val component = JsComponent[Props, Children.Varargs, Null](RawComponent)

  /**
    *
    * @param xl
    * @param lg
    * @param md
    * @param sm
    * @param xs
    * @param children
    * @return
    */
  def apply(xl: String = "",
            lg: String = "",
            md: String = "",
            sm: String = "",
            xs: String = "")(children: ChildArg*): VdomElement =
    component(props(xl, lg, md, sm, xs))(children: _*).vdomElement
}
