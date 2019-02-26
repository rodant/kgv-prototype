package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap.{Container, Form, NavLink}
import me.spoter.models.AllotmentOffering
import me.spoter.services.OfferingService
import me.spoter.{Session, SessionTracker, StateXSession}

import scala.concurrent.Future

/**
  *
  */
object OfferingsPage extends SessionTracker[Unit, Iterable[AllotmentOffering], Unit] {
  private val initialSession = Session(URI.create("_blank"))
  private val component = ScalaComponent
    .builder[Unit]("OfferingsPage")
    .initialState(StateXSession[Iterable[AllotmentOffering]](Seq(), Some(initialSession)))
    .render_S { sxs =>
      Container(
        <.h1("Meine Gartenangebote"),
        Form()(
          <.h2("Bitten einloggen!").when(sxs.session.isEmpty),
          <.h2("Keine Objekte gefunden.").when(sxs.state.isEmpty && sxs.session.getOrElse(initialSession) != initialSession),
          sxs.state.toTagMod(renderListObject).when(sxs.session.isDefined))
      )
    }
    .componentDidMount(trackSessionOn(fetchGardens))
    .componentWillUnmountConst(trackSessionOff())
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(): VdomElement = component().vdomElement

  def renderListObject(g: AllotmentOffering): VdomElement = {
    NavLink(href = s"#offerings?uri=${g.uri}")(g.title)
  }

  private def fetchGardens(s: Session): Future[Iterable[AllotmentOffering]] = OfferingService.fetchOfferingsBy(s)

}
