package proxymaker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

private const val DEFAULT_PROPERTIES_PATH = "src/main/resources/properties.yaml"

fun main() {
    var propertiesPath = DEFAULT_PROPERTIES_PATH
    if (!File(propertiesPath).exists() || !File(propertiesPath).canRead()) {
        propertiesPath = askForInput("Enter properties.yaml path:")
    }
    val properties =
        ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .readValue(File(propertiesPath), Properties::class.java)

    val mode = askForInput(
        prompt = "Choose mode: pngmaker|pdfmaker",
        allowedInputs = listOf("pngmaker", "pdfmaker")
    ).let { Mode.fromString(it) }

    when (mode) {
        Mode.PngMaker -> PngMaker.makeProxyImages(properties.pngMaker)
        Mode.PdfMaker -> makePdf(properties.pdfMaker)
    }
}

private fun makePdf(properties: PdfMakerProperties) = PdfMaker.makePdf(
    decksDir = properties.decksDir ?: askForInput("Enter images directory:"),
    imageExtension = properties.imageExtension,
    borderColor = properties.borderColor,
    fileName = properties.fileName,
)

private fun askForInput(prompt: String, allowedInputs: List<String>? = null): String {
    println(prompt)
    while (true) {
        val input = readLine()
        if (!input.isNullOrEmpty() && allowedInputs?.contains(input) != false) {
            return input
        }
    }
}

private enum class Mode {
    PngMaker, PdfMaker;

    companion object {
        fun fromString(string: String): Mode = values().first { it.name.lowercase() == string }
    }
}
