package cz.utb.photostudio.filter

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import cz.utb.photostudio.R
import cz.utb.photostudio.util.CustomSeekBar

class Contrast(name: String, contrast: Float = 1.0f) : Filter(name) {

    private var contrast: Float = 1.0f

    private var fragment: FilterFragment? = null

    init {
        this.contrast = contrast
        this.fragment = FilterFragment()
        this.fragment!!.contrastChanged = { c ->
            this.contrast = c
            this.changed?.invoke()
        }
    }

    fun getContrast(): Float {
        return this.contrast
    }

    override fun getControllFragment(): Fragment? {
        return this.fragment
    }

    override fun applyFilter(matrix: ColorMatrix) {
        adjustContrast(matrix, this.contrast)
    }

    override fun reset() {
        this.contrast = 1.0f
    }

    private fun adjustContrast(matrix: ColorMatrix, contrast: Float) {
        val translate: Float = (-.5f * contrast + .5f) * 255f
        val contrastMatrix = floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
        val mat = ColorMatrix(contrastMatrix)
        matrix.postConcat(mat)
    }

    /**
     * Fragment pro ovladani filtru
     */
    class FilterFragment : Fragment() {
        var contrastChanged: ((c: Float)->Unit)? = null
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            val view: View = inflater.inflate(R.layout.filter_contrast, container, false)
            val seekBar: CustomSeekBar = view.findViewById(R.id.seekBar)
            seekBar.fromCenter = true
            seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        contrastChanged?.invoke((p0.progress / 100.0f) * 2.0f)
                    }
                }
            })
            return view
        }
    }

}