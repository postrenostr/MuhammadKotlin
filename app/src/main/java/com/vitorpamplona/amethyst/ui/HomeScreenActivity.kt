package com.vitorpamplona.amethyst.ui

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitorpamplona.amethyst.LocalPreferences
import com.vitorpamplona.amethyst.model.ImageSearch
import com.vitorpamplona.amethyst.service.relays.Client
import com.vitorpamplona.amethyst.ui.navigation.Route
import com.vitorpamplona.amethyst.ui.screen.AccountScreen
import com.vitorpamplona.amethyst.ui.screen.AccountStateViewModel
import com.vitorpamplona.amethyst.ui.screen.HomeScreenUI
import com.vitorpamplona.amethyst.ui.theme.AmethystTheme

class HomeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.data != null) {
            Intent(this, MainActivity::class.java).apply {
                data = intent.data
            }.run {
                startActivity(this)
            }

            return
        }

        setContent {
            AmethystTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    val accountStateViewModel: AccountStateViewModel = viewModel {
                        AccountStateViewModel(this@HomeScreenActivity)
                    }

                    AccountScreen(accountStateViewModel) { word ->

                        val imageSearch = ImageSearch(true, word)
                        val isImage= isImageSearch(word)

                        Intent(this, MainActivity::class.java).apply {
                            putExtra("searchImage", isImageSearch(word))
                            if (!isImage) {
                                putExtra("localRoute", getRoutes(word))
                            }else {
                                putExtra("imageSearch", imageSearch)
                            }
                            data = intent.data
                        }.run {
                            startActivity(this)
                        }
                    }

                }
            }

        }


    }


    fun getRoutes(name: String): String {
        return when (name) {
            "Messages" -> Route.Message.route
            "Videos" -> Route.Video.route
            "Feed" -> Route.Search.route
            else -> name
        }
    }

    fun isImageSearch(name: String): Boolean {
        return when (name) {
            "Messages" -> false
            "Videos" -> false
            "Feed" -> false
            else -> true
        }
    }
}