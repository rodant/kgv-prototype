package me.spoter.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap._
import me.spoter.models.Address
import me.spoter.services.rdf_mapping.BasicField.StreetAndNumber

/**
  *
  */
object AddressComponent {

  case class Props(address: Address, changeHandler: Address => Callback)

  case class State(address: Address, workingCopy: Address, editing: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) {

    private def onConfirm(): Callback = {
      for {
        handler <- bs.props.map(_.changeHandler)
        address <- bs.state.map(_.workingCopy)
        _ <- handler(address)
        _ <- bs.modState(old => old.copy(address = address, editing = false))
      } yield ()
    }

    private def onCancel(): Callback = bs.modState(s => s.copy(editing = false, workingCopy = s.address))

    private def updateHandler(e: ReactEventFromInput)(transform: Address => Address): Callback = {
      e.persist()
      bs.modState(old => old.copy(workingCopy = transform(old.workingCopy)))
    }

    private def handleKey(e: ReactKeyboardEvent): Callback =
      handleEsc(onCancel).orElse(handleEnter(onConfirm)).orElse(ignoreKey)(e.keyCode)

    def render(state: State): VdomElement = {
      if (state.editing) {
        val address = state.workingCopy
        Row()(
          FormLabel(column = true)("Adresse:"),
          Col(xl = 8, lg = 8, md = 8)(
            Row()(
              FormControl(
                value = address.streetAndNumber.value,
                onChange = (e: ReactEventFromInput) => updateHandler(e)(a => a.copy(streetAndNumber = a.streetAndNumber.copy(value = e.target.value)))
              )(^.autoFocus := true, ^.required := true, ^.onKeyUp ==> handleKey)
            ),
            Row()(^.className := "address-2nd-line",
              Col(xl = 5, lg = 5, md = 5)(
                FormControl(
                  value = address.postalCode.value,
                  onChange = (e: ReactEventFromInput) => updateHandler(e)(a => a.copy(postalCode = a.postalCode.copy(value = e.target.value)))
                )(^.required := true, ^.`type` := "number", ^.max := 99999, ^.onKeyUp ==> handleKey),
              ),
              Col(xl = 7, lg = 7, md = 7)(
                FormControl(
                  value = address.region.value,
                  onChange = (e: ReactEventFromInput) => updateHandler(e)(a => a.copy(region = a.region.copy(value = e.target.value)))
                )(^.required := true, ^.onKeyUp ==> handleKey))
            ),
            Row()(
              <.div(^.marginTop := 10.px,
                <.i(^.className := "fas fa-check fa-lg",
                  ^.title := "Bestätigen",
                  ^.color := "darkseagreen",
                  ^.marginLeft := 10.px,
                  ^.onClick --> onConfirm()),
                <.i(^.className := "fas fa-times fa-lg",
                  ^.title := "Abbrechen",
                  ^.color := "red",
                  ^.marginLeft := 10.px,
                  ^.onClick --> onCancel())
              )
            )
          )
        )
      } else {
        val address = state.address
        val viewString =
          if (address.streetAndNumber != StreetAndNumber.default)
            s"${address.streetAndNumber.value}, ${address.postalCode.value} ${address.region.value}"
          else ""
        Row()(
          FormLabel(column = true)("Adresse:"),
          Col(xl = 8, lg = 8, md = 8) {
            FormControl(value = viewString, readOnly = true, plaintext = true)(
              ^.onClick --> bs.modState(old => old.copy(editing = true)))()
          }
        )
      }
    }
  }

  private val component = ScalaComponent
    .builder[Props]("AddressComponent")
    .initialStateFromProps(props => State(address = props.address, workingCopy = props.address))
    .renderBackend[Backend]
    .componentWillReceiveProps(c => c.modState(_.copy(address = c.nextProps.address, workingCopy = c.nextProps.address)))
    .build

  def apply(address: Address, changeHandler: Address => Callback = _ => Callback()): VdomElement =
    component(Props(address, changeHandler)).vdomElement
}
