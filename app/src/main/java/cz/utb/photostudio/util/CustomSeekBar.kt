package cz.utb.photostudio.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.SeekBar


@SuppressLint("AppCompatCustomView")
class CustomSeekBar: SeekBar {
    private var rect: Rect? = null
    private var paint: Paint? = null
    private var seekbar_height = 0
    var fromCenter: Boolean = true

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        rect = Rect()
        paint = Paint()
        seekbar_height = 6
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context,
        attrs,
        defStyle) {
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        rect!!.set(0 + thumbOffset,
            height / 2 - seekbar_height / 2,
            width - thumbOffset,
            height / 2 + seekbar_height / 2)
        paint!!.color = Color.GRAY
        canvas.drawRect(rect!!, paint!!)

        if(this.fromCenter) {
            if (this.progress > 50) {
                rect!!.set(width / 2,
                    height / 2 - seekbar_height / 2,
                    width / 2 + width / 100 * (progress - 50),
                    height / 2 + seekbar_height / 2)
                paint!!.color = Color.rgb(68, 17, 102)
                canvas.drawRect(rect!!, paint!!)
            }
            if (this.progress < 50) {
                rect!!.set(width / 2 - width / 100 * (50 - progress),
                    height / 2 - seekbar_height / 2,
                    width / 2,
                    height / 2 + seekbar_height / 2)
                paint!!.color = Color.rgb(68, 17, 102)
                canvas.drawRect(rect!!, paint!!)
            }
        } else {
            rect!!.set(0,
                height / 2 - seekbar_height / 2,
                width / 2 + width / 100 * (progress - 50),
                height / 2 + seekbar_height / 2)
            paint!!.color = Color.rgb(68, 17, 102)
            canvas.drawRect(rect!!, paint!!)
        }

        super.onDraw(canvas)
    }
}