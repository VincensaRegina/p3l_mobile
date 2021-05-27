package com.vincensaregina.p3lproject.ui.menu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vincensaregina.p3lproject.DetailActivity
import com.vincensaregina.p3lproject.JenisMenuAdapter
import com.vincensaregina.p3lproject.MenuAdapter
import com.vincensaregina.p3lproject.R
import com.vincensaregina.p3lproject.api.ApiClient
import com.vincensaregina.p3lproject.api.ApiInterface
import com.vincensaregina.p3lproject.api.MenuDAO
import com.vincensaregina.p3lproject.api.MenuResponse
import com.vincensaregina.p3lproject.databinding.FragmentMenuBinding
import com.vincensaregina.p3lproject.databinding.RowMenuPesanBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MenuFragment : Fragment(), MenuPesanAdapter.CallLoadMenu {

    private var _binding: FragmentMenuBinding? = null

    //Recycler View
    private lateinit var menuPesanAdapter: MenuPesanAdapter
    private lateinit var jenisAdapter: JenisMenuAdapter

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    //Retrofit
    private var call: Call<MenuResponse>? = null

    //Shared Pref
    private lateinit var shared: SharedPreferences

    //Bottom Sheet
    private lateinit var layoutBottomSheet: ConstraintLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //View Binding inflate
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        val view = binding.root

        //Munculin Options Menu di Fragment
        setHasOptionsMenu(true)

        // Init Shared Pref
        shared = requireActivity().getSharedPreferences("dataQR", Context.MODE_PRIVATE)

        //Get from shared pref
        var namaCustomer = shared.getString("nama_customer", "Kosong")

        ///////ActionBar
        //Remove shadow
        (activity as AppCompatActivity).supportActionBar?.elevation = 0F
        //Remove default title in toolbar
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.tvWelcome.text = "Welcome, $namaCustomer"

        //Set adapter
        jenisAdapter = JenisMenuAdapter()
        menuPesanAdapter = MenuPesanAdapter()
        menuPesanAdapter.notifyDataSetChanged()

        //Bottom Sheet
        layoutBottomSheet = binding.bottomSheetDetail.bottomSheetDetail
        bottomSheetBehavior = BottomSheetBehavior.from(layoutBottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        //Tampil menu
        loadMenu("Semua")

        return view
    }

    //Interface dari MenuPesanAdapter biar adapter bisa panggil method milik menufragment
    override fun callLoadMenu(data: String) {
        loadMenu(data)
    }

    //Memunculkan menu di action bar
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.options_menu, menu)
        //karena ini option menu, jd perlu dikasih tau bahwa ini searchview.
        (menu.findItem(R.id.search).actionView as SearchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                menuPesanAdapter.filter(query)
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {
                menuPesanAdapter.filter(newText)
                return true
            }
        })
    }

    //API call untuk load menu
    fun loadMenu(jenis_menu: String) {
        val apiService: ApiInterface =
            (ApiClient.getClient()?.create(ApiInterface::class.java) ?: 0) as ApiInterface

        //Untuk filter berdasarkan jenis menu
        if (jenis_menu == "Semua") call = apiService.getAllMenu()
        else call = apiService.getSpecificMenu(jenis_menu)

        call?.enqueue(object : Callback<MenuResponse> {
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                Toast.makeText(
                    context,
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
                Toast.makeText(context, "Kesalahan Jaringan", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    //Set Recycler View + on click
    private fun generateDataList(menuList: List<MenuDAO>) {
        //Load Jenis Menu
        context?.let { jenisAdapter.setData(it) }
        binding.rvJenis.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvJenis.adapter = jenisAdapter

        //Load List Menu
        menuPesanAdapter.setData(menuList, this)
        binding.rvMenu.layoutManager = LinearLayoutManager(context)
        binding.rvMenu.itemAnimator = DefaultItemAnimator()
        binding.rvMenu.setItemViewCacheSize(10)
        binding.rvMenu.adapter = menuPesanAdapter

        //Click item jenis menu (semua, makanan utama, side dish, minuman)
        jenisAdapter.setOnItemClickCallback(object : JenisMenuAdapter.OnItemClickCallback {
            override fun onItemClicked(data: String) {
                loadMenu(data)
            }
        })

        //Click item list menu
        menuPesanAdapter.setOnItemClickCallback(object : MenuPesanAdapter.OnItemClickCallback {
            override fun onItemClicked(data: MenuDAO) {
                with(binding.bottomSheetDetail) {
                    tvNamaMenuDetail.text = data.nama
                    tvHargaMenuDetail.text = data.harga.toString()
                    tvServSizeDetail.text = data.serv_size.toString()
                    tvUnitDetail.text = data.unit_bahan
                    tvDeskripsiDetail.text =data.desc
                    context?.let {
                        val myOptions = RequestOptions()
                            .fitCenter() // or centerCrop
                            .override(120, 120) //biar ga besar size imagenya
                        Glide.with(it)
                            .load(data.gambar)
                            .placeholder(R.drawable.akb_resto)
                            .apply(myOptions)
                            .into(imgMenuDetail)
                    }
                }
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED //bottom sheet muncul
            }
        })

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}