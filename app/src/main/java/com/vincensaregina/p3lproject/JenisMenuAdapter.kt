package com.vincensaregina.p3lproject

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.vincensaregina.p3lproject.databinding.RowJenisBinding

class JenisMenuAdapter : RecyclerView.Adapter<JenisMenuAdapter.ListViewHolder>() {
    private lateinit var clickCallback: OnItemClickCallback
    private var mMenu = ArrayList<String>()
    private var row_index: Int = -1
    private lateinit var context: Context
    //Shared Pref
    private lateinit var shared: SharedPreferences

    private lateinit var mainActivity: MainActivity
//    private lateinit var jenis_menu: String


    fun setData(context: Context) {
        mMenu = arrayListOf()
        mMenu.add("Semua")
        mMenu.add("Makanan Utama")
        mMenu.add("Makanan Side Dish")
        mMenu.add("Minuman")
        this.context = context
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.clickCallback = onItemClickCallback
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
//        val view: View =
//            LayoutInflater.from(parent.context).inflate(R.layout.row_menu, parent, false)
        val binding = RowJenisBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        mainActivity = MainActivity()
        //Shared Pref
        shared =  context.getSharedPreferences("jenis_menu", Context.MODE_PRIVATE)
        return ListViewHolder(binding)
    }

    override fun getItemCount(): Int = mMenu.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(mMenu[position])
        holder.itemView.setOnClickListener {
            clickCallback.onItemClicked(mMenu[holder.adapterPosition])
        }
//
//        holder.itemView.setOnClickListener(View.OnClickListener {
//            row_index = position
//            //Change jenis_menu value for url parameter change
////            val editor = shared.edit()
////            editor.putString("jenis_menu", mMenu[position])
////            editor.apply()
//
////            mainActivity.loadUser(mMenu[position])
//
//        })

//        if (row_index.equals(position)) {
//            holder.itemView.background = context?.let {
//                ContextCompat.getDrawable(
//                    it,
//                    R.drawable.rounded_corner
//                )
//            }
//        } else {
//            holder.itemView.background = context?.let {
//                ContextCompat.getDrawable(
//                    it,
//                    R.drawable.rounded_corner_jenismenu
//                )
//            }
//        }

    }

    inner class ListViewHolder(val binding: RowJenisBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(jenis: String) {
            binding.btnJenisMenu.text = jenis
        }

    }

}