package cz.utb.photostudio.filter

import android.annotation.SuppressLint
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
        this.fragment = FilterFragment()
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
    }

    private fun adjustRGB(matrix: ColorMatrix, red: Float, green: Float, blue: Float) {
        val mat = ColorMatrix()
        mat.setScale(red, green, blue, 1f)
        matrix.postConcat(mat)
    }

    /**
     * Fragment pro ovladani filtru
     */
    class FilterFragment : Fragment() {
        var redChanged: ((c: Float)->Unit)? = null
        var greenChanged: ((c: Float)->Unit)? = null
        var blueChanged: ((c: Float)->Unit)? = null
        @SuppressLint("CutPasteId")
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            val view: View = inflater.inflate(R.layout.filter_rgb, container, false)
            // red
            val seekBar: CustomSeekBar = view.findViewById(R.id.seekBarRed)
            seekBar.fromCenter = true
            seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        redChanged?.invoke(p0.progress / 100.0f * 2.0f)
                    }
                }
            })
            // green
            val seekBar2: CustomSeekBar = view.findViewById(R.id.seekBarGreen)
            seekBar2.fromCenter = true
            seekBar2.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        greenChanged?.invoke(p0.progress / 100.0f * 2.0f)
                    }
                }
            })
            // blue
            val seekBar3: CustomSeekBar = view.findViewById(R.id.seekBarBlue)
            seekBar3.fromCenter = true
            seekBar3.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        blueChanged?.invoke(p0.progress / 100.0f * 2.0f)
                    }
                }
            })
            return view
        }
    }

}