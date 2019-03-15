package me.spoter.models

import java.net.URI

import me.spoter.models.AllotmentCondition.Good
import me.spoter.services.GardenService.RdfLiteral

import scala.scalajs.js.UndefOr

/**
  * AllotmentGarden entity.
  *
  */
case class AllotmentGarden private(uri: URI,
                                   title: RdfLiteral,
                                   address: Address,
                                   location: Location,
                                   images: Seq[URI],
                                   description: String,
                                   area: Area,
                                   bungalow: Option[Bungalow],
                                   condition: AllotmentCondition) extends KGVEntity {

  override def withNewTitle(t: RdfLiteral): KGVEntity = copy(title = t)
}

object AllotmentGarden {
  def apply(uri: URI = URI.create(""),
            title: RdfLiteral = RdfLiteral("", UndefOr.any2undefOrA("de")),
            address: Address = Address("", 0, "", ""),
            location: Location = Location(0, 0),
            images: Seq[URI] = List(URI.create("public/kgv/images/image-1.svg")),
            description: String = "",
            area: Area = Area(),
            bungalow: Option[Bungalow] = None,
            condition: AllotmentCondition = Good): AllotmentGarden = {
    val dirUri = if (uri.getPath.endsWith("/")) uri else URI.create(s"$uri/")
    new AllotmentGarden(dirUri, title, address, location, images, description, area, bungalow, condition)
  }
}
