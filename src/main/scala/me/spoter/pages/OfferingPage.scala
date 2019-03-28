package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import me.spoter.components.bootstrap._
import me.spoter.components.{AddressComponent, SpoterMap}
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
        <.h1(offering.name.value),
        Form()(
          Row()(^.height := 280.px)(
            Col() {
              Carousel()(
                garden.images.map { uri =>
                  CarouselItem(
                    <.img(^.src := uri.toString,
                      ^.alt := "Bild x",
                      ^.className := "d-block w-100")
                  )
                }: _*
              )
            },
            Col() {
              SpoterMap(garden.location.latitude.value.toDouble, garden.location.longitude.value.toDouble)
            },
            Col()(
              FormGroup(controlId = "size") {
                Row()(
                  FormLabel(column = true)("Größe:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = s"${garden.area.a} m²",
                      readOnly = true,
                      plaintext = true)()
                  }
                )
              },
              FormGroup(controlId = "address") {
                AddressComponent(garden.address)
              },
              FormGroup(controlId = "price") {
                Row()(
                  FormLabel(column = true)("Preis:"),
                  Col(xl = 8, lg = 8, md = 8) {
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
            Col(xl = 8, lg = 8, md = 8) {
              FormGroup(controlId = "description") {
                FormControl(
                  as = "textarea",
                  value = offering.description,
                  rows = 20,
                  readOnly = true,
                  plaintext = true)()
              }
            },
            Col()(
              FormGroup(controlId = "bungalow") {
                Row()(
                  FormLabel(column = true)("Bungalow:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = garden.bungalow.map(_ => "Ja").getOrElse[String]("Nein"),
                      readOnly = true,
                      plaintext = true)()
                  }
                )
              },
              FormGroup(controlId = "condition") {
                Row()(
                  FormLabel(column = true)("Zustand:"),
                  Col(xl = 8, lg = 8, md = 8) {
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
                  FormLabel(column = true)("Verfügbar ab:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = offering.availabilityStarts.toLocaleDateString(),
                      readOnly = true,
                      plaintext = true)()
                  })
              },
              FormGroup(controlId = "contact") {
                Row()(
                  FormLabel(column = true)("Kontakt:"),
                  Col(xl = 8, lg = 8, md = 8) {
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
