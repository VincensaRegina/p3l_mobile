package com.vincensaregina.p3lproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vincensaregina.p3lproject.api.MenuDAO
import com.vincensaregina.p3lproject.databinding.RowMenuBinding
import java.text.DecimalFormat
import java.text.NumberFormat

class MenuAdapter : RecyclerView.Adapter<MenuAdapter.ListViewHolder>() {

    private lateinit var clickCallback: OnItemClickCallback
    private val mMenu = ArrayList<MenuDAO>()
    private val mMenuCopy = ArrayList<MenuDAO>()

    //Number Formatter
    private lateinit var formatter : NumberFormat

    fun setData(items: List<MenuDAO>) {
        mMenu.clear()
        mMenuCopy.clear()
        mMenu.addAll(items) //data yg akan difilter utk search
        mMenuCopy.addAll(items) //data lengkap
        notifyDataSetChanged()
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
        val binding = RowMenuBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun getItemCount(): Int = mMenu.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(mMenu[position])
        holder.itemView.setOnClickListener { clickCallback.onItemClicked(mMenu[holder.adapterPosition]) }
    }

    inner class ListViewHolder(val binding: RowMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        //Number Formatter
        val formatter = DecimalFormat("#,###")
        fun bind(menu: MenuDAO) {
            with(binding) {
                tvNamaMenu.text = menu.nama
                tvHargaMenu.text =formatter.format(menu.harga)
            }

            Glide.with(itemView.context)
                .load(menu.gambar)
                .override(120,120)
                .placeholder(R.drawable.akb_resto)
                .fitCenter()
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

}