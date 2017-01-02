package org.lolhens.skylands.proxy
import java.io.File

import org.lolhens.skylands.{ClientSkylands, Skylands}

/**
  * Created by pierr on 02.01.2017.
  */
class ClientProxy extends CommonProxy {
  override def skylands(configFile: File): Skylands = new ClientSkylands(configFile)
}
