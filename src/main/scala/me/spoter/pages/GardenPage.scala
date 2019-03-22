package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^.{VdomElement, _}
import me.spoter.components.bootstrap._
import me.spoter.components.{AddressComponent, SpoterMap}
import me.spoter.models.AllotmentCondition.{Excellent, Good, Poor, Undefined}
import me.spoter.models._
import me.spoter.services.rdf_mapping.BasicField._
import me.spoter.services.{GardenService, GeoCodingService}
import org.scalajs.dom.ext.KeyCode

/**
  * A page showing the data of an allotment garden.
  */
object GardenPage {

  private val component = ScalaComponent
    .builder[Props]("GardenPage")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(c => c.backend.fetchData(c.props.uri))
    .build

  def apply(uri: String): VdomElement = component(Props(new URI(uri))).vdomElement

  private def renderWhen(b: Boolean)(r: => VdomElement): Option[VdomElement] = if (b) Some(r) else None

  case class Props(uri: URI)

  case class State(g: AllotmentGarden = AllotmentGarden(), editing: Boolean = false, workingCopy: AllotmentGarden = AllotmentGarden())

  class Backend(bs: BackendScope[Props, State]) {

    private def onCancel(): Callback = bs.modState(s => s.copy(editing = false, workingCopy = s.g))

    private def handleKeyForName(e: ReactKeyboardEvent): Callback = handleExc.orElse(handleEnter).orElse(ignoreKey)(e.keyCode)

    private def handleKeyForDesc(e: ReactKeyboardEvent): Callback = handleExc.orElse(ignoreKey)(e.keyCode)

    private val handleExc: PartialFunction[Int, Callback] = {
      case KeyCode.Escape => onCancel()
    }

    private val handleEnter: PartialFunction[Int, Callback] = {
      case KeyCode.Enter => onUpdateName()
    }

    private val ignoreKey: PartialFunction[Int, Callback] = {
      case _ => Callback()
    }

    def render(state: State): VdomElement = {
      val garden = if (state.editing) state.workingCopy else state.g
      Container(
        Form(validated = true)(^.noValidate := true)(
          Row()(
            renderWhen(!state.editing)(
              <.h1(garden.name.value, ^.onClick --> switchToEditing())),
            renderWhen(state.editing) {
              <.div(^.width := "100%",
                FormControl(
                  size = "lg",
                  value = s"${garden.name.value}",
                  onChange = (e: ReactEventFromInput) => changeHandler(e, bs)(g => g.copy(name = g.name.copy(value = e.target.value))))(
                  ^.placeholder := "Name des Gartens", ^.autoFocus := true, ^.required := true, ^.maxLength := 40,
                  ^.onKeyUp ==> handleKeyForName)(),
                <.div(^.marginTop := 10.px,
                  <.i(^.className := "fas fa-check fa-lg",
                    ^.title := "Bestätigen",
                    ^.color := "darkseagreen",
                    ^.marginLeft := 10.px,
                    ^.onClick --> onUpdateName()),
                  <.i(^.className := "fas fa-times fa-lg",
                    ^.title := "Abbrechen",
                    ^.color := "red",
                    ^.marginLeft := 10.px,
                    ^.onClick --> onCancel())
                )
              )
            }
          ),
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
              FormGroup(controlId = "newAddress") {
                AddressComponent(garden.address, addressChangeHandler)
              }
            )
          ),
          Row()(
            Col(xl = 8, lg = 8, md = 8) {
              FormGroup(controlId = "description")(
                FormControl(
                  as = "textarea",
                  value = garden.description.value,
                  rows = 18,
                  readOnly = !state.editing,
                  plaintext = !state.editing,
                  onChange = (e: ReactEventFromInput) => changeHandler(e, bs)(g =>
                    g.copy(description = g.description.copy(value = e.target.value))))(
                  ^.placeholder := "Beschreibung", ^.maxLength := 3000, ^.onClick --> switchToEditing(),
                  ^.onKeyUp ==> handleKeyForDesc)(),
                renderWhen(state.editing) {
                  <.div(^.marginTop := 10.px,
                    <.i(^.className := "fas fa-check fa-lg",
                      ^.title := "Bestätigen",
                      ^.color := "darkseagreen",
                      ^.marginLeft := 10.px,
                      ^.onClick --> onUpdateDesc()),
                    <.i(^.className := "fas fa-times fa-lg",
                      ^.title := "Abbrechen",
                      ^.color := "red",
                      ^.marginLeft := 10.px,
                      ^.onClick --> onCancel())
                  )
                }
              )
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

    private def switchToEditing(): Callback = bs.modState(_.copy(editing = true))

    private def changeHandler(e: ReactEventFromInput, bs: BackendScope[Props, State])
                             (transform: AllotmentGarden => AllotmentGarden): Callback = {
      e.persist()
      bs.modState(old => old.copy(workingCopy = transform(old.g)))
    }

    private def onUpdateName(): Callback = {
      bs.state.flatMap { state =>
        if (state.workingCopy.name.value.isEmpty) Callback()
        else
          Callback.future {
            val workingCopy = state.workingCopy
            val updateF = GardenService.update(IRI(workingCopy.uri), Name, state.g.name, workingCopy.name)
            updateF.map(_ => bs.setState(state.copy(g = workingCopy, editing = false)))
          }
      }
    }

    private def onUpdateDesc(): CallbackTo[Unit] =
      bs.state.flatMap { state =>
        Callback.future {
          val workingCopy = state.workingCopy
          val updateF = GardenService.update(IRI(workingCopy.uri), Description, state.g.description, workingCopy.description)
          updateF.map(_ => bs.setState(state.copy(g = workingCopy, editing = false)))
        }
      }

    private def addressChangeHandler(newAddress: Address): Callback = bs.state.flatMap { state =>
      val garden = state.g
      Callback.future {
        for {
          _ <- GardenService.update(IRI(garden.uri), StreetAndNumber, garden.address.streetAndNumber, newAddress.streetAndNumber)
          _ <- GardenService.update(IRI(garden.uri), PostalCode, garden.address.postalCode, newAddress.postalCode)
          _ <- GardenService.update(IRI(garden.uri), AddressRegion, garden.address.region, newAddress.region)
          loc <- GeoCodingService.locationFrom(newAddress).recover { case _ => Location() }
          _ <- GardenService.update(IRI(garden.uri), Latitude, garden.location.latitude, loc.latitude)
          _ <- GardenService.update(IRI(garden.uri), Longitude, garden.location.longitude, loc.longitude)
        } yield bs.setState(state.copy(g = garden.copy(address = newAddress, location = loc), editing = false))
      }
    }

    def fetchData(uri: URI, force: Boolean = false): Callback = Callback.future {
      GardenService.fetchGarden(uri, force)
        .map(g => bs.modState(s => s.copy(g = g, workingCopy = g, editing = false)))
    }
  }

}
