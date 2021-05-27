package com.vincensaregina.p3lproject

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vincensaregina.p3lproject.api.MenuDAO
import com.vincensaregina.p3lproject.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_DATA = "extra_data"
    }

    //View Binding
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //View Binding
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ///////ActionBar
        //Remove shadow
        supportActionBar?.elevation = 0F
        //Remove default title in toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //Receive data
        val dataIntent: MenuDAO? = intent.getParcelableExtra(EXTRA_DATA)

        if (dataIntent != null) {

            with(binding) {
                tvNamaMenuDetail.text = dataIntent.nama
                tvDeskripsiDetail.text = dataIntent.desc
                tvServSizeDetail.text = dataIntent.serv_size.toString()
                tvHargaMenuDetail.text = dataIntent.harga.toString()
                tvUnitDetail.text = dataIntent.unit_bahan
            }

            Glide.with(this)
                .load(dataIntent.gambar)
                .apply(RequestOptions())
                .into(binding.imgMenuDetail)
        }

    }
}