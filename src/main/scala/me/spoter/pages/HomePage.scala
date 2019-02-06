package me.spoter.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap._
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

object HomePage {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  private val component =
    ScalaComponent.builder[Null]("HomePage")
      .render_(
        Container(
          <.h1("Spoter.me Kleingarten Berlin"),
          Form(
            Row(
              Col() {
                FormControl(as = "select")(
                  <.option("13dd0a8d-443d-4b22-b7d9-1998b76a458a"),
                  <.option("73691542-b1c6-4db7-9c96-7b173ecc0252"),
                  <.option("89d41980-9d6c-4522-8fc0-598c8bd438a4")
                )
              },
              Col() {
                Button()("Suchen")
              }
            )
          )
        )
      )
      .build

  def apply(): VdomElement = component(null).vdomElement
}
