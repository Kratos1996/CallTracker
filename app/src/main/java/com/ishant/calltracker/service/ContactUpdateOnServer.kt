package com.ishant.calltracker.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.collectAsState
import com.ishant.calltracker.database.room.DatabaseRepository
import com.ishant.calltracker.database.room.UploadContact
import com.ishant.calltracker.database.room.UploadContactType
import com.ishant.calltracker.domain.ContactUseCase
import com.ishant.calltracker.network.Resource
import com.ishant.calltracker.service.ServiceRestarterService.Companion.TAG
import com.ishant.calltracker.utils.Utils
import com.ishant.calltracker.utils.stopServiceContactUpload
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ContactUpdateOnServer :Service() {
    @Inject lateinit var databaseRepository: DatabaseRepository
    @Inject lateinit var contactUseCase: ContactUseCase
    private var uploadContactList : List<UploadContact> = (arrayListOf())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentIndex = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("CallTracker : ","CallTracker : Service > ContactUpdateOnServer > StartProcess")
       getUploadContactsList()
    }

    private fun startProcess() {
        val context = this
        if (currentIndex < uploadContactList.size) {
            Log.e("CallTracker : ","CallTracker : Service > ContactUpdateOnServer > StartProcess > Current Index : $currentIndex")
            val currentItem = uploadContactList[currentIndex]
            scope.launch {
                saveContact(uploadContact = currentItem) { onSuccess ->
                    if(onSuccess){
                        currentIndex++
                        startProcess()
                    }else{
                        scope.cancel()
                        context.stopServiceContactUpload()
                        Log.e("CallTracker : ","CallTracker : Service > ContactUpdateOnServer > StartProcess > Current Index : $currentIndex")
                    }
                }
            }
        }else{
            scope.cancel()
            context.stopServiceContactUpload()
        }
    }

    private fun saveContact(uploadContact: UploadContact, onSuccess :  (Boolean) ->Unit) {
        contactUseCase.uploadContact(
            sourceMobileNo = Utils.extractLast10Digits(uploadContact.sourceMobileNo),
            mobile = Utils.extractLast10Digits(uploadContact.mobile),
            name = /*AppPreference.user.name ?: ""*/uploadContact.name,
            type = uploadContact.type).onEach { result ->
            when (result) {
                is Resource.Error -> {
                    Log.e("CallTracker : ","CallTracker : Service > ContactUpdateOnServer > StartProcess > saveContact > onError")
                    onSuccess(false)
                }

                is Resource.Success -> {
                    Log.e("CallTracker : ","CallTracker : Service > ContactUpdateOnServer > saveContact > Call Saved On Server")
                    withContext(scope.coroutineContext) {
                        delay(4000)
                        onSuccess(true)
                        updateUploadCall(uploadContact.serialNo, UploadContactType.COMPLETE)
                    }

                }
                else ->{
                    Log.e("CallTracker : ","CallTracker : Service > ContactUpdateOnServer > StartProcess > saveContact > elsePart")
                }
            }
        }.launchIn(scope)
    }

    private suspend fun updateUploadCall(serialNo:Long, type: String) {
            Log.e("CallTracker : ","CallTracker : Service > ContactUpdateOnServer > Save Call on Database > $type")
            databaseRepository.updateUploadContact(serialNo,type)

    }

    private fun getUploadContactsList(type:String = UploadContactType.PENDING){
        scope.launch {
            databaseRepository.getUploadContactList(type).collectLatest {
                if(uploadContactList.isEmpty()){
                    uploadContactList = it
                    startProcess()
                }
            }
            Log.e(TAG,"CallTracker : Service > ContactUpdateOnServer > GetAllPendingList")

        }

    }
}