package imagemagick.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file

/**
 * @author https://www.baeldung.com/kotlin/convert-camel-case-snake-case#1-using-the-replace-function-with-regex
 */
fun String.camelToSnakeCase(): String {
    val pattern = "(?<=.)[A-Z]".toRegex()
    return this.replace(pattern, " $0").lowercase()
}

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
            .replace("""public void Should(.*)\(\)""".toRegex()) {
                val (methodName) = it.destructured

                "should(\"${methodName.camelToSnakeCase().replace('_', ' ')}\")"
            }
            .replace("""Assert.(False|True)\((.*)\);""".toRegex()) {
                val (boolStr, assertion) = it.destructured

                "$assertion shouldBe ${boolStr.lowercase()}"
            }
            .replace("""Assert.Equal\((.*),(.*)\);""".toRegex()) {
                val (expected, actual) = it.destructured

                "${actual.lowercase()} shouldBe $expected"
            }
            .replace("var", "val")
            .filterNot { c -> c == ';' }

        outputFile?.let {
            it.outputStream().writer(Charsets.UTF_8).use {osw ->
                osw.write(translated)
            }
        } ?: print(translated)
    }
}
