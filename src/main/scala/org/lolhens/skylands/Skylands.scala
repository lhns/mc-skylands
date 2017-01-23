package org.lolhens.skylands

import java.io.File
import java.lang.reflect.{Field, Method}

import com.google.common.collect.Sets
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.chunk.{ChunkRenderDispatcher, RenderChunk}
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemBlock}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.DimensionType
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.storage.loot.conditions.LootCondition
import net.minecraft.world.storage.loot.functions.LootFunction
import net.minecraft.world.storage.loot.{LootEntry, LootEntryItem, LootPool, RandomValueRange}
import net.minecraftforge.common.{DimensionManager, MinecraftForge}
import net.minecraftforge.event.LootTableLoadEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.lolhens.skylands.block.{BlockBean, BlockBeanStem, BlockCloud}
import org.lolhens.skylands.feature.{FallIntoOverworld, FeatherGliding}
import org.lolhens.skylands.tileentities.TileEntityBeanPlant
import org.lolhens.skylands.world.WorldProviderSkylands

import scala.collection.JavaConversions._

/**
  * Created by pierr on 02.01.2017.
  */
class Skylands(configFile: File) {
  val config = new Config(configFile)

  val blockBeanStem = new BlockBeanStem()
  GameRegistry.register(blockBeanStem.setRegistryName("beanstem"))
  GameRegistry.register(new ItemBlock(blockBeanStem).setRegistryName(blockBeanStem.getRegistryName))

  val blockBean = new BlockBean()
  GameRegistry.register(blockBean.setRegistryName("bean"))
  GameRegistry.register(new ItemBlock(blockBean).setRegistryName(blockBean.getRegistryName))
  GameRegistry.registerTileEntity(classOf[TileEntityBeanPlant], "bean_tile_entity")

  val blockCloud = new BlockCloud()
  GameRegistry.register(blockCloud.setRegistryName("cloud"))
  GameRegistry.register(new ItemBlock(blockCloud).setRegistryName(blockCloud.getRegistryName))

  val skylandsOverlap = 15

  val skylandsDimensionType: DimensionType = DimensionType.register("Skylands", "sky", config.dimensionId, classOf[WorldProviderSkylands], false)
  DimensionManager.registerDimension(config.dimensionId, skylandsDimensionType)

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  private val beanChests = List(
    "minecraft:chests/stronghold_corridor",
    "minecraft:chests/simple_dungeon",
    "minecraft:chests/nether_bridge",
    "minecraft:chests/igloo_chest",
    "minecraft:chests/abandoned_mineshaft",
    "minecraft:chests/stronghold_crossing",
    "minecraft:chests/jungle_temple",
    "minecraft:chests/desert_pyramid",
    "minecraft:chests/stronghold_library",
    "minecraft:chests/village_blacksmith",
    "minecraft:chests/jungle_temple_dispenser",
    "minecraft:chests/end_city_treasure",
    "minecraft:chests/spawn_bonus_chest"
  )

  @SubscribeEvent
  def onLootTableLoad(event: LootTableLoadEvent): Unit = {
    def LootPool(name: String, entries: List[LootEntry], conditions: List[LootCondition] = Nil, rolls: Range, bonusRolls: Range = 0 to 0) =
      new LootPool(entries.toArray, conditions.toArray, new RandomValueRange(rolls.start, rolls.end), new RandomValueRange(bonusRolls.start, bonusRolls.end), name)

    def LootEntryItem(item: Item, weight: Int = 1, quality: Int = 0, functions: List[LootFunction] = Nil, conditions: List[LootCondition] = Nil) =
      new LootEntryItem(item, weight, quality, functions.toArray, conditions.toArray, item.getRegistryName.toString)

    if (beanChests.contains(event.getName.toString))
      event.getTable.addPool(
        LootPool(
          name = "skylands",
          rolls = 1 to 1,
          entries = List(LootEntryItem(
            Item.getItemFromBlock(blockBean)
          ))
        )
      )
  }

  def keepSkylandsLoaded(): Unit = {
    skylandsDimensionType.setLoadSpawn(true)
    _keepSkylandsLoaded = true
  }

  private var _keepSkylandsLoaded = false

  private val m = classOf[RenderGlobal].getDeclaredField("chunksToUpdate")

  @SubscribeEvent
  def onWorldTick(event: TickEvent.WorldTickEvent): Unit = {
    if (event.world.provider.getDimensionType == skylandsDimensionType) {
      m.setAccessible(true)
      m.set(Minecraft.getMinecraft.renderGlobal, Sets.newLinkedHashSet[RenderChunk])
      if (_keepSkylandsLoaded)
        _keepSkylandsLoaded = false
      else if (skylandsDimensionType.shouldLoadSpawn())
        skylandsDimensionType.setLoadSpawn(false)
    }
  }

  lazy val setLightUpdatesField: Field = {
    val result = classOf[RenderGlobal].getDeclaredField("setLightUpdates")
    result.setAccessible(true)
    result
  }

  lazy val renderDispatcherField: Field = {
    val result = classOf[RenderGlobal].getDeclaredField("renderDispatcher")
    result.setAccessible(true)
    result
  }

  object markBlocksForUpdate {
    private lazy val markBlocksForUpdateMethod: Method = {
      val result = classOf[RenderGlobal].getDeclaredMethod("markBlocksForUpdate", toObjectClassSeq((0 until 6).map(_ => Integer.TYPE) :+ java.lang.Boolean.TYPE: _*): _*)
      result.setAccessible(true)
      result
    }

    def apply(renderGlobal: RenderGlobal, minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, update: Boolean): Unit =
      markBlocksForUpdateMethod.invoke(renderGlobal, toObjectSeq(minX, minY, minZ, maxX, maxY, maxZ, update): _*)
  }

  object relightBlock {
    private lazy val relightBlockMethod: Method = {
      val result = classOf[Chunk].getDeclaredMethod("relightBlock", toObjectClassSeq((0 until 3).map(_ => classOf[Int]): _*): _*)
      result.setAccessible(true)
      result
    }

    def apply(chunk: Chunk, pos: BlockPos): Unit =
      relightBlockMethod.invoke(chunk, toObjectSeq(pos.getX & 15, pos.getY, pos.getZ & 15): _*)
  }

  def toObjectClassSeq(seq: Class[_]*): Seq[Class[_]] = seq.map {
    case boolean if boolean == classOf[Boolean] => java.lang.Boolean.TYPE
    case int if int == classOf[Int] => Integer.TYPE
    case clazz => clazz
  }

  def toObjectSeq(seq: Any*): Seq[Object] = seq.map {
    case boolean: Boolean => Boolean.box(boolean)
    case int: Int => Int.box(int)
    case value: AnyRef => value
  }

  var lightUpdates: Set[BlockPos] = Set.empty
  val lightUpdatesLock = new Object()

  @SubscribeEvent
  def onClientTick(event: TickEvent.ClientTickEvent): Unit = {
    if (event.phase == TickEvent.Phase.START) {
      val minecraft = Minecraft.getMinecraft
      if (minecraft.world != null) {
        if (!minecraft.isGamePaused) {
          val setLightUpdates = setLightUpdatesField.get(minecraft.renderGlobal).asInstanceOf[java.util.Set[BlockPos]].toSet
          val renderDispatcher = renderDispatcherField.get(minecraft.renderGlobal).asInstanceOf[ChunkRenderDispatcher]
          if (setLightUpdates.nonEmpty && !renderDispatcher.hasNoFreeRenderBuilders) {
            //lightUpdatesLock.synchronized(this.lightUpdates = setLightUpdates)
            //val lightUpdates = setLightUpdates.groupBy(_.getY).map(e => e._1 -> e._2.size)
            //println(lightUpdates.values.sum + "   " + lightUpdates.toList.sortBy(_._1).map(e => s"${e._1}:${e._2}").mkString(" "))
            setLightUpdates /*.take(10)*/ .foreach { pos =>
              //if (minecraft.world.getBlockState(pos).getBlock == Blocks.AIR)
                //minecraft.world.setBlockState(pos, Blocks.AIR.getDefaultState)
              //val chunk = minecraft.world.getChunkFromBlockCoords(pos)
              //relightBlock(chunk, pos)
              //markBlocksForUpdate(minecraft.renderGlobal, pos.getX - 1, pos.getY - 1, pos.getZ - 1, pos.getX + 1, pos.getY + 1, pos.getZ + 1, false)
            }
          }
        }
      }
    }
  }

  @SubscribeEvent
  def onServerTick(event: TickEvent.ServerTickEvent): Unit = {
    if (event.phase == TickEvent.Phase.END) {
      val minecraft = Minecraft.getMinecraft
      if (minecraft.world != null) {
        lightUpdatesLock.synchronized {
          if (lightUpdates.nonEmpty) println(lightUpdates.size)
          lightUpdates /*.take(10)*/ .foreach { pos =>
            if (minecraft.world.getBlockState(pos).getBlock == Blocks.AIR)
              minecraft.world.setBlockState(pos, Blocks.AIR.getDefaultState)
            val chunk = minecraft.world.getChunkFromBlockCoords(pos)
            relightBlock(chunk, pos)
            markBlocksForUpdate(minecraft.renderGlobal, pos.getX - 1, pos.getY - 1, pos.getZ - 1, pos.getX + 1, pos.getY + 1, pos.getZ + 1, false)
          }
          lightUpdates = Set.empty
        }
      }
    }
  }

  @SubscribeEvent
  def onPlayerTick(event: TickEvent.PlayerTickEvent): Unit = {
    val player = event.player

    player match {
      case player: EntityPlayerMP =>
        FallIntoOverworld.update(player)

      case _ =>
    }

    FeatherGliding.update(player)
  }
}
