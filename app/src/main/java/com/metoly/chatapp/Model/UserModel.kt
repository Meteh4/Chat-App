package com.metoly.chatapp.Model

data class userData(
    var UID: String? = "",
    var Name: String? = "",
    var NickName: String? = "",
    var ImgURL: String? = ""
) {
    fun toMap() = mapOf(
        "userId" to UID,
        "name" to Name,
        "nickName" to NickName,
        "imageUrl" to ImgURL,
    )
}