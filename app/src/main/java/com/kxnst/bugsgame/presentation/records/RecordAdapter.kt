package com.kxnst.bugsgame.presentation.records

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.kxnst.bugsgame.R
import com.kxnst.bugsgame.databinding.ItemRecordBinding
import com.kxnst.bugsgame.domain.user.UserProfile

class RecordAdapter : ListAdapter<UserProfile, RecordAdapter.RecordViewHolder>(DiffCallback) {
    private var currentUserName: String? = null

    fun setCurrentUser(name: String?) {
        if (currentUserName == name) return

        val oldName = currentUserName
        currentUserName = name

        val oldPos = currentList.indexOfFirst { it.name == oldName }
        val newPos = currentList.indexOfFirst { it.name == name }

        if (oldPos >= 0) notifyItemChanged(oldPos)
        if (newPos >= 0) notifyItemChanged(newPos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = ItemRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return RecordViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1, getItem(position).name == currentUserName)
    }

    class RecordViewHolder(
        private val binding: ItemRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserProfile, position: Int, current: Boolean) {
            binding.tvRecordPosition.text = position.toString()
            binding.tvRecordName.text = user.name
            binding.tvRecordScore.text = user.bestScore.toString()

            val context = binding.root.context
            val strokeWidth = context.resources.getDimensionPixelSize(
                R.dimen.stroke_button_default
            )

            binding.cvRecord.strokeWidth = if (current) strokeWidth else 0
            binding.cvRecord.strokeColor = ContextCompat.getColor(
                context,
                if (current) R.color.primary else R.color.outlineVariant
            )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<UserProfile>() {
        override fun areItemsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem == newItem
        }
    }
}
