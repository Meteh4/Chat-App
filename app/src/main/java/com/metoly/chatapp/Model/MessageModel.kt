package com.metoly.chatapp.Model

import java.util.Calendar
import java.util.Date

data class MessageItem(
    val messageId: String? = "",
    val sendBy: String? = "",
    val message: String? = "",
    val timestamp: Date? = Calendar.getInstance().time,
)

data class MessageList(
    val messages: List<MessageItem> = listOf()
)