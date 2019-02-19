package me.spoter.components

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import me.spoter.components.bootstrap.Button

/**
  *
  */
object AuthButton {

  case class Props(popupUri: String, callbackUri: String)

  private val component = ScalaComponent
    .builder[Props]("AuthButton")
    .initialState(false)
    .renderBackend[Backend]
    .build

  class Backend(bs: BackendScope[Props, Boolean]) {
    def render(loggedIn: Boolean): VdomElement = {
      if (!loggedIn)
        Button()(^.onClick --> login)("Einlogen")
      else
        Button()(^.onClick --> logout)("Ausloggen")
    }

    def login(): Callback =  bs.modState(_ => true)

    def logout(): Callback = bs.modState(_ => false)
  }

  def apply(popupUri: String, callbackUri: String = null): VdomElement = component(Props(popupUri, callbackUri)).vdomElement
}