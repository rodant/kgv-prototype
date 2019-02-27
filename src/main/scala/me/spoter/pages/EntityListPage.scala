package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.EntityList
import me.spoter.components.bootstrap.Container
import me.spoter.models.KGVEntity
import me.spoter.{Session, SessionTracker, StateXSession}

import scala.concurrent.Future

/**
  * Abstraction over a page showing a list of kgv entities (e.g. garden, offering) conforming to the shape defined through KGVEntity.
  */
trait EntityListPage[E <: KGVEntity] extends SessionTracker[Unit, Iterable[E], Unit] {
  protected val entityUriFragment: String
  protected val componentName: String
  protected val entityRenderName: String

  private val initialSession = Session(URI.create("_blank"))
  private val component = ScalaComponent
    .builder[Unit](componentName)
    .initialState(StateXSession[Iterable[E]](Seq(), Some(initialSession)))
    .render_S { sxs =>
      Container(
        <.h1(s"Meine $entityRenderName"),
        renderWhen(sxs.session.isEmpty) {
          <.h2("Bitten einloggen!")
        },
        renderWhen(sxs.state.isEmpty && sxs.session.getOrElse(initialSession) != initialSession) {
          <.h2(s"Keine $entityRenderName gefunden.")
        },
        renderWhen(sxs.session.isDefined) {
          EntityList(entityUriFragment, sxs.state)
        }
      )
    }
    .componentDidMount(trackSessionOn(fetchEntities))
    .componentWillUnmountConst(trackSessionOff())
    .configure(Reusability.shouldComponentUpdate)
    .build

  protected def fetchEntities(s: Session): Future[Iterable[E]]

  def apply(): VdomElement = component().vdomElement

  private def renderWhen(b: Boolean)(r: => VdomElement): Option[VdomElement] = if (b) Some(r) else None
}
