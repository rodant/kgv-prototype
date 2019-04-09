package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^.{VdomElement, _}
import me.spoter.components.bootstrap._
import me.spoter.components.{AddressComponent, SpoterMap, _}
import me.spoter.models.AllotmentCondition.{Excellent, Good, Poor, Undefined}
import me.spoter.models._
import me.spoter.rdf.RdfLiteral
import me.spoter.services.rdf_mapping.BasicField._
import me.spoter.services.{GardenService, GeoCodingService}
import me.spoter.solid_libs.RDFHelper
import org.scalajs.dom.ext.Ajax

import scala.scalajs.js

/**
  * A page showing the data of an allotment garden.
  */
object GardenPage extends DetailsPageTemplate {

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

    private val ImageCommandHandler = new ImageCarousel.CommandHandler {

      import scala.concurrent.ExecutionContext.Implicits.global

      override def addImage(name: String, data: Ajax.InputData): Callback = {
        for {
          uri <- bs.props.map(_.uri)
          encodedName = js.Dynamic.global.encodeURI(name).toString
          imgIri = GardenService.imagesIRIFor(IRI(uri)).concatPath(encodedName)
          upload = RDFHelper.uploadFile(imgIri, data, contentType = "image")
          stateChange <- Callback.future {
            upload.map { _ =>
              bs.modState { old =>
                val newImages =
                  if (old.g.images != AllotmentGarden.defaultImages) imgIri.innerUri +: old.g.images
                  else Seq(imgIri.innerUri)
                old.copy(g = old.g.copy(images = newImages))
              }.flatMap(_ => onCancel())
            }
          }
        } yield stateChange
      }

      override def removeImage(index: Int): Callback = {
        for {
          imgUri <- bs.state.map(_.g.images(index))
          deletion = RDFHelper.deleteResource(IRI(imgUri))
          stateChange <- Callback.future {
            deletion.map { _ =>
              bs.modState { old =>
                val newImages = old.g.images.filter(_ != imgUri)
                old.copy(g = old.g.copy(images = newImages))
              }.flatMap(_ => onCancel())
            }
          }
        } yield stateChange
      }
    }

    private def onCancel(): Callback = bs.modState(s => s.copy(editing = false, workingCopy = s.g))

    private def handleKeyForName(e: ReactKeyboardEvent): Callback =
      handleEsc(onCancel).orElse(handleEnter(onUpdateName)).orElse(ignoreKey)(e.keyCode)

    private def handleKeyForDesc(e: ReactKeyboardEvent): Callback = handleEsc(onCancel).orElse(ignoreKey)(e.keyCode)

    def render(state: State): VdomElement = {
      val garden = if (state.editing) state.workingCopy else state.g
      fillInLayout(
        nameSlot =
          if (!state.editing) {
            <.h1(garden.name.value, ^.onClick --> switchToEditing())
          } else {
            WithConfirmAndCancel(() => onUpdateName(), () => onCancel()) {
              <.div(^.width := "100%",
                FormControl(
                  size = "lg",
                  value = garden.name.value,
                  onChange = (e: ReactEventFromInput) =>
                    changeHandler(e, bs)(g => g.copy(name = g.name.copy(value = e.target.value))))(
                  ^.placeholder := "Name des Gartens", ^.autoFocus := true, ^.required := true, ^.maxLength := 40,
                  ^.onKeyUp ==> handleKeyForName)()
              )
            }
          },
        imageSlot = ImageCarousel(garden.images.map(IRI(_)), activeIndex = 0, ImageCommandHandler),
        mapSlot = SpoterMap(garden.location.latitude.value.toDouble, garden.location.longitude.value.toDouble),
        addressSlot = AddressComponent(garden.address, addressChangeHandler),
        sizeSlot = AreaComponent(garden.area, Some(areaUpdateHandler)),
        descriptionSlot =
          WithConfirmAndCancel(() => onUpdateDesc(), () => onCancel(), show = state.editing) {
            <.div(
              FormControl(
                as = "textarea",
                value = garden.description.value,
                rows = 18,
                readOnly = !state.editing,
                plaintext = !state.editing,
                onChange = (e: ReactEventFromInput) => changeHandler(e, bs)(g =>
                  g.copy(description = g.description.copy(value = e.target.value))))(
                ^.placeholder := "Beschreibung", ^.maxLength := 3000, ^.onClick --> switchToEditing(),
                ^.onKeyUp ==> handleKeyForDesc)()
            )
          },
        bungalowSlot = FormControl(
          as = "select",
          value = garden.bungalow.fold("no")(_ => "yes"),
          onChange = updateBungalow(_))(
          <.option(^.value := "no", "Nein"),
          <.option(^.value := "yes", "Ja")),

        conditionSlot = FormControl(
          as = "select",
          value = garden.condition.toString,
          onChange = updateCondition(_))(
          <.option(^.value := Excellent.entryName, "Ausgezeichnet"),
          <.option(^.value := Good.entryName, "Gut"),
          <.option(^.value := Poor.entryName, "Dürftig"),
          <.option(^.value := Undefined.entryName, "Keine Angabe")
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

    private def areaUpdateHandler(area: Area): Callback = bs.state.flatMap { state =>
      val garden = state.g
      val prevDepthRdf = RdfLiteral(garden.area.a.toString)
      val nextDepthRdf = RdfLiteral(area.a.toString)
      Callback.future {
        for {
          _ <- GardenService.update(IRI(garden.uri), Depth, prevDepthRdf, nextDepthRdf)
        } yield bs.setState(state.copy(g = garden.copy(area = area), editing = false))
      }
    }

    private def updateBungalow(e: ReactEventFromInput): Callback = bs.state.flatMap { state =>
      val garden = state.g
      val prevValue = garden.bungalow
      val nextValue = if (e.target.value == "no") None else Option(Bungalow())
      Callback.future {
        GardenService
          .update(IRI(garden.uri), BungalowField, BungalowField.literal(prevValue), BungalowField.literal(nextValue))
          .map(_ => bs.modState(_.copy(g = garden.copy(bungalow = nextValue))))
      }
    }

    private def updateCondition(e: ReactEventFromInput): Callback = bs.state.flatMap { state =>
      val garden = state.g
      val prevValue = garden.condition
      val nextValue = AllotmentCondition.withNameInsensitive(e.target.value)
      Callback.future {
        GardenService
          .update(IRI(garden.uri), Condition, Condition.literal(prevValue), Condition.literal(nextValue))
          .map(_ => bs.modState(_.copy(g = garden.copy(condition = nextValue))))
      }
    }

    def fetchData(uri: URI, force: Boolean = false): Callback = Callback.future {
      GardenService.fetchGarden(uri, force)
        .map(g => bs.modState(s => s.copy(g = g, workingCopy = g, editing = false)))
    }
  }

}
