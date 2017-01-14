package org.lolhens.skylands.block.properties

import java.util

import com.google.common.base.{Objects, Optional}
import net.minecraft.block.properties.IProperty

import scala.collection.JavaConversions._

/**
  * Created by pierr on 14.01.2017.
  */
class PropertyMap[E <: Comparable[E]](val name: String, val allowedValues: Map[String, E]) extends IProperty[E] {
  override def parseValue(value: String): Optional[E] = allowedValues.get(value) match {
    case Some(result) => Optional.of(result)
    case None => Optional.absent()
  }

  override def getName: String = name

  override def getName(value: E): String = allowedValues.find(_._2 == value).map(_._1).get

  override def getValueClass: Class[E] = null

  override def getAllowedValues: util.Collection[E] = allowedValues.values

  override def toString: String = Objects.toStringHelper(this).add("name", name).add("values", getAllowedValues).toString

  override def equals(obj: scala.Any): Boolean = obj match {
    case thiz if thiz == this =>
      true

    case propertyMap: PropertyMap[E] =>
      name == propertyMap.name &&
        allowedValues == propertyMap.allowedValues

    case _ =>
      false
  }

  override def hashCode: Int = 31 * classOf[Map[String, E]].hashCode + name.hashCode
}
