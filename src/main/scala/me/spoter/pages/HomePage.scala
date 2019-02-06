package me.spoter.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap._
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

import scala.scalajs.js

object HomePage {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  class Backend(bs: BackendScope[Null, String]) {
    def render(id: String): VdomElement = {
      Container(
        <.h1("Spoter.me Kleingarten Berlin"),
        Form(
          Row(
            Col() {
              FormControl(as = "select", onChange = onSelection)(
                <.option("13dd0a8d-443d-4b22-b7d9-1998b76a458a"),
                <.option("73691542-b1c6-4db7-9c96-7b173ecc0252"),
                <.option("89d41980-9d6c-4522-8fc0-598c8bd438a4")
              )
            },
            Col() {
              Button(onClick = selectAllotment)("Suchen")
            }
          )
        )
      )
    }
  }

  private val component =
    ScalaComponent.builder[Null]("HomePage")
      .initialState("13dd0a8d-443d-4b22-b7d9-1998b76a458a")
      .renderBackend[Backend]
      .build

  def apply(): VdomElement = component(null).vdomElement

  val onSelection: js.Dynamic => Callback = id => Callback {
    println(s"Clicked: $id")
  }

  def selectAllotment: Callback = Callback {

  }
}
