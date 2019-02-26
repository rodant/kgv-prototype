package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap.{Container, Form, NavLink}
import me.spoter.{Session, SessionTracker, StateXSession}

import scala.concurrent.Future

/**
  *
  */
trait ObjectListPage[O <: ListObject] extends SessionTracker[Unit, Iterable[O], Unit] {
  private val initialSession = Session(URI.create("_blank"))
  private val component = ScalaComponent
    .builder[Unit]("OfferingsPage")
    .initialState(StateXSession[Iterable[O]](Seq(), Some(initialSession)))
    .render_S { sxs =>
      Container(
        <.h1(s"Meine $objectsName"),
        Form()(
          <.h2("Bitten einloggen!").when(sxs.session.isEmpty),
          <.h2("Keine Objekte gefunden.").when(sxs.state.isEmpty && sxs.session.getOrElse(initialSession) != initialSession),
          sxs.state.toTagMod(renderListObject).when(sxs.session.isDefined))
      )
    }
    .componentDidMount(trackSessionOn(fetchListObjects))
    .componentWillUnmountConst(trackSessionOff())
    .configure(Reusability.shouldComponentUpdate)
    .build

  protected val objectsUriFragment: String
  protected val objectsName: String

  def apply(): VdomElement = component().vdomElement

  def renderListObject(g: O): VdomElement = {
    NavLink(href = s"#$objectsUriFragment?uri=${g.uri}")(g.title)
  }

  protected def fetchListObjects(s: Session): Future[Iterable[O]]

}

trait ListObject {
  val uri: URI
  val title: String
}
