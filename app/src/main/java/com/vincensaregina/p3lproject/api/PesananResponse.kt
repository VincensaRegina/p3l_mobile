package com.vincensaregina.p3lproject.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PesananResponse {
    @SerializedName("data")
    @Expose
    private val pesanan: ArrayList<PesananDAO>? = null

    @SerializedName("message")
    @Expose
    private val message: String? = null

    fun getPesanan(): ArrayList<PesananDAO>? {
        return pesanan
    }

    fun getMessage(): String? {
        return message
    }
}