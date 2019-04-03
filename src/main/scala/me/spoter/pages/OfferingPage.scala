package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import me.spoter.components.bootstrap._
import me.spoter.components.{AddressComponent, ImageCarousel, SpoterMap}
import me.spoter.models.AllotmentCondition._
import me.spoter.models._
import me.spoter.services.OfferingService

/**
  *
  */
object OfferingPage {
  //val uriRegex =
  //  "(?:(https?|ircs?):\\/\\/(?:www\\.)?|www\\.)((?:(?:[-\\w]+\\.)+)[-\\w]+)(?::\\d+)?(?:\\/((?:[-a-zA-Z;./\\d#:_?=&,]*)))?"

  private val component = ScalaComponent
    .builder[Props]("OfferingPage")
    .initialState(AllotmentOffering(offeredBy = User(new URI("")), garden = AllotmentGarden()))
    .renderBackend[Backend]
    .componentDidMount(c => c.backend.updateState(c.props))
    .build

  case class Props(uri: URI)

  def apply(uri: String): VdomElement = component(Props(new URI(uri))).vdomElement

  class Backend(bs: BackendScope[Props, AllotmentOffering]) {
    def render(offering: AllotmentOffering): VdomElement = {
      val garden = offering.garden
      Container(
        Form()(
          Row()(
            <.h1(offering.name.value)
          ),
          Row()(
            Col(sm = 12, xs = 12) {
              ImageCarousel(garden.images.map(IRI(_)))
            },
            Col(sm = 12, xs = 12) {
              <.div(^.height := 280.px,
                SpoterMap(garden.location.latitude.value.toDouble, garden.location.longitude.value.toDouble)
              )
            },
            Col(sm = 12, xs = 12)(
              FormGroup(controlId = "size") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 3, xs = 3)(
                    FormLabel(column = true)("Größe:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 9, xs = 9) {
                    FormControl(
                      value = s"${garden.area.a} m²",
                      readOnly = true,
                      plaintext = true)()
                  }
                )
              },
              FormGroup(controlId = "address") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 3, xs = 3)(
                    FormLabel(column = true)("Adresse:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 9, xs = 9) {
                    AddressComponent(garden.address, _ => Callback.empty)
                  }
                )
              },
              FormGroup(controlId = "price") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 3, xs = 3)(
                    FormLabel(column = true)("Preis:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 9, xs = 9) {
                    FormControl(
                      value = (offering.price.amount / 100.0).formatted("%.2f €"),
                      readOnly = true,
                      plaintext = true)()
                  }
                )
              }
            )
          ),
          Row()(
            Col(xl = 8, lg = 8, md = 8, sm = 12, xs = 12) {
              FormGroup(controlId = "description") {
                FormControl(
                  as = "textarea",
                  value = offering.description,
                  rows = 20,
                  readOnly = true,
                  plaintext = true)()
              }
            },
            Col(sm = 12, xs = 12)(
              FormGroup(controlId = "bungalow") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 6, xs = 6)(
                    FormLabel(column = true)("Bungalow:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 6, xs = 6) {
                    FormControl(
                      value = garden.bungalow.map(_ => "Ja").getOrElse[String]("Nein"),
                      readOnly = true,
                      plaintext = true)()
                  }
                )
              },
              FormGroup(controlId = "condition") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 6, xs = 6)(
                    FormLabel(column = true)("Zustand:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 6, xs = 6) {
                    FormControl(
                      value = garden.condition match {
                        case Excellent => "Ausgezeichnet"
                        case Good => "Gut"
                        case Poor => "Dürftig"
                        case Undefined => "KA"
                      },
                      readOnly = true,
                      plaintext = true)()
                  })
              },
              FormGroup(controlId = "availabilityStarts") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 6, xs = 6)(
                    FormLabel(column = true)("Verfügbar ab:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 6, xs = 6) {
                    FormControl(
                      value = offering.availabilityStarts.toLocaleDateString(),
                      readOnly = true,
                      plaintext = true)()
                  })
              },
              FormGroup(controlId = "contact") {
                Row()(
                  Col(xl = 4, lg = 4, md = 4, sm = 6, xs = 6)(
                    FormLabel(column = true)("Kontakt:")
                  ),
                  Col(xl = 8, lg = 8, md = 8, sm = 6, xs = 6) {
                    offering.offeredBy.emailUri.fold(FormControl(value = "KA", readOnly = true, plaintext = true)()) { uri =>
                      FormControl(as = "a", readOnly = true, plaintext = true)(^.href := uri.toString)("Email zum Anbieter")
                    }
                  })
              }
            )
          )
        )
      )
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    def updateState(props: Props): Callback = Callback.future(OfferingService.fetchOffering(props.uri).map(o => bs.modState(_ => o)))

  }

}
