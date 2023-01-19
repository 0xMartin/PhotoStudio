package cz.utb.photostudio.filter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import cz.utb.photostudio.R


class Contrast(contrast: Float = 0.5f) : Filter() {

    private var contrast: Float = 0.0f
    private var fragment: FilterFragment? = null

    init {
        this.contrast = contrast
        this.fragment = FilterFragment()
        this.fragment!!.contrastChanged = { c ->
            this.contrast = c
            this.changed?.invoke()
        }
    }

    override fun getControllFragment(): Fragment? {
        return this.fragment
    }

    override fun applyFilter(input: Bitmap): Bitmap? {
        return adjustContrast(input, this.contrast)
    }

    override fun reset() {
        this.contrast = 0.0f
    }

    private fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val newBitmap = Bitmap.createBitmap(width, height, bitmap.config)
        val contrastMatrix = floatArrayOf(
            contrast, 0f, 0f, 0f, 0f,
            0f, contrast, 0f, 0f, 0f,
            0f, 0f, contrast, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
        val canvas = Canvas(newBitmap)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(contrastMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return newBitmap
    }

    class FilterFragment : Fragment() {
        var contrastChanged: ((c: Float)->Unit)? = null
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            val view: View = inflater.inflate(R.layout.filter_contrast, container, false)
            val seekBar: SeekBar = view.findViewById(R.id.seekBar)
            seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    p0?.let {
                        contrastChanged?.invoke(p0.progress / 100.0f)
                    }
                }
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })
            return view
        }


    }

}