package proxymaker

import org.openqa.selenium.By
import org.openqa.selenium.Keys
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO
import kotlin.io.path.pathString

object PngMaker {
    private data class Deck(val name: String, val cards: List<Card>)
    private data class Card(val name: String, val count: Int)

    fun makeProxyImages(properties: PngMakerProperties) =
        makeProxyImages(
            oracleUrl = properties.oracleUrl,
            decklistPath = properties.decklist,
            outputDir = properties.decksDir,
        )

    private fun makeProxyImages(
        oracleUrl: String,
        decklistPath: String,
        outputDir: String,
        imageExtension: String = "png"
    ) {
        val decks = readDecks(decklistPath)
        val driver = chromeDriver()
        try {
            driver.prepareOracle(oracleUrl)
            decks.forEach { (deckName, cards) ->
                cards.forEach { card ->
                    driver.fetchCard(card)
                    val outputFile = prepareOutputFile(outputDir, deckName, card, imageExtension)
                    val cardElement = driver.getCardElement()
                    val screenshot = cardElement.getScreenshot(imageExtension)
                    screenshot.copyTo(outputFile, overwrite = true)
                    multiplyIfNotSingle(card, outputFile, screenshot, imageExtension)
                }
            }
            println("Saved images to $outputDir")
        } catch (e: Exception) {
            println(e)
        } finally {
            driver.quit()
        }
    }

    private fun readDecks(decklistPath: String): List<Deck> {
        val decks = mutableListOf<Deck>()
        var deckName = ""
        var cards: MutableList<Card> = mutableListOf()
        FileReader(decklistPath).readLines()
            .forEach {
                val prefix = it.substring(0, 1.coerceAtMost(it.length))
                when {
                    prefix == DECK_PREFIX -> {
                        deckName = it.substringAfter(DECK_PREFIX)
                        cards = mutableListOf()
                    }
                    prefix.matches(CARD_PREFIX) -> {
                        val count = it.substringBefore(" ").toInt()
                        val cardName = it.substringAfter(" ")
                        cards.add(Card(cardName, count))
                    }
                    else -> {
                        decks.add(Deck(deckName, cards.toList()))
                    }
                }
            }
        val lastDeck = Deck(deckName, cards.toList())
        if (!decks.contains(lastDeck)) {
            decks.add(lastDeck)
        }
        return decks
    }

    private fun chromeDriver() = ChromeDriver(ChromeOptions().apply { addArguments("--kiosk") })

    private fun ChromeDriver.prepareOracle(oracleUrl: String) {
        get(oracleUrl)
        findElement(By.tagName("button")).click()
        while (true) {
            try {
                findElements(By.tagName("input")).first { it.getDomAttribute("formcontrolname") == "mpcBleed" }.click()
                findElements(By.tagName("input")).first { it.getDomAttribute("formcontrolname") == "highRes" }.click()
                break
            } catch (ignored: Exception) {
                Thread.sleep(100)
            }
        }
    }

    private fun ChromeDriver.fetchCard(card: Card) {
        println("Getting ${card.name}")
        findElement(By.name("cardNames")).sendKeys(Keys.chord(osSpecificControlKey(), "A"))
        findElement(By.name("cardNames")).sendKeys(Keys.DELETE)
        findElement(By.name("cardNames")).sendKeys(card.name)
        findElement(By.tagName("button")).click()
    }

    private fun osSpecificControlKey(): Keys =
        when (System.getProperty("os.name")) {
            "Mac OS X" -> Keys.COMMAND
            else -> Keys.CONTROL
        }

    private fun prepareOutputFile(decksDir: String, deckName: String, card: Card, imageExtension: String): File {
        val dirPath = Paths.get("$decksDir/$deckName")
        if (!Files.isDirectory(dirPath)) {
            Files.createDirectory(dirPath)
        }
        return File("${dirPath.pathString}/${card.name}.${imageExtension}")
    }

    private fun ChromeDriver.getCardElement(): WebElement {
        var cardElement: WebElement
        while (true) {
            try {
                findElement(By.tagName("app-card"))
                // illustration may load a moment later, retry once
                Thread.sleep(750)
                cardElement = findElement(By.tagName("app-card"))
                break
            } catch (ignored: Exception) {
                Thread.sleep(250)
            }
        }
        return cardElement
    }

    private fun WebElement.getScreenshot(imageExtension: String): File {
        val screenshot = getScreenshotAs(OutputType.FILE)
        val cardWithMarginImage = ImageIO.read(screenshot)

        val withMarginWidth = cardWithMarginImage.width
        val withMarginHeight = cardWithMarginImage.height

        val cardScreenshot = cardWithMarginImage.getSubimage(
            (withMarginWidth - DESIRED_WIDTH) / 2,
            (withMarginHeight - DESIRED_HEIGHT) / 2,
            DESIRED_WIDTH,
            DESIRED_HEIGHT
        )
        ImageIO.write(cardScreenshot, imageExtension, screenshot)
        return screenshot
    }

    private fun multiplyIfNotSingle(card: Card, outputFile: File, screenshot: File, imageExtension: String) {
        if (card.count in 2..MAX_DUPLICATE_COUNT) {
            for (i in 2..card.count) {
                val copyPath = Path.of("${outputFile.path.dropLast(".${imageExtension}".length)}-${i}.$imageExtension")
                Files.copy(screenshot.toPath(), copyPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private const val DECK_PREFIX = "#"
    private val CARD_PREFIX = Regex("\\d")
    private const val MAX_DUPLICATE_COUNT = 50
    private const val DESIRED_WIDTH = 744 // px
    private const val DESIRED_HEIGHT = 1038 // px
}