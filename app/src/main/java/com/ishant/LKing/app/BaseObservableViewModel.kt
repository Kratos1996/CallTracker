package com.ishant.LKing.app

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ishant.LKing.app.constant.AppConst
import com.ishant.LKing.network.ErrorMessage
import com.ishant.LKing.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
open class BaseObservableViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app), Observable {
    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    val showLoading = mutableStateOf(false)
    val _errorListener = MutableSharedFlow<Response.Message>()
    val errorListener = _errorListener.asSharedFlow()
    var isRefresh = mutableStateOf(false)
    var showShimmer = mutableStateOf(false)
    private val _navigationListener = MutableSharedFlow<NavigationData>()
    val navigationListener = _navigationListener.asSharedFlow()
    var searchString = ""
    fun navigateTo(navCode: Int, navData: String = "") {
        viewModelScope.launch {
            _navigationListener.emit(
                NavigationData(navCode = navCode, navData = navData)
            )
        }
    }
    fun navigateBack() {
        navigateTo(AppConst.NAV_BACK_CLICK)
    }

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) {
        callbacks.remove(callback)
    }

    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }

    fun MutableSharedFlow<Response.Message>.emitError(message: String?) {
        viewModelScope.launch {
            emit(
                Response.Message(message ?: ErrorMessage.SOME_THING_WRONG)
            )
        }
    }
    data class NavigationData(
        val navCode: Int = 0,
        val navData: String = ""
    )
}