package com.taboor.features.home.presentation.partner

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessPartnerBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isArabic = LocaleManager.isArabic

    fun openBusinessWebsite() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://taboor.app/business")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = AppColors.Paper,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AppColors.Gray300)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge
            Surface(
                shape = RoundedCornerShape(50),
                color = AppColors.SoftTeal,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storefront,
                        contentDescription = null,
                        tint = AppColors.Amber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isArabic) "طابور للأعمال والعيادات" else "Taboor for Business",
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }
            }

            // Title
            Text(
                text = if (isArabic) "انضم إلى شركاء طابور" else "Join Taboor Business Partners",
                fontFamily = Tajawal,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.Ink
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = if (isArabic)
                    "نظام سحابي ذكي لإدارة الطوابير والحجوزات لعيادتك أو نشاطك التجاري بكل سهولة واحترافية."
                else
                    "Smart cloud queue and appointment management system for your clinic or enterprise.",
                fontFamily = Tajawal,
                fontSize = 13.sp,
                color = AppColors.TextSecondary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pillars
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PillarItem(
                    icon = Icons.Rounded.Groups,
                    color = AppColors.Teal,
                    title = if (isArabic) "تنظيم الحجوزات ومنع التكدس" else "Queue Control & Less Crowding",
                    desc = if (isArabic)
                        "تخلص من الزحام في صالة الانتظار ونظّم دخول العملاء والمرضى بأرقام إلكترونية دقيقة."
                    else
                        "Eliminate crowded waiting rooms with precise digital tickets."
                )

                PillarItem(
                    icon = Icons.Rounded.Tv,
                    color = AppColors.Indigo,
                    title = if (isArabic) "شاشات عرض القاعة والنداء الصوتي" else "Lounge TV & Voice Calling",
                    desc = if (isArabic)
                        "اعرض الدور الحالي ونادي على العميل صوتياً على أي شاشة تلفزيون داخل صالتك تلقائياً."
                    else
                        "Broadcast live turns on smart TV screens with automated speech calling."
                )

                PillarItem(
                    icon = Icons.Rounded.NotificationsActive,
                    color = AppColors.Amber,
                    title = if (isArabic) "تطبيق موبايل وتنبيهات فورية لعملائك" else "Client Mobile App & Push Alerts",
                    desc = if (isArabic)
                        "يُمكّن عملاءك من متابعة موعدهم والحضور في الوقت المناسب دون انتظار طويل في المكان."
                    else
                        "Allow customers to track wait time remotely and arrive right on schedule."
                )

                PillarItem(
                    icon = Icons.Rounded.Insights,
                    color = AppColors.Teal,
                    title = if (isArabic) "تقارير أداء الموظفين ورضا العملاء" else "Staff Analytics & Customer Insights",
                    desc = if (isArabic)
                        "لوحة تحكم مباشرة لقياس متوسط وقت الخدمة وعدد العملاء وتقييمات الفروع."
                    else
                        "Live dashboard tracking service duration, branch volume and customer satisfaction."
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Button
            Button(
                onClick = {
                    openBusinessWebsite()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .bounceClick(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isArabic) "ابدأ التجربة المجانية (14 يوماً)" else "Start 14-Day Free Trial",
                        fontFamily = Tajawal,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isArabic) "بدون بطاقة ائتمانية • إعداد سهل في 5 دقائق" else "No credit card required • 5 min setup",
                fontFamily = Tajawal,
                fontSize = 12.sp,
                color = AppColors.TextTertiary
            )
        }
    }
}

@Composable
private fun PillarItem(
    icon: ImageVector,
    color: Color,
    title: String,
    desc: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AppColors.CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = Tajawal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontFamily = Tajawal,
                    fontSize = 11.5.sp,
                    color = AppColors.TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
