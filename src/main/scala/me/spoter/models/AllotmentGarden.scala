package me.spoter.models

import java.net.URI

import me.spoter.models.AllotmentCondition.Good

/**
  * AllotmentGarden entity.
  *
  */
case class AllotmentGarden(uri: URI = new URI(""),
                           title: String = "",
                           address: Address = Address("", 0, "", ""),
                           location: Location = Location(0, 0),
                           images: Seq[URI] = List(),
                           description: String = "",
                           area: Area = Area(0),
                           price: Money = Money(0),
                           bungalow: Option[Bungalow] = None,
                           condition: AllotmentCondition = Good) extends KGVEntity
