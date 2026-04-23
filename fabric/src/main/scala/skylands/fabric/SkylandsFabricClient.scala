package skylands.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.{ModelLoadingPlugin, ModelModifier}
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.resources.ResourceLocation
import skylands.SkylandsCommon
import skylands.client.SkylandsClient
import skylands.fabric.client.CloudBakedModel

// See SkylandsFabric — the Scala adapter needs a singleton object.
object SkylandsFabricClient extends ClientModInitializer:
  override def onInitializeClient(): Unit =
    SkylandsClient.init()
    installCloudBakedModel()

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

