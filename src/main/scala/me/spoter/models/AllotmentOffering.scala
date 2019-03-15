package me.spoter.models

import java.net.URI

import me.spoter.services.GardenService.RdfLiteral

import scala.scalajs.js
import scala.scalajs.js.UndefOr

/**
  * A garden offering.
  */
case class AllotmentOffering(uri: URI = URI.create(""),
                             title: RdfLiteral = RdfLiteral("", UndefOr.any2undefOrA("de")),
                             description: String = "",
                             price: Money = Money(0),
                             availabilityStarts: js.Date = new js.Date(),
                             offeredBy: User,
                             garden: AllotmentGarden) extends KGVEntity {

  override def withNewTitle(t: RdfLiteral): KGVEntity = copy(title = t)
}
