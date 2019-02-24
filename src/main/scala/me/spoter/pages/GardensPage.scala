package me.spoter.pages

import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, CallbackTo, ScalaComponent}
import me.spoter.components.bootstrap.{Container, Form}
import me.spoter.models.AllotmentGarden
import me.spoter.{Session, SessionTracker, StateXSession}

/**
  *
  */
object GardensPage extends SessionTracker[Unit, Iterable[AllotmentGarden], Unit] {
  private val component = ScalaComponent
    .builder[Unit]("GardensPage")
    .initialState(StateXSession[Iterable[AllotmentGarden]](Seq(), None))
    .render_S { sxs =>
      Container(
        <.h1("Meine Gärten"),
        Form()(
          <.h2("Bitten einloggen!").when(sxs.session.isEmpty),
          sxs.state.toVdomArray(renderGarden).when(sxs.session.isDefined))
      )
    }
    .componentDidMount(trackSession(updateState))
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(): VdomElement = component().vdomElement

  def renderGarden(g: AllotmentGarden): VdomElement = {
    <.div(^.key := g.title.hashCode, g.title)
  }

  private def updateState(s: Session): CallbackTo[Iterable[AllotmentGarden]] = CallbackTo(Seq())

}
