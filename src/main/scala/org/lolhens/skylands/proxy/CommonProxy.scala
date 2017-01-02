package org.lolhens.skylands.proxy

import java.io.File

import org.lolhens.skylands.Skylands

/**
  * Created by pierr on 02.01.2017.
  */
class CommonProxy {
  def skylands(configFile: File): Skylands = new Skylands(configFile)
}
