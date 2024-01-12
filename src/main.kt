package imagemagick

import com.github.ajalt.clikt.core.subcommands
import imagemagick.cmd.GenerateEnumsForResources
import imagemagick.cmd.GenerateMagickColors
import imagemagick.cmd.RoughlyTranslateCsharpEnum
import imagemagick.cmd.RoughlyTranslateCsharpInterface
import imagemagick.cmd.RoughlyTranslateCsharpTest
import imagemagick.cmd.Tools

fun _main(args: Array<String>) {
    Tools().subcommands(
        GenerateEnumsForResources(),
        GenerateMagickColors(),
        RoughlyTranslateCsharpEnum(),
        RoughlyTranslateCsharpInterface(),
        RoughlyTranslateCsharpTest(),
    ).main(args)
}

fun main(args: Array<String>) {
    _main(args)
}
