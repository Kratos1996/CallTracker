package com.ishant.calltracker.ui.dashboard.screens.sms


import android.content.pm.PackageManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.ishant.calltracker.R
import com.ishant.calltracker.api.response.sms.SendSmsRes
import com.ishant.calltracker.app.showAsBottomSheet
import com.ishant.calltracker.ui.dashboard.screens.call.SmsViewModel
import com.ishant.calltracker.ui.dashboard.screens.common.DashboardCommon
import com.ishant.calltracker.utils.AppPreference
import com.ishant.calltracker.utils.getActivityContext
import com.ishant.calltracker.utils.isPackageInstalled
import com.ishant.calltracker.utils.sendSmsUsingSimSlot
import com.ishant.calltracker.utils.sendWhatsAppMessage
import com.ishant.calltracker.utils.toast
import com.ishant.corelibcompose.toolkit.colors.gray_divider2
import com.ishant.corelibcompose.toolkit.colors.text_primary
import com.ishant.corelibcompose.toolkit.colors.text_secondary
import com.ishant.corelibcompose.toolkit.colors.white
import com.ishant.corelibcompose.toolkit.colors.white_only
import com.ishant.corelibcompose.toolkit.constant.AppConst
import com.ishant.corelibcompose.toolkit.ui.checkbox.CircularBox
import com.ishant.corelibcompose.toolkit.ui.clickables.bounceClick
import com.ishant.corelibcompose.toolkit.ui.clickables.noRippleClickable
import com.ishant.corelibcompose.toolkit.ui.custom_pullrefresh.CustomPullToRefresh
import com.ishant.corelibcompose.toolkit.ui.imageLib.CoreImageView
import com.ishant.corelibcompose.toolkit.ui.imageLib.MultiMediaView
import com.ishant.corelibcompose.toolkit.ui.other.OtherModifiers.LineDivider
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.textstyles.RegularText
import com.ishant.corelibcompose.toolkit.ui.textstyles.SearchViewNew
import com.ishant.corelibcompose.toolkit.ui.textstyles.SubHeadingText

@Composable
fun SmsScreen() {
    val context = LocalContext.current
    val smsViewModel: SmsViewModel = hiltViewModel(context.getActivityContext())
    val initialApiCalled = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        if (!initialApiCalled.value) {
            initialApiCalled.value = true
            smsViewModel.searchString = ""
            smsViewModel.getSms()
        }
    }
    LoadSmsScreen(smsViewModel)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadSmsScreen(
    smsViewModel: SmsViewModel
) {
    val lazyColumnListState = rememberLazyListState()
    CustomPullToRefresh(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = smsViewModel.isRefreshing.value,
        onRefresh = {
            smsViewModel.getSms()
        },
        refreshingOffset = 110.sdp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.white)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.white),
                state = lazyColumnListState,
                contentPadding = PaddingValues(bottom = 60.sdp)
            ) {
                item { SearchViewWithCheckBox(viewModel = smsViewModel) }
                smsDataList(smsViewModel)
            }
        }
    }
}


@Composable
private fun SearchViewWithCheckBox(viewModel: SmsViewModel) {
    SearchViewNew(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 20.sdp, vertical = 20.sdp),
    ) {
        viewModel.searchString = (it.trim())
        viewModel.filterSearch()
    }
}

private fun LazyListScope.smsDataList(
    viewModel: SmsViewModel
) {
    if (viewModel.showLoading.value) {
        item {
            DashboardCommon.CustomShimmer()
        }
    } else {
        if (viewModel.sendSmsDataFilterList.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 500.sdp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RegularText(title = stringResource(R.string.no_records_found))
                }
            }
        } else {
            itemsIndexed(viewModel.sendSmsDataFilterList) { _, item ->
                SmsItem(item = item, viewModel)
            }
        }
    }
}



@Composable
private fun SmsItem(item: SendSmsRes.SendSmsData, viewModel: SmsViewModel) {
    val randomColor = DashboardCommon.getRandomColor()
    val context = LocalContext.current
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 12.sdp, start = 12.sdp, end = 12.sdp)

    ) {
        val (icon, coinCode, coinName, coinBalance,msgImg,wpImg, divider) = createRefs()
        CircularBox(
            modifier = Modifier
                .height(28.sdp)
                .width(28.sdp)
                .background(color = randomColor, shape = RoundedCornerShape(60.sdp))
                .constrainAs(icon) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        ) {
            SubHeadingText.Medium(
                title = (item.name?.uppercase() ?: item.mobile.toString()).take(2),
                textColor = MaterialTheme.colors.white_only
            )
        }
        SubHeadingText.Medium(
            title = item.name?.uppercase() ?: "",
            textColor = MaterialTheme.colors.text_primary,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .padding(start = 10.sdp, end = 4.sdp)
                .constrainAs(coinCode) {
                    start.linkTo(icon.end)
                    end.linkTo(wpImg.start)
                    top.linkTo(icon.top)
                    width = Dimension.fillToConstraints
                }
        )
        RegularText(
            title = item.mobile.toString(),
            textColor = MaterialTheme.colors.text_secondary,
            modifier = Modifier
                .padding(top = 3.sdp, start = 10.sdp, end = 4.sdp)
                .constrainAs(coinName) {
                    start.linkTo(icon.end)
                    top.linkTo(coinCode.bottom)
                }
        )
        CoreImageView.FromLocalDrawable(
            painterResource = R.drawable.ic_whatsapp,
            modifier = Modifier.constrainAs(wpImg){
                end.linkTo(msgImg.start, margin = 10.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            onClick = {
                if(item.mobile.isNullOrEmpty()){
                    context.toast("There are some issues with your mobile number.please try again later")
                }else {
                    val packageManager: PackageManager = context.packageManager
                    val whatsApp: Boolean = isPackageInstalled("com.whatsapp", packageManager)
                    val WhatsappBusiness: Boolean =
                        isPackageInstalled("com.whatsapp.w4b", packageManager)
                    if (whatsApp && WhatsappBusiness) {
                        context.getActivityContext().showAsBottomSheet {
                            Column (modifier = Modifier.fillMaxWidth().padding(vertical = 10.sdp)) {
                                RegularText(title = "WhatsApp", modifier = Modifier
                                    .padding(horizontal = 10.sdp , vertical = 15.sdp).bounceClick {
                                    context.sendWhatsAppMessage(
                                        "+91" + item.mobile ?: "",
                                        item.message ?: AppPreference.replyMsg,
                                        packageName = "com.whatsapp"
                                    )
                                })
                                Divider(thickness = 1.sdp, color = MaterialTheme.colors.gray_divider2, modifier = Modifier.padding(horizontal = 10.sdp))
                                RegularText(title = "WhatsApp Business", modifier = Modifier
                                    .padding(horizontal = 10.sdp , vertical = 15.sdp)
                                    .bounceClick {
                                        context.sendWhatsAppMessage(
                                            "+91" + item.mobile ?: "",
                                            item.message ?: AppPreference.replyMsg,
                                            packageName = "com.whatsapp.w4b"
                                        )
                                    })
                            }
                        }
                    } else {
                        context.sendWhatsAppMessage(
                            "+91" + item.mobile ?: "",
                            item.message ?: AppPreference.replyMsg
                        )
                    }
                }

            }
        )
        CoreImageView.FromLocalDrawable(
            painterResource = R.drawable.ic_message,
            modifier = Modifier.constrainAs(msgImg){
                end.linkTo(parent.end)
                top.linkTo(icon.top)
                bottom.linkTo(parent.bottom)
            },
            onClick = {
                if(item.mobile.isNullOrEmpty()){
                    context.toast("There are some issues with your mobile number.please try again later")
                }else{
                    context.sendSmsUsingSimSlot(AppPreference.simSlot,item.mobile?:"",item.message?:AppPreference.replyMsg)
                }

            }
        )
        LineDivider(modifier = Modifier
            .padding(top = 10.sdp)
            .constrainAs(divider) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(icon.bottom, 6.dp)
            }
        )
    }
}



