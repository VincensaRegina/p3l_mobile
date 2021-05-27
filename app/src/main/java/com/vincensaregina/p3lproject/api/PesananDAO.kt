package com.vincensaregina.p3lproject.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PesananDAO (
    var id: Int,
    var id_reservasi: Int,
    var id_menu: String?,
    var id_bahan: Int,
    var nama_menu: String?,
    var unit_menu: String?,
    var harga_menu: Double,
    var qty: Int,
    var stok_bahan: Int,
    var serv_size: Int,
    var subtotal: Double ,
    var locked: String?,
    var status: String?,
    var gambar: String?,
): Parcelable