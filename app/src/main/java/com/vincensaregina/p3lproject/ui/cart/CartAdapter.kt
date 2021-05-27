package com.vincensaregina.p3lproject.ui.cart

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.vincensaregina.p3lproject.R
import com.vincensaregina.p3lproject.api.*
import com.vincensaregina.p3lproject.databinding.RowMenuCartBinding
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.DecimalFormat
import java.text.NumberFormat


class CartAdapter : RecyclerView.Adapter<CartAdapter.ListViewHolder>() {
    private lateinit var clickCallback: CartAdapter.OnItemClickCallback
    private val mPesanan = ArrayList<PesananDAO>()
    private val mPesananCopy = ArrayList<PesananDAO>()
    private lateinit var binding: RowMenuCartBinding

    //Shared Pref
    private lateinit var shared: SharedPreferences

    //Variable for shared pref and others
    private lateinit var unit_menu: String
    private var stok_bahan: Int = -1
    private var id_bahan: Int = -1
    private var qty: Int = -1
    private var serv_size: Int = -1
    private var hargaMenu: Double = -1.0
    private var subtotal: Double = -1.0
    private lateinit var gambar: String
    private lateinit var namaMenu: String
    private var id_pesanan: Int = -1
    private var servXqty: Int = -1
    private var qtyBaru: Int = -1

    //context ambil dari view group di oncreate
    private lateinit var context: Context

    //Retrofit
    private var call: Call<Result<PesananResponse>>? = null

    //Event Listener: biar bisa call method dari tempat lain. Kasus ini: dari Cart Fragment
    private var listener: CallLoadPesanan? = null

    //Alert Dialog
    private lateinit var mAlertDialog: AlertDialog

    //Progress Bar
    private lateinit var pb_dialog_qty: ProgressBar

    // This object helps you save/restore the open/close state of each view
    private val viewBinderHelper = ViewBinderHelper()

    //Number Formatter
    private lateinit var formatter : NumberFormat

    private lateinit var v: View

    fun setData(items: List<PesananDAO>, listener: CallLoadPesanan) {
        mPesanan.clear()
        mPesananCopy.clear()
        mPesanan.addAll(items) //data yg akan difilter utk search
        mPesananCopy.addAll(items) //data lengkap
        this.listener = listener
        viewBinderHelper.setOpenOnlyOne(true)
        notifyDataSetChanged()
    }

    //Membuat interface yang akan diimplementasi oleh fragment
    interface CallLoadPesanan {
        fun callLoadPesanan()
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.clickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: PesananDAO)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.ListViewHolder {
        binding = RowMenuCartBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        // Init Shared Pref
        shared = parent.context.getSharedPreferences("dataQR", Context.MODE_PRIVATE)
        context = parent.context

        return ListViewHolder(binding)
    }

    fun initDataPesanan(pesanan: PesananDAO) {
        id_pesanan = pesanan.id
        namaMenu = pesanan.nama_menu.toString()
        id_bahan = pesanan.id_bahan
        serv_size = pesanan.serv_size
        qty = pesanan.qty
        servXqty = serv_size * qty
        stok_bahan = pesanan.stok_bahan
        unit_menu = pesanan.unit_menu.toString()
        hargaMenu = pesanan.harga_menu
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val pesanan = mPesanan[position]

        // Save/restore the open/close state.
        // You need to provide a String id which uniquely defines the data object.
        Log.d("ID PESANAN", pesanan.id.toString())
        viewBinderHelper.bind(holder.binding.swipeRevealLayout, pesanan.id.toString())

        //Bind
        holder.bind(pesanan)
        holder.itemView.setOnClickListener { clickCallback.onItemClicked(mPesanan[holder.adapterPosition]) }
        holder.binding.imgbtnEdit.setOnClickListener {
            initDataPesanan(pesanan)
            dialogEditPesanan()
        }
        holder.binding.imgbtnDelete.setOnClickListener {
            v = it
            initDataPesanan(pesanan)
            deletePesanan()
        }


    }

    override fun getItemCount(): Int = mPesanan.size

    inner class ListViewHolder(val binding: RowMenuCartBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(pesanan: PesananDAO) {
            //Number Formatter
            val formatter = DecimalFormat("#,###")
            with(binding) {
                tvNamaMenu.text = pesanan.nama_menu
                tvHargaMenu.text = formatter.format(pesanan.harga_menu)
                tvQty.text = pesanan.qty.toString()
                tvSubtotalCart.text = formatter.format(pesanan.subtotal)
            }
            val myOptions = RequestOptions()
                .fitCenter() // or centerCrop
                .override(100, 100) //biar ga besar size imagenya
            Glide.with(itemView.context)
                .load(pesanan.gambar)
                .apply(myOptions)
                .into(binding.imgMenu)
        }
    }

    //utk search yg dijalankan tiap kali ada perubahan teks di search view
    fun filter(text: String) {
        var text = text
        mPesanan.clear() //variabel utk data yg difilter dikosongkan
        if (text.isEmpty()) {
            mPesanan.addAll(mPesananCopy) //jika query kosong, maka variabel mMenu diisi dengan data lengkap
        } else {
            text = text.toLowerCase()
            for (item in mPesananCopy) {
                if (item.nama_menu?.toLowerCase()?.contains(text) == true
                ) {
                    mPesanan.add(item) //jika query diisi, maka item yg sesuai diadd ke variabel mMenu
                }
            }
        }
        notifyDataSetChanged()
    }

    //Dialog edit pesanan utk edit qty
    fun dialogEditPesanan() {
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

        btn_lanjut.text = "Save"

        tv_qty.text = "Edit Kuantitas $namaMenu"
        til_qty.editText?.setText(qty.toString())

        //Show dialog
        mAlertDialog = mBuilder.show()

        //Klik tombol lanjut
        btn_lanjut.setOnClickListener {
            v = it
            //Kondisi
            if (TextUtils.isEmpty(ti_qty.text.toString())) til_qty.error =
                "Kuantitas tidak boleh kosong!"
            else if (Integer.parseInt(ti_qty.text.toString()) <= 0) til_qty.error =
                "Kuantitas harus lebih dari 0!"
            else {
                //Ambil nilai dari edittext
                qtyBaru = Integer.parseInt(ti_qty.text.toString())
                subtotal = qty * hargaMenu
                //Ubah stok bahan dan RS
                if (qty > qtyBaru) { //qty lama lebih besar dari qty baru (pesanan berkurang)
                    val selisihQty = qty - qtyBaru
                    subtotal = qtyBaru * hargaMenu
                    servXqty = serv_size * selisihQty //untuk menambah stok bahan dan RS (pesanan dikembalikan)
                    updateStokRS("editLebih")
                    showLoading(true, pb_dialog_qty)
                } else if (qty < qtyBaru) { //qty lama lebih besar dari qty baru (pesanan bertambah)
                    val selisihQty = qtyBaru - qty
                    subtotal = qtyBaru * hargaMenu
                    servXqty = serv_size * selisihQty //untuk menambah stok bahan dan RS (pesanan dikembalikan)
                    updateStokRS("editKurang")
                    showLoading(true, pb_dialog_qty)
                } else if (qty == qtyBaru) {
                    viewBinderHelper.closeLayout(id_pesanan.toString())
                    mAlertDialog.dismiss()
                }

            }
        }
        //Klik tombol cancel
        btn_cancel.setOnClickListener {
            //dismiss dialog
            viewBinderHelper.closeLayout(id_pesanan.toString())
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

    ///////API CALL
    fun deletePesanan() {

        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface

        call = apiService.deletePesanan(id_pesanan)

        call?.enqueue(object : Callback<Result<PesananResponse>> {
            override fun onResponse(
                call: Call<Result<PesananResponse>>,
                response: Response<Result<PesananResponse>>
            ) {
                Toast.makeText(
                    context,
                    "$namaMenu terhapus!",
                    Toast.LENGTH_SHORT
                )
                    .show()
                updateStokRS("hapusP")
            }

            override fun onFailure(call: Call<Result<PesananResponse>>, t: Throwable) {
                Toast.makeText(context, t.message, Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    fun updateQty() {
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface
        val update = apiService.updateQty(
            qtyBaru, subtotal, id_pesanan
        )

        println("Masuk call response update qty")

        update.enqueue(object : Callback<Result<PesananResponse>> {
            override fun onResponse(
                call: Call<Result<PesananResponse>>,
                response: Response<Result<PesananResponse>>
            ) {
                //If response's code is 200
                if (response.isSuccessful) {
                    println("Masuk on response update qty")
                    //panggil loadpesanan lagi
                    listener?.callLoadPesanan()
                    showLoading(false, pb_dialog_qty)
                    mAlertDialog.dismiss()
                    viewBinderHelper.closeLayout(id_pesanan.toString())

                } else {  //If response's code is 4xx (error)
                    try {
                        val error = JSONObject(response.errorBody()!!.string())
                        Log.d("Error update qty", error.optString("message"))
                        Toast.makeText(context, error.optString("message"), Toast.LENGTH_SHORT)
                            .show()
                        println("on response update qty gagal")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<Result<PesananResponse>>, t: Throwable) {
                Toast.makeText(context, "Edit pesanan gagal!", Toast.LENGTH_SHORT)
                    .show()
                Log.d("error onfailure", t.message.toString())
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
                    println("Masuk on response update stok bahan sukses")
                    if (jenisUbah == "editLebih" || jenisUbah == "editKurang") {
                        //Save update qty
                        updateQty()

                    } else if (jenisUbah == "hapusP") {
                        //Restart fragment
                        v.findNavController().navigate(R.id.action_navigation_cart_self)
                        viewBinderHelper.closeLayout(id_pesanan.toString())
                    }


                } else {  //If response's code is 4xx (error)
                    try {
                        val error = JSONObject(response.errorBody()!!.string())
                        Log.d("Error stok bahan kurang", error.optString("message"))
                        Toast.makeText(context, error.optString("message"), Toast.LENGTH_SHORT)
                            .show()
                        println("on response update stok bahan kurang gagal")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<Result<MenuResponse>>, t: Throwable) {
//                                Toast.makeText(context, "Add to Cart gagal!", Toast.LENGTH_SHORT)
//                                    .show()
                Log.d("error onfailure", t.message.toString())
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
                    //jika jenisnya edit, maka akan update stok bahan dengan jenis edit. Harus cek RS dulu baru kurangi bahan
                    if (jenisUbah == "editLebih" || jenisUbah == "editKurang") {
                        updateStokBahan(jenisUbah)
                    } else if (jenisUbah == "hapusP") {
                        updateStokBahan("hapusP")
                    }

                } else {  //If response's code is 4xx (error)
                    try {
                        val error = JSONObject(response.errorBody()!!.string())
                        Log.d("Error stok RS kurang", error.optString("message"))
                        val tersedia = stok_bahan / serv_size //berapa porsi tersedia
                        Toast.makeText(
                            context,
                            error.optString("message") + " Tersedia: $tersedia $unit_menu",
                            Toast.LENGTH_SHORT
                        )
                            .show()
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

    //SwipeRevealLayout
    fun saveStates(outState: Bundle?) {
        viewBinderHelper.saveStates(outState)
    }

    fun restoreStates(inState: Bundle?) {
        viewBinderHelper.restoreStates(inState)
    }
}