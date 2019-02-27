package me.spoter.models

import java.net.URI

import scala.scalajs.js

/**
  * A garden offering.
  */
case class AllotmentOffering(uri: URI = new URI(""),
                             title: String = "",
                             description: String = "",
                             price: Money = Money(0),
                             availabilityStarts: js.Date = new js.Date(),
                             offeredBy: User,
                             garden: AllotmentGarden) extends KGVEntity
