package com.metoly.chatapp.Model

open class Event<out T>(val content: T) {
    private var HasBeenHandled = false
    fun GetContentOrNull(): T? {
        return if (HasBeenHandled) null
        else {
            HasBeenHandled = true
            content
        }
    }
}