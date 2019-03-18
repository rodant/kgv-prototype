package me.spoter.models

import java.net.URI

import me.spoter.models.AllotmentCondition.Good
import me.spoter.rdf.RdfLiteral
import me.spoter.services.GardenService.{Description, Name}

/**
  * AllotmentGarden entity.
  *
  */
case class AllotmentGarden private(uri: URI,
                                   title: RdfLiteral,
                                   address: Address,
                                   location: Location,
                                   images: Seq[URI],
                                   description: RdfLiteral,
                                   area: Area,
                                   bungalow: Option[Bungalow],
                                   condition: AllotmentCondition) extends KGVEntity {

  override def withNewTitle(t: RdfLiteral): KGVEntity = copy(title = t)
}

object AllotmentGarden {
  def apply(uri: URI = URI.create(""),
            title: RdfLiteral = Name.default,
            address: Address = Address("", 0, "", ""),
            location: Location = Location(0, 0),
            images: Seq[URI] = List(URI.create("public/kgv/images/image-1.svg")),
            description: RdfLiteral = Description.default,
            area: Area = Area(),
            bungalow: Option[Bungalow] = None,
            condition: AllotmentCondition = Good): AllotmentGarden = {
    val dirUri = if (uri.getPath.endsWith("/")) uri else URI.create(s"$uri/")
    new AllotmentGarden(dirUri, title, address, location, images, description, area, bungalow, condition)
  }
}
