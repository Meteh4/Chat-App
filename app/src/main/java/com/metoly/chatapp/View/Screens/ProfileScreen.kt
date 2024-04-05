package com.metoly.chatapp.View.Screens

import BottomNavBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metoly.chatapp.DestinationScreen
import com.metoly.chatapp.ui.theme.CustomDivider
import com.metoly.chatapp.ui.theme.CustomImage
import com.metoly.chatapp.ui.theme.CommonProgressBar
import com.metoly.chatapp.ui.theme.navigateTo
import com.metoly.chatapp.viewmodel.UserAuthViewModel

@Composable
fun ProfileScreen(navController: NavController, vm: UserAuthViewModel) {
    val inProgress = vm.inProgress.value
    if (inProgress) {
        CommonProgressBar()
    } else {
        val userData = vm.userData.value
        var name by rememberSaveable {
            mutableStateOf(userData?.Name?:"")
        }
        var nickName by rememberSaveable {
            mutableStateOf(userData?.NickName?:"")
        }

        Scaffold (
            bottomBar = {
                BottomNavBar(
                    selectedItem = BottomNavBarItems.PROFILE,
                    navController = navController
                )
            }
        ){
            paddingValues ->
            Column{
                ProfileContent(
                    modifier = Modifier
                        .padding(paddingValues)
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp),
                    vm = vm,
                    name = name,
                    nickName = nickName,
                    onNameChange = { name = it },
                    onNickNameChange = { nickName = it },
                    onSave = {vm.createOrUpdateProfile(name = name, nickName = nickName)},
                    onBack = { navigateTo(navController = navController, route = DestinationScreen.Chat.route) },
                    onLogout = {
                        vm.logOut()
                        navigateTo(navController = navController, route = DestinationScreen.Login.route)
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileContent(
    modifier: Modifier,
    onBack: () -> Unit,
    onSave: () -> Unit,
    name: String,
    nickName: String,
    onNameChange: (String) -> Unit,
    onNickNameChange: (String) -> Unit,
    onLogout: () -> Unit,
    vm: UserAuthViewModel
) {
    Column {
        val imageUrl = vm.userData.value?.ImgURL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Back", Modifier.clickable {
                onBack.invoke()
            })
            Text(text = "Save", Modifier.clickable {
                onSave.invoke()
            })
        }
        CustomDivider()
        ProfileImage(imageUrl = imageUrl, vm = vm)

        CustomDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Name", modifier = Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Nick Name", modifier = Modifier.width(100.dp))
            TextField(
                value = nickName,
                onValueChange = onNickNameChange,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                )
            )
        }

        CustomDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "LogOut", modifier = Modifier.clickable { onLogout.invoke() })
        }
    }
}

@Composable
fun ProfileImage(imageUrl: String?, vm: UserAuthViewModel) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            vm.uploadProfileImage(uri)
        }
    }
    Box(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                imageUrl?.let { CustomImage(data = it) }
            }
            Text(text = "Change Profile Image")
        }
        if (vm.inProgress.value) {
            CommonProgressBar()
        }
    }
}