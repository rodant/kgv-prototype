package me.spoter.models

/**
  * A GPS location.
  */
case class Location(lat: Double, longitude: Double)

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

import enumeratum._

import scala.collection.immutable

/**
  *
  */
sealed trait GardenCondition extends EnumEntry

object GardenCondition extends Enum[GardenCondition] {

  override def values: immutable.IndexedSeq[GardenCondition] = findValues

  case object Excellent extends GardenCondition

  case object Good extends GardenCondition

  case object Poor extends GardenCondition

}

/**
  *
  * @param streetAndNumber
  * @param zipCode
  * @param region
  * @param country
  */
case class Address(streetAndNumber: String, zipCode: Int, region: String, country: String)