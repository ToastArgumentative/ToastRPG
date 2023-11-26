package pine.toast.toastrpg.monsters

import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import pine.toast.toastrpg.ToastRPG
import pine.toast.toastrpg.entities.EntityHandler
import pine.toast.toastrpg.events.MonsterDeathEvent
import pine.toast.toastrpg.events.MonsterSpawnEvent
import pine.toast.toastrpg.events.MonsterTargetPlayerEvent

class MonsterFactory : Listener {
    private val monsters: MutableMap<Monster, EntityHandler> = HashMap()


    /**
     * Marks a monster as a monster.
     * And registers their handlers.
     * @param monster The monster to mark.
     */
    fun markMonster(monster: Monster) {
        monsters[monster] = monster.getMonsterHandler()
    }

    /**
     * Checks if a living entity is a monster.
     * @param livingEntity The living entity to check.
     * @return True if the living entity is a monster.
     */
    fun isMonster(livingEntity: LivingEntity): Boolean {
        return monsters.keys.any { it.getLivingEntity() == livingEntity }
    }

    /**
     * Gets the monster from a living entity.
     * @param livingEntity The living entity to get the monster from.
     */
    fun getMonster(livingEntity: LivingEntity): Monster {
        return monsters.keys.first { it.getLivingEntity() == livingEntity }
    }


    private fun handleMonsterSpawn(event: MonsterSpawnEvent) {
        val handler = monsters[event.monster]
        handler?.onMonsterSpawn(event)
    }

    private fun handleMonsterDeath(event: MonsterDeathEvent) {
        val handler = monsters[event.getMonster()]
        handler?.onMonsterDeath(event)
    }

    private fun handlerMonsterTarget(event: MonsterTargetPlayerEvent) {
        val handler = monsters[event.getMonster()]
        handler?.onMonsterTarget(event)
    }


    @EventHandler
    private fun onMonsterSpawn(event: MonsterSpawnEvent) {
        ToastRPG.getMonsterFactory()!!.handleMonsterSpawn(event)
    }

    @EventHandler
    private fun onMonsterDeath(event: MonsterDeathEvent) {
        ToastRPG.getMonsterFactory()!!.handleMonsterDeath(event)
    }


    @EventHandler
    private fun onMonsterTarget(event: MonsterTargetPlayerEvent) {
        ToastRPG.getMonsterFactory()!!.handlerMonsterTarget(event)
    }

}