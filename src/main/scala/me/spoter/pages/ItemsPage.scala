package me.spoter.pages

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.LeftNav
import me.spoter.routes.Item
import scalacss.ScalaCssReact._
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

object ItemsPage {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  import CssSettings._

  object Style extends StyleSheet.Inline {

    import dsl._

    val container: StyleA = style(display.flex, minHeight(600.px))

    val nav: StyleA =
      style(width(190.px), borderRight :=! "1px solid rgb(223, 220, 220)")

    val content: StyleA = style(padding(30.px))
  }

  private val component = ScalaComponent
    .builder[Props]("ItemsPage")
    .render_P { P =>
      <.div(
        Style.container,
        <.div(Style.nav,
          LeftNav(LeftNav.Props(Item.menu, P.selectedPage, P.ctrl))),
        <.div(Style.content, P.selectedPage.render())
      )
    }
    .build

  case class Props(selectedPage: Item, ctrl: RouterCtl[Item])

  def apply(props: Props): VdomElement = component(props).vdomElement

}
