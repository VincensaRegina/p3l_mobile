package com.vincensaregina.p3lproject.api


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MenuDAO (
    var id: Int,
    var nama: String?,
    var harga: Double?,
    var serv_size: Int,
    var unit: String?,
    var unit_bahan: String?,
    var desc: String?,
    var gambar: String?,
    var id_bahan: Int,
    var stok_bahan: Int,
): Parcelable
//class Menu {
//    @SerializedName("id")
//    private var id: String? = null
//
//    @SerializedName("nama")
//    private var nama_menu: String? = null
//
//    @SerializedName("harga")
//    private var harga: Double? = null
//
//    @SerializedName("serv_size")
//    private var serv_size: Int? = null
//
//    @SerializedName("unit")
//    private var unit: String? = null
//
//    @SerializedName("desc")
//    private var desc: String? = null
//
//    @SerializedName("gambar")
//    private var gambar: String? = null
//
//    fun UserDAO(
//        id: String,
//        nama: String,
//        harga: Double,
//        serv_size: String,
//        unit: String,
//        desc: String,
//        gambar: String
//    ) {
//        this.id = id
//        this.nama_menu = nama
//        this.harga = harga
//        this.serv_size = serv_size
//        this.unit = unit
//        this.desc = desc
//        this.gambar = gambar
//    }
//
//}