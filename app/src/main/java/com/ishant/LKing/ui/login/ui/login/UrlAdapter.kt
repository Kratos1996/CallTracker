package com.ishant.LKing.ui.login.ui.login

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import com.ishant.LKing.R
import com.ishant.LKing.api.response.UrlResponse

class UrlAdapter(val contextM: Context, val resource: Int, private var objects: List<UrlResponse.Data>) : ArrayAdapter<UrlResponse.Data?>(contextM, resource, objects) {
    private val mInflater: LayoutInflater = LayoutInflater.from(contextM)

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = mInflater.inflate(resource, parent, false)
        val spineText = view.findViewById<CheckedTextView>(R.id.textSpinner)
        spineText.text = objects[position].urlName
        return view
    }
}
