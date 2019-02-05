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
case class Area(a: Double = 0) extends AnyVal

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
sealed trait AllotmentCondition extends EnumEntry

object AllotmentCondition extends Enum[AllotmentCondition] {

  override def values: immutable.IndexedSeq[AllotmentCondition] = findValues

  case object Excellent extends AllotmentCondition

  case object Good extends AllotmentCondition

  case object Poor extends AllotmentCondition

  case object Undefined extends AllotmentCondition

}

/**
  *
  * @param streetAndNumber
  * @param zipCode
  * @param region
  * @param country
  */
case class Address(streetAndNumber: String, zipCode: Int, region: String, country: String)