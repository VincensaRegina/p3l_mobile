package com.vincensaregina.p3lproject.api

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TransaksiResponse {
    @SerializedName("data")
    @Expose
    private val jumlah: Int = -1

    @SerializedName("message")
    @Expose
    private val message: String? = null

    fun getJumlah(): Int {
        return jumlah
    }

    fun getMessage(): String? {
        return message
    }
}