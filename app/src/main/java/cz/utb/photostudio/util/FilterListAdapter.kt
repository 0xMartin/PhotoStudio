package cz.utb.photostudio.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cz.utb.photostudio.R
import cz.utb.photostudio.filter.*
import java.util.LinkedList


class FilterListAdapter(context: Context, values: LinkedList<Filter>) : RecyclerView.Adapter<FilterListAdapter.ViewHolder>() {

    private val context: Context
    private var filters: LinkedList<Filter>

    private var lastSelectedView: ViewHolder? = null

    private var changed: ((filter: Filter)->Unit)? = null

    init {
        this.context = context
        this.filters = values
    }

    fun setOnChangedCallback(changed: ((filter: Filter)->Unit)?) {
        this.changed = changed
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
        val context: Context = parent.context
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.filter_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull holder: ViewHolder, position: Int) {
        val data: Filter = filters[position]
        holder.textView.text = data.filter_Name
        holder.initClickListener(data)
        holder.setIcon(data)
    }

    fun removeAllFilters() {
        val size: Int = filters.size
        if (size > 0) {
            this.lastSelectedView = null
            filters.clear()
            notifyItemRangeRemoved(0, size)
        }
    }

    fun removeFilter(filter: Filter) {
        val index: Int = filters.indexOf(filter)
        if(filters.remove(filter)) {
            notifyItemRemoved(index)
        }
    }

    fun addFilter(filter: Filter) {
        if(filters.add(filter)) {
            notifyItemInserted(filters.size - 1)
        }
    }

    fun getFilterList(): List<Filter> {
        return this.filters
    }

    override fun getItemCount(): Int {
        return filters.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView

        init {
            textView = itemView.findViewById(R.id.text1)
            this.highlight()
        }

        private fun highlight() {
            if(lastSelectedView != null) {
                lastSelectedView?.itemView?.backgroundTintList = null
            }
            lastSelectedView = this
            this.itemView.backgroundTintList = context.getColorStateList(R.color.purple_100)
        }

        fun initClickListener(filter: Filter) {
            itemView.setOnClickListener {
                this.highlight()
                changed?.invoke(filter)
            }
        }

        fun setIcon(filter: Filter) {
            val img: ImageView = itemView.findViewById(R.id.filter_icon)
            when (filter) {
                is Contrast -> {
                    img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.contrast_icon))
                }
                is Brightness -> {
                    img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.brightness_icon))
                }
                is Saturation -> {
                    img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.saturation_icon))
                }
                is RGB -> {
                    img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.rgb_icon))
                }
            }
        }
    }

}