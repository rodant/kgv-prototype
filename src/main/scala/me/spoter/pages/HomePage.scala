package me.spoter.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.AuthButton
import scalacss.ScalaCssReact._
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

object HomePage {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  import CssSettings._

  object Style extends StyleSheet.Inline {

    import dsl._

    val content: StyleA = style(textAlign.center,
      fontSize(30.px),
      minHeight(450.px),
      paddingTop(40.px))
  }

  private val component =
    ScalaComponent.builder[Null]("HomePage")
      .render_(
        <.div(Style.content, "Spoter.me Kleingarten Berlin",
          <.div(AuthButton("popup.html"))))
      .build

  def apply(): VdomElement = component(null).vdomElement
}
