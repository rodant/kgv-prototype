package me.spoter.components

import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import me.spoter.components.bootstrap.Button
import me.spoter.solid_libs.SolidAuth

import scala.scalajs.js

/**
  *
  */
object AuthButton {

  case class Props(popupUri: String, callbackUri: String)

  private val component = ScalaComponent
    .builder[Props]("AuthButton")
    .initialState(false)
    .renderBackend[Backend]
    .componentDidMount(c => c.backend.trackSession(c))
    .build

  class Backend(bs: BackendScope[Props, Boolean]) {
    def render(loggedIn: Boolean): VdomElement = {
      if (!loggedIn)
        Button()(^.onClick --> login)("Einlogen")
      else
        Button()(^.onClick --> logout)("Ausloggen")
    }

    private def login(): Callback = bs.props.map { p =>
      val args = js.Dynamic.literal(popupUri = p.popupUri)
      SolidAuth.popupLogin(args)
      ()
    }

    private def logout(): Callback = bs.props.map { _ =>
      SolidAuth.logout()
      ()
    }

    def trackSession(c: ComponentDidMount[Props, Boolean, Backend]): Callback = Callback {
      SolidAuth.trackSession { s =>
        (if (s != null)
          c.modState(_ => true)
        else
          c.modState(_ => false)).runNow()
      }
    }
  }

  def apply(popupUri: String, callbackUri: String = null): VdomElement = component(Props(popupUri, callbackUri)).vdomElement
}