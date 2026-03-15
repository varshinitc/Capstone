package com.aidriven.notificationdetector.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aidriven.notificationdetector.R
import com.aidriven.notificationdetector.adapters.NotificationAdapter
import com.aidriven.notificationdetector.databinding.ActivityHistoryBinding
import com.aidriven.notificationdetector.viewmodels.HistoryViewModel
import com.google.android.material.chip.Chip

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: NotificationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        
        setupRecyclerView()
        setupFilterChips()
        observeData()
        
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Clear All button
        binding.btnClearAll.setOnClickListener {
            showClearAllDialog()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            // Open detail activity
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("notification_id", notification.id)
                putExtra("app_name", notification.appName)
                putExtra("package_name", notification.packageName)
                putExtra("title", notification.title)
                putExtra("content", notification.content)
                putExtra("risk_level", notification.riskLevel)
                putExtra("risk_score", notification.riskScore)
                putExtra("detected_issues", notification.detectedIssues)
                putExtra("timestamp", notification.timestamp)
                putExtra("user_feedback", notification.userFeedback)
            }
            startActivity(intent)
        }
        
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
    
    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            selectChip(binding.chipAll)
            viewModel.filterNotifications("ALL")
        }
        
        binding.chipSafe.setOnClickListener {
            selectChip(binding.chipSafe)
            viewModel.filterNotifications("SAFE")
        }
        
        binding.chipSuspicious.setOnClickListener {
            selectChip(binding.chipSuspicious)
            viewModel.filterNotifications("SUSPICIOUS")
        }
        
        binding.chipHighRisk.setOnClickListener {
            selectChip(binding.chipHighRisk)
            viewModel.filterNotifications("HIGH_RISK")
        }
        
        // Select "All" by default
        selectChip(binding.chipAll)
    }
    
    private fun selectChip(selectedChip: Chip) {
        binding.chipAll.isChecked = false
        binding.chipSafe.isChecked = false
        binding.chipSuspicious.isChecked = false
        binding.chipHighRisk.isChecked = false
        selectedChip.isChecked = true
    }
    
    private fun observeData() {
        viewModel.allNotifications.observe(this) { notifications ->
            if (viewModel.getCurrentFilter() == "ALL") {
                adapter.submitList(notifications)
                updateEmptyState(notifications.isEmpty())
            }
        }
        
        viewModel.filteredNotifications.observe(this) { notifications ->
            if (viewModel.getCurrentFilter() != "ALL") {
                adapter.submitList(notifications)
                updateEmptyState(notifications.isEmpty())
            }
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.tvEmptyState.visibility = android.view.View.VISIBLE
            binding.recyclerView.visibility = android.view.View.GONE
        } else {
            binding.tvEmptyState.visibility = android.view.View.GONE
            binding.recyclerView.visibility = android.view.View.VISIBLE
        }
    }
    
    private fun showClearAllDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear All History")
            .setMessage("Are you sure you want to delete all analyzed notifications? This cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                viewModel.clearAllNotifications()
                android.widget.Toast.makeText(this, "All history cleared", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
