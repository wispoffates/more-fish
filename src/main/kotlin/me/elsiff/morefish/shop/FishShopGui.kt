package me.elsiff.morefish.shop

import me.elsiff.morefish.configuration.Lang
import me.elsiff.morefish.fishing.Fish
import me.elsiff.morefish.gui.ChestInventoryGui
import me.elsiff.morefish.gui.state.ComponentClickState
import me.elsiff.morefish.gui.state.GuiCloseState
import me.elsiff.morefish.gui.state.GuiDragState
import me.elsiff.morefish.gui.state.GuiItemChangeState
import me.elsiff.morefish.item.FishItemStackConverter
import me.elsiff.morefish.item.edit
import me.elsiff.morefish.util.InventoryUtils
import me.elsiff.morefish.util.OneTickScheduler
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * Created by elsiff on 2019-01-03.
 */
class FishShopGui(
    private val shop: FishShop,
    private val converter: FishItemStackConverter,
    private val oneTickScheduler: OneTickScheduler,
    private val user: Player
) : ChestInventoryGui(user.server, 4, Lang.text("shop-gui-title")) {
    private val bottomBarSlots: List<Int> = slotsOf(minX..maxX, maxY)
    private val priceIconSlot: Int = slotOf(centerX, maxY)
    private val fishSlots: List<Int> = slotsOf(minX..maxX, minY until maxY)
    private val totalPrice: Double
        get() {
            var sum = 0.0
            allFish().forEach { fish, itemStack ->
                repeat(itemStack.amount) {
                    sum += shop.priceOf(fish)
                }
            }
            return sum
        }

    init {
        val bottomBarIcon = ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
        bottomBarIcon.edit<ItemMeta> { displayName = " " }
        for (slot in bottomBarSlots) {
            inventory.setItem(slot, bottomBarIcon)
        }

        updatePriceIcon(0.0)
        controllableSlots.addAll(fishSlots)
    }

    override fun handleItemChange(state: GuiItemChangeState) {
        oneTickScheduler.scheduleLater(this) { updatePriceIcon() }
    }

    override fun handleComponentClick(state: ComponentClickState) {
        if (state.slot == priceIconSlot) {
            val allFish = allFish()
            if (allFish.isEmpty()) {
                user.sendMessage(Lang.text("shop-no-fish"))
            } else {
                val totalPrice = totalPrice
                allFish.forEach { fish, itemStack ->
                    repeat(itemStack.amount) {
                        shop.sell(user, fish)
                    }
                    itemStack.amount = 0
                }
                updatePriceIcon(0.0)
                val msg = Lang.format("shop-sold").replace("%price%" to totalPrice.toString()).output
                user.sendMessage(msg)
            }
        }
    }

    override fun handleDrag(state: GuiDragState) {
        inventory.setItem(priceIconSlot, InventoryUtils.emptyStack())
        oneTickScheduler.scheduleLater(this) { updatePriceIcon() }
    }

    override fun handleClose(state: GuiCloseState) {
        oneTickScheduler.cancelAllOf(this)
        dropAllFish()
    }

    private fun dropAllFish() {
        allFish().forEach { _, itemStack ->
            user.world.dropItem(user.location, itemStack.clone())
        }
    }

    private fun allFish(): Map<Fish, ItemStack> {
        return fishSlots
            .mapNotNull { slot -> inventory.getItem(slot) ?: null }
            .filter { itemStack -> converter.isFish(itemStack) }
            .map { itemStack -> Pair(converter.fish(itemStack), itemStack) }
            .toMap()
    }

    private fun updatePriceIcon(price: Double = totalPrice) {
        val emeraldIcon = ItemStack(Material.EMERALD)
        emeraldIcon.edit<ItemMeta> {
            displayName = Lang.format("shop-emerald-icon-name")
                .replace("%price%" to price.toString())
                .output
        }
        inventory.setItem(priceIconSlot, emeraldIcon)
    }
}