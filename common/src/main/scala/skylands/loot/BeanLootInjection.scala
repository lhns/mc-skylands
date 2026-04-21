package skylands.loot

import dev.architectury.event.events.common.LootEvent
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraft.world.level.storage.loot.{BuiltInLootTables, LootPool, LootTable}
import skylands.registry.SkylandsItems

// Faithful port of the 1.12.2 Skylands.onLootTableLoad loot injection.
// Adds a single-entry bean pool (rolls=1) to the vanilla loot tables listed
// in the original mod. Uses Architectury's cross-loader LootEvent so the
// same code drives both Fabric and NeoForge at runtime.
object BeanLootInjection:
  // 1.12.2 mapped "chests/village_blacksmith" to what was then the single
  // village blacksmith chest. The closest modern equivalent is the weaponsmith.
  private val beanChests: Seq[ResourceKey[LootTable]] = Seq(
    BuiltInLootTables.STRONGHOLD_CORRIDOR,
    BuiltInLootTables.SIMPLE_DUNGEON,
    BuiltInLootTables.NETHER_BRIDGE,
    BuiltInLootTables.IGLOO_CHEST,
    BuiltInLootTables.ABANDONED_MINESHAFT,
    BuiltInLootTables.STRONGHOLD_CROSSING,
    BuiltInLootTables.JUNGLE_TEMPLE,
    BuiltInLootTables.DESERT_PYRAMID,
    BuiltInLootTables.STRONGHOLD_LIBRARY,
    BuiltInLootTables.VILLAGE_WEAPONSMITH,
    BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER,
    BuiltInLootTables.END_CITY_TREASURE,
    BuiltInLootTables.SPAWN_BONUS_CHEST
  )

  private val targets: java.util.Set[ResourceKey[LootTable]] =
    val set = new java.util.HashSet[ResourceKey[LootTable]]()
    beanChests.foreach(set.add)
    set

  def register(): Unit =
    LootEvent.MODIFY_LOOT_TABLE.register((id, ctx, builtin) =>
      if targets.contains(id) then
        ctx.addPool(
          LootPool
            .lootPool()
            .setRolls(ConstantValue.exactly(1f))
            .add(LootItem.lootTableItem(SkylandsItems.BEAN.get()))
        )
    )
