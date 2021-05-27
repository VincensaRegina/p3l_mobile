package com.vincensaregina.p3lproject.ui.menu

import android.app.Application
import android.view.View
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vincensaregina.p3lproject.MenuActivity
import com.vincensaregina.p3lproject.api.ApiClient
import com.vincensaregina.p3lproject.api.ApiInterface
import com.vincensaregina.p3lproject.api.MenuDAO
import com.vincensaregina.p3lproject.api.MenuResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
//android view model = ada application ref utk context
class MenuViewModel : ViewModel() {

//    companion object {
//        private val TAG = MenuActivity::class.java.simpleName
//    }
//
//    private val listMenu = MutableLiveData<ArrayList<MenuDAO>>()
//    private var call: Call<MenuResponse>? = null
//
//    fun loadMenu(jenis_menu: String) {
//        val apiService: ApiInterface =
//            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface
//
//        //Untuk filter berdasarkan jenis menu
//        if (jenis_menu.equals("Semua")) call = apiService.getAllMenu()
//        else call = apiService.getSpecificMenu(jenis_menu)
//
//        call?.enqueue(object : Callback<MenuResponse> {
//            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
//                Toast.makeText(
//                    getApplication(),
//                    "$jenis_menu berhasil ditampilkan",
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//                listMenu = response.body().getMenu()
//            }
//
//            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
//                Toast.makeText( getApplication(), "Kesalahan Jaringan", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        })
//    }
//    fun getUsers(): LiveData<ArrayList<MenuDAO>> {
//        return listMenu
//    }
}