package dev.mathieuburnat.piratefocus.shop

import kotlin.test.Test
import kotlin.test.assertEquals

class ShopTest {

    @Test
    fun `acheter un objet le paie et le porte`() {
        val purchase = Shop.buy(doubloons = 100, inventory = Inventory(), item = Item.PERROQUET)

        assertEquals(Purchase.Done(40, Inventory(setOf(Item.PERROQUET), setOf(Item.PERROQUET))), purchase)
    }

    @Test
    fun `pas assez de doublons`() {
        assertEquals(Purchase.TooPoor, Shop.buy(doubloons = 10, inventory = Inventory(), item = Item.GALION))
    }

    @Test
    fun `on n'achète pas deux fois`() {
        val inventory = Inventory(owned = setOf(Item.CHAPEAU))
        assertEquals(Purchase.AlreadyOwned, Shop.buy(doubloons = 999, inventory = inventory, item = Item.CHAPEAU))
    }

    @Test
    fun `mettre et retirer un objet`() {
        val owned = Inventory(owned = setOf(Item.CHAPEAU))
        val worn = Shop.toggle(owned, Item.CHAPEAU)

        assertEquals(setOf(Item.CHAPEAU), worn.equipped)
        assertEquals(owned, Shop.toggle(worn, Item.CHAPEAU))
        // Pas question de porter ce qu'on n'a pas payé.
        assertEquals(owned, Shop.toggle(owned, Item.GALION))
    }

    @Test
    fun `l'inventaire se sauvegarde et se relit`() {
        assertEquals(setOf(Item.GALION, Item.PERROQUET), Shop.decode(Shop.encode(setOf(Item.GALION, Item.PERROQUET))))
        assertEquals(emptySet(), Shop.decode(null))
        assertEquals(setOf(Item.CHAPEAU), Shop.decode("CHAPEAU,LICORNE"))
    }
}
