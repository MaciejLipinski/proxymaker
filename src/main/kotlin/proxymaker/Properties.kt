package proxymaker

import javax.swing.filechooser.FileSystemView

data class Properties(
    val pngMaker: PngMakerProperties,
    val pdfMaker: PdfMakerProperties,
)

data class PngMakerProperties(
    val oracleUrl: String,
    val decksDir: String = desktopDir(),
    val decklist: String,
)

data class PdfMakerProperties(
    val decksDir: String?,
    val imageExtension: String,
    val borderColor: BorderColor,
    val fileName: String
)

private fun desktopDir(): String =
    FileSystemView.getFileSystemView().homeDirectory.absolutePath