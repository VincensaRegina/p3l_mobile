package com.vincensaregina.p3lproject.ui.menu

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vincensaregina.p3lproject.R
import com.vincensaregina.p3lproject.api.ApiClient
import com.vincensaregina.p3lproject.api.ApiInterface
import com.vincensaregina.p3lproject.api.MenuDAO
import com.vincensaregina.p3lproject.api.MenuResponse
import com.vincensaregina.p3lproject.databinding.RowMenuPesanBinding
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat

class MenuPesanAdapter : RecyclerView.Adapter<MenuPesanAdapter.ListViewHolder>() {

    private lateinit var clickCallback: OnItemClickCallback
    private val mMenu = ArrayList<MenuDAO>()
    private val mMenuCopy = ArrayList<MenuDAO>()
    private lateinit var binding: RowMenuPesanBinding

    //Shared Pref
    private lateinit var shared: SharedPreferences

    //Variable for shared pref and others
    private var id_reservasi: Int = -1
    private var id_menu: Int = -1
    private var qty: Int = -1
    private var serv_size: Int = -1
    private var hargaMenu: Double = -1.0
    private var subtotal: Double = -1.0
    private lateinit var gambar: String
    private lateinit var namaMenu: String
    private lateinit var unitMenu: String
    private var id_bahan: Int = -1
    private var servXqty: Int = -1
    private var stokBahan: Int = -1

    //context ambil dari view group di oncreate
    private lateinit var context: Context

    //Retrofit
    private var call: Call<MenuResponse>? = null

    //Event Listener: biar bisa call method dari tempat lain. Kasus ini: dari Menu Fragment
    private var listener: CallLoadMenu? = null

    //Alert Dialog
    private lateinit var mAlertDialog: AlertDialog
    //Progress Bar
    private lateinit var pb_dialog_qty: ProgressBar
    //Number Formatter
    private lateinit var formatter : NumberFormat


    fun setData(items: List<MenuDAO>, listener: CallLoadMenu) {
        mMenu.clear()
        mMenuCopy.clear()
        mMenu.addAll(items) //data yg akan difilter utk search
        mMenuCopy.addAll(items) //data lengkap
        this.listener = listener
        notifyDataSetChanged()
    }

    //Membuat interface yang akan diimplementasi oleh fragment
    interface CallLoadMenu {
        fun callLoadMenu(data: String)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.clickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: MenuDAO)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
//        val view: View =
//            LayoutInflater.from(parent.context).inflate(R.layout.row_menu, parent, false)
        binding = RowMenuPesanBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        // Init Shared Pref
        shared = parent.context.getSharedPreferences("dataQR", Context.MODE_PRIVATE)
        context = parent.context
        return ListViewHolder(binding)
    }

    override fun getItemCount(): Int = mMenu.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        var menu = mMenu[position]

        holder.bind(menu)
        holder.itemView.setOnClickListener { clickCallback.onItemClicked(mMenu[holder.adapterPosition]) }

        //Get data
        id_reservasi = shared.getInt("id_reservasi", -1)
        cekMenuCart(holder.binding.btnAddToCart, menu.id)

        //Click Add to cart button
        holder.binding.btnAddToCart.setOnClickListener(View.OnClickListener {
            //Get data
            id_reservasi = shared.getInt("id_reservasi", -1)
            id_menu = menu.id
            gambar = menu.gambar.toString()
            hargaMenu = menu.harga!!
            namaMenu = menu.nama.toString()
            id_bahan = menu.id_bahan
            serv_size = menu.serv_size
            stokBahan = menu.stok_bahan
            unitMenu = menu.unit.toString()
            Log.d(
                "Add to cart",
                "berhasil ditekan $id_reservasi $id_menu $gambar $hargaMenu $serv_size"
            )
            //Pemanggilan fungsi dialog
            dialogAddToCart()
        })
    }

    inner class ListViewHolder(val binding: RowMenuPesanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(menu: MenuDAO) {
            //Number Formatter
            val formatter = DecimalFormat("#,###")
            with(binding) {
                tvNamaMenu.text = menu.nama
                tvHargaMenu.text = formatter.format(menu.harga)
            }

            val myOptions = RequestOptions()
                .fitCenter() // or centerCrop
                .override(120, 120) //biar ga besar size imagenya
            Glide.with(itemView.context)
                .load(menu.gambar)
                .placeholder(R.drawable.akb_resto)
                .apply(myOptions)
                .into(binding.imgMenu)
        }

    }

    //utk search yg dijalankan tiap kali ada perubahan teks di search view
    fun filter(text: String) {
        var text = text
        mMenu.clear() //variabel utk data yg difilter dikosongkan
        if (text.isEmpty()) {
            mMenu.addAll(mMenuCopy) //jika query kosong, maka variabel mMenu diisi dengan data lengkap
        } else {
            text = text.toLowerCase()
            for (item in mMenuCopy) {
                if (item.nama?.toLowerCase()?.contains(text) == true
                ) {
                    mMenu.add(item) //jika query diisi, maka item yg sesuai diadd ke variabel mMenu
                }
            }
        }
        notifyDataSetChanged()
    }

    //Dialog setelah add to cart
    fun dialogAddToCart() {
        //Inflate the dialog with custom view
        val mDialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_qty, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(context, R.style.RoundedCornersDialog)
            .setView(mDialogView)
        //find view by id
        pb_dialog_qty = mDialogView.findViewById(R.id.pb_dialog_qty)
        val tv_qty = mDialogView.findViewById<TextView>(R.id.tv_qty_title)
        val til_qty = mDialogView.findViewById<TextInputLayout>(R.id.til_qty)
        val ti_qty = mDialogView.findViewById<TextInputEditText>(R.id.ti_qty)
        val btn_cancel = mDialogView.findViewById<MaterialButton>(R.id.btn_cancel_dialog_qty)
        val btn_lanjut = mDialogView.findViewById<MaterialButton>(R.id.btn_lanjut_dialog_qty)

        tv_qty.text = "Kuantitas $namaMenu"

        //Show dialog
        mAlertDialog = mBuilder.show()

        //Klik tombol lanjut
        btn_lanjut.setOnClickListener {

            //Kondisi
            if (TextUtils.isEmpty(ti_qty.text.toString())) til_qty.error =
                "Kuantitas tidak boleh kosong!"
            else if (Integer.parseInt(ti_qty.text.toString()) <= 0) til_qty.error =
                "Kuantitas harus lebih dari 0!"
            else {
                //Ambil nilai dari edittext
                qty = Integer.parseInt(ti_qty.text.toString())
                subtotal = qty * hargaMenu
                servXqty = serv_size * qty

                updateStokRS("tambahP")
                showLoading(true, pb_dialog_qty)
            }
        }
        //Klik tombol cancel
        btn_cancel.setOnClickListener {
            //dismiss dialog
            mAlertDialog.dismiss()
        }
    }

    private fun showLoading(state: Boolean, pb: ProgressBar) {
        if (state) {
            pb.visibility = View.VISIBLE
        } else {
            pb.visibility = View.GONE
        }
    }

    /////////API CALL
    fun addPesanan() {
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface
        val add = apiService.addPesanan(
            id_reservasi, id_menu, qty, subtotal, "no", "Not Ready"
        )

        println("Masuk call response")

        add.enqueue(object : Callback<Result<MenuResponse>> {
            override fun onResponse(
                call: Call<Result<MenuResponse>>,
                response: Response<Result<MenuResponse>>
            ) {
                //If response's code is 200
                if (response.isSuccessful) {
                    //Progress Bar Stop
                    Toast.makeText(
                        context,
                        "$qty x $namaMenu berhasil masuk Cart!",
                        Toast.LENGTH_SHORT
                    ).show()
                    println("Masuk on response add to cart sukses")

                    //Load menu (panggil method dari fragment), biar tombolnya terdisable.
                    listener?.callLoadMenu("Semua")

                    //Matiin loading
                    showLoading(false, pb_dialog_qty)
                    //dismiss dialog
                    mAlertDialog.dismiss()

                } else {  //If response's code is 4xx (error)
                    try {
                        val error = JSONObject(response.errorBody()!!.string())
                        Toast.makeText(
                            context,
                            error.optString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                        //Matiin loading
                        showLoading(false, pb_dialog_qty)
//                        Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
                        //                        response.body()?.getMessage()?.let { Log.d("error", it) }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<Result<MenuResponse>>, t: Throwable) {
                //                Toast.makeText(context, "Add to Cart gagal!", Toast.LENGTH_SHORT)
                //                    .show()
                Log.d("error onfailure", t.message.toString())
                //Matiin loading
                showLoading(false, pb_dialog_qty)
            }
        })
    }

    fun updateStokBahan(jenisUbah: String) {
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface
        val update = apiService.updateStokBahan(
            servXqty, id_bahan, jenisUbah
        )

        println("Masuk call response update stok bahan kurang")

        update.enqueue(object : Callback<Result<MenuResponse>> {
            override fun onResponse(
                call: Call<Result<MenuResponse>>,
                response: Response<Result<MenuResponse>>
            ) {
                //If response's code is 200
                if (response.isSuccessful) {

                } else {  //If response's code is 4xx (error)
                    try {
                        val error = JSONObject(response.errorBody()!!.string())
                        Log.d("Error stok bahan kurang", error.optString("message"))
                        Toast.makeText(context, error.optString("message"), Toast.LENGTH_SHORT)
                            .show()
                        println("on response update stok bahan kurang gagal")
                        showLoading(false, pb_dialog_qty)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<Result<MenuResponse>>, t: Throwable) {
                //                Toast.makeText(context, "Add to Cart gagal!", Toast.LENGTH_SHORT)
                //                    .show()
                Log.d("error onfailure", t.message.toString())
                showLoading(false, pb_dialog_qty)
            }
        })
    }

    //update mengurangi kolom stok_keluar tabel remaining stock
    fun updateStokRS(jenisUbah: String) {
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface
        val update = apiService.updateStokRS(
            servXqty, id_bahan, jenisUbah
        )

        println("Masuk call response update stok RS kurang")

        update.enqueue(object : Callback<Result<MenuResponse>> {
            override fun onResponse(
                call: Call<Result<MenuResponse>>,
                response: Response<Result<MenuResponse>>
            ) {
                //If response's code is 200
                if (response.isSuccessful) {
                    updateStokBahan("tambahP")
                    addPesanan()
                    println("Masuk on response update stok bahan kurang sukses")

                } else {  //If response's code is 4xx (error)
                    try {
                        val error = JSONObject(response.errorBody()!!.string())
                        Log.d("Error stok RS kurang", error.optString("message"))
                        var sedia: Int = stokBahan / serv_size
                        Toast.makeText(
                            context,
                            error.optString("message") + "Tersedia: $sedia $unitMenu",
                            Toast.LENGTH_SHORT
                        ).show()
                        println("on response update stok RS kurang gagal")
                        showLoading(false, pb_dialog_qty)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<Result<MenuResponse>>, t: Throwable) {
                Log.d("error onfailure", t.message.toString())
                showLoading(false, pb_dialog_qty)
            }
        })
    }

    //API call untuk cek apakah menu yg ditambahkan ke cart sudah ada di cart atau belum utk disable button
    fun cekMenuCart(btnAddToCart: MaterialButton, idMenu: Int) {
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface

        call = apiService.cekMenuCart(id_reservasi, idMenu)
        Log.d("cek isi id", "$id_reservasi $idMenu")

        call?.enqueue(object : Callback<MenuResponse> {
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                Log.d("response getmessage", response.body()?.getMessage().toString())
                if (response.body()?.getMessage().equals("Ada data")) {
                    Log.d("masuk ada data", "yuhu ada data")
                    btnAddToCart.isEnabled = false
                    btnAddToCart.text = "Sudah di Cart"
                } else {
                    Log.d("masuk tidak ada data", "tidak ada data")
                }
            }

            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                Toast.makeText(context, "Kesalahan Jaringan", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}