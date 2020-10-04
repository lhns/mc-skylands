package de.lolhens.minecraft.skylandsmod.config

import io.circe.Codec

case class SkylandsConfig() {
}

object SkylandsConfig extends Config[SkylandsConfig] {
  override val default: SkylandsConfig = SkylandsConfig()

  override protected def codec: Codec[SkylandsConfig] = makeCodec
}
