package me.spoter.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap._
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

object HomePage {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  val baseUrl = "https://orisha1.solid.community/spoterme/offers/"

  class Backend(bs: BackendScope[Null, String]) {

    def selectAllotment(e: ReactEventFromInput): Callback = bs.setState(e.target.value)

    def render(uri: String): VdomElement = {
      Container(
        <.h1("spoter.ME Kleingarten Berlin"),
        Form(
          Row()(
            Col() {
              FormControl(as = "select", onChange = selectAllotment _)(
                <.option(s"${baseUrl}17be10f3-802f-42be-bbd0-bb03be89c812"),
                <.option(s"${baseUrl}630cedbb-162a-4021-b38c-38cb7b6ed5d7"),
                //<.option("89d41980-9d6c-4522-8fc0-598c8bd438a4")
              )
            },
            Col() {
              Button(href = "#offering?uri=" + uri)("Suchen")
            }
          )
        )
      )
    }
  }

  private val component =
    ScalaComponent.builder[Null]("HomePage")
      .initialState(s"${baseUrl}17be10f3-802f-42be-bbd0-bb03be89c812")
      .renderBackend[Backend]
      .build

  def apply(): VdomElement = component(null).vdomElement
}
