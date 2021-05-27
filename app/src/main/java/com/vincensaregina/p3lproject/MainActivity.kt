package com.vincensaregina.p3lproject

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.zxing.integration.android.IntentIntegrator
import com.vincensaregina.p3lproject.DetailActivity.Companion.EXTRA_DATA
import com.vincensaregina.p3lproject.api.ApiClient
import com.vincensaregina.p3lproject.api.ApiInterface
import com.vincensaregina.p3lproject.api.MenuDAO
import com.vincensaregina.p3lproject.api.MenuResponse
import com.vincensaregina.p3lproject.databinding.ActivityMainBinding
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    //View Binding
    private lateinit var binding: ActivityMainBinding
    private var amb = "blabla"

    //Recycler View
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var jenisAdapter: JenisMenuAdapter

    //Retrofit
    private var call: Call<MenuResponse>? = null

    //Shared Pref
    private lateinit var shared: SharedPreferences

    //qr code scanner object
    private lateinit var intentIntegrator: IntentIntegrator

    //Variable for shared pref
    private var id_customer: Int = -1
    private var id_reservasi: Int = -1
    private lateinit var nama_customer: String
    private var no_meja: Int = -1
    private var id_meja: Int = -1
    private lateinit var tgl_reservasi: String
    private lateinit var sesi: String

    //Dialog
    private lateinit var mDialogView: View

    //Bottom Sheet
    private lateinit var layoutBottomSheet: ConstraintLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //View Binding -> biar gausah pake findviewbyid
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ///////ActionBar
        //Remove shadow
        supportActionBar?.elevation = 0F
        //Remove default title in toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //Set adapter
        jenisAdapter = JenisMenuAdapter()
        menuAdapter = MenuAdapter()
        menuAdapter.notifyDataSetChanged()

        // Init Shared Pref
        shared = getSharedPreferences("dataQR", Context.MODE_PRIVATE)

        //Bottom Sheet
        layoutBottomSheet = binding.bottomSheetDetail.bottomSheetDetail
        bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        //Load Data awal dengan semua menu
        loadMenu("Semua")


        //Floating button
        binding.fab.setOnClickListener {
            // Inisialisasi IntentIntegrator(scanQR)
            // IntentIntegrator: an utility class which helps ease integration with Barcode Scanner via Intents
            // This s a simple way to invoke barcode scanning and receive the result,
            // without any need to integrate, modify, or learn the project's source code.
            intentIntegrator = IntentIntegrator(this).setBeepEnabled(false)
            intentIntegrator.initiateScan()
        }
    }

    //Memunculkan menu di action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        //karena ini option menu, jd perlu dikasih tau bahwa ini searchview.
        (menu.findItem(R.id.search).actionView as SearchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                menuAdapter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                menuAdapter.filter(newText)
                return true
            }
        })
        return true
    }

    fun loadMenu(jenis_menu: String) {
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface

        //Untuk filter berdasarkan jenis menu
        if (jenis_menu.equals("Semua")) call = apiService.getAllMenu()
        else call = apiService.getSpecificMenu(jenis_menu)

        call?.enqueue(object : Callback<MenuResponse> {
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                Toast.makeText(
                    this@MainActivity,
                    "$jenis_menu berhasil ditampilkan",
                    Toast.LENGTH_SHORT
                )
                    .show()
                response.body()?.getMenu()?.let { generateDataList(it) }

                // Stopping Shimmer Effect's animation after data is loaded to ListView
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
            }

            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Kesalahan Jaringan", Toast.LENGTH_SHORT)
                    .show()
                Log.d("error load menu", t.message.toString());
            }
        })
    }

    private fun generateDataList(menuList: List<MenuDAO>) {
        //Load Jenis Menu
        jenisAdapter.setData(applicationContext)
        binding.rvJenis.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvJenis.adapter = jenisAdapter

        //Load List Menu
        menuAdapter.setData(menuList)
        binding.rvMenu.layoutManager = LinearLayoutManager(this)
        binding.rvMenu.setHasFixedSize(true)
        binding.rvMenu.itemAnimator = DefaultItemAnimator()
        binding.rvMenu.adapter = menuAdapter

        //Click item jenis menu (semua, makanan utama, side dish, minuman)
        jenisAdapter.setOnItemClickCallback(object : JenisMenuAdapter.OnItemClickCallback {
            override fun onItemClicked(data: String) {
                loadMenu(data)
            }
        })

        //Click item list menu
        menuAdapter.setOnItemClickCallback(object : MenuAdapter.OnItemClickCallback {
            override fun onItemClicked(data: MenuDAO) {
                with(binding.bottomSheetDetail) {
                    tvNamaMenuDetail.text = data.nama
                    tvHargaMenuDetail.text = data.harga.toString()
                    tvServSizeDetail.text = data.serv_size.toString()
                    tvUnitDetail.text = data.unit_bahan
                    tvDeskripsiDetail.text =data.desc

                    Glide.with(this@MainActivity)
                        .load(data.gambar)
                        .override(200,200)
                        .placeholder(R.drawable.akb_resto)
                        .fitCenter()
                        .into(imgMenuDetail)
                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED //bottom sheet muncul
            }
        })
    }

    // Mendapatkan hasil scan QR
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //Dapat tanggal hari ini
        val currentDateTime = LocalDateTime.now()
        val currentDate = currentDateTime.format(DateTimeFormatter.ISO_DATE)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Hasil tidak ditemukan", Toast.LENGTH_SHORT).show()
            } else {
                // jika qrcode berisi data
                try {
                    // converting the data json
                    val `object` = JSONObject(result.contents)
                    id_customer = `object`.getInt("id_customer")
                    nama_customer = `object`.getString("nama_customer")
                    id_reservasi = `object`.getInt("id_reservasi")
                    id_meja = `object`.getInt("id_meja")
                    no_meja = `object`.getInt("no_meja")
                    tgl_reservasi = `object`.getString("tgl_reservasi")
                    sesi = `object`.getString("sesi")
                    println("tanggal hari ini $currentDate")

                    if(tgl_reservasi.equals(currentDate))
                    {
                        //Masukkan nilai ke sharepref
                        var editor = shared.edit()
                        editor.putInt("id_customer", id_customer)
                        editor.putString("nama_customer", nama_customer)
                        editor.putInt("id_reservasi", id_reservasi)
                        editor.putInt("id_meja", id_meja)
                        editor.putInt("no_meja", no_meja)
                        editor.putString("tgl_reservasi", tgl_reservasi)
                        editor.putString("sesi", sesi)
                        editor.apply()

                        dialogCustomer()
                    } else {
                        Toast.makeText(this, "QR code sudah tidak berlaku!", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    // jika format encoded tidak sesuai maka hasil
                    // ditampilkan ke toast
                    Toast.makeText(this, "QR Code tidak berlaku!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //Dialog setelah scan QR
    fun dialogCustomer() {

        //Inflate the dialog with custom view
        mDialogView =
            LayoutInflater.from(this).inflate(R.layout.dialog_customer, null)
        //AlertDialogBuilder
        var mBuilder: AlertDialog.Builder? = null
        mBuilder = AlertDialog.Builder(this, R.style.RoundedCornersDialog)
            .setView(mDialogView)
        //find view by id
        val tv_nama_customer = mDialogView.findViewById<TextView>(R.id.tv_nama_customer)
        val tv_no_meja = mDialogView.findViewById<TextView>(R.id.tv_no_meja)
        val tv_tgl_reservasi = mDialogView.findViewById<TextView>(R.id.tv_tgl_reservasi)
        val tv_sesi = mDialogView.findViewById<TextView>(R.id.tv_sesi)
        val btn_cancel = mDialogView.findViewById<MaterialButton>(R.id.btn_cancel_dialog)
        val btn_lanjut = mDialogView.findViewById<MaterialButton>(R.id.btn_lanjut_dialog)

        tv_nama_customer.text = nama_customer
        tv_no_meja.text = no_meja.toString()
        tv_tgl_reservasi.text = tgl_reservasi
        tv_sesi.text = sesi

        //Show dialog
        val mAlertDialog = mBuilder.show()

        //Klik tombol lanjut
        btn_lanjut.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
            //get text from EditTexts of custom layout
            val i = Intent(applicationContext, MenuActivity::class.java)
            startActivity(i)
        }
        //Klik tombol cancel
        btn_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
        //Start shimmer
        binding.shimmerViewContainer.startShimmer()
    }

    override fun onPause() {
        //Stop shimmer
        binding.shimmerViewContainer.stopShimmer()
        super.onPause()
    }


}