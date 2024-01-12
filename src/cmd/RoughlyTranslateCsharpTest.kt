package imagemagick.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import imagemagick.camelToSnakeCase

class RoughlyTranslateCsharpTest : CliktCommand() {
    private val sourceFile by argument().file(mustBeReadable = true, mustExist = true).help("The file to translate.")

    private val outputFile by option().file(mustExist = false)

    /**
     *
     */
    override fun run() {
        val fileContent = sourceFile.readText()

        val translated = fileContent
            .replace("[Fact]", "")
            .replace("new ", "")
            .replace("public class (.*)".toRegex()) {
                val (contextName) = it.destructured
                """context("$contextName")"""
            }
            .replace("""public void Should(.*)\(\)""".toRegex()) {
                val (methodName) = it.destructured

                "should(\"${methodName.camelToSnakeCase().replace('_', ' ')}\")"
            }
            .replace("""Assert.(False|True)\((.*)\);""".toRegex()) {
                val (boolStr, assertion) = it.destructured

                val actual = assertion.trim()
                    .splitToSequence('.')
                    .map { seq -> seq.replaceFirstChar { char -> char.lowercaseChar() } }
                    .joinToString(".")

                "${actual} shouldBe ${boolStr.lowercase()}"
            }
            .replace("""Assert.Equal\((.*),(.*)\);""".toRegex()) {
                val (expected, actualRaw) = it.destructured

                val actual = actualRaw.trim()
                    .splitToSequence('.')
                    .map { seq -> seq.replaceFirstChar { char -> char.lowercaseChar() } }
                    .joinToString(".")

                "${actual} shouldBe ${expected.trim()}"
            }
            .replace("""Assert.NotEqual\((.*),(.*)\);""".toRegex()) {
                val (expected, actualRaw) = it.destructured

                val actual = actualRaw.trim()
                    .splitToSequence('.')
                    .map { seq -> seq.replaceFirstChar { char -> char.lowercaseChar() } }
                    .joinToString(".")

                "${actual} shouldNotBe ${expected.trim()}"
            }
            .replace("""Assert.Null\((.*)\);""".toRegex()) {
                val (actualRaw) = it.destructured

                val actual = actualRaw.trim()
                    .splitToSequence('.')
                    .map { seq -> seq.replaceFirstChar { char -> char.lowercaseChar() } }
                    .joinToString(".")

                "${actual} shouldBe null"
            }
            .replace("""Assert.NotNull\((.*)\);""".toRegex()) {
                val (actualRaw) = it.destructured

                val actual = actualRaw.trim()
                    .splitToSequence('.')
                    .map { seq -> seq.replaceFirstChar { char -> char.lowercaseChar() } }
                    .joinToString(".")

                "${actual} shouldNotBe null"
            }
                .replace("""Assert.InRange\((.*), (.*), (.*)\);""".toRegex()) {
                val (actualRaw, from, to) = it.destructured

                val actual = actualRaw.trim()
                    .splitToSequence('.')
                    .map { seq -> seq.replaceFirstChar { char -> char.lowercaseChar() } }
                    .joinToString(".")

                "${actual} shouldBeIn ${from}..${to}"
            }
            .replace("""Assert.Throws<(.*)>\("(.*)", \(\) =>""".toRegex()) {
                val (type, _) = it.destructured

                "shouldThrow<${type}> {"
            }
            .replace("var", "val")
            .replace("Quantum.Max", "Quantum.max")
            .filterNot { c -> c == ';' }

        outputFile?.let {
            it.outputStream().writer(Charsets.UTF_8).use { osw ->
                osw.write(translated)
            }
        } ?: print(translated)
    }
}
