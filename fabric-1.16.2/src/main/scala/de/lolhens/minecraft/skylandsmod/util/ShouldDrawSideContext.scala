package de.lolhens.minecraft.skylandsmod.util

import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView

object ShouldDrawSideContext {
  private val localWorld: ThreadLocal[BlockView] = new ThreadLocal()
  private val localPos: ThreadLocal[BlockPos] = new ThreadLocal()

  def startShouldDrawSide(world: BlockView, pos: BlockPos): Unit = {
    localWorld.set(world)
    localPos.set(pos)
  }

  def endShouldDrawSide(): Unit = {
    localWorld.remove()
    localPos.remove()
  }

  def isActive: Boolean = localWorld.get() != null

  def world: BlockView = localWorld.get()

  def pos: BlockPos = localPos.get()
}
