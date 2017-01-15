package org.lolhens.skylands.tileentities

import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.world.WorldServer
import org.lolhens.skylands.generator.BeanstalkGenerator

/**
  * Created by pierr on 14.01.2017.
  */
class TileEntityBeanPlant extends TileEntity with ITickable {
  var beanstalkGenerator: Option[BeanstalkGenerator] = None

  override def update(): Unit = {
    world match {
      case world: WorldServer =>
        beanstalkGenerator.getOrElse {
          val result = new BeanstalkGenerator(world, pos)
          beanstalkGenerator = Some(result)
          result
        }.update()

      case _ =>
    }
  }
}
