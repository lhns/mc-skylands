package org.lolhens.skylands.ops

import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.entity.{Entity, EntityList}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.SPacketRespawn
import net.minecraft.util.math.Vec3d
import net.minecraft.world.{DimensionType, World, WorldServer}
import org.lolhens.skylands.ops.EntityOps._
import org.lolhens.skylands.world.SimpleTeleporter

import scala.language.implicitConversions

class EntityOps(val self: Entity) extends AnyVal {
  def setPositionVectorAndRotation(position: Vec3d, yaw: Float, pitch: Float): Unit = {
    self.setPositionAndRotation(position.x, position.y, position.z, yaw, pitch)
  }

  def setPositionVector(position: Vec3d): Unit = {
    self.setPositionVectorAndRotation(position, self.rotationYaw, self.rotationPitch)
  }

  def getMotionVector: Vec3d = new Vec3d(self.motionX, self.motionY, self.motionZ)

  def setMotionVector(motion: Vec3d): Unit = {
    self.motionX = motion.x
    self.motionY = motion.y
    self.motionZ = motion.z
  }

  def worldServer: WorldServer = self.world match {
    case worldServer: WorldServer => worldServer
  }

  def teleportTo(position: Vec3d,
                 motion: Vec3d): Unit =
    teleportTo(position, motion, self.rotationYaw, self.rotationPitch)

  def teleportTo(position: Vec3d,
                 motion: Vec3d,
                 yaw: Float, pitch: Float): Unit =
    teleportTo(self.world.provider.getDimensionType, position, motion, yaw, pitch)

  def teleportTo(dimension: DimensionType,
                 position: Vec3d,
                 motion: Vec3d): Unit =
    teleportTo(dimension, position, motion, self.rotationYaw, self.rotationPitch)

  def teleportTo(dimension: DimensionType,
                 position: Vec3d,
                 motion: Vec3d,
                 yaw: Float, pitch: Float): Unit = if (!self.world.isRemote) {
    lazy val server = self.getServer

    lazy val dimensionWorld = server.getWorld(dimension.getId)

    lazy val teleporter = new SimpleTeleporter(
      dimensionWorld,
      position,
      motion,
      yaw, pitch
    )

    def sameDimension = dimension == self.world.provider.getDimensionType

    if (sameDimension) {
      self.setPositionVectorAndRotation(position, yaw, pitch)
      self.setMotionVector(motion)
    } else {
      if (self.world.isRemote)
        self match {
          case _: EntityPlayer =>
          case entity: Entity =>
          //entity.changeDimension(dimension.getId)
          //self.setPositionVector(position)
          //self.setMotionVector(motion)
        }
      else
        self match {
          case player: EntityPlayerMP =>
            server.getPlayerList.transferPlayerToDimension(player, dimension.getId, teleporter)
          case entity =>
            //self.world.removeEntity(self)
            //self.isDead = false
            server.getPlayerList.transferEntityToWorld(entity, dimension.getId, self.worldServer, dimensionWorld, teleporter)
            self.setPositionVectorAndRotation(position, yaw, pitch)
            self.setMotionVector(motion)
        }
    }
  }

  def removeFromWorld(world: World): Unit = {
    self match {
      case player: EntityPlayer =>
        player.closeScreen()
        world.playerEntities.remove(player)
        world.updateAllPlayersSleepingFlag()

        lazy val (chunkX, chunkY, chunkZ) = (self.chunkCoordX, self.chunkCoordY, self.chunkCoordZ)
        lazy val chunk = world.getChunkFromChunkCoords(chunkX, chunkZ)
        if (self.addedToChunk && world.getChunkProvider.isChunkGeneratedAt(chunkX, chunkZ)) {
          chunk.removeEntity(self)
          chunk.setModified(true)
        }

        world.loadedEntityList.remove(self)
        world.onEntityRemoved(self)
    }
  }

  def toNBT: NBTTagCompound = {
    val dead = self.isDead
    self.isDead = false

    val nbt = new NBTTagCompound()
    val entityString = EntityList.getEntityString(self)
    nbt.setString("id", entityString)
    self.writeToNBT(nbt)

    self.isDead = dead

    nbt
  }

  def copy(newWorld: World): Entity = {
    val nbt = self.toNBT
    val newEntity = EntityList.createEntityFromNBT(nbt, newWorld)

    newEntity.dimension = newWorld.provider.getDimension
    newWorld.spawnEntity(newEntity)
    newEntity.setWorld(newWorld)

    newEntity
  }

  def teleportEntity(newWorld: World, position: Vec3d): Entity = {
    if (self.isRiding) {
      val mount = self.getRidingEntity
      self.startRiding(null)

      val newMount = mount.teleportEntity(newWorld, position)
      val newSelf = self.teleportEntity(newWorld, position)

      newSelf match {
        case player: EntityPlayerMP =>
          newWorld.updateEntity(player)
        case _ =>
      }

      newSelf.startRiding(newMount)

      newSelf
    } else {
      val oldWorld = self.world
      val sameWorld = newWorld == oldWorld
      val newDimension = newWorld.provider.getDimensionType

      oldWorld.updateEntityWithOptionalForce(self, false)

      self match {
        case player: EntityPlayerMP =>
          player.closeScreen()
          if (!sameWorld) {
            player.dimension = newDimension.getId
            val respawnPacket = new SPacketRespawn(
              newDimension.getId,
              newWorld.getDifficulty,
              newWorld.getWorldInfo.getTerrainType,
              player.interactionManager.getGameType
            )
            player.connection.sendPacket(respawnPacket)
          }
        case _ =>
      }

      oldWorld.removeEntityDangerously(self)

      self match {
        case player: EntityPlayer =>
          newWorld.spawnEntity(player)
          player.setWorld(newWorld)

        case entity =>
          entity.copy(newWorld)
      }
      ???
    }
  }
}

object EntityOps {
  implicit def fromEntity(entity: Entity): EntityOps = new EntityOps(entity)
}
