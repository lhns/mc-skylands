package org.lolhens.skylands

import java.io.File

import net.minecraftforge.common.config.Configuration

/**
  * Created by pierr on 01.01.2017.
  */
class Config(configFile: File) {
  private val config = new Configuration(configFile)
  config.load()

  val dimensionId: Int = config.get("IDs", "Dimension Id", 50, "Id of the skylands dimension").getInt

  config.save()
}
