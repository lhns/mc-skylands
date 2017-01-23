package org.lolhens.skylands.enrich

import java.lang.reflect.{Field, Method}

import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.storage.ExtendedBlockStorage
import org.lolhens.skylands.enrich.RichChunk._
import org.lolhens.skylands.util.ReflectHelper

import scala.language.implicitConversions

/**
  * Created by pierr on 23.01.2017.
  */
class RichChunk(val self: Chunk) extends AnyVal {
  def heightMapMinimum: Int = heightMapMinimumField.get(self).asInstanceOf[Int]

  def heightMapMinimum_=(value: Int): Unit = heightMapMinimumField.set(self, value)

  def precipitationHeightMap: Array[Int] = precipitationHeightMapField.get(self).asInstanceOf[Array[Int]]

  def precipitationHeightMap_=(value: Array[Int]): Unit = precipitationHeightMapField.set(self, value)

  def heightMap: Array[Int] = heightMapField.get(self).asInstanceOf[Array[Int]]

  def heightMap_=(value: Array[Int]): Unit = heightMapField.set(self, value)

  def storageArrays: Array[ExtendedBlockStorage] = storageArraysField.get(self).asInstanceOf[Array[ExtendedBlockStorage]]

  def storageArrays_=(value: Array[ExtendedBlockStorage]): Unit = storageArraysField.set(self, value)

  def isModified: Boolean = isModifiedField.get(self).asInstanceOf[Boolean]

  def isModified_=(value: Boolean): Unit = isModifiedField.set(self, value)

  def getBlockLightOpacity(x: Int, y: Int, z: Int): Int = getBlockLightOpacityMethod.invoke(self, ReflectHelper.toObjectSeq(x, y, z): _*).asInstanceOf[Int]

  def relightBlock(x: Int, y: Int, z: Int): Unit =
    relightBlockMethod.invoke(self, ReflectHelper.toObjectSeq(x, y, z): _*)

  def generateSkylightMap2(): Unit = {
    val i = self.getTopFilledSegment
    heightMapMinimum = Integer.MAX_VALUE

    val precipitationHeightMap = self.precipitationHeightMap
    val heightMap = self.heightMap
    val storageArrays = self.storageArrays

    for (x <- 0 until 16;
         z <- 0 until 16) {
      precipitationHeightMap(x + (z << 4)) = -999

      def loop1(): Unit =
        for (y <- i + 16 until 0 by -1
             if getBlockLightOpacity(x, y - 1, z) != 0) {
          heightMap(z << 4 | x) = y

          if (y < heightMapMinimum) heightMapMinimum = y

          return
        }

      loop1()

      if (!self.getWorld.provider.hasNoSky) {
        var lightValue = 15
        var y = i + 16 - 1

        def loop2(): Unit = {
          while (true) {
            var opacity = {
              val opacity = getBlockLightOpacity(x, y, z)
              if (opacity == 0 && lightValue != 15) 1 else opacity
            }

            lightValue -= opacity

            if (lightValue > 0) {
              val storage: ExtendedBlockStorage = storageArrays(y >> 4)

              if (storage != Chunk.NULL_BLOCK_STORAGE) {
                storage.setExtSkylightValue(x, y & 15, z, lightValue)
                self.getWorld.notifyLightSet(new BlockPos((self.xPosition << 4) + x, y, (self.zPosition << 4) + z))
              }
            }

            y -= 1

            if (y <= 0 || lightValue <= 0) return
          }
        }

        loop2()
      }
    }

    isModified = true
  }

  def generateSkylightMap3(): Unit = {
    val topY = self.getTopFilledSegment
    heightMapMinimum = Integer.MAX_VALUE

    val precipitationHeightMap = self.precipitationHeightMap
    val heightMap = self.heightMap
    val storageArrays = self.storageArrays

    for (x <- 0 until 16;
         z <- 0 until 16) {
      precipitationHeightMap(x + (z << 4)) = -999

      def loop1(): Unit =
        for (y <- topY + 16 until 0 by -1
             if getBlockLightOpacity(x, y - 1, z) != 0) {
          heightMap(z << 4 | x) = y

          if (y < heightMapMinimum) heightMapMinimum = y

          return
        }

      loop1()

      if (!self.getWorld.provider.hasNoSky) {
        var lightValue = 15

        for (y <- topY + 16 - 1 to 0 by -1) {
          val opacity = {
            val opacity = getBlockLightOpacity(x, y, z)
            if (opacity == 0 && lightValue != 15) -1 else opacity
          }

          lightValue = Math.max(Math.min(lightValue - opacity, 15), 0)

          if (lightValue > 0) {
            val storage: ExtendedBlockStorage = storageArrays(y >> 4)

            if (storage != Chunk.NULL_BLOCK_STORAGE) {
              storage.setExtSkylightValue(x, y & 15, z, lightValue)
              self.getWorld.notifyLightSet(new BlockPos((self.xPosition << 4) + x, y, (self.zPosition << 4) + z))
            }
          }

        }
      }
    }

    isModified = true
  }
}

object RichChunk {
  implicit def fromChunk(chunk: Chunk): RichChunk = new RichChunk(chunk)

  private lazy val heightMapMinimumField: Field = {
    val result = classOf[Chunk].getDeclaredField("heightMapMinimum")
    result.setAccessible(true)
    result
  }

  private lazy val precipitationHeightMapField: Field = {
    val result = classOf[Chunk].getDeclaredField("precipitationHeightMap")
    result.setAccessible(true)
    result
  }

  private lazy val heightMapField: Field = {
    val result = classOf[Chunk].getDeclaredField("heightMap")
    result.setAccessible(true)
    result
  }

  private lazy val storageArraysField: Field = {
    val result = classOf[Chunk].getDeclaredField("storageArrays")
    result.setAccessible(true)
    result
  }

  private lazy val isModifiedField: Field = {
    val result = classOf[Chunk].getDeclaredField("isModified")
    result.setAccessible(true)
    result
  }

  private lazy val getBlockLightOpacityMethod: Method = {
    val result = classOf[Chunk].getDeclaredMethod("getBlockLightOpacity", ReflectHelper.toObjectClassSeq(classOf[Int], classOf[Int], classOf[Int]): _*)
    result.setAccessible(true)
    result
  }

  private lazy val relightBlockMethod: Method = {
    val result = classOf[Chunk].getDeclaredMethod("relightBlock", ReflectHelper.toObjectClassSeq(classOf[Int], classOf[Int], classOf[Int]): _*)
    result.setAccessible(true)
    result
  }
}
