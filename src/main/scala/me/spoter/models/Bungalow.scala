package me.spoter.models

import java.net.URI

/**
  * Bungalow entity.
  *
  * @param uri
  * @param area
  */
case class Bungalow(uri: URI = new URI(""), area: Area = Area())
