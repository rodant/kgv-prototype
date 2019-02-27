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

  class Backend(bs: BackendScope[Null, String]) {

    def selectAllotment(e: ReactEventFromInput): Callback = bs.setState(e.target.value)

    def render(uri: String): VdomElement = {
      Container(
        <.h1("Kleingarten Berlin"),
        Form()(
          Row()(
            Col() {
              FormControl(as = "select", onChange = selectAllotment _)(
                <.option(^.value := "", "Alle Bezirke"),
                <.option(^.value := s"?uri=${baseUrl}17be10f3-802f-42be-bbd0-bb03be89c812", "Angebot 1"),
                <.option(^.value := s"?uri=${baseUrl}630cedbb-162a-4021-b38c-38cb7b6ed5d7", "Angebot 2"),
                <.option(^.value := "?uri=https://orisha2.solid.community/spoterme/offers/94e1194d-33a9-46de-b45b-100d17fd4236", "Angebot 3")
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
      .initialState("")
      .renderBackend[Backend]
      .build

  def apply(): VdomElement = component(null).vdomElement
}
