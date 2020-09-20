package me.elsiff.morefish.item

import me.elsiff.morefish.fishing.Fish
import me.elsiff.morefish.fishing.FishTypeTable
import org.bukkit.NamespacedKey
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.tags.ItemTagType

/**
 * Created by elsiff on 2019-01-03.
 */
class FishItemTagReader(
    private val fishTypeTable: FishTypeTable,
    private val fishTypeKey: NamespacedKey,
    private val fishLengthKey: NamespacedKey
) {
    fun canRead(itemMeta: ItemMeta): Boolean {
        return itemMeta.customTagContainer.let { tags ->
            tags.hasCustomTag(fishTypeKey, ItemTagType.STRING) && tags.hasCustomTag(fishLengthKey, ItemTagType.DOUBLE)
        }
    }

    fun read(itemMeta: ItemMeta): Fish {
        return itemMeta.customTagContainer.let { tags ->
            require(tags.hasCustomTag(fishTypeKey, ItemTagType.STRING)) { "Item meta must have fish type tag" }
            require(tags.hasCustomTag(fishLengthKey, ItemTagType.DOUBLE)) { "Item meta must have fish length tag" }

            val typeName = tags.getCustomTag(fishTypeKey, ItemTagType.STRING)
            val type = fishTypeTable.types.find { it.name == typeName }
                ?: throw IllegalStateException("Fish type doesn't exist")
            val length = tags.getCustomTag(fishLengthKey, ItemTagType.DOUBLE)
            Fish(type, length!!)
        }
    }
}