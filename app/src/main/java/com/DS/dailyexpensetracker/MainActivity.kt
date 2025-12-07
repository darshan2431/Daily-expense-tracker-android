package com.DS.dailyexpensetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.DS.dailyexpensetracker.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import android.graphics.Color
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: ExpenseDatabase
    private lateinit var adapter: ExpenseAdapter
    private var allExpenses: List<Expense> = emptyList()
    private var filteredExpenses: List<Expense> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = ExpenseDatabase.getDatabase(this)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupFilterChips()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onDeleteClick = { expense ->
                deleteExpense(expense)
            },
            onItemClick = { expense ->
                editExpense(expense)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupObservers() {
        // Observe expenses list
        lifecycleScope.launch {
            database.expenseDao().getAllExpenses().collect { expenses ->
                allExpenses = expenses
                applyFilter()
            }
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedChangeListener { _, checkedId ->
            applyFilter()
        }
    }

    private fun applyFilter() {
        filteredExpenses = when (binding.chipGroupFilter.checkedChipId) {
            binding.chipWeek.id -> filterByWeek(allExpenses)
            binding.chipMonth.id -> filterByMonth(allExpenses)
            else -> allExpenses // All Time
        }

        // Update UI with filtered data
        adapter.submitList(filteredExpenses)
        updateSummaryCards(filteredExpenses)
        updatePieChart(filteredExpenses)
    }

    private fun filterByWeek(expenses: List<Expense>): List<Expense> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.timeInMillis

        return expenses.filter { it.date >= weekStart }
    }

    private fun filterByMonth(expenses: List<Expense>): List<Expense> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStart = calendar.timeInMillis

        return expenses.filter { it.date >= monthStart }
    }

    private fun updateSummaryCards(expenses: List<Expense>) {
        // Calculate total
        val total = expenses.sumOf { it.amount }
        binding.tvTotal.text = String.format("₹%.2f", total)

        // Update count
        binding.tvExpenseCount.text = expenses.size.toString()

        // Find highest expense
        val highest = expenses.maxByOrNull { it.amount }?.amount ?: 0.0
        binding.tvHighestExpense.text = String.format("₹%.2f", highest)
    }

    private fun updatePieChart(expenses: List<Expense>) {
        if (expenses.isEmpty()) {
            binding.pieChart.clear()
            binding.pieChart.centerText = "No expenses yet"
            return
        }

        // Group expenses by category and sum amounts
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }

        // Create pie entries
        val entries = categoryTotals.map { (category, total) ->
            PieEntry(total, category)
        }

        // Create dataset
        val dataSet = PieDataSet(entries, "")

        // Set colors for each category
        val colors = listOf(
            Color.parseColor("#FF5722"), // Food
            Color.parseColor("#2196F3"), // Transport
            Color.parseColor("#9C27B0"), // Shopping
            Color.parseColor("#FFC107"), // Bills
            Color.parseColor("#E91E63"), // Entertainment
            Color.parseColor("#607D8B")  // Other
        )
        dataSet.colors = colors
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        // Create pie data
        val pieData = PieData(dataSet)

        // Configure chart
        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            centerText = "" // Clear center text
            holeRadius = 40f
            transparentCircleRadius = 45f
            setDrawEntryLabels(false)
            legend.isEnabled = true
            legend.textSize = 12f
            animateY(1000)
            invalidate()
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun deleteExpense(expense: Expense) {
        lifecycleScope.launch {
            database.expenseDao().delete(expense)
        }
    }

    private fun editExpense(expense: Expense) {
        val intent = Intent(this, AddExpenseActivity::class.java)
        intent.putExtra("EXPENSE_ID", expense.id)
        intent.putExtra("EXPENSE_AMOUNT", expense.amount)
        intent.putExtra("EXPENSE_DESCRIPTION", expense.description)
        intent.putExtra("EXPENSE_CATEGORY", expense.category)
        startActivity(intent)
    }
}