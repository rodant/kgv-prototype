package me.spoter.components

import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ReactEventFromInput, ReactKeyboardEvent, ScalaComponent}
import me.spoter.components.bootstrap.{FormControl, Row}
import me.spoter.models.Area

object AreaComponent {

  case class Props(area: Area, updateHandler: Option[Area => Callback])

  case class State(area: Area, editing: Boolean = false, workingCopy: Area)

  class Backend(bs: BackendScope[Props, State]) {
    def render(props: Props, state: State): VdomNode = {
      val area = if (!state.editing) props.area else state.workingCopy
      if (!state.editing || props.updateHandler.isEmpty) {
        FormControl(
          value = s"${area.a} m²",
          readOnly = true,
          plaintext = true)(^.onClick --> bs.modState(_.copy(editing = true)),
          ^.onFocus ==> (_ => bs.modState(_.copy(editing = true))))
      } else {
        WithConfirmAndCancel(() => confirm(), () => cancel()) {
          Row()(
            FormControl(
              value = s"${area.a}",
              `type` = "number",
              onChange = fieldUpdater((v, a) => a.copy(a = v))(_)
            )(^.min := 0, ^.autoFocus := true, ^.onKeyUp ==> handleKey)
          )
        }
      }
    }

    private def handleKey(e: ReactKeyboardEvent): Callback =
      handleEsc(cancel).orElse(handleEnter(confirm)).orElse(ignoreKey)(e.keyCode)

    private def cancel(): Callback = bs.modState(old => old.copy(editing = false, workingCopy = old.area))

    private def confirm(): Callback = {
      for {
        handler <- bs.props.map(_.updateHandler)
        area <- bs.state.map(_.area)
        newArea <- bs.state.map(_.workingCopy)
        _ <- if (area != newArea) handler.fold(Callback.empty)(_ (newArea)) else Callback.empty
          .flatMap(_ => bs.modState(old => old.copy(area = newArea, editing = false)))
      } yield ()
    }

    private def fieldUpdater(transform: (Double, Area) => Area)(e: ReactEventFromInput): Callback = {
      e.persist()
      val value = if (e.target.value.isEmpty) 0 else e.target.value.toDouble
      bs.modState(old => old.copy(workingCopy = transform(value, old.workingCopy)))
    }

  }

  private def stateFromProps(props: Props): State = State(area = props.area, workingCopy = props.area)

  private val component = ScalaComponent
    .builder[Props]("AreaComponent")
    .initialStateFromProps(stateFromProps)
    .renderBackend[Backend]
    .componentWillReceiveProps(c => c.setState(stateFromProps(c.nextProps)))
    .build

  def apply(area: Area, updateHandler: Option[Area => Callback] = None): VdomElement =
    component(Props(area, updateHandler)).vdomElement
}
