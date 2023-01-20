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

class Brightness(name: String, brightness: Float = 0.0f) : Filter(name) {

    private var brightness: Float = 0.0f
    private var fragment: FilterFragment? = null

    init {
        this.brightness = brightness
        this.fragment = FilterFragment()
        this.fragment?.refresh(this.brightness)
        this.fragment!!.brightnessChanged = { c ->
            this.brightness = c
            this.changed?.invoke()
        }
    }

    fun getBrightness(): Float {
        return this.brightness
    }

    override fun getControllFragment(): Fragment? {
        return this.fragment
    }

    override fun applyFilter(matrix: ColorMatrix) {
        adjustBrightness(matrix, this.brightness)
    }

    override fun reset() {
        this.brightness = 1.0f
        this.fragment?.refresh(this.brightness)
    }

    private fun adjustBrightness(matrix: ColorMatrix, brightness: Float) {
        val brightnessMatrix = floatArrayOf(
            1f, 0f, 0f, 0f, brightness,
            0f, 1f, 0f, 0f, brightness,
            0f, 0f, 1f, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        )
        val mat = ColorMatrix(brightnessMatrix)
        matrix.postConcat(mat)
    }

    /**
     * Fragment pro ovladani filtru
     */
    class FilterFragment() : Fragment() {

        var brightnessChanged: ((c: Float)->Unit)? = null

        private var seekBar: CustomSeekBar? = null

        private var brightness: Float = 1.0f
        init {
            this.brightness = brightness
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            val view: View = inflater.inflate(R.layout.filter_brightness, container, false)
            this.seekBar = view.findViewById(R.id.seekBar)
            this.seekBar?.fromCenter = true
            this.seekBar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        brightnessChanged?.invoke((p0.progress / 100.0f) * 120.0f - 60.0f)
                    }
                }
            })
            refresh(this.brightness)
            return view
        }

        fun refresh(brightness: Float) {
            this.brightness = brightness
            this.seekBar?.progress = ((brightness + 60.0f) / 120.0f * 100.0f).toInt()
        }

    }

}