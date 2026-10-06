package dev.mathieuburnat.piratefocus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.mathieuburnat.piratefocus.focus.Phase
import dev.mathieuburnat.piratefocus.focus.PirateQuotes
import dev.mathieuburnat.piratefocus.shop.Inventory
import dev.mathieuburnat.piratefocus.shop.Item

/** La boutique de Barbe-Grise : on y dépense ses doublons, et on voit le résultat tout de suite. */
@Composable
fun ShopScreen(
    doubloons: Int,
    inventory: Inventory,
    onBuy: (Item) -> Unit,
    onToggle: (Item) -> Unit,
    onBack: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "< MENU",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .align(Alignment.Start)
                    .clickable(onClick = onBack)
                    .padding(vertical = 4.dp),
            )
            Text("=== BOUTIQUE ===", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text("coffre: $doubloons doublons", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            // L'aperçu : le capitaine et son navire avec ce qu'il porte en ce moment.
            PixelPirate(
                phase = Phase.IDLE,
                size = 140.dp,
                fancyHat = inventory.isEquipped(Item.CHAPEAU),
                parrot = inventory.isEquipped(Item.PERROQUET),
            )
            PixelShip(phase = Phase.IDLE, isGalleon = inventory.isEquipped(Item.GALION))
            Text(
                "« ${PirateQuotes.SHOP_WELCOME} »",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )

            Item.entries.forEach { item ->
                ShopItem(item, owned = item in inventory.owned, equipped = inventory.isEquipped(item), affordable = doubloons >= item.price, onBuy, onToggle)
            }
        }
    }
}

@Composable
private fun ShopItem(
    item: Item,
    owned: Boolean,
    equipped: Boolean,
    affordable: Boolean,
    onBuy: (Item) -> Unit,
    onToggle: (Item) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground), RectangleShape)
            .padding(12.dp),
    ) {
        Text(item.label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(item.pitch, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        when {
            owned -> OutlinedButton(
                onClick = { onToggle(item) },
                shape = RectangleShape,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (equipped) "[x] PORTÉ" else "[ ] AU PLACARD") }
            else -> Button(
                onClick = { onBuy(item) },
                shape = RectangleShape,
                // Grisé si on n'a pas les moyens, mais cliquable : le marchand a une réplique pour ça.
                colors = if (affordable) ButtonDefaults.buttonColors() else ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("> ACHETER  (${item.price} doublons)") }
        }
    }
}
