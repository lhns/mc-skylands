package org.lolhens.skylands

import java.io.File

import net.minecraftforge.common.config.Configuration

/**
  * Created by pierr on 01.01.2017.
  */
class Config(configFile: File) {
  private var _dimensionId = 50

  private val config = new Configuration(configFile)
  config.load()

  _dimensionId = config.get("IDs", "Dimension Id", _dimensionId, "Id of the skylands dimension").getInt

  config.save()

  def dimensionId: Int = _dimensionId
}
