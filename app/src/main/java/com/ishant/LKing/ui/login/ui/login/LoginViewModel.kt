package com.ishant.LKing.ui.login.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ishant.LKing.api.response.LoginResponse
import com.ishant.LKing.api.response.UrlResponse
import com.ishant.LKing.database.room.DatabaseRepository
import com.ishant.LKing.di.BaseUrlInterceptor
import com.ishant.LKing.domain.ContactUseCase
import com.ishant.LKing.network.Resource
import com.ishant.LKing.utils.AppPreference
import com.ishant.LKing.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel  @Inject constructor(
    private val contactUseCase: ContactUseCase,
    val  databaseRepository: DatabaseRepository,
     val baseUrlInterceptor: BaseUrlInterceptor
) : ViewModel() {

    init {
        getDomains()
    }

    private val _loginResponse = MutableStateFlow<Response<LoginResponse>>(Response.Empty)
    val loginResponse = _loginResponse.asSharedFlow()

    private val _domains = MutableStateFlow<Response<UrlResponse>>(Response.Empty)
    val domain = _domains.asSharedFlow()

     val permissionGrantedMain = MutableStateFlow(false)
     val permissionGranted = permissionGrantedMain.asSharedFlow()

    fun login(username: String, password: String) {
        contactUseCase.loginNow(username,password).onEach { result ->
            when(result){
                is Resource.Error -> _loginResponse.value = Response.Message(result.message)
                is Resource.Loading -> _loginResponse.value = Response.Loading(isLoading = true)
                is Resource.Success -> {
                    AppPreference.isUserLoggedIn = true
                    AppPreference.loginUser = result.data?: LoginResponse()
                    AppPreference.firebaseToken = result.data?.token?: ""
                    _loginResponse.value = Response.Success(result.data)
                }
            }
        }.launchIn(viewModelScope)
    }
    fun getDomains() {
        contactUseCase.getDomains().onEach { result ->
            when(result){
                is Resource.Error -> _domains.value = Response.Message(result.message)
                is Resource.Loading -> _domains.value = Response.Loading(isLoading = true)
                is Resource.Success -> { _domains.value = Response.Success(result.data)
                }
            }
        }.launchIn(viewModelScope)
    }
}