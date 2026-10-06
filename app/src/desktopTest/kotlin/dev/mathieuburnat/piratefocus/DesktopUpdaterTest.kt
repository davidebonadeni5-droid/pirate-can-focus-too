package dev.mathieuburnat.piratefocus

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DesktopUpdaterTest {

    @Test
    fun `lire la dernière version sur GitHub`() {
        val json = """
            {"tag_name": "v0.2.42", "assets": [
              {"name": "pirate-focus-0.2.42.apk", "browser_download_url": "https://github.com/x/y/releases/download/v0.2.42/pirate-focus-0.2.42.apk"},
              {"name": "PirateFocus-1.0.42.msi", "browser_download_url": "https://github.com/x/y/releases/download/v0.2.42/PirateFocus-1.0.42.msi"}
            ]}
        """.trimIndent()

        assertEquals(
            DesktopUpdater.Update(42, "https://github.com/x/y/releases/download/v0.2.42/PirateFocus-1.0.42.msi"),
            DesktopUpdater.parse(json),
        )
    }

    @Test
    fun `pas de msi, pas de mise à jour`() {
        assertNull(DesktopUpdater.parse("""{"tag_name": "v0.2.7", "assets": []}"""))
    }
}
