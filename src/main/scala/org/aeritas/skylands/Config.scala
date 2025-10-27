package org.aeritas.skylands

import java.io.File
import net.minecraftforge.common.config.Configuration

class Config(configFile: File) {
  private val config = new Configuration(configFile)

  def load(): Unit = {
    config.load()
    // Add configuration options here
    config.save()
  }
}