package skylands.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.model.loading.v1.{ModelLoadingPlugin, ModelModifier}
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.resources.ResourceLocation
import skylands.SkylandsCommon
import skylands.client.SkylandsClient
import skylands.fabric.client.CloudBakedModel
import skylands.gameplay.FeatherGliding

// See SkylandsFabric — the Scala adapter needs a singleton object.
object SkylandsFabricClient extends ClientModInitializer:
  override def onInitializeClient(): Unit =
    SkylandsClient.init()
    installCloudBakedModel()
    installClientFeatherTick()

  // FeatherGliding has to tick on the client because MC 1.21's player motion
  // is client-authoritative: anything the server writes to deltaMovement on
  // its tick is overwritten by the very next C2S move packet. NeoForge's
  // PlayerTickEvent.Post fires on both sides automatically; Fabric's server
  // tick callback (in FabricPlatform) doesn't, so we add the client side here.
  private def installClientFeatherTick(): Unit =
    ClientTickEvents.END_CLIENT_TICK.register { (mc: Minecraft) =>
      val p = mc.player
      if p != null then FeatherGliding.onPlayerTick(p)
    }

  // Wrap the baked model for the cloud block with CloudBakedModel so our
  // QuadTransform sees every quad during mesh building. NeoForge gets the same
  // behavior via CloudBlockNeoForge.hidesNeighborFace.
  private def installCloudBakedModel(): Unit =
    val cloudId = ResourceLocation.fromNamespaceAndPath(SkylandsCommon.ModId, "cloud")
    ModelLoadingPlugin.register(ctx =>
      ctx.modifyModelAfterBake().register(new ModelModifier.AfterBake {
        override def modifyModelAfterBake(
            model: BakedModel | Null,
            modCtx: ModelModifier.AfterBake.Context
        ): BakedModel | Null =
          if model == null then null
          else
            val top = modCtx.topLevelId()
            if top != null && top.id() == cloudId then new CloudBakedModel(model)
            else model
      })
    )

