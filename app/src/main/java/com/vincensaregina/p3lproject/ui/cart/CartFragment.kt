package com.vincensaregina.p3lproject.ui.cart

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.vincensaregina.p3lproject.MainActivity
import com.vincensaregina.p3lproject.R
import com.vincensaregina.p3lproject.api.*
import com.vincensaregina.p3lproject.databinding.FragmentCartBinding
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CartFragment : Fragment(), CartAdapter.CallLoadPesanan {
    //View Binding
    private var _binding: FragmentCartBinding? = null

    //Recycler View
    private lateinit var cartAdapter: CartAdapter

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    //Retrofit
    private var call: Call<PesananResponse>? = null

    //Shared Pref
    private lateinit var shared: SharedPreferences
    private var idReservasi: Int = -1

    //Bottom Sheet
    private lateinit var layoutBottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    //Alert Dialog
    private lateinit var mAlertDialogSelesai: AlertDialog
    //View
    private lateinit var v: View
    //Variabel penyimpan lainnya
    private var jumlahTransaksi: Int = -1
    private var total: Double = 0.0

    /*
    > BottomSheetBehavior provides callbacks and make the BottomSheet work with CoordinatorLayout.
    > BottomSheetBehavior.BottomSheetCallback() provides callback when the Bottom Sheet changes its state.
    > toggleBottomSheet() Opens or closes the bottom sheet on button click.
    */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //View Binding inflate
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val view = binding.root

        //Munculin Options Menu di Fragment
        setHasOptionsMenu(true)

        // Init Shared Pref
        shared = requireActivity().getSharedPreferences("dataQR", Context.MODE_PRIVATE)

        ///////ActionBar
        //Remove shadow
        (activity as AppCompatActivity).supportActionBar?.elevation = 0F
        //Remove default title in toolbar
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        //Set adapter
        cartAdapter = CartAdapter()
        cartAdapter.notifyDataSetChanged()

        //Bottom Sheet
        layoutBottomSheet = binding.bottomSheetTotal.bottomSheetTotal
        bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        //Tampil menu
        loadPesanan()

        return view
    }

    //Interface dari MenuPesanAdapter biar adapter bisa panggil method milik menufragment
    override fun callLoadPesanan() {
        loadPesanan()
    }

    //Memunculkan menu di action bar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
        //karena ini option menu, jd perlu dikasih tau bahwa ini searchview.
        (menu.findItem(R.id.search).actionView as SearchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                cartAdapter.filter(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                cartAdapter.filter(newText)
                return true
            }
        })
    }

    //Set Recycler View + on click
    private fun generateDataList(pesananList: List<PesananDAO>) {
        //Bottom sheet total muncul
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        //Load Pesanan
        cartAdapter.setData(pesananList, this)
        binding.rvMenuCart.layoutManager = LinearLayoutManager(context)
        binding.rvMenuCart.adapter = cartAdapter

        total = 0.00
        //Hitung total
        for (pesanan in pesananList) total += pesanan.subtotal
        //Number Formatter
        val formatter = DecimalFormat("#,###")

        //Munculin bottom sheet total
        with(binding.bottomSheetTotal) {
            tvNamaCustomerBs.text = "Nama Customer: " + shared.getString("nama_customer", "kosong")
            tvNoMejaBs.text = "Nomor Meja: " + shared.getInt("no_meja", -1).toString()
            tvTglReservasiBs.text =
                "Tanggal Reservasi: " + shared.getString("tgl_reservasi", "kosong")
            tvSesiBs.text = "Sesi: " + shared.getString("sesi", "kosong")
            tvTotalHargaBs.text = "Rp " + formatter.format(total)
            btnTransaksi.setOnClickListener {
                v = it
                dialogKonfirmasiSelesai(pesananList)
            }
        }
    }

    fun dialogKonfirmasiSelesai(pesananList: List<PesananDAO>) {
        val builder = android.app.AlertDialog.Builder(context, R.style.DialogKonfirmasi)
        builder.setTitle("Konfirmasi Penyelesaian Pesanan")
        builder.setMessage("Apakah anda yakin untuk menyelesaikan pemesanan?")
        builder.setPositiveButton(
            "Ya"
        ) { dialog, which ->
            dialog.cancel()
            for (pesanan in pesananList) {
                updateLocked(pesanan.id)
                addRiwayatKeluar(pesanan)
            }
            countTransaksi()
            dialogPemesananSelesai()
        }
        builder.setNegativeButton(
            "Tidak"
        ) { dialog, which ->
            dialog.cancel()
        }
        val alert = builder.create()
        alert.show()
    }

    //Dialog setelah konfirmasi pesanan selesai
    fun dialogPemesananSelesai() {
        //Inflate the dialog with custom view
        val mDialogView =
            LayoutInflater.from(context).inflate(R.layout.dialog_selesai_pesan, null)
        //AlertDialogBuilder
        val mBuilder = AlertDialog.Builder(requireContext(), R.style.RoundedCornersDialog)
            .setView(mDialogView)

        val btn_kembali = mDialogView.findViewById<MaterialButton>(R.id.btn_kembali_ke_menu)

        //Show dialog
        mAlertDialogSelesai = mBuilder.show()

        //Klik tombol kembali ke menu utama
        btn_kembali.setOnClickListener {
//            activity?.onBackPressed()
            mAlertDialogSelesai.dismiss()
            v.findNavController().navigate(R.id.action_navigation_cart_to_mainActivity)
//            val intent = Intent(activity, MainActivity::class.java)
//            startActivity(intent)
        }

    }

    ///////API CALL
    //update menambah data riwayat keluar
    fun addRiwayatKeluar(pesanan: PesananDAO) {

        val currentDateTime = LocalDateTime.now()
        val servXqty = pesanan.serv_size * pesanan.qty

        val currentDate = currentDateTime.format(DateTimeFormatter.ISO_DATE)
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface
        val update = apiService.addRiwayatKeluar(
            pesanan.id_bahan, currentDate, servXqty, "Keluar"
        )

        println("Masuk call response add riwayat keluar kurang")

        update.enqueue(object : Callback<Result<PesananDAO>> {
            override fun onResponse(
                call: Call<Result<PesananDAO>>,
                response: Response<Result<PesananDAO>>
            ) {
                //If response's code is 200
                if (response.isSuccessful) {
                    println("Masuk on response add riwayat keluar  sukses ${pesanan.id}")
                } else {  //If response's code is 4xx (error)
                    try {
                        println("on response add riwayat keluar gagal")
                        val error = JSONObject(response.errorBody()!!.string())
                        Log.d("Error add riwayat keluar kurang", error.optString("message"))
                        Toast.makeText(context, error.optString("message"), Toast.LENGTH_SHORT)
                            .show()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<Result<PesananDAO>>, t: Throwable) {
                Log.d("error onfailure", t.message.toString())
            }
        })
    }

    //menambah data transaksi
    fun addTransaksi() {
        println("current date time format masuk")
        val currentDateTime = LocalDateTime.now()
        val currentDate = currentDateTime.format(DateTimeFormatter.ofPattern("ddMMyy"))
        println("current date time format $currentDate")
        val no_transaksi = "AKB-$currentDate-$jumlahTransaksi"
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface
        val update = apiService.addTransaksi(
            idReservasi, 0, 0, no_transaksi, total, "Belum"
        )

        println("Masuk call response add transaksi")

        update.enqueue(object : Callback<Result<PesananDAO>> {
            override fun onResponse(
                call: Call<Result<PesananDAO>>,
                response: Response<Result<PesananDAO>>
            ) {
                //If response's code is 200
                if (response.isSuccessful) {
                    println("Masuk on response add transaksi sukses")
                    Log.d("ID reservasi dalam on response", idReservasi.toString())
                } else {  //If response's code is 4xx (error)
                    try {
                        println("on response add transaksi gagal")
                        val error = JSONObject(response.errorBody()!!.string())
                        Log.d("Error add transaksi", error.optString("message"))
                        Toast.makeText(context, error.optString("message"), Toast.LENGTH_SHORT)
                            .show()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<Result<PesananDAO>>, t: Throwable) {
                Log.d("error onfailure add transaksi", t.message.toString())
            }
        })
    }

    //update menambah data riwayat keluar
    fun updateLocked(id_pesanan: Int) {
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface
        val update = apiService.updateLocked(
            "yes", id_pesanan
        )

        println("Masuk call response update locked")

        update.enqueue(object : Callback<Result<PesananDAO>> {
            override fun onResponse(
                call: Call<Result<PesananDAO>>,
                response: Response<Result<PesananDAO>>
            ) {
                //If response's code is 200
                if (response.isSuccessful) {
                    println("Masuk on response update locked $id_pesanan")
                } else {  //If response's code is 4xx (error)
                    try {
                        println("on response add riwayat keluar gagal")
                        val error = JSONObject(response.errorBody()!!.string())
                        Log.d("Error update locked", error.optString("message"))
                        Toast.makeText(context, error.optString("message"), Toast.LENGTH_SHORT)
                            .show()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<Result<PesananDAO>>, t: Throwable) {
                Log.d("error onfailure", t.message.toString())
            }
        })
    }

    //API call untuk load pesanan
    fun loadPesanan() {
        //Get from shared pref
        idReservasi = shared.getInt("id_reservasi", -1)

        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface

        call = apiService.getPesananCustomer(idReservasi)

        call?.enqueue(object : Callback<PesananResponse> {
            override fun onResponse(
                call: Call<PesananResponse>,
                response: Response<PesananResponse>
            ) {
                response.body()?.getMessage()?.let { Log.d("message cart loadpesanan", it) }
                Log.d("setelah message cart loadpesanan", "setelah")
                response.body()?.getPesanan()?.let { generateDataList(it) }

                // Stopping Shimmer Effect's animation after data is loaded to ListView
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
            }

            override fun onFailure(call: Call<PesananResponse>, t: Throwable) {
                Toast.makeText(context, "Kesalahan Jaringan", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    //untuk menghitung berapa transaksi hari ini
    fun countTransaksi() {

        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface

        val call = apiService.countTransaksi()

        call?.enqueue(object : Callback<TransaksiResponse> {
            override fun onResponse(
                call: Call<TransaksiResponse>,
                response: Response<TransaksiResponse>
            ) {
                response.body()?.getMessage()?.let { Log.d("message cart loadpesanan", it) }
                Log.d("setelah message cart loadpesanan", "setelah")
                jumlahTransaksi = response.body()?.getJumlah() ?: -1
                addTransaksi()
            }

            override fun onFailure(call: Call<TransaksiResponse>, t: Throwable) {
//                Toast.makeText(context, "Kesalahan Jaringan", Toast.LENGTH_SHORT)
//                    .show()
                println("on failure count transaksi: ${t.message}")
            }
        })
    }
}