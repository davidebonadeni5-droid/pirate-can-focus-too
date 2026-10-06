package dev.mathieuburnat.piratefocus.shop

/** Ce qu'on peut s'offrir avec ses doublons. */
enum class Item(val label: String, val price: Int, val pitch: String) {
    PERROQUET("Perroquet", 60, "Il répète tout. Surtout les gros mots."),
    CHAPEAU("Chapeau à plume", 120, "Bordé d'or. La plume est d'origine douteuse."),
    GALION("Galion à deux mâts", 250, "Plus de voiles, plus de panache, plus de prestige."),
}

/** Les objets achetés, et ceux que le capitaine porte en ce moment. */
data class Inventory(
    val owned: Set<Item> = emptySet(),
    val equipped: Set<Item> = emptySet(),
) {
    fun isEquipped(item: Item): Boolean = item in equipped
}

sealed interface Purchase {
    data class Done(val doubloons: Int, val inventory: Inventory) : Purchase
    data object TooPoor : Purchase
    data object AlreadyOwned : Purchase
}

/** Logique pure de la boutique. */
object Shop {

    fun buy(doubloons: Int, inventory: Inventory, item: Item): Purchase = when {
        item in inventory.owned -> Purchase.AlreadyOwned
        doubloons < item.price -> Purchase.TooPoor
        // Un objet acheté est aussitôt porté : on veut le voir tout de suite !
        else -> Purchase.Done(doubloons - item.price, Inventory(inventory.owned + item, inventory.equipped + item))
    }

    /** Mettre ou retirer un objet déjà acheté. */
    fun toggle(inventory: Inventory, item: Item): Inventory = when {
        item !in inventory.owned -> inventory
        item in inventory.equipped -> inventory.copy(equipped = inventory.equipped - item)
        else -> inventory.copy(equipped = inventory.equipped + item)
    }

    fun encode(items: Set<Item>): String = items.joinToString(",") { it.name }

    fun decode(text: String?): Set<Item> =
        text.orEmpty().split(',').mapNotNull { name -> Item.entries.firstOrNull { it.name == name } }.toSet()
}
