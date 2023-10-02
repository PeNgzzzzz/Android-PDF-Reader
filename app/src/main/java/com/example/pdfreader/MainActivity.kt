package com.example.pdfreader

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.OrientationEventListener
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// PDF sample code from
// https://medium.com/@chahat.jain0/rendering-a-pdf-document-in-android-activity-fragment-using-pdfrenderer-442462cb8f9a
// Issues about cache etc. are not at all obvious from documentation, so we should expect people to need this.
// We may wish to provide this code.
class MainActivity : AppCompatActivity() {
    val LOGNAME = "pdf_viewer"
    val FILENAME = "shannon1948.pdf"
    val FILERESID = R.raw.shannon1948

    // manage the pages of the PDF, see below
    lateinit var pdfRenderer: PdfRenderer
    lateinit var parcelFileDescriptor: ParcelFileDescriptor
    var currentPage: PdfRenderer.Page? = null

    // custom ImageView class that captures strokes and draws them over the image
    lateinit var pageImage: PDFimage

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        try {
            curPage()
        } catch (exception: IOException) {
            Log.d(LOGNAME, "Error opening PDF")
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            closeRenderer()
        } catch (ex: IOException) {
            Log.d(LOGNAME, "Unable to close PDF renderer")
        }
    }

    private fun curPage() {
        initPage()
        val pageNum = findViewById<TextView>(R.id.pageNum)
        // open page 0 of the PDF
        // it will be displayed as an image in the pageImage (above)
        try {
            val ori = this.resources.configuration.orientation
            pageImage.setScale(ori)
            openRenderer(this)
            showPage(current)
            pageNum.text = (current.plus(1).toString()) + "/" + pdfRenderer.pageCount.toString()
            pageNum.textSize = 20F
        } catch (exception: IOException) {
            Log.d(LOGNAME, "Error opening PDF")
        }
    }

    private fun initPage() {
        val layout = findViewById<LinearLayout>(R.id.pdfLayout)
        layout.isEnabled = true

        val linearLayout:LinearLayout = findViewById(R.id.pdfLayout)
        linearLayout.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                linearLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val widthPx = linearLayout.width
                val heightPx = linearLayout.height // Get height in pixels
                val density = resources.displayMetrics.density // Get screen density
                val heightDp = heightPx / density // Convert pixels to dp
                val widthDp = widthPx / density // Convert pixels to dp
                Log.d("height", heightDp.toString())
                Log.d("width", widthDp.toString())
            }
        })

        pageImage = PDFimage(this)
        if (layout.size > 0) {
            layout.removeViewAt(layout.size - 1)
        }
        layout.addView(pageImage)
        pageImage.minimumWidth = 1000
        pageImage.minimumHeight = 2000

        val pageNum = findViewById<TextView>(R.id.pageNum)

        // Set file name
        findViewById<TextView>(R.id.fileName).apply {
            text = FILENAME
            textSize = 20F
        }

        findViewById<ImageButton>(R.id.undo).setOnClickListener{
            if(!undo.isEmpty() && current == undo.peek().page){
                undo.peek().apply { isVisible = !isVisible }
                redo.push(undo.pop())
            }
        }
        findViewById<ImageButton>(R.id.redo).setOnClickListener{
            if(!redo.isEmpty()&& current == redo.peek().page){
                redo.peek().apply { isVisible = !isVisible }
                undo.push(redo.pop())
            }
        }

        findViewById<ImageButton>(R.id.hand).setOnClickListener {
            curTool = "hand"
        }
        findViewById<ImageButton>(R.id.pen).setOnClickListener {
            curTool = "pen"
        }
        findViewById<ImageButton>(R.id.highlighter).setOnClickListener {
            curTool = "highlighter"
        }
        findViewById<ImageButton>(R.id.eraser).setOnClickListener {
            curTool = "eraser"
        }

        findViewById<ImageButton>(R.id.left).setOnClickListener {
            if(current != 0 ) {
                current--
                showPage(current)
                pageNum.text = current.plus(1).toString() + "/" + pdfRenderer.pageCount.toString()
            }
        }
        findViewById<ImageButton>(R.id.right).setOnClickListener {
            if(current != pdfRenderer.pageCount ) {
                current++
                showPage(current)
                pageNum.text = current.plus(1).toString() + "/" + pdfRenderer.pageCount.toString()
            }
        }
    }

    @Throws(IOException::class)
    private fun openRenderer(context: Context) {
        // In this sample, we read a PDF from the assets directory.
        val file = File(context.cacheDir, FILENAME)
        if (!file.exists()) {
            // pdfRenderer cannot handle the resource directly,
            // so extract it into the local cache directory.
            val asset = this.resources.openRawResource(FILERESID)
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var size: Int
            while (asset.read(buffer).also { size = it } != -1) {
                output.write(buffer, 0, size)
            }
            asset.close()
            output.close()
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

        // capture PDF data
        // all this just to get a handle to the actual PDF representation
        pdfRenderer = PdfRenderer(parcelFileDescriptor)
    }

    // do this before you quit!
    @Throws(IOException::class)
    private fun closeRenderer() {
        currentPage?.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()
        currentPage = null
    }

    private fun showPage(index: Int) {
        if (pdfRenderer.pageCount <= index) {
            return
        }
        // Close the current page before opening another one.
        currentPage?.close()

        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index)

        if (currentPage != null) {
            // Important: the destination bitmap must be ARGB (not RGB).
            val bitmap = Bitmap.createBitmap(resources.displayMetrics.densityDpi * currentPage!!.getWidth() / 72,
                resources.displayMetrics.densityDpi * currentPage!!.getHeight() / 72, Bitmap.Config.ARGB_8888)

            // Here, we render the page onto the Bitmap.
            // To render a portion of the page, use the second and third parameter. Pass nulls to get the default result.
            // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
            currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // Display the page
            pageImage.setImage(bitmap)
        }
    }
}