package com.metoly.chatapp.Model

data class ChatData(
    val chatId : String? = "",
    val user1 : ChatUser = ChatUser(),
    val user2 : ChatUser = ChatUser(),
)

data class GroupChatData(
    val chatId : String? = "",
    val groupName : String? = "",
    val users: List<ChatUser> = listOf(),
    val groupImageUrl : String? = "",
    val adminId : String? = "",
)

data class ChatUser(
    val userId: String? = "",
    val name: String? = "",
    val imageUrl: String? = "",
    val nickName: String? = "",
)