package com.example.project.ui.membership

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.example.project.data.model.MyMembershipUi
import androidx.compose.ui.Alignment

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MembershipPager(
    items: List<MyMembershipUi>,
    activatingId: String?,
    onActivateToday: (String) -> Unit,
    onClick: (MyMembershipUi) -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { items.size })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        pageSpacing = 12.dp
    ) { page ->
        val item = items[page]
        val isCurrent = pagerState.currentPage == page

        val scale by animateFloatAsState(
            targetValue = if (isCurrent) 1f else 0.94f,
            label = "membershipScale"
        )

        MembershipCard(
            item = item,
            activating = activatingId == item.purchaseId,
            onActivateToday = { onActivateToday(item.purchaseId) },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clickable { onClick(item) }
        )
    }
}

@Composable
private fun MembershipCard(
    item: MyMembershipUi,
    activating: Boolean,
    onActivateToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(status = item.status)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "${item.trainingType.uppercase()} • ${item.level} • ${item.ageGroup}",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(10.dp))

            val isActive = item.status.equals("active", ignoreCase = true)
            val isPending = item.status.equals("pending", ignoreCase = true)

            if (isActive && item.startAtMillis > 0L && item.endAtMillis > 0L) {
                Text(
                    text = "${formatDate(item.startAtMillis)} — ${formatDate(item.endAtMillis)}",
                    color = Color(0xFFFF9800),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = "Абонемент ще не активовано",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Занять: ${item.visitsLeft}/${item.visitsTotal}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.weight(1f))

            if (isPending) {
                Button(
                    onClick = onActivateToday,
                    enabled = !activating,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    if (activating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Активація...", color = Color.White)
                    } else {
                        Text("Активувати сьогодні", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (text, bg) = when (status.lowercase()) {
        "active" -> "Активний" to Color(0xFFFF9800)
        "pending" -> "Очікує" to Color(0xFF616161)
        else -> status to Color(0xFF616161)
    }

    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

private fun formatDate(ms: Long): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date(ms))
}
