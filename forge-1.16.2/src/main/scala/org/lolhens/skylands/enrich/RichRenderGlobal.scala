package org.lolhens.skylands.enrich

import java.lang.reflect.{Field, Method}

import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.chunk.{ChunkRenderDispatcher, RenderChunk}
import net.minecraft.util.math.BlockPos
import org.lolhens.skylands.enrich.RichRenderGlobal._
import org.lolhens.skylands.util.ReflectHelper

import scala.language.implicitConversions

/**
  * Created by pierr on 23.01.2017.
  */
class RichRenderGlobal(val self: RenderGlobal) extends AnyVal {
  def setLightUpdates: java.util.Set[BlockPos] = setLightUpdatesField.get(self).asInstanceOf[java.util.Set[BlockPos]]

  def setLightUpdates_=(value: java.util.Set[BlockPos]): Unit = setLightUpdatesField.set(self, value)

  def renderDispatcher: ChunkRenderDispatcher = renderDispatcherField.get(self).asInstanceOf[ChunkRenderDispatcher]

  def renderDispatcher_=(value: ChunkRenderDispatcher): Unit = renderDispatcherField.set(self, value)

  def chunksToUpdate: java.util.Set[RenderChunk] = chunksToUpdateField.get(self).asInstanceOf[java.util.Set[RenderChunk]]

  def chunksToUpdate_=(value: java.util.Set[RenderChunk]): Unit = chunksToUpdateField.set(self, value)

  def markBlocksForUpdate(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, notify: Boolean): Unit =
    markBlocksForUpdateMethod.invoke(self, ReflectHelper.toObjectSeq(minX, minY, minZ, maxX, maxY, maxZ, notify): _*)
}

object RichRenderGlobal {
  implicit def fromRenderGlobal(renderGlobal: RenderGlobal): RichRenderGlobal = new RichRenderGlobal(renderGlobal)

  private lazy val setLightUpdatesField: Field = {
    val result = classOf[RenderGlobal].getDeclaredField("setLightUpdates")
    result.setAccessible(true)
    result
  }

  private lazy val renderDispatcherField: Field = {
    val result = classOf[RenderGlobal].getDeclaredField("renderDispatcher")
    result.setAccessible(true)
    result
  }

  private lazy val chunksToUpdateField: Field = {
    val result = classOf[RenderGlobal].getDeclaredField("chunksToUpdate")
    result.setAccessible(true)
    result
  }

  private lazy val markBlocksForUpdateMethod: Method = {
    val result = classOf[RenderGlobal].getDeclaredMethod("markBlocksForUpdate",
      ReflectHelper.toObjectClassSeq(classOf[Int], classOf[Int], classOf[Int], classOf[Int], classOf[Int], classOf[Int], classOf[Boolean]): _*)
    result.setAccessible(true)
    result
  }
}
