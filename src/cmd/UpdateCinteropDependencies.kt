package imagemagick.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.sources.PropertiesValueSource
import com.github.ajalt.mordant.animation.coroutines.CoroutineProgressTaskAnimator
import com.github.ajalt.mordant.animation.coroutines.animateInCoroutine
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.progress.completed
import com.github.ajalt.mordant.widgets.progress.percentage
import com.github.ajalt.mordant.widgets.progress.progressBar
import com.github.ajalt.mordant.widgets.progress.progressBarLayout
import com.github.ajalt.mordant.widgets.progress.speed
import com.github.ajalt.mordant.widgets.progress.text
import com.github.ajalt.mordant.widgets.progress.timeRemaining
import com.kgit2.kommand.process.Command
import com.kgit2.kommand.process.Stdio
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.time.temporal.ChronoUnit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.visitFileTree
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GHWorkflowRun.Status
import org.kohsuke.github.GitHubBuilder

@Serializable
data class NugetPackage(
    @SerialName("@id")
    val id: String,
    val packageContent: String,
)

class UpdateCinteropDependencies : CliktCommand() {
    init {
        context {
            val home = System.getenv("HOME")

            autoEnvvarPrefix = "MAGICKKT"
            valueSources(
                PropertiesValueSource.from("$home/.config/magick-kt-tooling.properties"),
                PropertiesValueSource.from("$home/.magick-kt-tooling.properties"),
            )
        }
    }

    private val verbose by option("-v", "--verbose", help = "Verbose mode").boolean().default(false)

    private val githubUsername by option(valueSourceKey = "githubUsername").required()
    private val githubPassword by option(valueSourceKey = "githubPassword").required()
    private val outputDir by argument().file(mustExist = false, canBeFile = false).help("outputDir.")

    private val parent by lazy {
        Path(outputDir.absolutePath, "magick-kt-native-q8/src/nativeInterop/cinterop")
    }

    private val libDir by lazy {
        parent / "lib"
    }

    private val includeDir by lazy {
        parent / "include"
    }

    private val githubClient by lazy {
        GitHubBuilder().withPassword(githubUsername, githubPassword)
            .build()
    }

    private val httpClient by lazy {
        HttpClient(CIO) {
            install(HttpTimeout) {
            }
            install(ContentNegotiation) {
                json(json = Json { ignoreUnknownKeys = true })
            }
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(githubUsername, githubPassword)
                    }
                }
            }
        }
    }

    private val t = Terminal()

    @OptIn(ExperimentalPathApi::class)
    override fun run() {
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            t.danger("Failed to create output dir ${outputDir.absolutePath}")
            return
        }

        libDir.createDirectories()
        includeDir.createDirectories()

        val magickNativeVersion: String = retrieveLatestMagickNativeVersion()

        val magickNativeCommit: String? = runBlocking {
            // Extract Magick.Native files
            extractMagickNativeFiles(magickNativeVersion)
        }

        if (magickNativeCommit == null) {
            return
        }

        val imageMagickRelease: GHRelease? = retrieveImageMagickRelease(magickNativeCommit)
        if (imageMagickRelease == null) {
            return
        }

        runBlocking {
            // Extract ImageMagick files
            extractImageMagickFiles(imageMagickRelease)
        }

        val fileList = mutableListOf<String>()

        // Generate .def file
        (includeDir / "Magick.Native").visitFileTree {
            onVisitFile { file, _ ->
                if (file.absolutePathString().endsWith(".h")) {
                    fileList.add("Magick.Native/${file.absolutePathString().substringAfter("Magick.Native/")}")
                }

                FileVisitResult.CONTINUE
            }
        }

        val defFile = parent / "libMagickNative.def"

        fileList.takeUnless { it.isEmpty() }?.let { list ->
            list.sort()

            defFile.outputStream().bufferedWriter().use { writer ->
                list.removeFirst().let { writer.append("headers = $it \\").appendLine() }

                val last = list.removeLast()

                list.forEach { writer.append("    $it \\").appendLine() }

                writer.append("    $last").appendLine()
            }
        }
    }

    private fun retrieveLatestMagickNativeVersion(): String {
        t.info("Fetching latest Magick.Native version")
        return githubClient
            .getRepository("dlemstra/Magick.NET")
            .getFileContent("src/Magick.Native/Magick.Native.version")
            .read()
            .reader()
            .use { it.readText() }
            .also {
                t.success("Wanted Magick.Native version: $it")
            }
    }

    private fun retrieveImageMagickRelease(magickNativeCommit: String): GHRelease? {
        val commit = githubClient.getRepository("dlemstra/Magick.Native")
            .getFileContent("src/ImageMagick/ImageMagick.commit", magickNativeCommit)
            .read().reader().use { it.readText() }

        val tag = githubClient.getRepository("ImageMagick/ImageMagick")
            .listTags()
            .firstOrNull {
                it.commit.shA1 == commit
            }

        if (tag == null) {
            t.danger("ImageMagick tag not found for commit $commit")
            return null
        }

        val release = githubClient.getRepository("Gounlaf/ImageMagick-builds")
            .listReleases()
            .firstOrNull {
                it.tagName == tag.name
            }

        if (release == null) {
            t.danger("ImageMagick release not found for tag ${tag.name}")
            return null
        }

        return release
    }

    @OptIn(ExperimentalPathApi::class)
    private suspend fun extractImageMagickFiles(
        release: GHRelease,
    ) {
        val asset = release.listAssets().first {
            it.name.endsWith("clang-Q8-x86_64.AppImage")
        }

        val extractDirTmp = createTempDirectory()
        val assetFile = createTempFile(
            directory = extractDirTmp, prefix = null, suffix = ".AppImage", PosixFilePermissions.asFileAttribute(
                setOf(
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                )
            )
        )
        downloadFile(asset.browserDownloadUrl, 5.minutes, assetFile, asset.browserDownloadUrl)


        // AppImage are 7z compatible, but Apache Commons Compress doesn't recognize header
        // -> need to extract AppImage via subprocess

        t.info("extracting AppImage")
        Command(assetFile.absolutePathString())
            .arg("--appimage-extract")
            .cwd(extractDirTmp.absolutePathString())
            .stdout(Stdio.Null)
            .spawn()
            .wait()

        extractDirTmp.visitFileTree {
            onVisitFile { file, _ ->
                val entry = file.absolutePathString()

                when {
                    entry.contains("/usr/include/ImageMagick-7") && entry.endsWith(".h") -> {
                        Path(
                            includeDir.absolutePathString(),
                            entry.substringAfter("/usr/include/")
                        ).let { dst ->
                            dst.parent.createDirectories()
                            file.copyTo(dst)
                        }
                    }

                    entry.contains("/usr/lib/libMagick") -> {
                        Path(
                            libDir.absolutePathString(),
                            entry.substringAfter("/usr/lib/")
                        ).let { dst ->
                            dst.parent.createDirectories()
                            file.copyTo(dst)
                        }
                    }
                }

                FileVisitResult.CONTINUE
            }
        }

        extractDirTmp.deleteRecursively()

        t.success("Magick.Native sources files extracted")
    }

    private suspend fun extractMagickNativeFiles(version: String): String? {
        val versionDate =
            LocalDate.Format {
                year(padding = Padding.NONE)
                char('.')
                monthNumber(padding = Padding.NONE)
                char('.')
                dayOfMonth(padding = Padding.NONE)
            }
                .parse(version.substringBeforeLast('.'))
                .atStartOfDayIn(TimeZone.UTC)
                .toJavaInstant()

        val workflowRun = githubClient.getRepository("dlemstra/Magick.Native")
            .getWorkflow(5196860) // doesn't work with name .getWorkflow("main")
            .listRuns()
            .firstOrNull findRun@{ ghWorkflowRun ->
                val workflowCreatedAt = ghWorkflowRun.createdAt.toInstant()

                if (verbose) {
                    t.info("analysing workflow run ${ghWorkflowRun.id} / $workflowCreatedAt")
                }
                if (workflowCreatedAt.truncatedTo(ChronoUnit.DAYS) > versionDate) {
                    if (verbose) {
                        t.info("skip run because of invalid date ${ghWorkflowRun.id} / $workflowCreatedAt")
                    }

                    return@findRun false
                }

                val job = ghWorkflowRun.listJobs()
                    .firstOrNull findJob@{ ghWorkflowJob ->
                        if (ghWorkflowJob.name != "Publish .NET library") {
                            return@findJob false
                        }

                        if (ghWorkflowJob.status != Status.COMPLETED) {
                            return@findJob false
                        }

                        val logs =
                            ghWorkflowJob.downloadLogs { inputStream ->
                                inputStream.bufferedReader().use { bufferedReader ->
                                    bufferedReader.readText()
                                }
                            }

                        logs.contains("Pushing Magick.Native.$version.nupkg")
                    }

                job != null
            }

        if (workflowRun == null) {
            t.danger("Cannot found corresponding commit for version $version")
            return null
        }

        t.success("found corresponding commit: ${workflowRun.headCommit.id}")

        val sourceUrl = "https://github.com/dlemstra/Magick.Native/archive/${workflowRun.headCommit.id}.zip"

        // Extract header files from source code
        val sourceFile = createTempFile(suffix = ".zip")
        downloadFile(sourceUrl, 5.minutes, sourceFile, sourceUrl)

        sourceFile.let {
            it.inputStream().buffered().use { inputStream ->
                val factory = ArchiveStreamFactory()

                factory.createArchiveInputStream<ZipArchiveInputStream>(inputStream).use { zipArchiveInputStream ->
                    var entry = zipArchiveInputStream.nextEntry
                    while (entry != null) {
                        if (entry.name.contains("/src/Magick.Native") && entry.name.endsWith(".h")) {
                            Path(includeDir.absolutePathString(), entry.name.substringAfter("src/")).let { dst ->
                                dst.parent.createDirectories()
                                dst.outputStream().use { outputStream ->
                                    zipArchiveInputStream.copyTo(outputStream)
                                }
                            }
                        }
                        entry = zipArchiveInputStream.nextEntry
                    }
                }
            }
            it.deleteExisting()
        }

        t.success("Magick.Native sources files extracted")

        // Extract compiled library

        val packageResponse = httpClient.get("https://nuget.pkg.github.com/dlemstra/magick.native/$version.json")
        val packageDetails = packageResponse.body<NugetPackage>()

        val compiledLibrary = createTempFile(suffix = packageDetails.packageContent.substringAfterLast("/"))
        downloadFile(packageDetails.packageContent, 5.minutes, compiledLibrary, packageDetails.packageContent)

        compiledLibrary.inputStream().buffered().use { inputStream ->
            val factory = ArchiveStreamFactory()

            factory.createArchiveInputStream<ZipArchiveInputStream>(inputStream).use { zipArchiveInputStream ->
                var entry = zipArchiveInputStream.nextEntry
                while (entry != null) {
                    // TODO Support other Quantum
                    if (entry.name.startsWith("content/linux/ReleaseQ8/") && entry.name.contains("x64")) {
                        Path(libDir.absolutePathString(), entry.name.substringAfterLast('/')).outputStream().use {
                            zipArchiveInputStream.copyTo(it)
                        }
                    }
                    entry = zipArchiveInputStream.nextEntry
                }
            }
        }

        t.success("Magick.Native compiled library extracted")

        return workflowRun.headCommit.id
    }

    private suspend fun downloadFile(
        url: String,
        timeout: Duration,
        destination: Path,
        statusText: String,
    ) {
        val progressBar = progressBarLayout {
            text(statusText)
            percentage()
            progressBar()
            completed()
            speed("B/s")
            timeRemaining()
        }.animateInCoroutine(t, start = false)

        httpClient.downloadFile(url, timeout, destination, progressBar)
    }
}

//suspend fun HttpStatement.downloadFile(
//    destination: Path,
//    progress: ProgressAnimation,
//) = this.execute { response: HttpResponse ->
//
//
//    progress.start()
//    progress.updateTotal(response.contentLength())
//
//    val channel: ByteReadChannel = response.body()
//    println("channel.isClosedForRead: ${channel.isClosedForRead}")
//    while (!channel.isClosedForRead) {
//        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
//        println("packet.isEmpty: ${packet.isEmpty}")
//        while (!packet.isEmpty) {
//            packet.readBytes().let {
//                println("it.size.toLong(): ${it.size.toLong()}")
//                destination.appendBytes(it)
//                progress.advance(it.size.toLong())
//            }
//        }
//    }
//}

suspend fun HttpClient.downloadFile(
    url: String,
    timeout: Duration,
    destination: Path,
    progress: CoroutineProgressTaskAnimator<Unit>,
) {
    this.prepareGet(url) {
        timeout {
            requestTimeoutMillis = timeout.inWholeMilliseconds
        }
        onDownload { bytesSentTotal, contentLength ->
            progress.update {
                total = contentLength
                completed = bytesSentTotal
            }
        }
    }.execute { response: HttpResponse ->
        coroutineScope {
            launch {
                progress.execute()
            }

            launch {
                response.bodyAsChannel().copyAndClose(destination.toFile().writeChannel())
            }
        }.start()

        progress.stop()
    }
}
