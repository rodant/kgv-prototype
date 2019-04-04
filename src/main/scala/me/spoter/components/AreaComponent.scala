package me.spoter.components

import com.payalabs.scalajs.react.bridge.WithPropsAndTagsMods
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{Callback, ReactEventFromInput, ReactKeyboardEvent, ScalaComponent}
import me.spoter.components.bootstrap.FormControl
import me.spoter.models.Area

object AreaComponent {

  case class Props(area: Area, updateHandler: Option[Area => Callback] = None)

  case class State(area: Area, editing: Boolean = false, workingCopy: Area)

  class Backend(bs: BackendScope[Props, State]) {
    def render(props: Props, state: State): WithPropsAndTagsMods = {
      val area = if (!state.editing) props.area else state.workingCopy
      if (!state.editing || props.updateHandler.isEmpty) {
        FormControl(
          value = s"${area.a} m²",
          readOnly = true,
          plaintext = true)(^.onClick --> bs.modState(_.copy(editing = true)))
      } else {
        FormControl(
          value = s"${area.a}",
          `type` = "number",
          onChange = fieldUpdater(_)
        )(^.min := 0, ^.onBlur --> confirm(), ^.onKeyUp ==> handleKey)
      }
    }

    private def handleKey(e: ReactKeyboardEvent): Callback =
      handleEsc(cancel).orElse(handleEnter(confirm)).orElse(ignoreKey)(e.keyCode)

    private def cancel(): Callback = bs.modState(old => old.copy(editing = false, workingCopy = old.area))

    private def confirm(): Callback = {
      for {
        handler <- bs.props.map(_.updateHandler)
        area <- bs.state.map(_.workingCopy)
        _ <- handler.fold(Callback.empty)(_ (area)).flatMap(_ => bs.modState(old => old.copy(area = area, editing = false)))
      } yield ()
      Callback.empty
    }

    private def fieldUpdater(e: ReactEventFromInput): Callback = {
      e.persist()
      val area = if (e.target.value.isEmpty) Area() else Area(e.target.value.toDouble)
      bs.modState(_.copy(workingCopy = area))
    }

  }

  private def stateFromProps(props: Props): State = State(area = props.area, workingCopy = props.area)

  private val component = ScalaComponent
    .builder[Props]("AreaComponent")
    .initialStateFromProps(stateFromProps)
    .renderBackend[Backend]
    .componentWillReceiveProps(c => c.setState(stateFromProps(c.nextProps)))
    .build

  def apply(area: Area): VdomElement = component(Props(area)).vdomElement
}
