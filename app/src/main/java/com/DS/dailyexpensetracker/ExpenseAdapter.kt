package com.DS.dailyexpensetracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.DS.dailyexpensetracker.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private val onDeleteClick: (Expense) -> Unit,
    private val onItemClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))

        // Add fade-in animation
        holder.itemView.alpha = 0f
        holder.itemView.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.apply {
                tvAmount.text = String.format("₹%.2f", expense.amount)
                tvDescription.text = expense.description
                tvCategory.text = expense.category
                tvDate.text = formatDate(expense.date)

                // Set category icon and color
                val (icon, color) = getCategoryIconAndColor(expense.category)
                tvCategoryIcon.text = icon
                categoryColorBar.setBackgroundColor(color)

                btnDelete.setOnClickListener {
                    onDeleteClick(expense)
                }
            }

                // Make entire card clickable for editing
                // Make entire card clickable for editing
            binding.root.setOnClickListener {
                onItemClick(expense)
            }
        }

        private fun getCategoryIconAndColor(category: String): Pair<String, Int> {
            return when (category) {
                "Food" -> Pair("🍔", 0xFFFF5722.toInt())
                "Transport" -> Pair("🚗", 0xFF2196F3.toInt())
                "Shopping" -> Pair("🛍️", 0xFF9C27B0.toInt())
                "Bills" -> Pair("📄", 0xFFFFC107.toInt())
                "Entertainment" -> Pair("🎬", 0xFFE91E63.toInt())
                else -> Pair("💰", 0xFF607D8B.toInt())
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}