package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap.{Container, Form, FormControl, Row}
import me.spoter.components.{EntityList, _}
import me.spoter.models.KGVEntity
import me.spoter.{Session, StateXSession}

case class State(es: Iterable[KGVEntity], newEntity: Option[KGVEntity] = None)

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
            ^.onClick --> bs.modState(old => old.copy(state = old.state.copy(newEntity = Option(newEntity())))))
        }
      ),
      renderWhen(sxs.session.isEmpty) {
        <.h2("Bitten einloggen!")
      },
      renderWhen(es.isEmpty && sxs.session.getOrElse(initialSession) != initialSession) {
        <.h2(s"Keine $entityRenderName gefunden.")
      },
      sxs.session.flatMap { _ =>
        sxs.state.newEntity.map { e =>
          Row()(
            Form(validated = true)(^.noValidate := true)(
              FormControl(value = e.name.value, onChange = onChangeName(_))
              (^.placeholder := "Name", ^.autoFocus := true, ^.onKeyUp ==> handleKey, ^.required := true, ^.maxLength := 40),
              <.div(^.marginTop := 10.px,
                <.i(^.className := "fas fa-check fa-lg",
                  ^.title := "Bestätigen",
                  ^.color := "darkseagreen",
                  ^.marginLeft := 10.px,
                  ^.onClick --> onConfirm),
                <.i(^.className := "fas fa-times fa-lg",
                  ^.title := "Abbrechen",
                  ^.color := "red",
                  ^.marginLeft := 10.px,
                  ^.onClick --> onCancel())
              )
            )
          )
        }
      },
      renderWhen(sxs.session.isDefined) {
        EntityList(entityUriFragment, es)
      }
    )
  }

  private def onConfirm(): Callback = bs.state.flatMap[Unit](onCreateGarden)

  private def onCancel(): Callback = bs.modState(old => old.copy(state = old.state.copy(newEntity = None)))

  private def onCreateGarden(sxs: StateXSession[State]): Callback =
    if (sxs.state.newEntity.get.name.value.isEmpty) Callback()
    else createEntity(sxs)

  private def renderWhen(b: Boolean)(r: => VdomElement): Option[VdomElement] = if (b) Some(r) else None

  private def onChangeName(e: ReactEventFromInput): Callback = {
    e.persist()
    bs.modState(old =>
      old.copy(state =
        old.state.copy(newEntity =
          old.state.newEntity.map(g => g.withNewTitle(g.name.copy(value = e.target.value))))))
  }

  private def handleKey(e: ReactKeyboardEvent): Callback =
    handleEsc(onCancel()).orElse(handleEnter(onConfirm())).orElse(ignoreKey)(e.keyCode)
}