package com.taboor.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Inter

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Taboor Logo Icon & Wordmark
 * Directly mirrored from Flutter: `lib/features/splash/presentation/widgets/taboor_logo.dart`
 * Always forced to LTR so brand identity, icon and wordmark never flip.
 */
@Composable
fun TaboorLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showWordmark: Boolean = true
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TaboorLogoIcon(size = size)

            if (showWordmark) {
                Spacer(modifier = Modifier.width(size * 0.22f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "taboor",
                        fontFamily = Inter,
                        fontSize = (size.value * 0.55f).sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1.5).sp,
                        color = AppColors.Primary
                    )
                    Text(
                        text = ".",
                        fontFamily = Inter,
                        fontSize = (size.value * 0.55f).sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1.5).sp,
                        color = AppColors.Accent
                    )
                }
            }
        }
    }
}

@Composable
fun TaboorLogoIcon(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        val boxSize = size * (95f / 110f)
        val cornerRadius = size * (28f / 110f)
        val offset = size * (15f / 110f)

        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.TopStart
        ) {
            // Orange background container (bottom-right offset)
            Box(
                modifier = Modifier
                    .size(boxSize)
                    .offset(x = offset, y = offset)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(AppColors.Accent)
            )

            // Primary Ink container (top-left) with white SVG Path
            Box(
                modifier = Modifier
                    .size(boxSize)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(AppColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                TbLogoSymbol(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(boxSize * 0.08f),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Custom Canvas drawing the exact SVG Path from Flutter's TbLogoPainter
 */
@Composable
fun TbLogoSymbol(
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val scaleX = size.width / 1024f
        val scaleY = size.height / 1024f

        val path = Path().apply {
            fillType = PathFillType.EvenOdd

            // ---------------------------------------------------------
            // Part 1: Outer lower loop
            // ---------------------------------------------------------
            moveTo(808f * scaleX, 537f * scaleY)
            lineTo(802f * scaleX, 525f * scaleY)
            lineTo(785f * scaleX, 502f * scaleY)
            lineTo(766f * scaleX, 487f * scaleY)
            lineTo(746f * scaleX, 477f * scaleY)
            lineTo(726f * scaleX, 471f * scaleY)
            lineTo(715f * scaleX, 471f * scaleY)
            lineTo(711f * scaleX, 469f * scaleY)
            lineTo(683f * scaleX, 471f * scaleY)
            lineTo(680f * scaleX, 473f * scaleY)
            lineTo(667f * scaleX, 475f * scaleY)
            lineTo(639f * scaleX, 489f * scaleY)
            lineTo(615f * scaleX, 511f * scaleY)
            lineTo(600f * scaleX, 532f * scaleY)
            lineTo(591f * scaleX, 541f * scaleY)
            lineTo(584f * scaleX, 552f * scaleY)
            lineTo(575f * scaleX, 561f * scaleY)
            lineTo(554f * scaleX, 590f * scaleY)
            lineTo(546f * scaleX, 598f * scaleY)
            lineTo(540f * scaleX, 608f * scaleY)
            lineTo(531f * scaleX, 617f * scaleY)
            lineTo(525f * scaleX, 627f * scaleY)
            lineTo(514f * scaleX, 638f * scaleY)
            lineTo(495f * scaleX, 639f * scaleY)
            lineTo(487f * scaleX, 643f * scaleY)
            lineTo(480f * scaleX, 650f * scaleY)
            lineTo(475f * scaleX, 661f * scaleY)
            lineTo(477f * scaleX, 674f * scaleY)
            lineTo(487f * scaleX, 686f * scaleY)
            lineTo(495f * scaleX, 690f * scaleY)
            lineTo(720f * scaleX, 690f * scaleY)
            lineTo(748f * scaleX, 682f * scaleY)
            lineTo(765f * scaleX, 673f * scaleY)
            lineTo(781f * scaleX, 660f * scaleY)
            lineTo(795f * scaleX, 645f * scaleY)
            lineTo(806f * scaleX, 626f * scaleY)
            lineTo(816f * scaleX, 591f * scaleY)
            lineTo(816f * scaleX, 569f * scaleY)
            lineTo(814f * scaleX, 566f * scaleY)
            lineTo(814f * scaleX, 557f * scaleY)
            close()

            // ---------------------------------------------------------
            // Part 2: Inner loop cutout
            // ---------------------------------------------------------
            moveTo(757f * scaleX, 552f * scaleY)
            lineTo(762f * scaleX, 570f * scaleY)
            lineTo(761f * scaleX, 598f * scaleY)
            lineTo(748f * scaleX, 620f * scaleY)
            lineTo(742f * scaleX, 626f * scaleY)
            lineTo(727f * scaleX, 635f * scaleY)
            lineTo(712f * scaleX, 639f * scaleY)
            lineTo(590f * scaleX, 639f * scaleY)
            lineTo(584f * scaleX, 637f * scaleY)
            lineTo(661f * scaleX, 539f * scaleY)
            lineTo(669f * scaleX, 531f * scaleY)
            lineTo(680f * scaleX, 525f * scaleY)
            lineTo(695f * scaleX, 520f * scaleY)
            lineTo(709f * scaleX, 520f * scaleY)
            lineTo(724f * scaleX, 524f * scaleY)
            lineTo(740f * scaleX, 533f * scaleY)
            close()

            // ---------------------------------------------------------
            // Part 3: Main "T" letter structure
            // ---------------------------------------------------------
            moveTo(540f * scaleX, 323f * scaleY)
            lineTo(527f * scaleX, 319f * scaleY)
            lineTo(288f * scaleX, 319f * scaleY)
            lineTo(285f * scaleX, 317f * scaleY)
            lineTo(276f * scaleX, 317f * scaleY)
            lineTo(273f * scaleX, 319f * scaleY)
            lineTo(257f * scaleX, 319f * scaleY)
            lineTo(254f * scaleX, 321f * scaleY)
            lineTo(249f * scaleX, 321f * scaleY)
            lineTo(231f * scaleX, 329f * scaleY)
            lineTo(214f * scaleX, 343f * scaleY)
            lineTo(205f * scaleX, 353f * scaleY)
            lineTo(195f * scaleX, 373f * scaleY)
            lineTo(195f * scaleX, 379f * scaleY)
            lineTo(191f * scaleX, 389f * scaleY)
            lineTo(191f * scaleX, 448f * scaleY)
            lineTo(199f * scaleX, 468f * scaleY)
            lineTo(207f * scaleX, 476f * scaleY)
            lineTo(215f * scaleX, 480f * scaleY)
            lineTo(227f * scaleX, 483f * scaleY)
            lineTo(281f * scaleX, 484f * scaleY)
            lineTo(281f * scaleX, 512f * scaleY)
            lineTo(283f * scaleX, 515f * scaleY)
            lineTo(281f * scaleX, 520f * scaleY)
            lineTo(281f * scaleX, 621f * scaleY)
            lineTo(287f * scaleX, 644f * scaleY)
            lineTo(293f * scaleX, 656f * scaleY)
            lineTo(311f * scaleX, 676f * scaleY)
            lineTo(340f * scaleX, 689f * scaleY)
            lineTo(353f * scaleX, 691f * scaleY)
            lineTo(374f * scaleX, 690f * scaleY)
            lineTo(388f * scaleX, 686f * scaleY)
            lineTo(405f * scaleX, 677f * scaleY)
            lineTo(423f * scaleX, 659f * scaleY)
            lineTo(432f * scaleX, 642f * scaleY)
            lineTo(436f * scaleX, 624f * scaleY)
            lineTo(436f * scaleX, 484f * scaleY)
            lineTo(481f * scaleX, 483f * scaleY)
            lineTo(492f * scaleX, 478f * scaleY)
            lineTo(500f * scaleX, 468f * scaleY)
            lineTo(502f * scaleX, 462f * scaleY)
            lineTo(502f * scaleX, 449f * scaleY)
            lineTo(496f * scaleX, 437f * scaleY)
            lineTo(484f * scaleX, 429f * scaleY)
            lineTo(450f * scaleX, 427f * scaleY)
            lineTo(447f * scaleX, 429f * scaleY)
            lineTo(425f * scaleX, 429f * scaleY)
            lineTo(414f * scaleX, 433f * scaleY)
            lineTo(405f * scaleX, 439f * scaleY)
            lineTo(391f * scaleX, 453f * scaleY)
            lineTo(381f * scaleX, 475f * scaleY)
            lineTo(381f * scaleX, 620f * scaleY)
            lineTo(377f * scaleX, 630f * scaleY)
            lineTo(370f * scaleX, 636f * scaleY)
            lineTo(364f * scaleX, 639f * scaleY)
            lineTo(355f * scaleX, 639f * scaleY)
            lineTo(343f * scaleX, 631f * scaleY)
            lineTo(336f * scaleX, 616f * scaleY)
            lineTo(336f * scaleX, 465f * scaleY)
            lineTo(340f * scaleX, 449f * scaleY)
            lineTo(349f * scaleX, 429f * scaleY)
            lineTo(345f * scaleX, 427f * scaleY)
            lineTo(246f * scaleX, 427f * scaleY)
            lineTo(244f * scaleX, 422f * scaleY)
            lineTo(244f * scaleX, 401f * scaleY)
            lineTo(249f * scaleX, 388f * scaleY)
            lineTo(259f * scaleX, 377f * scaleY)
            lineTo(271f * scaleX, 372f * scaleY)
            lineTo(514f * scaleX, 372f * scaleY)
            lineTo(522f * scaleX, 375f * scaleY)
            lineTo(532f * scaleX, 383f * scaleY)
            lineTo(538f * scaleX, 399f * scaleY)
            lineTo(539f * scaleX, 572f * scaleY)
            lineTo(583f * scaleX, 517f * scaleY)
            lineTo(592f * scaleX, 503f * scaleY)
            lineTo(592f * scaleX, 387f * scaleY)
            lineTo(590f * scaleX, 384f * scaleY)
            lineTo(590f * scaleX, 377f * scaleY)
            lineTo(578f * scaleX, 351f * scaleY)
            lineTo(562f * scaleX, 335f * scaleY)
            close()
        }

        drawPath(path = path, color = tint)
    }
}
