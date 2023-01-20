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

class RGB(name: String, red: Float = 1.0f, green: Float = 1.0f, blue: Float = 1.0f) : Filter(name) {

    private var red: Float = 1.0f
    private var green: Float = 1.0f
    private var blue: Float = 1.0f
    private var fragment: FilterFragment? = null

    init {
        this.red = red
        this.green = green
        this.blue = blue
        this.fragment = FilterFragment(red, green, blue)
        this.fragment!!.redChanged = { c ->
            this.red = c
            this.changed?.invoke()
        }
        this.fragment!!.greenChanged = { c ->
            this.green = c
            this.changed?.invoke()
        }
        this.fragment!!.blueChanged = { c ->
            this.blue = c
            this.changed?.invoke()
        }
    }

    fun getRed(): Float {
        return this.red
    }

    fun getGreen(): Float {
        return this.green
    }

    fun getBlue(): Float {
        return this.blue
    }

    override fun getControllFragment(): Fragment? {
        return this.fragment
    }

    override fun applyFilter(matrix: ColorMatrix) {
        adjustRGB(matrix, this.red, this.green, this.blue)
    }

    override fun reset() {
        this.red = 1.0f
        this.green = 1.0f
        this.blue = 1.0f
        this.fragment?.refresh(red, green, blue)
    }

    private fun adjustRGB(matrix: ColorMatrix, red: Float, green: Float, blue: Float) {
        val mat = ColorMatrix()
        mat.setScale(red, green, blue, 1f)
        matrix.postConcat(mat)
    }

    /**
     * Fragment pro ovladani filtru
     */
    class FilterFragment(red: Float, green: Float, blue: Float) : Fragment() {

        var redChanged: ((c: Float)->Unit)? = null
        var greenChanged: ((c: Float)->Unit)? = null
        var blueChanged: ((c: Float)->Unit)? = null

        private var seekBarRed: CustomSeekBar? = null
        private var seekBarGreen: CustomSeekBar? = null
        private var seekBarBlue: CustomSeekBar? = null

        private var red: Float = 1.0f
        private var green: Float = 1.0f
        private var blue: Float = 1.0f
        init {
            this.red = red
            this.green = green
            this.blue = blue
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            val view: View = inflater.inflate(R.layout.filter_rgb, container, false)
            // red
            this.seekBarRed = view.findViewById(R.id.seekBarRed)
            this.seekBarRed?.fromCenter = true
            this.seekBarRed?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        redChanged?.invoke(p0.progress / 100.0f * 2.0f)
                    }
                }
            })
            // green
            this.seekBarGreen = view.findViewById(R.id.seekBarGreen)
            this.seekBarGreen?.fromCenter = true
            this.seekBarGreen?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        greenChanged?.invoke(p0.progress / 100.0f * 2.0f)
                    }
                }
            })
            // blue
            this.seekBarBlue = view.findViewById(R.id.seekBarBlue)
            this.seekBarBlue?.fromCenter = true
            this.seekBarBlue?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        blueChanged?.invoke(p0.progress / 100.0f * 2.0f)
                    }
                }
            })
            this.refresh(red, green, blue)
            return view
        }

        fun refresh(red: Float, green: Float, blue: Float) {
            this.red = red
            this.green = green
            this.blue = blue

            this.seekBarRed?.progress = (red / 2.0f * 100.0f).toInt()
            this.seekBarGreen?.progress = (green / 2.0f * 100.0f).toInt()
            this.seekBarBlue?.progress = (blue / 2.0f * 100.0f).toInt()
            this.seekBarRed?.invalidate()
            this.seekBarGreen?.invalidate()
            this.seekBarBlue?.invalidate()
        }

    }

}