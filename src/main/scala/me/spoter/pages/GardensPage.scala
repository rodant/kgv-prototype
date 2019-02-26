package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap.{Container, Form, NavLink}
import me.spoter.models.AllotmentGarden
import me.spoter.services.GardenService
import me.spoter.{Session, SessionTracker, StateXSession}

import scala.concurrent.Future

/**
  *
  */
object GardensPage extends SessionTracker[Unit, Iterable[AllotmentGarden], Unit] {
  private val initialSession = Session(URI.create("_blank"))
  private val component = ScalaComponent
    .builder[Unit]("GardensPage")
    .initialState(StateXSession[Iterable[AllotmentGarden]](Seq(), Some(initialSession)))
    .render_S { sxs =>
      Container(
        <.h1("Meine Gärten"),
        Form()(
          <.h2("Bitten einloggen!").when(sxs.session.isEmpty),
          <.h2("Keine Objekte gefunden.").when(sxs.state.isEmpty && sxs.session.getOrElse(initialSession) != initialSession),
          sxs.state.toTagMod(renderGarden).when(sxs.session.isDefined))
      )
    }
    .componentDidMount(trackSessionOn(fetchGardens))
    .componentWillUnmountConst(trackSessionOff())
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(): VdomElement = component().vdomElement

  def renderGarden(g: AllotmentGarden): VdomElement = {
    NavLink(href = s"#gardens?uri=${g.uri}")(g.title)
  }

  private def fetchGardens(s: Session): Future[Iterable[AllotmentGarden]] = GardenService.fetchGardensBy(s)

}
