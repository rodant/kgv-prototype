package me.spoter.models

import java.net.URI

import me.spoter.rdf.RdfLiteral
import me.spoter.services.rdf_mapping.BasicField.Name

import scala.scalajs.js

/**
  * A garden offering.
  */
case class AllotmentOffering(uri: URI = URI.create(""),
                             name: RdfLiteral = Name.default,
                             description: String = "",
                             price: Money = Money(0),
                             availabilityStarts: js.Date = new js.Date(),
                             offeredBy: User,
                             garden: AllotmentGarden) extends KGVEntity {

  override def withNewTitle(t: RdfLiteral): KGVEntity = copy(name = t)
}
