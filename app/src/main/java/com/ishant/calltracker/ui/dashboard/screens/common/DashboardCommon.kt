package com.ishant.calltracker.ui.dashboard.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import com.ishant.calltracker.R
import com.ishant.corelibcompose.toolkit.colors.blue_light_bg_60
import com.ishant.corelibcompose.toolkit.colors.text_secondary
import com.ishant.corelibcompose.toolkit.ui.clickables.noRippleClickable
import com.ishant.corelibcompose.toolkit.ui.imageLib.CoreImageView
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.textstyles.RegularText

object DashboardCommon {

    @Composable
    fun TitleSeparator(
        title: String,
        modifier: Modifier = Modifier,
        showArrow: Boolean = false,
        capitalize: Boolean = true,
        isBold: Boolean = true,
        onClick: (() -> Unit?)? = null
    ) {
        ConstraintLayout(
            modifier = modifier
            .background(MaterialTheme.colors.blue_light_bg_60)
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 12.sdp, horizontal = 16.sdp)
            .noRippleClickable {
                if(onClick != null) {
                    onClick()
                }
            }
        ) {
            val(text, arrow) = createRefs()
            if(isBold) {
                RegularText.Bold(
                    title = if(capitalize) title.uppercase() else title,
                    modifier = Modifier.constrainAs(text){
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                )
            } else {
                RegularText.Medium(
                    title = if(capitalize) title.uppercase() else title,
                    modifier = Modifier.constrainAs(text){
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                )
            }

            CoreImageView.FromLocalDrawable(
                painterResource = R.drawable.ic_setting_end_arrow,
                modifier = Modifier
                    .padding(end = 1.sdp)
                    .size(11.sdp)
                    .padding(start = 4.sdp)
                    .constrainAs(arrow) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        visibility = if(showArrow) Visibility.Visible else Visibility.Gone
                    },
                colorFilter = ColorFilter.tint(MaterialTheme.colors.text_secondary)
            )
        }

    }
}