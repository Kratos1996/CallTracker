package com.wabblaster.wabblasterai.ui.restricted

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wabblaster.wabblasterai.R
import com.wabblaster.wabblasterai.database.room.ContactList
import com.wabblaster.wabblasterai.databinding.RestrictedContactItemBinding
import com.wabblaster.wabblasterai.ui.home.HomeViewModel
import kotlin.collections.ArrayList
import kotlin.math.ln
import kotlin.math.pow

class ContactAdapter(var context: Context, val type: Int, val viewModel: HomeViewModel) :
    RecyclerView.Adapter<ContactAdapter.FavContactViewHolder>() {
    private var getAllContact: List<ContactList> = ArrayList()


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<ContactList>) {
        this.getAllContact=list
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RestrictedContactItemBinding.inflate( inflater,  parent,  false)
        return FavContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavContactViewHolder, position: Int) {

        holder.binding.headername.text= getAllContact[position].name
        if(getAllContact.get(position).isFav == true){
            holder.binding.isContactSelect.setImageResource(R.drawable.check1)
        }else{
            holder.binding.isContactSelect.setImageResource(R.drawable.check_not_select)
        }
        holder.binding.contactNumber.text= getAllContact[position].phoneNumber
        if(type==1){
            holder.binding.isContactSelect.visibility=View.VISIBLE
        }else{
            holder.binding.isContactSelect.visibility=View.INVISIBLE
        }
        holder.binding.isContactSelect.setOnClickListener {
            if(getAllContact[position].isFav == true){
                holder.binding.isContactSelect.setImageResource(R.drawable.check_not_select)
                viewModel.setRestrictedContact(getAllContact[position].phoneNumber,false)
            } else {
                holder.binding.isContactSelect.setImageResource(R.drawable.check1)
                viewModel.setRestrictedContact(getAllContact[position].phoneNumber,true)

            }
        }
    }

    override fun getItemCount(): Int {
        return getAllContact.size
    }

    inner class FavContactViewHolder(val binding: RestrictedContactItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )


    private fun Long.withSuffix(): String {
        if (this < 1000) return "" + this
        val exp = (ln(this.toDouble()) / ln(1000.0)).toInt()
        return String.format(
            "%.1f%c",
            this / 1000.0.pow(exp.toDouble()),
            "kMBTPE"[exp - 1]
        )
    }

    private fun getColor(power: Int): Int {
        val h = (itemCount - power) * 100 / itemCount
        val s = 1 // Saturation
        val v = 0.8 // Value
        return Color.HSVToColor(floatArrayOf(h.toFloat(), s.toFloat(), v.toFloat()))
    }

}

