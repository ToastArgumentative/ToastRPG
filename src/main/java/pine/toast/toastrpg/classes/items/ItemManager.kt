package pine.toast.toastrpg.classes.items

import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.plugin.PluginAwareness.Flags
import pine.toast.toastrpg.Keys
import pine.toast.toastrpg.ToastRPG
import pine.toast.toastrpg.colorapi.Colors
import pine.toast.toastrpg.events.PlayerLeftClickEvent
import pine.toast.toastrpg.events.PlayerRightClickEvent

class ItemManager : Listener {
    private val activeItems: HashMap<ItemStack, ItemHandler?> = HashMap()

    init {
        ToastRPG.getPassedPlugin()!!.logger.info(" - ItemManager ~ Started")
    }

    /**
     * This function will register an item with the item manager.
     * @param itemStack The item to register.
     * @param handler The handler class for the item.
     * @see ItemHandler
     */
    private fun registerHandledItem(itemStack: ItemStack, handler: Class<out ItemHandler?>) {
        activeItems[itemStack] = handler.getDeclaredConstructor().newInstance()
    }

    /**
     * This function will unregister an item with the item manager.
     * @param item The item to unregister.
     */
    private fun unregisterHandledItem(item: ItemStack) {
        activeItems.remove(item)
    }

    /**
     * This function will check if an item is registered with the item manager.
     * @param item The item to check.
     * @return True if the item is registered, false otherwise.
     */
    private fun isItemRegistered(item: ItemStack): Boolean {
        return activeItems.containsKey(item)
    }


    /**
     * This function will create an item and register it with the item manager.
     * @param itemMaterialClass The item material class to create the item from.
     * @return The item stack of the created item.
     */
    fun createAndRegisterItem(itemMaterialClass: Class<out ItemMaterial>): ItemStack {
        val itemMaterial: ItemMaterial = itemMaterialClass.getDeclaredConstructor().newInstance()
        val itemClass: Class<out Item> = itemMaterial.getItem()
        val item: Item = itemClass.getDeclaredConstructor().newInstance()
        val material: Material = itemMaterial.getMaterial()
        val itemStack = ItemStack(material)
        val itemMeta: ItemMeta = itemStack.itemMeta

        itemMeta.setCustomModelData(item.getCustomModelData())
        itemMeta.setDisplayName(item.getItemName())
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        
        val lore = ArrayList<String>()
        lore.add(Colors.GOLD + "--- Item Description ---" + Colors.RESET)
        lore.add("")
        lore.add("")
        lore.add("")
        lore.add(Colors.GOLD + "--- Item Stats ---" + Colors.RESET)

        lore.add(item.getItemStats().toString())

        val container: PersistentDataContainer = itemMeta.persistentDataContainer
        container.set(Keys.ITEM, ToastRPG.getAdapterManager()!!.itemAdapter, item)

        val damageModifier = AttributeModifier("damage", itemMaterial.getDamage(), AttributeModifier.Operation.ADD_NUMBER)
        val attackSpeedModifier = AttributeModifier("attackSpeed", itemMaterial.getAttackSpeed(), AttributeModifier.Operation.ADD_NUMBER)
        val armorModifier = AttributeModifier("armor", itemMaterial.getArmor(), AttributeModifier.Operation.ADD_NUMBER)
        val healthModifier = AttributeModifier("health", itemMaterial.getHealth(), AttributeModifier.Operation.ADD_NUMBER)
        val movementModifier = AttributeModifier("movement", itemMaterial.getMovementSpeed(), AttributeModifier.Operation.ADD_NUMBER)

        itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, damageModifier)
        itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attackSpeedModifier)
        itemMeta.addAttributeModifier(Attribute.GENERIC_ARMOR, armorModifier)
        itemMeta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, healthModifier)
        itemMeta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, movementModifier)

        itemStack.setItemMeta(itemMeta)

        registerHandledItem(itemStack, item.getEventHandlerClass())
        return itemStack
    }

    private fun handleRightClick(item: ItemStack, event: PlayerRightClickEvent) {
        val handler = activeItems[item]
        handler?.onPlayerRightClick(event)
    }

    private fun handleLeftClick(item: ItemStack, event: PlayerLeftClickEvent) {
        val handler = activeItems[item]
        handler?.onPlayerLeftClick(event)
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun onItemBreak(event: PlayerItemBreakEvent) {
        val item: ItemStack = event.brokenItem
        if (isItemRegistered(item)) unregisterHandledItem(item)
    }

    @EventHandler
    private fun onRightClick(event: PlayerRightClickEvent) {
        val item: ItemStack = event.getMainHand()
        val itemContainer: PersistentDataContainer = item.itemMeta.persistentDataContainer

        if (itemContainer.has(Keys.ITEM)) {
            val itemObject: Item? = ToastRPG.getAdapterManager()?.let { itemContainer.get(Keys.ITEM, it.itemAdapter) }
            if (itemObject != null) {
                this.handleRightClick(item, event)
            }
        }
    }

    @EventHandler
    private fun onLeftClick(event: PlayerLeftClickEvent) {
        val item: ItemStack = event.getMainHand()
        val container: PersistentDataContainer = item.itemMeta.persistentDataContainer
        if (container.has(Keys.ITEM)) {
            val itemObject = container.get(Keys.ITEM, ToastRPG.getAdapterManager()!!.itemAdapter)
            if (itemObject != null) {
                this.handleLeftClick(item, event)
            }
        }
    }
}
