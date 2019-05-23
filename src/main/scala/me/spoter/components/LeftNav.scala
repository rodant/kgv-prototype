package me.spoter.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.routes.Item
import scalacss.ScalaCssReact._
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

object LeftNav {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  import CssSettings._


  object Style extends StyleSheet.Inline {

    import dsl._

    val container: StyleA = style(display.flex,
      flexDirection.column,
      listStyle := "none",
      padding.`0`)

    val menuItem: Boolean => StyleA = styleF.bool { selected =>
      styleS(
        lineHeight(48.px),
        padding :=! "0 25px",
        cursor.pointer,
        textDecoration := "none",
        mixinIfElse(selected)(color.red, fontWeight._500)(
          color.black,
          &.hover(color(c"#555555"), backgroundColor(c"#ecf0f1"))
        )
      )
    }
  }

  case class Props(menus: Vector[Item],
                   selectedPage: Item,
                   ctrl: RouterCtl[Item])

  private implicit val currentPageReuse: Reusability[Item] = Reusability.by_==[Item]
  private implicit val propsReuse: Reusability[Props] = Reusability.by((_: Props).selectedPage)

  private val component = ScalaComponent
    .builder[Props]("LeftNav")
    .render_P { P =>
      <.ul(
        Style.container,
        P.menus.toTagMod(
          item =>
            <.li(^.key := item.title,
              Style.menuItem(item == P.selectedPage),
              item.title,
              P.ctrl setOnClick item)
        )
      )
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(props: Props): Unmounted[Props, Unit, Unit] = component(props)

}
