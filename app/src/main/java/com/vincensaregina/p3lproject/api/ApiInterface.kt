package com.vincensaregina.p3lproject.api

import okhttp3.ResponseBody
import retrofit2.Call

import retrofit2.http.*
import java.util.*

interface ApiInterface {
    //get all menu
    @GET("menuPublic")
    fun getAllMenu(): Call<MenuResponse>?

    //get menu from a specific menu type
    @GET("menuJenis/{jenis}")
    fun getSpecificMenu(@Path("jenis") data: String?): Call<MenuResponse>?

    //check whether there's the same menu being added to cart
    @GET("cekMenuCart/{id_reservasi}/{id_menu}")
    fun cekMenuCart(    @Path("id_reservasi") id_reservasi: Int,
                        @Path("id_menu") id_menu: Int
    ): Call<MenuResponse>?

    //check whether there's the same menu being added to cart
    @GET("pesananSpecificCustomer/{id_reservasi}")
    fun getPesananCustomer(@Path("id_reservasi") id_reservasi: Int,
    ): Call<PesananResponse>?

    //check whether there's the same menu being added to cart
    @GET("countTransaksiMobile")
    fun countTransaksi(): Call<TransaksiResponse>?

    //simpan pesanan
    @POST("pesanan")
    @FormUrlEncoded
    fun addPesanan(
        @Field("id_reservasi") id_reservasi: Int,
        @Field("id_menu") id_menu: Int,
        @Field("qty") qty: Int,
        @Field("subtotal") subtotal: Double,
        @Field("locked") locked: String?,
        @Field("status") status: String?,
    ): Call<Result<MenuResponse>> //I dunno why disuruh ganti jadi Result<..>.
                                    // Kalo engga, data ke save tapi masuk onfailure dengan
                                    //error IllegalStateException: Expected BEGIN_ARRAY but was BEGIN_OBJECT

    //update qty pada tabel pesanan saat edit pesanan
    @POST("pesanan/{id}")
    @FormUrlEncoded
    fun updateQty(
        @Field("qty") qty: Int,
        @Field("subtotal") subtotal: Double,
        @Path("id") id_pesanan: Int,
    ): Call<Result<PesananResponse>>

    //update stok pada tabel bahan
    @POST("updateStokMobile/{id_bahan}/{jenis}")
    @FormUrlEncoded
    fun updateStokBahan(
        @Field("servXqty") servXqty: Int,
        @Path("id_bahan") id_bahan: Int,
        @Path("jenis") jenisUbah: String,
    ): Call<Result<MenuResponse>>

    //update stok pada tabel remaining stock
    @POST("updateRSMobile/{id_bahan}/{jenis}")
    @FormUrlEncoded
    fun updateStokRS(
        @Field("servXqty") servXqty: Int,
        @Path("id_bahan") id_bahan: Int,
        @Path("jenis") jenisUbah: String,
    ): Call<Result<MenuResponse>>

    //simpan data transaksi
    @POST("transaksiMobile")
    @FormUrlEncoded
    fun addTransaksi(
        @Field("id_reservasi") id_reservasi: Int,
        @Field("id_karyawan") id_karyawan: Int,
        @Field("id_kartu") id_kartu: Int,
        @Field("no_transaksi") no_transaksi: String,
        @Field("subtotal") subtotal: Double,
        @Field("status") status: String,
    ): Call<Result<PesananDAO>>

    //update locked pesanan menjadi yes
    @POST("locked/{id}")
    @FormUrlEncoded
    fun updateLocked(
        @Field("locked") locked: String,
        @Path("id") id_pesanan: Int,
    ): Call<Result<PesananDAO>>

    //update stok pada tabel riwayat keluar
    @POST("riwayatKeluarMobile")
    @FormUrlEncoded
    fun addRiwayatKeluar(
        @Field("id_bahan") id_bahan: Int,
        @Field("tgl_keluar") tgl_keluar: String,
        @Field("stok_keluar") stok_keluar: Int,
        @Field("keterangan") keterangan: String,
    ): Call<Result<PesananDAO>>

    @DELETE("pesanan/{id}")
    fun deletePesanan(@Path("id") id_pesanan: Int,): Call<Result<PesananResponse>>?


}