package imagemagick.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

@OptIn(ExperimentalStdlibApi::class)
class GenerateMagickColors : CliktCommand() {
    val rawData = """
A 	

Gets the alpha component value of this Color structure.
AliceBlue 	

Gets a system-defined color that has an ARGB value of #FFF0F8FF.
AntiqueWhite 	

Gets a system-defined color that has an ARGB value of #FFFAEBD7.
Aqua 	

Gets a system-defined color that has an ARGB value of #FF00FFFF.
Aquamarine 	

Gets a system-defined color that has an ARGB value of #FF7FFFD4.
Azure 	

Gets a system-defined color that has an ARGB value of #FFF0FFFF.
B 	

Gets the blue component value of this Color structure.
Beige 	

Gets a system-defined color that has an ARGB value of #FFF5F5DC.
Bisque 	

Gets a system-defined color that has an ARGB value of #FFFFE4C4.
Black 	

Gets a system-defined color that has an ARGB value of #FF000000.
BlanchedAlmond 	

Gets a system-defined color that has an ARGB value of #FFFFEBCD.
Blue 	

Gets a system-defined color that has an ARGB value of #FF0000FF.
BlueViolet 	

Gets a system-defined color that has an ARGB value of #FF8A2BE2.
Brown 	

Gets a system-defined color that has an ARGB value of #FFA52A2A.
BurlyWood 	

Gets a system-defined color that has an ARGB value of #FFDEB887.
CadetBlue 	

Gets a system-defined color that has an ARGB value of #FF5F9EA0.
Chartreuse 	

Gets a system-defined color that has an ARGB value of #FF7FFF00.
Chocolate 	

Gets a system-defined color that has an ARGB value of #FFD2691E.
Coral 	

Gets a system-defined color that has an ARGB value of #FFFF7F50.
CornflowerBlue 	

Gets a system-defined color that has an ARGB value of #FF6495ED.
Cornsilk 	

Gets a system-defined color that has an ARGB value of #FFFFF8DC.
Crimson 	

Gets a system-defined color that has an ARGB value of #FFDC143C.
Cyan 	

Gets a system-defined color that has an ARGB value of #FF00FFFF.
DarkBlue 	

Gets a system-defined color that has an ARGB value of #FF00008B.
DarkCyan 	

Gets a system-defined color that has an ARGB value of #FF008B8B.
DarkGoldenrod 	

Gets a system-defined color that has an ARGB value of #FFB8860B.
DarkGray 	

Gets a system-defined color that has an ARGB value of #FFA9A9A9.
DarkGreen 	

Gets a system-defined color that has an ARGB value of #FF006400.
DarkKhaki 	

Gets a system-defined color that has an ARGB value of #FFBDB76B.
DarkMagenta 	

Gets a system-defined color that has an ARGB value of #FF8B008B.
DarkOliveGreen 	

Gets a system-defined color that has an ARGB value of #FF556B2F.
DarkOrange 	

Gets a system-defined color that has an ARGB value of #FFFF8C00.
DarkOrchid 	

Gets a system-defined color that has an ARGB value of #FF9932CC.
DarkRed 	

Gets a system-defined color that has an ARGB value of #FF8B0000.
DarkSalmon 	

Gets a system-defined color that has an ARGB value of #FFE9967A.
DarkSeaGreen 	

Gets a system-defined color that has an ARGB value of #FF8FBC8B.
DarkSlateBlue 	

Gets a system-defined color that has an ARGB value of #FF483D8B.
DarkSlateGray 	

Gets a system-defined color that has an ARGB value of #FF2F4F4F.
DarkTurquoise 	

Gets a system-defined color that has an ARGB value of #FF00CED1.
DarkViolet 	

Gets a system-defined color that has an ARGB value of #FF9400D3.
DeepPink 	

Gets a system-defined color that has an ARGB value of #FFFF1493.
DeepSkyBlue 	

Gets a system-defined color that has an ARGB value of #FF00BFFF.
DimGray 	

Gets a system-defined color that has an ARGB value of #FF696969.
DodgerBlue 	

Gets a system-defined color that has an ARGB value of #FF1E90FF.
Firebrick 	

Gets a system-defined color that has an ARGB value of #FFB22222.
FloralWhite 	

Gets a system-defined color that has an ARGB value of #FFFFFAF0.
ForestGreen 	

Gets a system-defined color that has an ARGB value of #FF228B22.
Fuchsia 	

Gets a system-defined color that has an ARGB value of #FFFF00FF.
G 	

Gets the green component value of this Color structure.
Gainsboro 	

Gets a system-defined color that has an ARGB value of #FFDCDCDC.
GhostWhite 	

Gets a system-defined color that has an ARGB value of #FFF8F8FF.
Gold 	

Gets a system-defined color that has an ARGB value of #FFFFD700.
Goldenrod 	

Gets a system-defined color that has an ARGB value of #FFDAA520.
Gray 	

Gets a system-defined color that has an ARGB value of #FF808080.
Green 	

Gets a system-defined color that has an ARGB value of #FF008000.
GreenYellow 	

Gets a system-defined color that has an ARGB value of #FFADFF2F.
Honeydew 	

Gets a system-defined color that has an ARGB value of #FFF0FFF0.
HotPink 	

Gets a system-defined color that has an ARGB value of #FFFF69B4.
IndianRed 	

Gets a system-defined color that has an ARGB value of #FFCD5C5C.
Indigo 	

Gets a system-defined color that has an ARGB value of #FF4B0082.
IsEmpty 	

Specifies whether this Color structure is uninitialized.
IsKnownColor 	

Gets a value indicating whether this Color structure is a predefined color. Predefined colors are represented by the elements of the KnownColor enumeration.
IsNamedColor 	

Gets a value indicating whether this Color structure is a named color or a member of the KnownColor enumeration.
IsSystemColor 	

Gets a value indicating whether this Color structure is a system color. A system color is a color that is used in a Windows display element. System colors are represented by elements of the KnownColor enumeration.
Ivory 	

Gets a system-defined color that has an ARGB value of #FFFFFFF0.
Khaki 	

Gets a system-defined color that has an ARGB value of #FFF0E68C.
Lavender 	

Gets a system-defined color that has an ARGB value of #FFE6E6FA.
LavenderBlush 	

Gets a system-defined color that has an ARGB value of #FFFFF0F5.
LawnGreen 	

Gets a system-defined color that has an ARGB value of #FF7CFC00.
LemonChiffon 	

Gets a system-defined color that has an ARGB value of #FFFFFACD.
LightBlue 	

Gets a system-defined color that has an ARGB value of #FFADD8E6.
LightCoral 	

Gets a system-defined color that has an ARGB value of #FFF08080.
LightCyan 	

Gets a system-defined color that has an ARGB value of #FFE0FFFF.
LightGoldenrodYellow 	

Gets a system-defined color that has an ARGB value of #FFFAFAD2.
LightGray 	

Gets a system-defined color that has an ARGB value of #FFD3D3D3.
LightGreen 	

Gets a system-defined color that has an ARGB value of #FF90EE90.
LightPink 	

Gets a system-defined color that has an ARGB value of #FFFFB6C1.
LightSalmon 	

Gets a system-defined color that has an ARGB value of #FFFFA07A.
LightSeaGreen 	

Gets a system-defined color that has an ARGB value of #FF20B2AA.
LightSkyBlue 	

Gets a system-defined color that has an ARGB value of #FF87CEFA.
LightSlateGray 	

Gets a system-defined color that has an ARGB value of #FF778899.
LightSteelBlue 	

Gets a system-defined color that has an ARGB value of #FFB0C4DE.
LightYellow 	

Gets a system-defined color that has an ARGB value of #FFFFFFE0.
Lime 	

Gets a system-defined color that has an ARGB value of #FF00FF00.
LimeGreen 	

Gets a system-defined color that has an ARGB value of #FF32CD32.
Linen 	

Gets a system-defined color that has an ARGB value of #FFFAF0E6.
Magenta 	

Gets a system-defined color that has an ARGB value of #FFFF00FF.
Maroon 	

Gets a system-defined color that has an ARGB value of #FF800000.
MediumAquamarine 	

Gets a system-defined color that has an ARGB value of #FF66CDAA.
MediumBlue 	

Gets a system-defined color that has an ARGB value of #FF0000CD.
MediumOrchid 	

Gets a system-defined color that has an ARGB value of #FFBA55D3.
MediumPurple 	

Gets a system-defined color that has an ARGB value of #FF9370DB.
MediumSeaGreen 	

Gets a system-defined color that has an ARGB value of #FF3CB371.
MediumSlateBlue 	

Gets a system-defined color that has an ARGB value of #FF7B68EE.
MediumSpringGreen 	

Gets a system-defined color that has an ARGB value of #FF00FA9A.
MediumTurquoise 	

Gets a system-defined color that has an ARGB value of #FF48D1CC.
MediumVioletRed 	

Gets a system-defined color that has an ARGB value of #FFC71585.
MidnightBlue 	

Gets a system-defined color that has an ARGB value of #FF191970.
MintCream 	

Gets a system-defined color that has an ARGB value of #FFF5FFFA.
MistyRose 	

Gets a system-defined color that has an ARGB value of #FFFFE4E1.
Moccasin 	

Gets a system-defined color that has an ARGB value of #FFFFE4B5.
Name 	

Gets the name of this Color.
NavajoWhite 	

Gets a system-defined color that has an ARGB value of #FFFFDEAD.
Navy 	

Gets a system-defined color that has an ARGB value of #FF000080.
OldLace 	

Gets a system-defined color that has an ARGB value of #FFFDF5E6.
Olive 	

Gets a system-defined color that has an ARGB value of #FF808000.
OliveDrab 	

Gets a system-defined color that has an ARGB value of #FF6B8E23.
Orange 	

Gets a system-defined color that has an ARGB value of #FFFFA500.
OrangeRed 	

Gets a system-defined color that has an ARGB value of #FFFF4500.
Orchid 	

Gets a system-defined color that has an ARGB value of #FFDA70D6.
PaleGoldenrod 	

Gets a system-defined color that has an ARGB value of #FFEEE8AA.
PaleGreen 	

Gets a system-defined color that has an ARGB value of #FF98FB98.
PaleTurquoise 	

Gets a system-defined color that has an ARGB value of #FFAFEEEE.
PaleVioletRed 	

Gets a system-defined color that has an ARGB value of #FFDB7093.
PapayaWhip 	

Gets a system-defined color that has an ARGB value of #FFFFEFD5.
PeachPuff 	

Gets a system-defined color that has an ARGB value of #FFFFDAB9.
Peru 	

Gets a system-defined color that has an ARGB value of #FFCD853F.
Pink 	

Gets a system-defined color that has an ARGB value of #FFFFC0CB.
Plum 	

Gets a system-defined color that has an ARGB value of #FFDDA0DD.
PowderBlue 	

Gets a system-defined color that has an ARGB value of #FFB0E0E6.
Purple 	

Gets a system-defined color that has an ARGB value of #FF800080.
R 	

Gets the red component value of this Color structure.
RebeccaPurple 	

Gets a system-defined color that has an ARGB value of #663399.
Red 	

Gets a system-defined color that has an ARGB value of #FFFF0000.
RosyBrown 	

Gets a system-defined color that has an ARGB value of #FFBC8F8F.
RoyalBlue 	

Gets a system-defined color that has an ARGB value of #FF4169E1.
SaddleBrown 	

Gets a system-defined color that has an ARGB value of #FF8B4513.
Salmon 	

Gets a system-defined color that has an ARGB value of #FFFA8072.
SandyBrown 	

Gets a system-defined color that has an ARGB value of #FFF4A460.
SeaGreen 	

Gets a system-defined color that has an ARGB value of #FF2E8B57.
SeaShell 	

Gets a system-defined color that has an ARGB value of #FFFFF5EE.
Sienna 	

Gets a system-defined color that has an ARGB value of #FFA0522D.
Silver 	

Gets a system-defined color that has an ARGB value of #FFC0C0C0.
SkyBlue 	

Gets a system-defined color that has an ARGB value of #FF87CEEB.
SlateBlue 	

Gets a system-defined color that has an ARGB value of #FF6A5ACD.
SlateGray 	

Gets a system-defined color that has an ARGB value of #FF708090.
Snow 	

Gets a system-defined color that has an ARGB value of #FFFFFAFA.
SpringGreen 	

Gets a system-defined color that has an ARGB value of #FF00FF7F.
SteelBlue 	

Gets a system-defined color that has an ARGB value of #FF4682B4.
Tan 	

Gets a system-defined color that has an ARGB value of #FFD2B48C.
Teal 	

Gets a system-defined color that has an ARGB value of #FF008080.
Thistle 	

Gets a system-defined color that has an ARGB value of #FFD8BFD8.
Tomato 	

Gets a system-defined color that has an ARGB value of #FFFF6347.
Transparent 	

Gets a system-defined color.
Turquoise 	

Gets a system-defined color that has an ARGB value of #FF40E0D0.
Violet 	

Gets a system-defined color that has an ARGB value of #FFEE82EE.
Wheat 	

Gets a system-defined color that has an ARGB value of #FFF5DEB3.
White 	

Gets a system-defined color that has an ARGB value of #FFFFFFFF.
WhiteSmoke 	

Gets a system-defined color that has an ARGB value of #FFF5F5F5.
Yellow 	

Gets a system-defined color that has an ARGB value of #FFFFFF00.
YellowGreen 	

Gets a system-defined color that has an ARGB value of #FF9ACD32.
    """.trimIndent()

    private val outputFile by option().file(mustExist = false)

    override fun run() {
        val colors = TypeSpec.objectBuilder("MagickColors")

        val color = ClassName("imagemagick.colors", "MagickColor")

        """
        None
        Gets a system-defined color that has an ARGB value of #00000000.
        
        Transparent
        Gets a system-defined color that has an ARGB value of #00000000.
        
        """.trimIndent()
            .plus(rawData)
            .lineSequence()
            // strip empty lines
            .filterNot { it.isEmpty() }
            // combine Name and Description
            .chunked(2)
//            .first().let {
            .forEach {
                if (it.size != 2) {
                    return@forEach
                }

                val colorDescription = it[1].trim()

                if (!colorDescription.contains('#')) {
                    return@forEach
                }

                val colorName = it[0].trim()

                val hexaColor = colorDescription.substringAfterLast('#').removeSuffix(".")
                    .padStart(8, 'F')
                val chunks = hexaColor.chunked(2)

                val alpha = chunks[0]
                val red = chunks[1]
                val green = chunks[2]
                val blue = chunks[3]

                val property = PropertySpec
                    .builder(colorName, color)
                    .initializer("MagickColor.fromRgba(%Lu, %Lu, %Lu, %Lu)",
                        red.hexToUByte(),
                        green.hexToUByte(),
                        blue.hexToUByte(),
                        alpha.hexToUByte()
                    )
                    .addKdoc("Gets a system-defined color that has an RGBA value of #${red}${green}${blue}${alpha}.")

                colors.addProperty(property.build())
            }

        val experimentalStdlibApi = ClassName("kotlin", "ExperimentalStdlibApi")
        val experimentalForeignApi = ClassName("kotlinx.cinterop", "ExperimentalForeignApi")

        val builder = FileSpec.builder("imagemagick.colors", "whatever")
            .addFileComment("Autogenerated file; can be edited manually if necessary")
            .addType(colors
                .addAnnotation(experimentalStdlibApi)
                .addAnnotation(experimentalForeignApi)
                .build())
            .build()

        outputFile?.let { out ->
            out.outputStream().writer(Charsets.UTF_8).use {
                builder.writeTo(it)
            }
        } ?: builder.writeTo(System.out)
    }
}
