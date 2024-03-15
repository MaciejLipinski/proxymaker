package proxymaker

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File
import java.io.FileWriter
import java.nio.file.Paths
import kotlin.system.exitProcess

private const val DEFAULT_PROPERTIES_PATH = "src/main/resources/properties.yaml"

class Main {
    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            if (!File(propertiesPath).exists() || !File(propertiesPath).canRead()) {
                createPropertiesFile()
                println("Edit properties.yaml before using")
                exitProcess(0)
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

        private val propertiesPath: String
            get() {
                val currentDir = Paths.get("").toAbsolutePath().toString()
                return "$currentDir/properties.yaml"
            }

        private fun createPropertiesFile() {
            val propertiesFile = File(propertiesPath)
            FileWriter(propertiesFile).use {
                it.write(
                    """
                |pngMaker:
                |  oracleUrl: https://premodernoracle.com
                |  decklist: decklist.txt
                |#  decksDir: # Will save to user's home directory when not set
                |pdfMaker:
                |  decksDir: some/dir
                |  imageExtension: png
                |  borderColor: BLACK # allowed values: BLACK, WHITE, GOLD, SILVER
                |  fileName: proxies.pdf
                |""".trimMargin()
                )
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
    }
}

private enum class Mode {
    PngMaker, PdfMaker;

    companion object {
        fun fromString(string: String): Mode = values().first { it.name.lowercase() == string }
    }
}
