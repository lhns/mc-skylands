package org.lolhens.skylands.util

/**
  * Created by pierr on 23.01.2017.
  */
object ReflectHelper {
  def toObjectClassSeq(seq: Class[_]*): Seq[Class[_]] = seq.map {
    case boolean if boolean == classOf[Boolean] => java.lang.Boolean.TYPE
    case int if int == classOf[Int] => Integer.TYPE
    case clazz => clazz
  }

  def toObjectSeq(seq: Any*): Seq[Object] = seq.map {
    case boolean: Boolean => Boolean.box(boolean)
    case int: Int => Int.box(int)
    case value: AnyRef => value
  }
}
