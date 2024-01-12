package imagemagick.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import imagemagick.camelToSnakeCase

class RoughlyTranslateCsharpEnum : CliktCommand() {
    private val sourceFile by argument().file(mustBeReadable = true, mustExist = true).help("The file to translate.")

    private val outputFile by option().file(mustExist = false)

    /**
     *
     */
    override fun run() {
        val fileContent = sourceFile.readText()

        val translated =
            fileContent
                // only a summary, nothing else
                .replace(""" {4}/// <summary>\n {4}/// (.+)\n {4}/// </summary>\n {4}([^/]+)\n""".toRegex()) {
                    val (summary, enum) = it.destructured
                    """    /** $summary */
    ${enum.camelToSnakeCase().uppercase()}
"""
                }

        outputFile?.let {
            it.outputStream().writer(Charsets.UTF_8).use { osw ->
                osw.write(translated)
            }
        } ?: print(translated)
    }
}
