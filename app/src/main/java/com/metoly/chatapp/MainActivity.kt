package com.metoly.chatapp
import GroupChatScreen
import GroupMessageScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.initialize
import com.metoly.chatapp.View.Screens.ChatScreen
import com.metoly.chatapp.View.Screens.LoginScreen
import com.metoly.chatapp.View.Screens.ProfileScreen
import com.metoly.chatapp.View.Screens.RegisterScreen
import com.metoly.chatapp.View.Screens.PrivateMessageScreen
import com.metoly.chatapp.ui.theme.ChatAppTheme
import com.metoly.chatapp.viewmodel.ChatViewModel
import com.metoly.chatapp.viewmodel.GroupChatViewModel
import com.metoly.chatapp.viewmodel.UserAuthViewModel
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(var route: String) {
    data object Signup : DestinationScreen("signup")
    data object Login : DestinationScreen("login")
    data object Profile : DestinationScreen("profile")
    data object Chat : DestinationScreen("chat")
    data object SingleChat : DestinationScreen("singlechat/{chatId}") {
        fun createRoute(id : String) = "SingleChat/$id" }
    data object GroupChat : DestinationScreen("groupchats")
    data object GroupMessage : DestinationScreen("groupmessage") {
        fun createRoute(chatId: String) = "groupmessage/$chatId" }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
            )
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    @Composable
    fun AppNavigation(){

        val navController = rememberNavController()
        val authVM = hiltViewModel<UserAuthViewModel>()
        val chatVM = hiltViewModel<ChatViewModel>()
        val groupChatVM = hiltViewModel<GroupChatViewModel>()
        NavHost(navController = navController, startDestination = DestinationScreen.Signup.route ){
            composable(DestinationScreen.Signup.route){
                RegisterScreen(navController,authVM)
            }

            composable(DestinationScreen.Login.route){
                LoginScreen(navController = navController,vm = authVM)
            }

            composable(DestinationScreen.Chat.route){
                ChatScreen(navController = navController,vm = chatVM)
            }

            composable(DestinationScreen.SingleChat.route){
                val chatId = it.arguments?.getString("chatId")
                chatId?.let {
                    PrivateMessageScreen(navController = navController, vm = chatVM, chatId = chatId)
                }
            }

            composable(DestinationScreen.GroupMessage.route + "/{chatId}") { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")
                if (chatId != null) {
                    GroupMessageScreen(navController = navController, vm = groupChatVM, chatId = chatId)
                }
            }

            composable(DestinationScreen.GroupChat.route){
                GroupChatScreen(navController = navController,vm = groupChatVM)
            }

            composable(DestinationScreen.Profile.route){
                ProfileScreen(navController = navController,vm = authVM)
            }
        }
    }
}
