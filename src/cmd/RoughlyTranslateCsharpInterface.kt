package imagemagick.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file

class RoughlyTranslateCsharpInterface : CliktCommand() {
    private val sourceFile by argument().file(mustBeReadable = true, mustExist = true).help("The file to translate.")

    private val outputFile by option().file(mustExist = false)

    private fun replaceType(type: String): String = when (type) {
        "byte[]" -> "ByteArray"
        "Stream" -> "Source"
        "FileInfo" -> "Path"
        "bool" -> "Boolean"
        else -> type.replaceFirstChar { char -> char.uppercase() }
    }

    override fun run() {
        val fileContent = sourceFile.readText()

        val translated = fileContent
            // Add some spacing around c# specific instructions to make the following replace works
            .replace("""#if (.+)\n""".toRegex()) {
                val (condition) = it.destructured
                "#if $condition\n\n"
            }

            // only a summary, nothing else
            .replace(""" {4}/// <summary>\n {4}/// (.+)\n {4}/// </summary>\n {4}([^/]+)\n""".toRegex()) {
                val (summary, declaration) = it.destructured
                """    /**
     * $summary
     */
    $declaration
"""
            }

            .replace("""\{\n {4}///""".toRegex()) {
                """
{
    /**
    ///"""
            }
            .replace("""\n\n {4}///""".toRegex()) {
                """

    /**
    ///"""
            }

            .replace(""" {4}/// (.+)\n {4}([^/]+)\n""".toRegex()) {
                val (comment, declaration) = it.destructured
                """    /// ${comment}
     */
    ${declaration}
"""
            }

            .replace(""" {4}/// <summary>\n(?<summary>( {4}/// (.+)\n)+) {4}/// </summary>""".toRegex()) {
                val summary = (it.groups["summary"]?.value ?: "").splitToSequence("\n").map { line ->
                    line.trim().removePrefix("/// ").prependIndent("     * ")
                }
                summary.joinToString("\n")
            }


            .replace(""" {4}/// <param name="(.+)">(.+)</param>""".toRegex()) {
                val (name, description) = it.destructured

                """     * @param $name $description"""
            }

            .replace(""" {4}/// <returns>(.+)</returns>""".toRegex()) {
                val (description) = it.destructured

                """     * @return $description"""
            }


            .replace(""" {4}/// <exception cref="(.+)">(.+)</exception>""".toRegex()) {
                val (ex, description) = it.destructured

                """     * @throws ${ex} ${description}"""
            }

//
//            .replace(""" {4}/// <exception cref="(.+)">(.+)</exception>""".toRegex()) {
//                val (ex, description) = it.destructured
//
//                """     * @throws ${ex} ${description}
//     */
//    /// <exception cref="${ex}">${description}</exception>"""
//            }
//
//            .replace(""" {4}/// <exception cref="(.+)">(.+)</exception>""".toRegex()) {
//                val (ex, _) = it.destructured
//
//                """    @Throws(${ex}::class)"""
//            }
//
            .replace("""<see cref="(.+)"/>""".toRegex()) {
                val (ex) = it.destructured

                """[${replaceType(ex)}]"""
            }

            .replace("""(?<returnType>[a-zA-Z\[\]]+) (?<funName>[a-zA-Z1-9]+)\((.*)\);""".toRegex()) {
                val (returnType, funName, params) = it.destructured

                val result = """((?<type>(params )?[a-zA-Z<>?\[\]]+) (?<name>[a-zA-Z]+)(, )?)"""
                    .toRegex()
                    .findAll(params)
                    .map { matchResult ->
                        var paramName = matchResult.groups["name"]?.value ?: ""
                        var paramType = matchResult.groups["type"]?.value ?: ""

                        if (paramType.startsWith("params ")) {
                            paramType = paramType.removePrefix("params ")
                                .removeSuffix("[]")
                            paramName = paramName.prependIndent("vararg ")
                        }

                        "${paramName}: ${paramType.replaceFirstChar { char -> char.uppercase() }}"
                    }
                    .joinToString(", ")

                val line = "fun ${funName.replaceFirstChar { char -> char.lowercase() }}(${result})"

                if (returnType == "void") {
                    line
                } else {
                    "${line}: ${replaceType(returnType)}"
                }
            }

            .replace("""(?<returnType>[a-zA-Z<>?]+) (?<varName>[a-zA-Z]+) \{ get; set; }""".toRegex()) {
                val returnType = it.groups["returnType"]?.value ?: ""
                val varName = it.groups["varName"]?.value?.replaceFirstChar { char -> char.lowercase() } ?: ""

                if (returnType == "void") {
                    "var ${varName}"
                } else {
                    "var ${varName}: ${replaceType(returnType)}"
                }
            }

            .replace("""(?<returnType>[a-zA-Z<>?]+) (?<varName>[a-zA-Z]+) \{ get; }""".toRegex()) {
                val returnType = it.groups["returnType"]?.value ?: ""
                val varName = it.groups["varName"]?.value?.replaceFirstChar { char -> char.lowercase() } ?: ""

                if (returnType == "void") {
                    "val ${varName}"
                } else {
                    "val ${varName}: ${replaceType(returnType)}"
                }
            }

            .replace(": void", "")

            .replace("""(.+): Task""".toRegex()) {
                val (line) = it.destructured

                "// ${line}: Task"
            }

//            .replace(""" {5}\* \n {4}(var|val|fun|@Throws)""".toRegex()) {
//                val (line) = it.destructured
//
//                "     */\n     ${line}"
//            }

            .replace("Byte[]", "ByteArray")
            .replace("Stream", "Source")
            .replace("FileInfo", "Path")

        outputFile?.let {
            it.outputStream().writer(Charsets.UTF_8).use { osw ->
                osw.write(translated)
            }
        } ?: print(translated)
    }
}
