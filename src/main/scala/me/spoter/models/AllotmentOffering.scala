package me.spoter.models

import java.net.URI
import java.time.LocalDate

/**
  * A garden offering.
  */
case class AllotmentOffering(uri: URI = new URI(""),
                             title: String = "",
                             description: String = "",
                             price: Money = Money(0),
                             availabilityStarts: LocalDate = LocalDate.now(),
                             offeredBy: URI,
                             garden: AllotmentGarden)
