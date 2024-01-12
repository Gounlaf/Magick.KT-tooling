package imagemagick

/**
 * @author https://www.baeldung.com/kotlin/convert-camel-case-snake-case#1-using-the-replace-function-with-regex
 */
fun String.camelToSnakeCase(): String {
    val pattern = "(?<=.)[A-Z]".toRegex()
    return this.replace(pattern, "_$0").lowercase()
}
