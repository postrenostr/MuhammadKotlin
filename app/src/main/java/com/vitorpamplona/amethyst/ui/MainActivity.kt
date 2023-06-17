package com.vitorpamplona.amethyst.ui

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitorpamplona.amethyst.LocalPreferences
import com.vitorpamplona.amethyst.ServiceManager
import com.vitorpamplona.amethyst.model.ImageSearch
import com.vitorpamplona.amethyst.service.model.ChannelCreateEvent
import com.vitorpamplona.amethyst.service.model.ChannelMessageEvent
import com.vitorpamplona.amethyst.service.model.ChannelMetadataEvent
import com.vitorpamplona.amethyst.service.model.PrivateDmEvent
import com.vitorpamplona.amethyst.service.nip19.Nip19
import com.vitorpamplona.amethyst.service.notifications.PushNotificationUtils
import com.vitorpamplona.amethyst.service.relays.Client
import com.vitorpamplona.amethyst.ui.components.DefaultMutedSetting
import com.vitorpamplona.amethyst.ui.navigation.Route
import com.vitorpamplona.amethyst.ui.note.Nip47
import com.vitorpamplona.amethyst.ui.screen.AccountScreen
import com.vitorpamplona.amethyst.ui.screen.AccountState
import com.vitorpamplona.amethyst.ui.screen.AccountStateViewModel
import com.vitorpamplona.amethyst.ui.screen.HomeScreenUI
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.screen.loggedIn.HomeScreen
import com.vitorpamplona.amethyst.ui.screen.loggedIn.MainScreen
import com.vitorpamplona.amethyst.ui.screen.loggedOff.LoginPage
import com.vitorpamplona.amethyst.ui.theme.AmethystTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : FragmentActivity() {

    private val imageSearch by lazy {
        intent.getSerializableExtra("imageSearch") as ImageSearch? ?: ImageSearch(false, null)
    }

    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalPreferences.migrateSingleUserPrefs()
        val uri = intent?.data?.toString()
        var startingPage = uriToRoute(uri)

        if (intent.getBooleanExtra("searchImage", false)) {
            startingPage = Route.Search.route
        } else if (intent.getStringExtra("localRoute") != null) {
            startingPage = intent.getStringExtra("localRoute")
        }

        setContent {
            AmethystTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    val accountStateViewModel: AccountStateViewModel = viewModel {
                        AccountStateViewModel(this@MainActivity)
                    }

                    val accountState = accountStateViewModel.accountContent.value

                    when (accountState) {
                        is AccountState.LoggedOff -> {
                            LoginPage(accountStateViewModel, isFirstLogin = true)
                        }

                        is AccountState.LoggedIn -> {
                            val accountViewModel: AccountViewModel = viewModel(
                                key = accountState.account.userProfile().pubkeyHex,
                                factory = AccountViewModel.Factory(accountState.account)
                            )

                            MainScreen(
                                accountViewModel,
                                accountStateViewModel,
                                startingPage,
                                imageSearch
                            )

                        }

                        is AccountState.LoggedInViewOnly -> {
                            val accountViewModel: AccountViewModel = viewModel(
                                key = accountState.account.userProfile().pubkeyHex,
                                factory = AccountViewModel.Factory(accountState.account)
                            )

                            MainScreen(
                                accountViewModel,
                                accountStateViewModel,
                                startingPage,
                                imageSearch
                            )
                        }
                    }

                }
            }
        }

        Client.lenient = true

    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
        // starts muted every time
        DefaultMutedSetting.value = true

        // Only starts after login
        GlobalScope.launch(Dispatchers.IO) {
            ServiceManager.start(this@MainActivity)
        }

        PushNotificationUtils().init(LocalPreferences.allSavedAccounts())
    }

    override fun onPause() {
        ServiceManager.pause()
        super.onPause()
    }

    /**
     * Release memory when the UI becomes hidden or when system resources become low.
     * @param level the memory-related event that was raised.
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        println("Trim Memory $level")
        GlobalScope.launch(Dispatchers.Default) {
            ServiceManager.cleanUp()
        }
    }

}

class GetMediaActivityResultContract : ActivityResultContracts.GetContent() {

    override fun createIntent(context: Context, input: String): Intent {
        return super.createIntent(context, input).apply {
            // Force only images and videos to be selectable
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
    }
}

fun uriToRoute(uri: String?): String? {
    return if (uri.equals("nostr:Notifications", true)) {
        Route.Notification.route.replace("{scrollToTop}", "true")
    } else {
        if (uri?.startsWith("nostr:Hashtag?id=") == true) {
            Route.Hashtag.route.replace("{id}", uri.removePrefix("nostr:Hashtag?id="))
        } else {
            val nip19 = Nip19.uriToRoute(uri)
            when (nip19?.type) {
                Nip19.Type.USER -> "User/${nip19.hex}"
                Nip19.Type.NOTE -> "Note/${nip19.hex}"
                Nip19.Type.EVENT -> {
                    if (nip19.kind == PrivateDmEvent.kind) {
                        "Room/${nip19.author}"
                    } else if (nip19.kind == ChannelMessageEvent.kind || nip19.kind == ChannelCreateEvent.kind || nip19.kind == ChannelMetadataEvent.kind) {
                        "Channel/${nip19.hex}"
                    } else {
                        "Event/${nip19.hex}"
                    }
                }

                Nip19.Type.ADDRESS -> "Note/${nip19.hex}"
                else -> null
            }
        } ?: try {
            uri?.let {
                Nip47.parse(it)
                val encodedUri = URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                Route.Home.base + "?nip47=" + encodedUri
            }
        } catch (e: Exception) {
            null
        }
    }
}


