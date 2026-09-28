package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.SportsRepository
import com.example.domain.notification.NotificationHelper
import com.example.domain.notification.NotificationPreferences
import com.example.ui.navigation.ReveNavGraph
import com.example.ui.theme.REVELiveSportsTheme
import com.example.ui.viewmodel.SportsViewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: SportsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize local notification preferences and notification channels
        NotificationPreferences.init(applicationContext)
        NotificationHelper.createNotificationChannels(applicationContext)

        val database = AppDatabase.getInstance(applicationContext)
        val repository = SportsRepository(database.favoriteDao())

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SportsViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, viewModelFactory)[SportsViewModel::class.java]

        setContent {
            REVELiveSportsTheme {
                ReveNavGraph(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::viewModel.isInitialized) {
            viewModel.setAppForegroundState(true)
        }
    }

    override fun onStop() {
        super.onStop()
        if (::viewModel.isInitialized) {
            viewModel.setAppForegroundState(false)
        }
    }
}
