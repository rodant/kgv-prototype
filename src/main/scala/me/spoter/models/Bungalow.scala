package me.spoter.models

import java.net.URI

/**
  * Bungalow entity.
  *
  * @param uri
  * @param area
  * @param watterConnection
  * @param powerConnection
  * @param toilet
  */
case class Bungalow(
                     uri: URI,
                     area: Area,
                     watterConnection: Boolean,
                     powerConnection: Boolean,
                     toilet: Option[Toilet])
