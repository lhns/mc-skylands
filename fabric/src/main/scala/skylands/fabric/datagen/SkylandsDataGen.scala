package skylands.fabric.datagen

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder
import net.fabricmc.fabric.api.datagen.v1.provider.{FabricBlockLootTableProvider, FabricLanguageProvider, FabricModelProvider}
import net.fabricmc.fabric.api.datagen.v1.{DataGeneratorEntrypoint, FabricDataGenerator, FabricDataOutput}
import net.minecraft.core.HolderLookup
import net.minecraft.data.models.blockstates.{MultiVariantGenerator, PropertyDispatch, Variant, VariantProperties}
import net.minecraft.data.models.model.{DelegatedModel, ModelLocationUtils, ModelTemplates, TextureMapping}
import net.minecraft.data.models.{BlockModelGenerators, ItemModelGenerators}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import skylands.SkylandsCommon.ModId
import skylands.block.BeanstalkBlock
import skylands.registry.SkylandsBlocks

import java.util.concurrent.CompletableFuture

// Fabric datagen entrypoint — wired in fabric.mod.json as `fabric-datagen`.
// Runs under :fabric:runDatagen, writes JSONs to common/src/main/generated/
// (configured in fabric/build.gradle's `fabricApi.configureDataGeneration`).
//
// Covers the mod's mundane data: block states, block/item models, block
// loot tables, en_us translations. The dimension_type/dimension JSONs and
// pack.mcmeta stay hand-written (worldgen datagen is low-ROI here) and the
// gametest empty.nbt template is generated from scripts/gen_empty_structure.py.
// `object` not `class` — Krysztal's ScalaLanguageAdapter reflectively reads
// MODULE$ on the companion class. A plain class fails with
// "Unable to instantiate mod skylands 0.3.0-SNAPSHOT".
object SkylandsDataGen extends DataGeneratorEntrypoint:
  override def onInitializeDataGenerator(gen: FabricDataGenerator): Unit =
    val pack = gen.createPack()
    pack.addProvider((out: FabricDataOutput) => new SkylandsModelProvider(out))
    pack.addProvider((out: FabricDataOutput, reg: CompletableFuture[HolderLookup.Provider]) =>
      new SkylandsBlockLootProvider(out, reg)
    )
    pack.addProvider((out: FabricDataOutput, reg: CompletableFuture[HolderLookup.Provider]) =>
      new SkylandsLanguageProvider(out, reg)
    )

private final class SkylandsModelProvider(out: FabricDataOutput) extends FabricModelProvider(out):

  override def generateBlockStateModels(gen: BlockModelGenerators): Unit =
    // Cloud + bean are plain cube_all blocks; createTrivialCube handles the
    // blockstate JSON, the block model (parent cube_all), and the item model
    // (parent = the block model) all at once.
    gen.createTrivialCube(SkylandsBlocks.CLOUD.get())
    gen.createTrivialCube(SkylandsBlocks.BEAN.get())

    // Beanstalk has a boolean "center" property → two cube_column models.
    // Vanilla BlockModelGenerators has no built-in helper for this pattern.
    val beanstalk = SkylandsBlocks.BEANSTALK.get()
    val mainModel = createPillarModel(beanstalk, "", "block/beanstalk", "block/beanstalk_side", gen)
    val centerModel = createPillarModel(beanstalk, "_center", "block/beanstalk_center", "block/beanstalk_center_side", gen)

    gen.blockStateOutput.accept(
      MultiVariantGenerator
        .multiVariant(beanstalk)
        .`with`(
          PropertyDispatch
            .property(BeanstalkBlock.CENTER)
            .select(java.lang.Boolean.FALSE, Variant.variant().`with`(VariantProperties.MODEL, mainModel))
            .select(java.lang.Boolean.TRUE, Variant.variant().`with`(VariantProperties.MODEL, centerModel))
        )
    )

    // Beanstalk item model — points at the default (center=false) block model.
    // Emitted here rather than in generateItemModels because ItemModelGenerators'
    // output consumer is private; the two generators share the same JSON sink
    // (gen.modelOutput), so emitting models/item/beanstalk from here writes the
    // same file either side would write.
    gen.modelOutput.accept(
      ModelLocationUtils.getModelLocation(beanstalk.asItem()),
      new DelegatedModel(mainModel)
    )

  override def generateItemModels(gen: ItemModelGenerators): Unit =
    // Cloud + bean item models are emitted by createTrivialCube above.
    // Beanstalk's item model is emitted in generateBlockStateModels (see note).
    ()

  private def createPillarModel(
      block: Block,
      suffix: String,
      endTexture: String,
      sideTexture: String,
      gen: BlockModelGenerators
  ): ResourceLocation =
    // TextureMapping.column(a, b) assigns a → SIDE, b → END. The 1.12.2
    // beanstalk used the plain "beanstalk" texture on the top/bottom caps
    // and "beanstalk_side" on the four side faces.
    val mapping = TextureMapping.column(rl(sideTexture), rl(endTexture))
    ModelTemplates.CUBE_COLUMN.createWithSuffix(block, suffix, mapping, gen.modelOutput)

  private def rl(path: String): ResourceLocation =
    ResourceLocation.fromNamespaceAndPath(ModId, path)

private final class SkylandsBlockLootProvider(
    out: FabricDataOutput,
    registryLookup: CompletableFuture[HolderLookup.Provider]
) extends FabricBlockLootTableProvider(out, registryLookup):
  override def generate(): Unit =
    dropSelf(SkylandsBlocks.CLOUD.get())
    dropSelf(SkylandsBlocks.BEAN.get())
    dropSelf(SkylandsBlocks.BEANSTALK.get())

private final class SkylandsLanguageProvider(
    out: FabricDataOutput,
    registryLookup: CompletableFuture[HolderLookup.Provider]
) extends FabricLanguageProvider(out, registryLookup):
  override def generateTranslations(lookup: HolderLookup.Provider, tb: TranslationBuilder): Unit =
    tb.add("itemGroup.skylands", "Skylands")
    tb.add(SkylandsBlocks.CLOUD.get(), "Cloud")
    tb.add(SkylandsBlocks.BEAN.get(), "Magic Bean")
    tb.add(SkylandsBlocks.BEANSTALK.get(), "Beanstalk")
