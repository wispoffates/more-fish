package me.elsiff.morefish.hooker

import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.elsiff.morefish.MoreFish
import me.elsiff.morefish.configuration.format.Format
import me.elsiff.morefish.fishing.competition.FishingCompetition
import org.bukkit.OfflinePlayer

/**
 * Created by elsiff on 2019-01-24.
 */
class PlaceholderApiHooker : PluginHooker {
    override val pluginName = "PlaceholderAPI"
    override var hasHooked = false

    override fun hook(plugin: MoreFish) {
        MoreFishPlaceholder(plugin).register()
        Format.init(this)
        hasHooked = true
    }

    fun tryReplacing(string: String, player: OfflinePlayer? = null): String {
        return PlaceholderAPI.setPlaceholders(player, string)
    }

    class MoreFishPlaceholder(moreFish: MoreFish) : PlaceholderExpansion() {
        private val competition: FishingCompetition = moreFish.competition

        override fun canRegister() = true
        override fun getVersion() = "1.0.0"
        override fun getAuthor() = "wispoffates"
        override fun getIdentifier() = "morefish"
        

        override fun onRequest(player: OfflinePlayer?, identifier: String): String? {
            return when {
                identifier.startsWith("top_player_") -> {
                    val number = identifier.replace("top_player_", "").toInt()
                    if (competition.ranking.size >= number)
                        competition.recordOf(number).fisher.name
                    else
                        "no one"
                }
                identifier.startsWith("top_fish_length_") -> {
                    val number = identifier.replace("top_fish_length_", "").toInt()
                    if (competition.ranking.size >= number)
                        competition.recordOf(number).fish.length.toString()
                    else
                        "0.0"
                }
                identifier.startsWith("top_fish_") -> {
                    val number = identifier.replace("top_fish_", "").toInt()
                    if (competition.ranking.size >= number)
                        competition.recordOf(number).fish.type.name
                    else
                        "none"
                }
                identifier == "rank" -> {
                    require(player != null) { "'rank' placeholder requires a player" }
                    if (competition.containsContestant(player)) {
                        val record = competition.recordOf(player)
                        competition.rankNumberOf(record).toString()
                    } else {
                        "0"
                    }
                }
                identifier == "fish_length" -> {
                    require(player != null) { "'fish_length' placeholder requires a player" }
                    if (competition.containsContestant(player))
                        competition.recordOf(player).fish.length.toString()
                    else
                        "0.0"
                }
                identifier == "fish" -> {
                    require(player != null) { "'fish' placeholder requires a player" }
                    if (competition.containsContestant(player))
                        competition.recordOf(player).fish.type.name
                    else
                        "none"
                }
                else -> null
            }
        }
    }
}