package me.spoter.models

import java.net.{URI, URL}
import java.time.Instant

import me.spoter.models.GardenCondition.Good

/**
  * Garden entity.
  *
  * @param uri
  * @param location
  * @param price
  * @param bungalow
  * @param title
  */
case class Garden(uri: URI,
                  title: String,
                  address: Address,
                  location: Location,
                  images: List[URL] = List(),
                  description: String = "",
                  area: Area,
                  price: Money = Money(0),
                  bungalow: Option[Bungalow] = None,
                  gardenCondition: GardenCondition = Good,
                  availableAfter: Instant = Instant.EPOCH)
