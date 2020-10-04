package org.lolhens.skylands.tileentities

import net.minecraft.tileentity.{ITickableTileEntity, TileEntity}
import net.minecraft.world.server.ServerWorld
import org.lolhens.skylands.SkylandsMod
import org.lolhens.skylands.generator.BeanstalkGenerator

/**
  * Created by pierr on 14.01.2017.
  */
class TileEntityBeanPlant extends TileEntity[] with ITickableTileEntity {
  var beanstalkGenerator: Option[BeanstalkGenerator] = None

  override def tick(): Unit = {
    world match {
      case world: ServerWorld =>
        SkylandsMod.skylands.keepSkylandsLoaded()

        if (world.rand.nextInt(3) == 0)
          beanstalkGenerator.getOrElse {
            val result = new BeanstalkGenerator(world, pos)
            beanstalkGenerator = Some(result)
            result
          }.update()

      case _ =>
    }
  }
}
