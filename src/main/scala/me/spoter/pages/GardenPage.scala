package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import me.spoter.components.SpoterMap
import me.spoter.components.bootstrap._
import me.spoter.models.AllotmentCondition.{Excellent, Good, Poor, Undefined}
import me.spoter.models._
import me.spoter.services.GardenService

/**
  * A page showing the data of an allotment garden.
  */
object GardenPage {

  case class Props(uri: URI)

  class Backend(bs: BackendScope[Props, AllotmentGarden]) {
    def render(garden: AllotmentGarden): VdomElement = {
      Container(
        <.h1(garden.title),
        Form(
          Row()(^.height := 280.px)(
            Col() {
              Carousel(
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
              SpoterMap(garden.location.latitude, garden.location.longitude)
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
                Row()(
                  FormLabel(column = true)("Adresse:"),
                  Col(xl = 8, lg = 8, md = 8) {
                    FormControl(
                      value = s"${garden.address.streetAndNumber}, ${garden.address.zipCode} ${garden.address.region}",
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
                  value = garden.description,
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
              }
            )
          )
        )
      )
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    def updateState(props: Props): Callback = Callback.future(GardenService.fetchGarden(props.uri).map(g => bs.modState(_ => g)))

  }

  private val component = ScalaComponent
    .builder[Props]("GardenPage")
    .initialState(AllotmentGarden())
    .renderBackend[Backend]
    .componentDidMount(c => c.backend.updateState(c.props))
    .build

  def apply(uri: String): VdomElement = component(Props(new URI(uri))).vdomElement
}
