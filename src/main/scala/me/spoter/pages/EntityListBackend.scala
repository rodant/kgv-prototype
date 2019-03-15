package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.EntityList
import me.spoter.components.bootstrap.{Container, FormControl, Row}
import me.spoter.models.KGVEntity
import me.spoter.{Session, StateXSession}

case class State(es: Iterable[KGVEntity], addingEntity: Option[KGVEntity] = None)

abstract class EntityListBackend(bs: BackendScope[Unit, StateXSession[State]]) {
  private val initialSession = Session(URI.create("_blank"))
  protected val entityUriFragment: String
  protected val entityRenderName: String

  protected def newEntity(): KGVEntity

  protected def createEntity(sxs: StateXSession[State]): Callback

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
      sxs.session.flatMap { _ =>
        sxs.state.addingEntity.map { e =>
          Row()(
            FormControl(value = e.title, onChange = onChangeName(_))(^.placeholder := "Name", ^.autoFocus := true),
            <.div(^.marginTop := 10.px,
              <.i(^.className := "fas fa-check fa-lg",
                ^.title := "Bestätigen",
                ^.color := "darkseagreen",
                ^.marginLeft := 10.px,
                ^.onClick --> bs.state.flatMap[Unit](onCreateGarden)),
              <.i(^.className := "fas fa-times fa-lg",
                ^.title := "Abbrechen",
                ^.color := "red",
                ^.marginLeft := 10.px,
                ^.onClick --> bs.modState(old => old.copy(state = old.state.copy(addingEntity = None))))
            )
          )
        }
      },
      renderWhen(sxs.session.isDefined) {
        EntityList(entityUriFragment, es)
      }
    )
  }

  private def onCreateGarden(sxs: StateXSession[State]): Callback =
    if (sxs.state.addingEntity.get.title.isEmpty) Callback()
    else createEntity(sxs)

  private def renderWhen(b: Boolean)(r: => VdomElement): Option[VdomElement] = if (b) Some(r) else None

  private def onChangeName(e: ReactEventFromInput): Callback = {
    e.persist()
    bs.modState(old =>
      old.copy(state =
        old.state.copy(addingEntity =
          old.state.addingEntity.map(_.withNewTitle(e.target.value)))))
  }
}