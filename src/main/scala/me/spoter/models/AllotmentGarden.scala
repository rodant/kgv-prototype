package me.spoter.models

import java.net.URI

import me.spoter.models.AllotmentCondition.Good
import me.spoter.rdf.RdfLiteral
import me.spoter.services.rdf_mapping.BasicField.{Description, Name}

/**
  * AllotmentGarden entity.
  *
  */
case class AllotmentGarden private(uri: URI,
                                   name: RdfLiteral,
                                   address: Address,
                                   location: Location,
                                   images: Seq[URI],
                                   description: RdfLiteral,
                                   area: Area,
                                   bungalow: Option[Bungalow],
                                   condition: AllotmentCondition) extends KGVEntity {

  override def withNewName(t: RdfLiteral): KGVEntity = copy(name = t)
}

object AllotmentGarden {
  val defaultImages: Seq[URI] = Seq(URI.create("public/kgv/images/image-1.svg"))

  def apply(uri: URI = URI.create(""),
            name: RdfLiteral = Name.default,
            address: Address = Address(),
            location: Location = Location(0, 0),
            images: Seq[URI] = defaultImages,
            description: RdfLiteral = Description.default,
            area: Area = Area(),
            bungalow: Option[Bungalow] = None,
            condition: AllotmentCondition = Good): AllotmentGarden = {
    val dirUri = if (uri.getPath.endsWith("/")) uri else URI.create(s"$uri/")
    new AllotmentGarden(dirUri, name, address, location, images, description, area, bungalow, condition)
  }
}
