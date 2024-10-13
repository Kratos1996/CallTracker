package com.ishant.jagtap.ui.logs

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ishant.jagtap.api.request.UploadContactRequest
import com.ishant.jagtap.databinding.CallLogsItemBinding
import kotlin.collections.ArrayList

class CallLogsAdapter(
    var context: Context
) : RecyclerView.Adapter<CallLogsAdapter.UploadCallLogsViewHolder>() {
    private var getAllUploadContact: List<UploadContactRequest.UploadContactData> = ArrayList()


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: List<UploadContactRequest.UploadContactData>) {
        this.getAllUploadContact = list
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadCallLogsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CallLogsItemBinding.inflate(inflater, parent, false)
        return UploadCallLogsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UploadCallLogsViewHolder, position: Int) {
        holder.binding.name.text = getAllUploadContact[position].name
        holder.binding.callTypeData.text = getAllUploadContact[position].type
        holder.binding.callDate.text = getAllUploadContact[position].dateTime
        holder.binding.callNumber.text = buildString {
        append(getAllUploadContact[position].mobile)
        append(" source mobile : ")
        append(getAllUploadContact[position].sourceMobileNo)
    }
        holder.binding.callDurationData.text = getAllUploadContact[position].duration


    }

    override fun getItemCount(): Int {
        return getAllUploadContact.size
    }

    inner class UploadCallLogsViewHolder(val binding: CallLogsItemBinding) :
        RecyclerView.ViewHolder(
            binding.root
        )



}

