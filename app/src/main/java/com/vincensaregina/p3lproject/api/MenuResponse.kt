package com.vincensaregina.p3lproject.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MenuResponse {
    @SerializedName("data") //telling the Parser when receiving a callback from the server i.e. of a Json format:
    @Expose // to declare which fields to serialize, and ignore the others
    private val menu: ArrayList<MenuDAO>? = null

    @SerializedName("message")
    @Expose
    private val message: String? = null

    fun getMenu(): ArrayList<MenuDAO>? {
        return menu
    }

    fun getMessage(): String? {
        return message
    }
}