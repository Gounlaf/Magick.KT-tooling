package imagemagick

import com.github.ajalt.clikt.core.subcommands
import imagemagick.cmd.GenerateEnumsForResources
import imagemagick.cmd.RoughlyTranslateCsharpInterface
import imagemagick.cmd.RoughlyTranslateCsharpTest
import imagemagick.cmd.Tools

fun _main(args: Array<String>) {
    Tools().subcommands(
        GenerateEnumsForResources(),
        RoughlyTranslateCsharpTest(),
        RoughlyTranslateCsharpInterface(),
    ).main(args)
}

fun main(args: Array<String>) {
    _main(args)
}
