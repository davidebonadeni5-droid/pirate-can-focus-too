package dev.mathieuburnat.piratefocus.journal

enum class Side { SPORT, BOISSON }

/** Une ligne du journal : une activité (côté muscles) ou une boisson (côté bouteilles). */
data class Category(
    val id: String,
    val side: Side,
    val label: String,
    val detail: String = "",
    /** Créée par l'utilisateur (on peut la supprimer). */
    val custom: Boolean = false,
)

/** Les lignes fournies d'office. On peut en ajouter d'autres, à soi. */
object Categories {
    val MEGA_SEANCE = Category("MEGA_SEANCE", Side.SPORT, "Méga séance", "pompes, tractions")
    val SALLE = Category("SALLE", Side.SPORT, "Salle", "muscu, machines")
    val GRIMPE = Category("GRIMPE", Side.SPORT, "Grimpe", "escalade, bloc")
    val ABDOS = Category("ABDOS", Side.SPORT, "P'tite séance abdos", "gainage, crunchs")
    val BIERE = Category("BIERE", Side.BOISSON, "Bière", "la pinte du marin")
    val COCKTAIL = Category("COCKTAIL", Side.BOISSON, "Cocktail", "avec une ombrelle")
    val VIN = Category("VIN", Side.BOISSON, "Vin !", "rouge, blanc, rosé")

    val builtIn = listOf(MEGA_SEANCE, SALLE, GRIMPE, ABDOS, BIERE, COCKTAIL, VIN)

    /** Pas de quoi déborder : on reste raisonnable, même pour un pirate. */
    const val MAX_COUNT = 99
    const val MAX_LABEL = 20
}

/** Le verdict du capitaine selon le match muscles contre bouteilles. */
enum class Verdict {
    /** Rien de noté. */
    PAGE_BLANCHE,

    /** Que du sport, pas une goutte. */
    ATHLETE,

    /** Que des verres, et au moins trois. */
    EPONGE,

    /** Plus de verres que de séances. */
    PILIER_DE_TAVERNE,

    /** Autant de l'un que de l'autre. */
    EQUILIBRE,

    /** Plus de séances que de verres. */
    SPORTIF,
    ;

    companion object {
        fun of(sport: Int, drinks: Int): Verdict = when {
            sport == 0 && drinks == 0 -> PAGE_BLANCHE
            drinks == 0 -> ATHLETE
            sport == 0 && drinks >= 3 -> EPONGE
            drinks > sport -> PILIER_DE_TAVERNE
            drinks == sport -> EQUILIBRE
            else -> SPORTIF
        }
    }
}

/** Les compteurs d'une journée, par identifiant de ligne. */
data class JournalPage(val counts: Map<String, Int> = emptyMap()) {
    fun count(id: String): Int = counts[id] ?: 0

    fun set(id: String, value: Int): JournalPage {
        val safe = value.coerceIn(0, Categories.MAX_COUNT)
        return JournalPage(if (safe == 0) counts - id else counts + (id to safe))
    }

    val isEmpty: Boolean get() = counts.values.all { it == 0 }
}

/**
 * Le journal complet : les lignes (fournies + perso) et une page par jour.
 * Les jours sont des numéros de jour local (jours depuis le 1er janvier 1970).
 */
data class Journal(
    val custom: List<Category> = emptyList(),
    val pages: Map<Long, JournalPage> = emptyMap(),
) {
    val categories: List<Category> get() = Categories.builtIn + custom

    fun categories(side: Side): List<Category> = categories.filter { it.side == side }

    fun page(day: Long): JournalPage = pages[day] ?: JournalPage()

    fun count(day: Long, id: String): Int = page(day).count(id)

    fun total(day: Long, side: Side): Int {
        val page = page(day)
        return categories(side).sumOf { page.count(it.id) }
    }

    fun verdict(day: Long): Verdict = Verdict.of(total(day, Side.SPORT), total(day, Side.BOISSON))

    fun set(day: Long, id: String, value: Int): Journal {
        val page = page(day).set(id, value)
        return copy(pages = if (page.isEmpty) pages - day else pages + (day to page))
    }

    fun add(day: Long, id: String, delta: Int = 1): Journal = set(day, id, count(day, id) + delta)

    /** Ajoute une ligne perso. Renvoie le journal inchangé si le nom est vide ou déjà pris. */
    fun addCategory(label: String, side: Side): Journal {
        val name = label.trim().take(Categories.MAX_LABEL)
        if (name.isEmpty() || categories.any { it.label.equals(name, ignoreCase = true) }) return this
        val next = (custom.mapNotNull { it.id.removePrefix(CUSTOM_PREFIX).toIntOrNull() }.maxOrNull() ?: 0) + 1
        return copy(custom = custom + Category("$CUSTOM_PREFIX$next", side, name, custom = true))
    }

    /** Supprime une ligne perso (et ses compteurs). Les lignes fournies restent. */
    fun removeCategory(id: String): Journal {
        if (custom.none { it.id == id }) return this
        val cleaned = pages.mapValues { (_, page) -> page.set(id, 0) }.filterValues { !it.isEmpty }
        return Journal(custom = custom.filterNot { it.id == id }, pages = cleaned)
    }

    /** Les jours où quelque chose a été noté, du plus récent au plus ancien. */
    fun history(): List<Long> = pages.filterValues { !it.isEmpty }.keys.sortedDescending()

    companion object {
        const val CUSTOM_PREFIX = "PERSO_"

        /** On garde un an de pages : au-delà, le capitaine a la mémoire qui flanche. */
        const val MAX_DAYS = 366

        fun encodeCategories(custom: List<Category>): String =
            custom.joinToString("\n") { "${it.id};${it.side.name};${it.label.replace(";", ",").replace("\n", " ")}" }

        fun decodeCategories(text: String?): List<Category> = text.orEmpty().lineSequence().mapNotNull { line ->
            val parts = line.split(';', limit = 3)
            if (parts.size != 3) return@mapNotNull null
            val side = Side.entries.firstOrNull { it.name == parts[1] } ?: return@mapNotNull null
            Category(parts[0], side, parts[2], custom = true)
        }.toList()

        fun encodePages(pages: Map<Long, JournalPage>): String =
            pages.keys.sortedDescending().take(MAX_DAYS).flatMap { day ->
                pages.getValue(day).counts.filterValues { it > 0 }.map { (id, n) -> "$day;$id;$n" }
            }.joinToString("\n")

        fun decodePages(text: String?): Map<Long, JournalPage> {
            val pages = mutableMapOf<Long, JournalPage>()
            text.orEmpty().lineSequence().forEach { line ->
                val parts = line.split(';')
                if (parts.size != 3) return@forEach
                val day = parts[0].toLongOrNull() ?: return@forEach
                val n = parts[2].toIntOrNull() ?: return@forEach
                pages[day] = (pages[day] ?: JournalPage()).set(parts[1], n)
            }
            return pages.filterValues { !it.isEmpty }
        }
    }
}
