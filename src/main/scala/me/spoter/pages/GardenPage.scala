package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import me.spoter.components.SpoterMap
import me.spoter.components.bootstrap._
import me.spoter.models.AllotmentCondition.{Excellent, Good, Poor, Undefined}
import me.spoter.models._
import me.spoter.solid_libs.RDFHelper

import scala.concurrent.Future

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
                      value = s"${garden.area.a} qm",
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

    def updateState(props: Props): Callback = Callback.future(fetchGarden(props.uri).map(o => bs.modState(_ => o)))

    private def fetchGarden(allotmentUri: URI): Future[AllotmentGarden] =
      RDFHelper.loadEntity[Future[AllotmentGarden]](allotmentUri) {
        val imageDir = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("image"))
        val imageDirUri = new URI(s"${allotmentUri.toString}/${imageDir.toString}")

        RDFHelper.listDir(imageDirUri).map[AllotmentGarden](createGarden(allotmentUri))
      }.flatten

    private def createGarden(allotmentUri: URI)(imageUris: Seq[URI]): AllotmentGarden = {
      val imagesUrisOrPlaceholder = if (imageUris.nonEmpty) imageUris else List(
        new URI("/public/kgv/images/image-1.svg"),
        new URI("/public/kgv/images/image-2.svg"),
        new URI("/public/kgv/images/image-3.svg"))

      val allotmentTitle = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("name"))
      val allotmentDesc = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("description"))

      val latitude = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("latitude"))
      val longitude = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("longitude"))
      val location = Location(latitude.toString.toDouble, longitude.toString.toDouble)

      val streetAddress = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("streetAddress"))
      val postalCode = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("postalCode"))
      val addressRegion = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("addressRegion"))
      val addressCountry = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("addressCountry"))
      val address = Address(streetAddress.toString, postalCode.toString.toInt, addressRegion.toString, addressCountry.toString)

      val includes = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("includes"))
      val condition = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("condition"))

      val width = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("width"))
      val depth = RDFHelper.get(allotmentUri, RDFHelper.GOOD_REL("depth"))

      AllotmentGarden(
        uri = allotmentUri,
        title = allotmentTitle.toString,
        images = imagesUrisOrPlaceholder,
        description = allotmentDesc.toString,
        location = location,
        address = address,
        bungalow = if (!includes.toString.isEmpty) Some(Bungalow()) else None,
        area = Area(width.toString.toDouble * depth.toString.toDouble),
        condition = AllotmentCondition.namesToValuesMap.getOrElse(condition.toString, AllotmentCondition.Undefined)
      )
    }
  }

  private val component = ScalaComponent
    .builder[Props]("GardenPage")
    .initialState(AllotmentGarden())
    .renderBackend[Backend]
    .componentDidMount(c => c.backend.updateState(c.props))
    .build

  def apply(uri: String): VdomElement = component(Props(new URI(uri))).vdomElement
}
