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

class Saturation(name: String, saturation: Float = 1.0f) : Filter(name) {

    private var saturation: Float = 1.0f
    private var fragment: FilterFragment? = null

    init {
        this.saturation = saturation
        this.fragment = FilterFragment()
        this.fragment!!.saturationChanged = { c ->
            this.saturation = c
            this.changed?.invoke()
        }
    }

    fun getSaturation(): Float {
        return this.saturation
    }

    override fun getControllFragment(): Fragment? {
        return this.fragment
    }

    override fun applyFilter(matrix: ColorMatrix) {
        adjustSaturation(matrix, this.saturation)
    }

    override fun reset() {
        this.saturation = 1.0f
    }

    private fun adjustSaturation(matrix: ColorMatrix, saturation: Float) {
        val mat = ColorMatrix()
        mat.setSaturation(saturation)
        matrix.postConcat(mat)
    }

    /**
     * Fragment pro ovladani filtru
     */
    class FilterFragment : Fragment() {
        var saturationChanged: ((c: Float)->Unit)? = null
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            val view: View = inflater.inflate(R.layout.filter_saturation, container, false)
            val seekBar: CustomSeekBar = view.findViewById(R.id.seekBar)
            seekBar.fromCenter = true
            seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}
                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let {
                        if(p0.progress <= 50) {
                            // leve polovina 0 <-> 1
                            saturationChanged?.invoke(p0.progress / 50.0f)
                        } else {
                            // prava polovina 1 <-> 3
                            saturationChanged?.invoke((p0.progress - 50.0f) / 50.0f * 3.0f + 1.0f)
                        }
                    }
                }
            })
            return view
        }
    }

}