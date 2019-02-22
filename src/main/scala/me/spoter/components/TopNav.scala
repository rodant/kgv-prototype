package me.spoter.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.component.builder.Lifecycle.ComponentDidMount
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.solid.Value
import me.spoter.models.Menu
import me.spoter.routes.AppRouter.AppPage
import me.spoter.solid_libs.SolidAuth
import scalacss.ScalaCssReact._
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

object TopNav {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  import CssSettings._


  object Style extends StyleSheet.Inline {

    import dsl._

    val navMenu: StyleA = style(
      display.flex,
      flexFlow := "row-reverse",
      alignItems.center,
      backgroundColor(c"#F2706D"),
      margin.`0`,
      listStyle := "none")

    val menuItem: Boolean => StyleA = styleF.bool { selected =>
      styleS(
        padding(20.px),
        fontSize(1.5.em),
        cursor.pointer,
        color(c"rgb(244, 233, 233)"),
        mixinIfElse(selected)(backgroundColor(c"#E8433F"), fontWeight._500)(
          &.hover(backgroundColor(c"#B6413E")))
      )
    }
  }

  case class Props(menus: Vector[Menu],
                   selectedPage: AppPage,
                   ctrl: RouterCtl[AppPage])

  private implicit val currentPageReuse: Reusability[AppPage] = Reusability.by_==[AppPage]
  private implicit val propsReuse: Reusability[Props] = Reusability.by((_: Props).selectedPage)

  private val component = ScalaComponent
    .builder[Props]("TopNav")
    .initialState(false)
    .render_PS { (props, loggedIn) =>
      <.header(
        <.nav(
          <.ul(
            Style.navMenu,
            props.menus.toTagMod { item =>
              <.li(
                ^.key := item.name,
                Style.menuItem(item.route.getClass == props.selectedPage.getClass),
                item.name,
                props.ctrl setOnClick item.route
              )
            },
            <.li(^.id := "login-button", ^.className := "ui-elem",
              AuthButton("https://solid.community/common/popup.html", loggedIn = loggedIn)),
            <.li(^.id := "logged-in-user", ^.className := "ui-elem", Value("user.name")).when(loggedIn)
          )
        )
      )
    }
    .componentDidMount(c => trackSession(c))
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(props: Props): Unmounted[Props, Boolean, Unit] = component(props)

  def trackSession(c: ComponentDidMount[Props, Boolean, Unit]): Callback = Callback {
    SolidAuth.trackSession { s =>
      (if (s != null)
        c.modState(_ => true)
      else
        c.modState(_ => false)).runNow()
    }
  }

}
