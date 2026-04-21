package skylands

import org.slf4j.{Logger, LoggerFactory}

object SkylandsCommon:
  val ModId: String = "skylands"
  val Log: Logger = LoggerFactory.getLogger(ModId)

  def init(): Unit =
    Log.info("Skylands init (common)")
