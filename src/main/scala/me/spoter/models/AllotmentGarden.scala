package me.spoter.models

import java.net.URI

import me.spoter.models.AllotmentCondition.Good

/**
  * AllotmentGarden entity.
  *
  */
case class AllotmentGarden(uri: URI = URI.create(""),
                           title: String = "",
                           address: Address = Address("", 0, "", ""),
                           location: Location = Location(0, 0),
                           images: Seq[URI] = List(URI.create("public/kgv/images/image-1.svg")),
                           description: String = "",
                           area: Area = Area(),
                           bungalow: Option[Bungalow] = None,
                           condition: AllotmentCondition = Good) extends KGVEntity {

  override def withNewTitle(t: String): KGVEntity = copy(title = t)
}
