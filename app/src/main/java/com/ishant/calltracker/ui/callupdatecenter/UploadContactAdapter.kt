package com.ishant.calltracker.ui.callupdatecenter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.databinding.CallUploadedOnServerItemBinding
import com.ishant.calltracker.ui.home.HomeViewModel
import kotlin.collections.ArrayList

class UploadContactAdapter(
    var context: Context,
    val uploadContact: (UploadContact) -> Unit,
    val message: (String) -> Unit
) :
    RecyclerView.Adapter<UploadContactAdapter.UploadContactViewHolder>() {
    private var getAllUploadContact: List<UploadContact> = ArrayList()


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<UploadContact>) {
        this.getAllUploadContact=list
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CallUploadedOnServerItemBinding.inflate( inflater,  parent,  false)
        return UploadContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UploadContactViewHolder, position: Int) {
        holder.binding.sourceNameTitleText.text = getAllUploadContact[position].name
        holder.binding.sourceMobileTitleText.text = getAllUploadContact[position].sourceMobileNo
        holder.binding.mainViewBtn.text = getAllUploadContact[position].type.capitalize()
        holder.binding.mainViewBtn.setOnClickListener {
            if(getAllUploadContact[position].type == UploadContactType.PENDING) {
                uploadContact(getAllUploadContact[position])
            }else{
                message("This Call Detail is Already Saved")
            }
        }
    }

    override fun getItemCount(): Int {
        return getAllUploadContact.size
    }

    inner class UploadContactViewHolder(val binding: CallUploadedOnServerItemBinding) : RecyclerView.ViewHolder(
        binding.root
    )

}

