package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^.{VdomElement, _}
import me.spoter.components.SpoterMap
import me.spoter.components.bootstrap._
import me.spoter.models.AllotmentCondition.{Excellent, Good, Poor, Undefined}
import me.spoter.models._
import me.spoter.services.GardenService

import scala.concurrent.Future

/**
  * A page showing the data of an allotment garden.
  */
object GardenPage {

  case class Props(uri: URI)

  case class State(g: AllotmentGarden = AllotmentGarden(), editing: Boolean = false)

  class Backend(bs: BackendScope[Props, State]) {
    def render(state: State): VdomElement = {
      val garden = state.g
      Container(
        renderWhen(!state.editing)(<.h1(garden.title)),
        renderWhen(state.editing) {
          FormControl(
            value = s"${garden.title}",
            onChange = (e: ReactEventFromInput) => changeHandler(e, bs)(g => g.copy(title = e.target.value)))(
            ^.placeholder := "Name des Gartens")()
        },
        Form()(
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
              renderWhen(!state.editing)(SpoterMap(garden.location.latitude, garden.location.longitude))
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
                  readOnly = !state.editing,
                  plaintext = !state.editing,
                  onChange = (e: ReactEventFromInput) => changeHandler(e, bs)(g => g.copy(description = e.target.value)))(
                  ^.placeholder := "Beschreibung")()
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
              renderWhen(state.editing)(
                Button(disabled = state.g.title.isEmpty, onClick = save(_))("Speichern"))
            )
          )
        )
      )
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    def fetchData(props: Props): Callback =
      Callback.future(GardenService.fetchGarden(props.uri).map(g => bs.modState(s => s.copy(g = g))))

    private def changeHandler(e: ReactEventFromInput, bs: BackendScope[Props, State])(transform: AllotmentGarden => AllotmentGarden): Callback = {
      e.persist()
      bs.modState(old => old.copy(g = transform(old.g)))
    }

    def save(e: ReactEventFromInput): Callback = {
      for {
        props <- bs.props
        _ <- Callback.future {
          Future(Callback())
        }
      } yield ()
    }
  }

  private val component = ScalaComponent
    .builder[Props]("GardenPage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(c => c.backend.fetchData(c.props))
    .build

  def apply(uri: String): VdomElement = component(Props(new URI(uri))).vdomElement

  private def renderWhen(b: Boolean)(r: => VdomElement): Option[VdomElement] = if (b) Some(r) else None
}
