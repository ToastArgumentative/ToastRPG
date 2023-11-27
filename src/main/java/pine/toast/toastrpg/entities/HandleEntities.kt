package pine.toast.toastrpg.entities

import org.bukkit.block.Block
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer
import pine.toast.toastrpg.Extras
import pine.toast.toastrpg.Keys
import pine.toast.toastrpg.ToastRPG
import pine.toast.toastrpg.events.*
import pine.toast.toastrpg.monsters.Monster
import pine.toast.toastrpg.monsters.MonsterType
import pine.toast.toastrpg.playerutils.PlayerAttributes
import pine.toast.toastrpg.playerutils.PlayerDirection

class HandleEntities : Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    private fun monsterSpawnCall(event: EntitySpawnEvent) {
        if (event.entity !is LivingEntity) return
        if (!ToastRPG.getMonsterFactory()!!.isMonster(event.entity as LivingEntity)) return

        val entityData: PersistentDataContainer = event.entity.persistentDataContainer
        val entity: LivingEntity = event.entity as LivingEntity
        val monster: Monster = ToastRPG.getMonsterFactory()!!.getMonster(entity)
        val monsterType: MonsterType = entityData.get(Keys.MONSTER_TYPE, ToastRPG.getAdapterManager()!!.monsterTypeAdapter)!!

        ToastRPG.getPassedPlugin()!!.server.pluginManager.callEvent(MonsterSpawnEvent(entity, monster, monsterType))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun monsterTargetCall(event: EntityTargetLivingEntityEvent) {
        if (event.entity !is LivingEntity) return
        if (!ToastRPG.getMonsterFactory()!!.isMonster(event.entity as LivingEntity)) return
        if (event.target !is Player) return

        val player: Player = event.target as Player
        val entity: LivingEntity = event.entity as LivingEntity
        val monster: Monster = ToastRPG.getMonsterFactory()!!.getMonster(entity)
        val monsterType: MonsterType = entity.persistentDataContainer.get(Keys.MONSTER_TYPE, ToastRPG.getAdapterManager()!!.monsterTypeAdapter)!!

        val distance: Double = Extras.calculateDistanceFrom(entity, player)

        ToastRPG.getPassedPlugin()!!.server.pluginManager.callEvent(MonsterTargetPlayerEvent(monster, monsterType, distance, player ))

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun monsterDeathCall(event: EntityDeathEvent) {
        if(!ToastRPG.getMonsterFactory()!!.isMonster(event.entity)) return

        val monster: Monster = ToastRPG.getMonsterFactory()!!.getMonster(event.entity)
        val monsterType: MonsterType = event.entity.persistentDataContainer.get(Keys.MONSTER_TYPE, ToastRPG.getAdapterManager()!!.monsterTypeAdapter)!!

        ToastRPG.getPassedPlugin()!!.server.pluginManager.callEvent(MonsterDeathEvent(event.entity, monster, monsterType))
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun playerClickCall(event: PlayerInteractEvent) {
        val player: Player = event.player
        val mainHand: ItemStack = player.inventory.itemInMainHand
        val offHand: ItemStack = player.inventory.itemInOffHand
        val block: Block = player.getTargetBlock(null, 5)
        val direction = PlayerDirection(player.yaw.toDouble())
        val isSneaking: Boolean = player.isSneaking

        if (event.action.isLeftClick) {
            ToastRPG.getPassedPlugin()!!.server.pluginManager.callEvent(PlayerLeftClickEvent(player, mainHand, offHand, block, direction, isSneaking))
        } else if (event.action.isRightClick) {
            ToastRPG.getPassedPlugin()!!.server.pluginManager.callEvent(PlayerRightClickEvent(player, mainHand, offHand, block, direction, isSneaking))
        }
    }

    @EventHandler
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        PlayerAttributes.setMana(event.player, 0.0)
    }
}
