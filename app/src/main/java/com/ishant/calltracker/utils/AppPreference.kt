package com.ishant.calltracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.ishant.calltracker.api.response.LoginResponse
import com.ishant.calltracker.utils.dataclassesUtils.TelePhoneManager

object AppPreference {

    private lateinit var pref: SharedPreferences
    private val PREF_IMPLICIT_TOKEN = "PREF_IMPLICIT_TOKEN"
    private val PREF_IMPLICIT_USER = "PREF_IMPLICIT_USER"
    private val PREF_SELECTED_SIM = "PREF_SELECTED_SIM"
    private val PREF_IS_LOGGED_IN= "PREF_IS_LOGGED_IN"
    private val PREF_SIM_MANAGER= "PREF_SIM_MANAGER"
    private val PREF_SIM_CHANGED= "PREF_SIM_CHANGED"
    private val PREF_SIM1_SELETED= "PREF_SIM_1_SELECTED"
    private val PREF_SIM2_SELECTED= "PREF_SIM_2_SELECTED"
    private val PREF_SPECIAL_PERMISION= "PREF_SPECIAL_PERMISSION"
    private const val APP_NAME = "callTracker:Ishant"

    fun init(context: Context) {
        pref = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
    }

    private inline fun SharedPreferences.edit(op: (SharedPreferences.Editor) -> Unit) {
        val editor = this.edit()
        op(editor)
        editor.apply()
    }

    var firebaseToken: String
        get() = getDataString(PREF_IMPLICIT_TOKEN)?:""
        set(value) = setDataString(PREF_IMPLICIT_TOKEN,value)

    var isUserLoggedIn: Boolean
        get() = getDataBoolean(PREF_IS_LOGGED_IN) ?:false
        set(value) = setDataBoolean(PREF_IS_LOGGED_IN,value)

    var isRegister: Boolean
        get() = getDataBoolean(PREF_SPECIAL_PERMISION) ?:false
        set(value) = setDataBoolean(PREF_SPECIAL_PERMISION,value)

    var isSim1Selected: Boolean
        get() = getDataBoolean(PREF_SIM1_SELETED)
        set(value) = setDataBoolean(PREF_SIM1_SELETED,value)

    var isSim2Selected: Boolean
        get() = getDataBoolean(PREF_SIM2_SELECTED)
        set(value) = setDataBoolean(PREF_SIM2_SELECTED,value)

    var user: LoginResponse.User
        get() {
            val gson = Gson()
            val value = getDataString(PREF_IMPLICIT_USER)
            if (value?.isEmpty() == true) {
                return LoginResponse.User()
            }
            return gson.fromJson(value, LoginResponse.User::class.java)
        }
        set(value) {
            val gson = Gson()
            val json = gson.toJson(value)
            setDataString(PREF_IMPLICIT_USER, json)
        }

    var simManager: TelePhoneManager
        get() {
            val gson = Gson()
            val value = getDataString(PREF_SIM_MANAGER)
            if (value?.isEmpty() == true) {
                return TelePhoneManager()
            }
            return gson.fromJson(value, TelePhoneManager::class.java)
        }
        set(value) {
            val gson = Gson()
            val json = gson.toJson(value)
            setDataString(PREF_SIM_MANAGER, json)
        }

    private fun getSharedPreferences(): SharedPreferences {
        return pref
    }

    private fun removePreferences(key: String) {
        getSharedPreferences().edit().remove(key).commit()
    }

    private fun getDataString(key: String): String? {
        var cbValue: String? = null
        try {
            cbValue = getSharedPreferences().getString(key, "")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cbValue
    }

    private fun getDataStringZero(key: String): String? {
        var cbValue: String? = null
        try {
            cbValue = getSharedPreferences().getString(key, "0.0")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cbValue
    }

    private fun setDataString(key: String, value: String) {
        val edit = getSharedPreferences().edit()
        edit.putString(key, value)
        edit.apply()
    }

    private fun GetDataFloat(key: String): Float {
        return getSharedPreferences().getFloat(key, 0.0f)
    }

    private fun setDataFloat(key: String, value: Float) {
        val edit = getSharedPreferences().edit()
        edit.putFloat(key, value)
        edit.apply()
    }

    private fun getDataInt(key: String): Int {
        return getSharedPreferences().getInt(key, 0)
    }

    private fun getDataInt(key: String, customVal: Int): Int {
        return getSharedPreferences().getInt(key, customVal)
    }

    private fun setDataInt(key: String, value: Int) {
        val edit = getSharedPreferences().edit()
        edit.putInt(key, value)
        edit.commit()
    }

    private fun getDataLong(key: String): Long {
        return getSharedPreferences().getLong(key, 0)
    }

    private fun setDataLong(key: String, value: Long) {
        val edit = getSharedPreferences().edit()
        edit.putLong(key, value)
        edit.apply()
    }

    private fun getDataBoolean(key: String): Boolean {
        return getSharedPreferences().getBoolean(key, false)
    }
    private fun getDataBoolean(key: String,defaultVlue:Boolean): Boolean {
        return getSharedPreferences().getBoolean(key, defaultVlue)
    }

    private fun setDataBoolean(key: String, value: Boolean) {
        val edit = getSharedPreferences().edit()
        edit.putBoolean(key, value)
        edit.apply()
    }

    fun logout() {
        getSharedPreferences().edit().clear().apply()
    }
}