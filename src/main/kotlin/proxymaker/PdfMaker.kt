package proxymaker

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.awt.Color
import java.io.File

object PdfMaker {
    fun makePdf(decksDir: String, imageExtension: String, borderColor: BorderColor, fileName: String) {
        val document = PDDocument()
        val pageCount: Int
        File(decksDir).walk()
            .filter { it.name.endsWith(".$imageExtension") }
            .also { println("Found ${it.count()} image${if (it.count() != 1) "s" else ""}") }
            .chunked(9).also { pageCount = it.count() }
            .forEachIndexed { zeroIndexedPageNumber, groupOf9 ->
                println("Drawing page ${zeroIndexedPageNumber + 1}/$pageCount")
                val page = PDPage(PDRectangle.A4).also { document.addPage(it) }
                val contentStream = PDPageContentStream(document, page)

                contentStream.drawBackground(
                    lineColor = Color.BLACK,
                    bleedColor = borderColor.color,
                    x = MARGIN * 2 / 3,
                    y = MARGIN * 2 / 3,
                    width = 3 * CARD_WIDTH + MARGIN * 2 / 3,
                    height = 3 * CARD_HEIGHT + MARGIN * 2 / 3
                )

                groupOf9.forEachIndexed { index, file ->
                    val image = PDImageXObject.createFromFile(file.absolutePath, document)
                    val horizontalOffset = index.mod(3) * CARD_WIDTH
                    val verticalOffset = index.div(3) * CARD_HEIGHT
                    contentStream.drawImage(
                        image,
                        MARGIN + horizontalOffset,
                        MARGIN + verticalOffset,
                        CARD_WIDTH,
                        CARD_HEIGHT
                    )
                }
                contentStream.close()
            }
        document.save("$decksDir/$fileName")
        document.close()
    }

    private fun PDPageContentStream.drawBackground(
        lineColor: Color,
        bleedColor: Color,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        // draw guides
        setLineWidth(POINTS_PER_MM / 4)
        setNonStrokingColor(lineColor)
        for (offset in 0..3) {
            // vertical
            moveTo(MARGIN + offset * CARD_WIDTH, 0f)
            lineTo(MARGIN + offset * CARD_WIDTH, PDRectangle.A4.height)
            stroke()
            // horizontal
            moveTo(0f, MARGIN + offset * CARD_HEIGHT)
            lineTo(PDRectangle.A4.width, MARGIN + offset * CARD_HEIGHT)
            stroke()
        }

        // draw card border bleed
        addRect(x, y, width, height)
        setNonStrokingColor(bleedColor)
        fill()
    }

    private const val POINTS_PER_INCH = 72f
    private const val POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH
    private const val CARD_WIDTH = 63 * POINTS_PER_MM
    private const val CARD_HEIGHT = 88 * POINTS_PER_MM
    private const val MARGIN = 10 * POINTS_PER_MM
}

enum class BorderColor(val color: Color) {
    BLACK(Color(17, 17, 17)),
    WHITE(Color(255, 255, 255)),
    GOLD(Color(170, 136, 68)),
    SILVER(Color(153, 153, 153)),
}