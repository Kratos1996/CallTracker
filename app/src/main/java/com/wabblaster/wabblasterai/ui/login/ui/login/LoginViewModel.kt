package com.wabblaster.wabblasterai.ui.login.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wabblaster.wabblasterai.api.response.LoginResponse
import com.wabblaster.wabblasterai.database.room.DatabaseRepository
import com.wabblaster.wabblasterai.domain.ContactUseCase
import com.wabblaster.wabblasterai.network.Resource
import com.wabblaster.wabblasterai.utils.AppPreference
import com.wabblaster.wabblasterai.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class LoginViewModel  @Inject constructor(
    private val contactUseCase: ContactUseCase,
    private val  databaseRepository: DatabaseRepository,
) : ViewModel() {

    private val _loginResponse = MutableStateFlow<Response<LoginResponse>>(Response.Empty)
    val loginResponse = _loginResponse.asSharedFlow()

     val permissionGrantedMain = MutableStateFlow(false)
     val permissionGranted = permissionGrantedMain.asSharedFlow()

    fun login(username: String, password: String) {
        contactUseCase.loginNow(username,password).onEach { result ->
            when(result){
                is Resource.Error -> _loginResponse.value = Response.Message(result.message)
                is Resource.Loading -> _loginResponse.value = Response.Loading(isLoading = true)
                is Resource.Success -> {
                    databaseRepository.deleteAll()
                    AppPreference.logout()
                    AppPreference.isUserLoggedIn = true
                    AppPreference.user = result.data?.user?: LoginResponse.User()
                    AppPreference.firebaseToken = result.data?.token?: ""
                    _loginResponse.value = Response.Success(result.data)
                }
            }
        }.launchIn(viewModelScope)
    }
}