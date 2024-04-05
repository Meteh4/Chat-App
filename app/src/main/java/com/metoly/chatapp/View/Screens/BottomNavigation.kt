import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metoly.chatapp.DestinationScreen
import com.metoly.chatapp.R
import com.metoly.chatapp.ui.theme.navigateTo


enum class BottomNavBarItems(val icon: Int, val destinationScreen: DestinationScreen) {
    CHAT(R.drawable.chats, DestinationScreen.Chat),
    STATUS(R.drawable.status, DestinationScreen.GroupChat),
    PROFILE(R.drawable.account, DestinationScreen.Profile),
}



@Composable
fun BottomNavBar(
    selectedItem: BottomNavBarItems,
    navController: NavController
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = BottomAppBarDefaults.ContentPadding
    ) {
        BottomNavBarItems.entries.forEach { item ->
            BottomNavigationItem(
                icon = {
                    Image(
                        painter = painterResource(id = item.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(
                            if (item == selectedItem) Color.Black else Color.Gray
                        )
                    )
                },
                selected = item == selectedItem,
                onClick = {
                    navigateTo(navController, item.destinationScreen.route)
                }
            )
        }
    }
}