package me.spoter.models

import java.net.URI

/**
  * A city district or a community out side of cities.
  *
  * @param uri  the resource id
  * @param name name of the district
  */
case class District(uri: URI, name: String)