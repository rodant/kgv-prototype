package me.spoter.models

import java.net.URI

/**
  * Garden entity.
  *
  * @param uri
  * @param location
  * @param district
  * @param price
  * @param bungalow
  * @param toilet
  */
case class Garden(
                   uri: URI,
                   location: Location,
                   district: District,
                   price: Money,
                   bungalow: Option[Bungalow] = None,
                   toilet: Option[Toilet] = None)
