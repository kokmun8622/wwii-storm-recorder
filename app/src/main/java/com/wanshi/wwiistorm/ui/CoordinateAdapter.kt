package com.wanshi.wwiistorm.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wanshi.wwiistorm.R
import com.wanshi.wwiistorm.model.CoordinateRecord
import java.text.SimpleDateFormat
import java.util.Locale

class CoordinateAdapter(
    private val onDelete: (Long) -> Unit
) : ListAdapter<CoordinateRecord, CoordinateAdapter.RecordViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.record_item, parent, false)
        return RecordViewHolder(view, onDelete)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecordViewHolder(
        itemView: View,
        private val onDelete: (Long) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(R.id.textTitle)
        private val coordinateView: TextView = itemView.findViewById(R.id.textCoordinates)
        private val metaView: TextView = itemView.findViewById(R.id.textMeta)
        private val deleteButton: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(record: CoordinateRecord) {
            titleView.text = record.title
            coordinateView.text = "X: ${record.x}, Y: ${record.y}"
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            metaView.text = "${record.mapName} • ${formatter.format(record.createdAt)}"
            deleteButton.setOnClickListener { onDelete(record.id) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CoordinateRecord>() {
            override fun areItemsTheSame(oldItem: CoordinateRecord, newItem: CoordinateRecord): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CoordinateRecord, newItem: CoordinateRecord): Boolean {
                return oldItem == newItem
            }
        }
    }
}
