package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.EntityList
import me.spoter.components.bootstrap.{Container, FormControl, Row}
import me.spoter.models.KGVEntity
import me.spoter.{Session, SessionTracker, StateXSession}

case class State(es: Iterable[KGVEntity], addingEntity: Option[KGVEntity] = None)

abstract class Backend(bs: BackendScope[Unit, StateXSession[State]]) {
  private val initialSession = Session(URI.create("_blank"))
  protected val entityUriFragment: String
  protected val entityRenderName: String

  protected def newEntity(): KGVEntity

  def render(sxs: StateXSession[State]): VdomElement = {
    val es = sxs.state.es
    Container(
      Row()(
        <.h1(s"Meine $entityRenderName"),
        renderWhen(sxs.session.isDefined) {
          <.i(^.className := "fas fa-plus-circle fa-2x",
            ^.title := "Neu Anlegen",
            ^.color := "darkseagreen",
            ^.marginLeft := 30.px,
            ^.onClick --> bs.modState(old => old.copy(state = old.state.copy(addingEntity = Option(newEntity())))))
        }
      ),
      renderWhen(sxs.session.isEmpty) {
        <.h2("Bitten einloggen!")
      },
      renderWhen(es.isEmpty && sxs.session.getOrElse(initialSession) != initialSession) {
        <.h2(s"Keine $entityRenderName gefunden.")
      },
      sxs.state.addingEntity.map { e =>
        Row()(
          FormControl(value = e.title, onChange = onChangeName(_))(^.placeholder := "Name"),
          <.div(^.marginTop := 10.px,
            <.i(^.className := "fas fa-check fa-lg",
              ^.title := "Bestätigen",
              ^.color := "darkseagreen",
              ^.marginLeft := 10.px),
            <.i(^.className := "fas fa-times fa-lg",
              ^.title := "Abbrechen",
              ^.color := "red",
              ^.marginLeft := 10.px)
          )
        )
      },
      renderWhen(sxs.session.isDefined) {
        EntityList(entityUriFragment, es)
      }
    )
  }

  private def renderWhen(b: Boolean)(r: => VdomElement): Option[VdomElement] = if (b) Some(r) else None

  private def onChangeName(e: ReactEventFromInput): Callback = {
    e.persist()
    bs.modState(old =>
      old.copy(state =
        old.state.copy(addingEntity =
          old.state.addingEntity.map(_.withNewTitle(e.target.value)))))
  }
}

/**
  * Abstraction over a page showing a list of kgv entities (e.g. garden, offering) conforming to the shape defined through KGVEntity.
  */
trait EntityListPage[E <: KGVEntity, B <: Backend] extends SessionTracker[Unit, State, B] {
  protected val initialSession = Session(URI.create("_blank"))
}
