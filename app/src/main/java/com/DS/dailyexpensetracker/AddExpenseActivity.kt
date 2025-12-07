package com.DS.dailyexpensetracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.DS.dailyexpensetracker.databinding.ActivityAddExpenseBinding
import kotlinx.coroutines.launch

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private lateinit var database: ExpenseDatabase
    private var expenseId: Int = 0
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = ExpenseDatabase.getDatabase(this)

        setupCategorySpinner()
        checkEditMode()
        setupClickListeners()
    }

    private fun checkEditMode() {
        expenseId = intent.getIntExtra("EXPENSE_ID", 0)

        if (expenseId != 0) {
            isEditMode = true
            binding.tvTitle.text = "Edit Expense"
            binding.btnSave.text = "Update"

            // Pre-fill data
            val amount = intent.getDoubleExtra("EXPENSE_AMOUNT", 0.0)
            val description = intent.getStringExtra("EXPENSE_DESCRIPTION") ?: ""
            val category = intent.getStringExtra("EXPENSE_CATEGORY") ?: ""

            binding.etAmount.setText(amount.toString())
            binding.etDescription.setText(description)

            // Set spinner selection
            val categories = arrayOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Other")
            val position = categories.indexOf(category)
            if (position >= 0) {
                binding.spinnerCategory.setSelection(position)
            }
        } else {
            binding.tvTitle.text = "Add New Expense"
            binding.btnSave.text = "Save"
        }
    }

    private fun setupCategorySpinner() {
        val categories = arrayOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            if (isEditMode) {
                updateExpense()
            } else {
                saveExpense()
            }
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveExpense() {
        val amountText = binding.etAmount.text.toString()
        val description = binding.etDescription.text.toString()
        val category = binding.spinnerCategory.selectedItem.toString()

        // Validation
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Create and save expense
        val expense = Expense(
            amount = amount,
            description = description,
            category = category
        )

        lifecycleScope.launch {
            database.expenseDao().insert(expense)
            Toast.makeText(this@AddExpenseActivity, "Expense added!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateExpense() {
        val amountText = binding.etAmount.text.toString()
        val description = binding.etDescription.text.toString()
        val category = binding.spinnerCategory.selectedItem.toString()

        // Validation
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Update expense
        val expense = Expense(
            id = expenseId,
            amount = amount,
            description = description,
            category = category
        )

        lifecycleScope.launch {
            database.expenseDao().update(expense)
            Toast.makeText(this@AddExpenseActivity, "Expense updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}