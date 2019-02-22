package me.spoter.pages

import java.net.URI

import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import me.spoter.components.Leaflet
import me.spoter.components.bootstrap._
import me.spoter.models.AllotmentCondition._
import me.spoter.models._
import me.spoter.solid_libs.RDFHelper
import scalacss.defaults.Exports
import scalacss.internal.mutable.Settings

import scala.concurrent.Future
import scala.scalajs.js

/**
  *
  */
object OfferingPage {
  // This will choose between dev/prod depending on your scalac `-Xelide-below` setting
  val CssSettings: Exports with Settings = scalacss.devOrProdDefaults

  //val uriRegex =
  //  "(?:(https?|ircs?):\\/\\/(?:www\\.)?|www\\.)((?:(?:[-\\w]+\\.)+)[-\\w]+)(?::\\d+)?(?:\\/((?:[-a-zA-Z;./\\d#:_?=&,]*)))?"

  private val component = ScalaComponent
    .builder[Props]("OfferingPage")
    .initialState(AllotmentOffering(offeredBy = User(new URI("")), garden = AllotmentGarden()))
    .renderBackend[Backend]
    .componentDidMount(c => c.backend.updateState(c.props))
    .componentDidUpdate(c => c.backend.renderMap(c.currentState.garden.location))
    .build

  case class Props(uri: URI)

  def apply(uri: String): VdomElement = component(Props(new URI(uri))).vdomElement

  class Backend(bs: BackendScope[Props, AllotmentOffering]) {
    def render(offering: AllotmentOffering): VdomElement = {
      val garden = offering.garden
      Container(
        <.h1(offering.title),
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
              <.div(^.id := "map", ^.height := 100.pct)
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
            ),
          )
        )
      )
    }

    def renderMap(location: Location): Callback = Callback {
      val credits = "Map data &copy; <a href=\"https://www.openstreetmap.org/\">OpenStreetMap</a> " +
        "contributors, <a href=\"https://creativecommons.org/licenses/by-sa/2.0/\">CC-BY-SA</a>, " +
        "Imagery © <a href=\"https://www.mapbox.com/\">Mapbox</a>"

      val center = Leaflet.latLng(location.lat, location.longitude)
      val map = Leaflet.map("map").setView(center, 16)
      Leaflet.tileLayer(
        "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}",
        js.Dynamic.literal(
          attribution = credits,
          maxZoom = 20,
          id = "mapbox.streets",
          accessToken = "pk.eyJ1Ijoicm9kYW50NjgiLCJhIjoiY2pzNXdmMHBkMDN1NzQzcWNjZWprOG0xMyJ9.I7FPD7O6HS03uDeh5v1vqg"
        )
      ).addTo(map)

      Leaflet.marker(center).addTo(map)
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    def updateState(props: Props): Callback = Callback.future(fetchOffering(props.uri).map(o => bs.modState(_ => o)))

    private def fetchOffering(offeringUri: URI): Future[AllotmentOffering] =
      RDFHelper.loadEntity(offeringUri) {
        val gardenUri = new URI(RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("includes")).value.toString)
        val offerorUri = new URI(RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("offeredBy")).value.toString)

        fetchGarden(gardenUri).zip(fetchOfferor(offerorUri))
          .map[AllotmentOffering] { case (g, u) =>
          createOffering(offeringUri, g, u)
        }
      }.flatten

    private def fetchOfferor(offerorUri: URI): Future[User] = {
      RDFHelper.loadEntity(offerorUri) {
        val hasEmailNode = RDFHelper.get(offerorUri, RDFHelper.VCARD("hasEmail"))
        hasEmailNode match {
          case n if js.isUndefined(n) => Future(User(offerorUri))
          case _ =>
            val emailUri = new URI(hasEmailNode.value.toString)
            RDFHelper.loadEntity(emailUri)(
              User(offerorUri, Some(new URI(RDFHelper.get(emailUri, RDFHelper.VCARD("value")).value.toString)))
            )
        }
      }.flatten
    }

    private def createOffering(offeringUri: URI, g: AllotmentGarden, offeror: User): AllotmentOffering = {
      val title = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("name"))
      val desc = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("description"))
      val price = RDFHelper.get(offeringUri, RDFHelper.SCHEMA_ORG("price"))
      val availabilityStarts = RDFHelper.get(offeringUri, RDFHelper.GOOD_REL("availabilityStarts")).toString

      AllotmentOffering(
        offeringUri,
        title.toString,
        desc.toString,
        Money(price.toString.toLong),
        offeredBy = offeror,
        availabilityStarts = new js.Date(availabilityStarts),
        garden = g
      )
    }

    private def fetchGarden(allotmentUri: URI): Future[AllotmentGarden] =
      RDFHelper.loadEntity[Future[AllotmentGarden]](allotmentUri) {
        val imageDir = RDFHelper.get(allotmentUri, RDFHelper.SCHEMA_ORG("image"))
        val imageDirUri = new URI(allotmentUri.toString + imageDir.toString)

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

}
