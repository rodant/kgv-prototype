package me.spoter.models

/**
  * A GPS location.
  */
case class Location(lat: Float, longitude: Float)

/**
  * Area in square meters
  *
  * @param a value
  */
case class Area(a: Double) extends AnyVal

/**
  * Amount of money in €.
  *
  * @param a amount of cents
  */
case class Money(a: Long) extends AnyVal

/**
  *
  * @param powerSupply
  * @param watterSupply
  * @param pool
  * @param fountain
  * @param planting
  * @param gardenTools
  */
case class Facilities(
                       powerSupply: Boolean,
                       watterSupply: Boolean,
                       pool: Boolean,
                       fountain: Boolean,
                       planting: Boolean,
                       gardenTools: Boolean)