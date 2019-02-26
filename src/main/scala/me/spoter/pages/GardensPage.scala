package me.spoter.pages

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap.{Container, Form}
import me.spoter.models.AllotmentGarden
import me.spoter.{Session, SessionTracker, StateXSession}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
    .componentDidMount(trackSessionOn(updateState))
    .componentWillUnmountConst(trackSessionOff())
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(): VdomElement = component().vdomElement

  def renderGarden(g: AllotmentGarden): VdomElement = {
    <.div(^.key := g.title.hashCode, g.title)
  }

  private def updateState(s: Session): Future[Iterable[AllotmentGarden]] = {

    Future(Seq())
  }

}
