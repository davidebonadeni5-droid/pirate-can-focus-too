package dev.mathieuburnat.piratefocus

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.time.Duration

/**
 * Mises à jour de la version Windows : à chaque lancement, on regarde la dernière Release GitHub.
 * Si elle est plus récente, on télécharge son .msi et on l'installe par-dessus (doublons conservés),
 * puis l'appli se relance toute seule. Sans réseau, on ne dit rien et on navigue comme d'habitude.
 */
object DesktopUpdater {

    private const val LATEST_RELEASE = "https://api.github.com/repos/davidebonadeni5-droid/pirate-can-focus-too/releases/latest"

    data class Update(val build: Int, val msiUrl: String)

    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    /** Le numéro de build de l'appli installée (1.0.<build>), ou null si on ne tourne pas depuis l'installateur. */
    private fun installedBuild(): Int? = System.getProperty("jpackage.app-version")?.substringAfterLast('.')?.toIntOrNull()

    private val isWindows = System.getProperty("os.name").orEmpty().startsWith("Windows")

    /** Une version plus récente que celle installée, ou null. Ne lève jamais d'exception. */
    fun findUpdate(): Update? = runCatching {
        if (!isWindows) return null
        val current = installedBuild() ?: return null
        val request = HttpRequest.newBuilder(URI(LATEST_RELEASE))
            .timeout(Duration.ofSeconds(10))
            .header("Accept", "application/vnd.github+json")
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) return null
        parse(response.body())?.takeIf { it.build > current }
    }.getOrNull()

    /** Lit le tag (v0.2.<build>) et l'adresse du .msi dans la réponse de GitHub. */
    internal fun parse(json: String): Update? {
        val build = Regex("\"tag_name\"\\s*:\\s*\"v?[0-9.]*?([0-9]+)\"").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: return null
        val msi = Regex("\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.msi)\"").find(json)?.groupValues?.get(1) ?: return null
        return Update(build, msi)
    }

    /**
     * Télécharge l'installateur puis lance un petit script qui attend la fermeture de l'appli,
     * installe la mise à jour sans poser de questions, et relance l'appli.
     */
    fun install(update: Update) {
        val dir = Files.createTempDirectory("piratefocus-update").toFile()
        val msi = File(dir, "PirateFocus.msi")
        val download = HttpRequest.newBuilder(URI(update.msiUrl)).timeout(Duration.ofMinutes(5)).build()
        client.send(download, HttpResponse.BodyHandlers.ofFile(msi.toPath()))

        val app = ProcessHandle.current().info().command().orElse(null)
        val script = File(dir, "update.cmd")
        script.writeText(
            buildString {
                appendLine("@echo off")
                appendLine("timeout /t 2 /nobreak >nul")
                appendLine("msiexec /i \"${msi.absolutePath}\" /passive")
                if (app != null) appendLine("start \"\" \"$app\"")
            },
        )
        ProcessBuilder("cmd", "/c", "start", "\"\"", "/min", script.absolutePath).start()
    }
}
