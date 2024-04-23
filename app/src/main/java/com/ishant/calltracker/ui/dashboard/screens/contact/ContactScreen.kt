package com.ishant.calltracker.ui.dashboard.screens.contact

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
import com.airbnb.lottie.model.content.CircleShape
import com.ishant.calltracker.R
import com.ishant.calltracker.api.response.getcalls.GetCallsRes
import com.ishant.calltracker.database.room.ContactList
import com.ishant.calltracker.ui.dashboard.screens.call.CallViewModel
import com.ishant.calltracker.ui.dashboard.screens.call.callNow
import com.ishant.calltracker.ui.dashboard.screens.common.DashboardCommon
import com.ishant.calltracker.ui.home.HomeViewModel
import com.ishant.calltracker.utils.getActivityContext
import com.ishant.corelibcompose.toolkit.colors.text_primary
import com.ishant.corelibcompose.toolkit.colors.text_secondary
import com.ishant.corelibcompose.toolkit.colors.white
import com.ishant.corelibcompose.toolkit.colors.white_only
import com.ishant.corelibcompose.toolkit.constant.AppConst.MEDIA_TYPE_LOTTIE
import com.ishant.corelibcompose.toolkit.ui.checkbox.CircularBox
import com.ishant.corelibcompose.toolkit.ui.clickables.noRippleClickable
import com.ishant.corelibcompose.toolkit.ui.custom_pullrefresh.CustomPullToRefresh
import com.ishant.corelibcompose.toolkit.ui.imageLib.MultiMediaView
import com.ishant.corelibcompose.toolkit.ui.other.OtherModifiers
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.textstyles.RegularText
import com.ishant.corelibcompose.toolkit.ui.textstyles.SearchViewNew
import com.ishant.corelibcompose.toolkit.ui.textstyles.SubHeadingText

@Composable
fun ContactScreen() {
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = hiltViewModel(context.getActivityContext())
    val initialApiCalled = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        if (!initialApiCalled.value) {
            initialApiCalled.value = true
            homeViewModel.getContacts("")
        }
    }
    LoadContactScreen(homeViewModel)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadContactScreen(
    homeViewModel: HomeViewModel
) {
    val lazyColumnListState = rememberLazyListState()
    CustomPullToRefresh(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = homeViewModel.isRefreshing.value,
        onRefresh = {
            homeViewModel.getContacts("")
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
                item { SearchViewWithCheckBox(homeViewModel = homeViewModel) }
                contactList(homeViewModel)
            }
        }
    }
}

@Composable
private fun SearchViewWithCheckBox(homeViewModel: HomeViewModel) {
    SearchViewNew(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 20.sdp, vertical = 20.sdp),
    ) {
        homeViewModel.searchString = (it.trim())
        homeViewModel.filterSearch()
    }
}

private fun LazyListScope.contactList(
    viewModel: HomeViewModel
) {
    if (viewModel.showLoading.value) {
        item {
            DashboardCommon.CustomShimmer()
        }
    } else {
        if (viewModel.contactDataFilterList.isNullOrEmpty()) {
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
            itemsIndexed(viewModel.contactDataFilterList) { _, item ->
                ContactDataItem(item = item, viewModel)
            }
        }
    }
}



@Composable
private fun ContactDataItem(item: ContactList, viewModel: HomeViewModel) {
    val randomColor = DashboardCommon.getRandomColor()
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 12.sdp, start = 12.sdp, end = 12.sdp)
            .noRippleClickable(true) {
                callNow(item.phoneNumber ?: "")
            }
    ) {
        val (icon, coinCode, coinName, coinBalance, coinValue, divider) = createRefs()
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
                title = (item?.name?.uppercase() ?: item?.phoneNumber.toString()).take(2),
                textColor = MaterialTheme.colors.white_only
            )
        }
        SubHeadingText.Medium(
            title = item?.name?.uppercase() ?: item?.phoneNumber.toString(),
            textColor = MaterialTheme.colors.text_primary,
            modifier = Modifier
                .padding(start = 10.sdp, end = 4.sdp)
                .constrainAs(coinCode) {
                    start.linkTo(icon.end)
                    top.linkTo(icon.top)
                }
        )
        RegularText(
            title = item?.phoneNumber.toString() ?: "",
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
            mediaType = MEDIA_TYPE_LOTTIE,
            roundCorner = 30.sdp,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(40.sdp)
                .width(42.sdp)
                .constrainAs(coinBalance) {
                    end.linkTo(parent.end)
                    top.linkTo(icon.top)
                })

        OtherModifiers.LineDivider(modifier = Modifier
            .padding(top = 10.sdp)
            .constrainAs(divider) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(icon.bottom, 6.dp)
            }
        )
    }
}