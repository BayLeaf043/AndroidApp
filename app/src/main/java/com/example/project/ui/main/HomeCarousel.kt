package com.example.project.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape

data class InfoCard(
    val title: String,
    val imageRes: Int,
    val textRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoCarousel(
    items: List<InfoCard>,
    onCardClick: (InfoCard) -> Unit
) {
    // Стан пейджера (який зараз слайд)
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { items.size }
    )


    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) { page ->

        // Легка анімація масштабу: активна картка трохи більша
        val isCurrent = pagerState.currentPage == page
        val scale by animateFloatAsState(
            targetValue = if (isCurrent) 1f else 0.9f,
            label = "cardScale"
        )

        val card = items[page]

        Card(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxSize()
                .clickable { onCardClick(card) },
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                // 🖼 Картинка на фоні
                Image(
                    painter = painterResource(id = card.imageRes),
                    contentDescription = card.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Легке затемнення, щоб текст читався
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )

                // Тільки заголовок
                Text(
                    text = card.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }
    }
}