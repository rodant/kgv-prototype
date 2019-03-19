package me.spoter.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import me.spoter.components.bootstrap.{Col, FormControl, FormLabel, Row}
import me.spoter.models.Address
import me.spoter.services.rdf_mapping.BasicField.StreetAndNumber

/**
  *
  */
object AddressComponent {

  case class Props(address: Address)

  case class State(address: Address, workingAddress: Address, editing: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) {
    def render(state: State): VdomElement = {
      val address = if (state.editing) state.workingAddress else state.address
      val viewString =
        if (address.streetAndNumber != StreetAndNumber.default) s"${address.streetAndNumber.value}, ${address.postalCode.value} ${address.region.value}"
        else ""
      Row()(
        FormLabel(column = true)("Adresse:"),
        Col(xl = 8, lg = 8, md = 8) {
          FormControl(
            value = viewString,
            readOnly = true,
            plaintext = true)()
        }
      )
    }
  }

  private val component = ScalaComponent
    .builder[Props]("AddressComponent")
    .initialStateFromProps(props => State(address = props.address, workingAddress = props.address))
    .renderBackend[Backend]
    .componentWillReceiveProps(c => c.modState(_.copy(address = c.nextProps.address, workingAddress = c.nextProps.address)))
    .build

  def apply(address: Address): VdomElement = component(Props(address)).vdomElement

}
