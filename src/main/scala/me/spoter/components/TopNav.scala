package me.spoter.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap._
import me.spoter.components.solid.Value
import me.spoter.{SessionTracker, StateXSession}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TopNav extends SessionTracker[Unit, Unit, Unit] {
  private val component = ScalaComponent
    .builder[Unit]("TopNav")
    .initialState(StateXSession((), None))
    .render_S(render)
    .componentDidMount(trackSessionOn(_ => Future()))
    .componentWillUnmountConst(trackSessionOff())
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(): Unmounted[Unit, StateXSession[Unit], Unit] = component()

  private def render(stateXSession: StateXSession[Unit]): VdomElement = {
    val loggedIn = stateXSession.session.isDefined
    NavBar(expand = "lg")(^.backgroundColor := "darkseagreen")(
      NavBarBrand("#")("spoter.ME KGV"),
      NavBarToggle()(^.aria.controls := "basic-navbar-nav"),
      NavBarCollapse()(^.id := "basic-navbar-nav")(
        Nav()(^.className := "mr-auto")(
          NavLink(href = "#gardens")("Meine Gärten"),
          NavLink(href = "#offerings")("Meine Gartenangebote")),
        Nav()(^.id := "login-button",
          NavBarText()(^.id := "logged-in-user", ^.className := "ui-elem", Value("user.name")).when(loggedIn),
          <.div(^.id := "login-button", AuthButton("https://solid.community/common/popup.html", loggedIn = loggedIn)))))
  }
}
