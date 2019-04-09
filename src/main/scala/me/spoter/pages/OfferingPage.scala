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
object OfferingPage extends DetailsPageTemplate {
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
      fillInLayout(
        nameSlot = <.h1(offering.name.value),
        imageSlot = ImageCarousel(garden.images.map(IRI(_))),
        mapSlot = SpoterMap(garden.location.latitude.value.toDouble, garden.location.longitude.value.toDouble),
        sizeSlot = FormControl(
          value = s"${garden.area.a} m²",
          readOnly = true,
          plaintext = true)(),
        addressSlot = AddressComponent(garden.address),
        priceSlot = Some(
          FormControl(
            value = (offering.price.amount / 100.0).formatted("%.2f €"),
            readOnly = true,
            plaintext = true)()),
        descriptionSlot = FormControl(
          as = "textarea",
          value = offering.description,
          rows = 20,
          readOnly = true,
          plaintext = true)(),
        bungalowSlot = FormControl(
          value = garden.bungalow.map(_ => "Ja").getOrElse[String]("Nein"),
          readOnly = true,
          plaintext = true)(),
        conditionSlot = FormControl(
          value = garden.condition match {
            case Excellent => "Ausgezeichnet"
            case Good => "Gut"
            case Poor => "Dürftig"
            case Undefined => "KA"
          },
          readOnly = true,
          plaintext = true)(),
        availableAfterSlot = Some(
          FormControl(
            value = offering.availabilityStarts.toLocaleDateString(),
            readOnly = true,
            plaintext = true)()),
        contactSlot = Some(
          offering.offeredBy.emailUri.fold(FormControl(value = "KA", readOnly = true, plaintext = true)()) { uri =>
            FormControl(as = "a", readOnly = true, plaintext = true)(^.href := uri.toString)("Email zum Anbieter")
          })
      )
    }

    import scala.concurrent.ExecutionContext.Implicits.global

    def updateState(props: Props): Callback = Callback.future(OfferingService.fetchOffering(props.uri).map(o => bs.modState(_ => o)))

  }

}
