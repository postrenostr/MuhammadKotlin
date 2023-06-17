package com.vitorpamplona.amethyst.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.vitorpamplona.amethyst.R


@Composable
fun HomeScreenUI(list: List<String>, clicked: (String) -> Unit) {

    val localRoutesList = listOf("Messages", "Videos", "Feed")

    Column(
        Modifier
            .padding(5.dp)
            .fillMaxSize()
    ) {

        Text(
            text = stringResource(id = R.string.image_search),
            modifier = Modifier.padding(start = 20.dp, top = 10.dp, end = 10.dp),
            fontSize = TextUnit(18f, TextUnitType.Sp)
        )

        LazyVerticalGrid(contentPadding = PaddingValues(10.dp), columns = GridCells.Fixed(3)) {
            items(count = list.size,
                itemContent = {
                    HomeListItem(string = list[it], clicked)
                }
            )
        }

        Text(
            text = stringResource(id = R.string.app_features),
            modifier = Modifier.padding(start = 20.dp, top = 10.dp, end = 10.dp),
            fontSize = TextUnit(18f, TextUnitType.Sp)
        )

        LazyVerticalGrid(contentPadding = PaddingValues(10.dp), columns = GridCells.Fixed(3)) {
            items(count = localRoutesList.size,
                itemContent = {
                    HomeListItem(string = localRoutesList[it], clicked)
                }
            )
        }

    }
}

@Composable
fun HomeListItem(string: String, clicked: (String) -> Unit) {
    val colorIs = colorResource(id = R.color.home_blue)
    Box(
        modifier = Modifier
            .height(120.dp)
            .padding(8.dp)
            .wrapContentHeight()
            .fillMaxWidth(),

        ) {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = true) {
                    clicked(string)
                }
                .drawBehind {
                    drawRoundRect(color = colorIs, cornerRadius = CornerRadius(10f, 10f))
                }
                .padding(top = 40.dp),
            text = string,
        )
    }

}