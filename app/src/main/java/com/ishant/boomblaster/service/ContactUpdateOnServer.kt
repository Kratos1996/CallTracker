package com.ishant.boomblaster.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.ishant.boomblaster.api.request.UploadContactRequest
import com.ishant.boomblaster.app.CallTrackerApplication
import com.ishant.boomblaster.app.CallTrackerApplication.Companion.isRefreshUi
import com.ishant.boomblaster.database.room.DatabaseRepository
import com.ishant.boomblaster.database.room.UploadContact
import com.ishant.boomblaster.database.room.UploadContactType
import com.ishant.boomblaster.domain.ContactUseCase
import com.ishant.boomblaster.network.Resource
import com.ishant.boomblaster.service.ServiceRestarterService.Companion.TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ContactUpdateOnServer : Service() {
    @Inject
    lateinit var databaseRepository: DatabaseRepository
    @Inject
    lateinit var contactUseCase: ContactUseCase
    private var uploadContactList: List<UploadContact> = (arrayListOf())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentIndex = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("CallTracker : ", "CallTracker : Service > ContactUpdateOnServer > StartProcess")
        getUploadContactsList()
    }

/*    private fun startProcess() {
        val context = this
        if (currentIndex < uploadContactList.size) {
            Log.e(
                "CallTracker : ",
                "CallTracker : Service > ContactUpdateOnServer > StartProcess > Current Index : $currentIndex"
            )
            val currentItem = uploadContactList[currentIndex]
            scope.launch {
                saveContact(uploadContact = currentItem) { onSuccess ->
                    if (onSuccess) {
                        currentIndex++
                        //startProcess()
                    } else {
                        scope.cancel()
                        context.stopServiceContactUpload()
                        Log.e(
                            "CallTracker : ",
                            "CallTracker : Service > ContactUpdateOnServer > StartProcess > Current Index : $currentIndex"
                        )
                    }
                }
            }
        } else {
            scope.cancel()
            context.stopServiceContactUpload()
        }
    }*/

    private fun saveContact(uploadContacts: UploadContactRequest?) {
        if (uploadContacts?.data?.isNotEmpty() == true) {
            contactUseCase.uploadContacts(request = uploadContacts).onEach { result ->
                when (result) {
                    is Resource.Error -> {
                        Log.e("CallTracker : ", "CallTracker: Contact Not Saved")
                        val data = UploadContact(
                            serialNo = System.currentTimeMillis(),
                            listOfCalls = Gson().toJson(uploadContacts),
                            type = UploadContactType.PENDING,
                            date = System.currentTimeMillis()
                        )
                        databaseRepository.insertUpload(data)
                        delay(1000)
                        CallTrackerApplication.isRefreshUi.value = true
                    }

                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        delay(1000)
                        CallTrackerApplication.isRefreshUi.value = true
                    }
                }
            }.launchIn(
                CoroutineScope(Dispatchers.Main)
            )
        }

    }

    private suspend fun updateUploadCall(serialNo: Long, type: String) {
        Log.e(
            "CallTracker : ",
            "CallTracker : Service > ContactUpdateOnServer > Save Call on Database > $type"
        )
        databaseRepository.updateUploadContact(serialNo, type)
        delay(1000)
        isRefreshUi.value = true

    }

    private fun getUploadContactsList(type: String = UploadContactType.PENDING) {
        scope.launch {
            databaseRepository.getUploadContactList(type).collectLatest {
                currentIndex = 0
                uploadContactList = it
                delay(4000)
                //startProcess()
            }
            Log.e(TAG, "CallTracker : Service > ContactUpdateOnServer > GetAllPendingList")

        }

    }
}