package com.ishant.boomblaster.ui.dashboard.screens.call


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.ishant.boomblaster.R
import com.ishant.boomblaster.api.response.getcalls.GetCallsRes
import com.ishant.boomblaster.ui.dashboard.screens.common.DashboardCommon
import com.ishant.boomblaster.utils.getActivityContext
import com.ishant.boomblaster.utils.initiatePhoneCall
import com.ishant.boomblaster.utils.toast
import com.ishant.corelibcompose.toolkit.colors.text_primary
import com.ishant.corelibcompose.toolkit.colors.text_secondary
import com.ishant.corelibcompose.toolkit.colors.white
import com.ishant.corelibcompose.toolkit.colors.white_only
import com.ishant.corelibcompose.toolkit.constant.AppConst
import com.ishant.corelibcompose.toolkit.ui.checkbox.CircularBox
import com.ishant.corelibcompose.toolkit.ui.clickables.noRippleClickable
import com.ishant.corelibcompose.toolkit.ui.custom_pullrefresh.CustomPullToRefresh
import com.ishant.corelibcompose.toolkit.ui.imageLib.MultiMediaView
import com.ishant.corelibcompose.toolkit.ui.other.OtherModifiers.LineDivider
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.textstyles.RegularText
import com.ishant.corelibcompose.toolkit.ui.textstyles.SearchViewNew
import com.ishant.corelibcompose.toolkit.ui.textstyles.SubHeadingText

@Composable
fun CallScreen() {
    val context = LocalContext.current
    val callViewModel: CallViewModel = hiltViewModel(context.getActivityContext())
    val initialApiCalled = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        if (!initialApiCalled.value) {
            initialApiCalled.value = true
            callViewModel.searchString = ""
            callViewModel.getCallDetails()
        }
    }
    LoadCallScreen(callViewModel)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadCallScreen(
    callViewModel: CallViewModel
) {
    val lazyColumnListState = rememberLazyListState()
    CustomPullToRefresh(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = callViewModel.isRefreshing.value,
        onRefresh = {
            callViewModel.getCallDetails()
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
                item { CallTab(callViewModel = callViewModel) }
                item { SearchViewWithCheckBox(viewModel = callViewModel) }
                callList(callViewModel)
            }
        }
    }
}
@Composable
private fun CallTab(callViewModel: CallViewModel){
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.sdp)) {
        DashboardCommon.Tab(
            title = stringResource(id = R.string.pending),
            isSelected = callViewModel.pendingCall.value
        ) {
            callViewModel.pendingCall.value = true
            callViewModel.completedCall.value = false
            callViewModel.getCallDetails()
        }
        DashboardCommon.Tab(
            title = stringResource(id = R.string.completed),
            isSelected = callViewModel.completedCall.value
        ) {
            callViewModel.pendingCall.value = false
            callViewModel.completedCall.value = true
            callViewModel.getCallDetails()
        }
    }
}

@Composable
private fun SearchViewWithCheckBox(viewModel: CallViewModel) {
    SearchViewNew(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 20.sdp, vertical = 20.sdp),
    ) {
        viewModel.searchString = (it.trim())
        viewModel.filterSearch()
    }
}

private fun LazyListScope.callList(
    viewModel: CallViewModel
) {
    if (viewModel.showLoading.value) {
        item {
            DashboardCommon.CustomShimmer()
        }
    } else {
        if (viewModel.callsDataFilterList.isEmpty()) {
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
            itemsIndexed(viewModel.callsDataFilterList) { _, item ->
                CallingItem(item = item, viewModel)
            }
        }
    }
}



@Composable
private fun CallingItem(item: GetCallsRes.GetCallsData, viewModel: CallViewModel) {
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
            modifier = Modifier
                .padding(start = 10.sdp, end = 4.sdp)
                .constrainAs(coinCode) {
                    start.linkTo(icon.end)
                    top.linkTo(icon.top)
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

        MultiMediaView.FromLocal(
            mediaDrawable = R.raw.call_ico,
            playAnimation = true,
            mediaType = AppConst.MEDIA_TYPE_LOTTIE,
            roundCorner = 30.sdp,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(40.sdp)
                .width(42.sdp)
                .constrainAs(coinBalance) {
                    end.linkTo(parent.end)
                    top.linkTo(icon.top)
                }.noRippleClickable(true) {
                    item.type = 1
                    viewModel.callDetailUpdateOnServer(item) { isSuccess, response ->
                        if (isSuccess) {
                            context.toast(response.message)
                            context.initiatePhoneCall(item.mobile ?: "")
                            viewModel.getCallDetails()
                        } else {
                            context.toast(context.getString(R.string.cannot_initiate_this_call))
                        }
                    }

                })

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



