package me.spoter.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap._
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

object HomePage {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  private val baseUrl = "https://orisha1.solid.community/spoterme/offers/"

  private val uriParamLeft = "?district="
  private val initialState = uriParamLeft + "*"

  class Backend(bs: BackendScope[Null, String]) {

    def selectAllotment(e: ReactEventFromInput): Callback = bs.setState(e.target.value)

    def render(uri: String): VdomElement = {
      Container(
        <.h1("Kleingarten Berlin"),
        Form()(
          Row()(
            Col() {
              FormControl(as = "select", onChange = selectAllotment _)(
                <.option(^.value := initialState, "Alle Bezirke"),
                <.option(^.value := s"${uriParamLeft}mitte", "Mitte"),
                <.option(^.value := s"${uriParamLeft}friedrichshain-kreuzberg", "Friedrichshain-Kreuzberg"),
                <.option(^.value := s"${uriParamLeft}pankow", "Pankow"),
                <.option(^.value := s"$uriParamLeft...", "...")
              )
            },
            Col() {
              Button(href = "#offerings" + uri)("Suchen")
            }
          )
        )
      )
    }
  }

  private val component =
    ScalaComponent.builder[Null]("HomePage")
      .initialState(initialState)
      .renderBackend[Backend]
      .build

  def apply(): VdomElement = component(null).vdomElement
}
