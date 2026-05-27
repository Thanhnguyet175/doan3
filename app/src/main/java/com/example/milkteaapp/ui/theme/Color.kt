package com.example.milkteaapp.ui.theme

import androidx.compose.ui.graphics.Color


val CreamBg      = Color(0xFFFBF8F4)   // Nền tổng thể siêu sáng, ngả kem mịn
val CardWhite    = Color(0xFFFFFFFF)   // Nền các thẻ chức năng trắng tinh khôi
val BrownPrimary = Color(0xFF6F4E37)   // Màu Nâu Đậm (Dành cho Button, Icon chính)
val TextDark     = Color(0xFF231F20)   // Chữ tiêu đề gần như đen để tăng độ tương phản
val TextSub      = Color(0xFF7F7571)   // Chữ mô tả/phụ màu xám ấm
val GreenBadge   = Color(0xFFE8F5E9)   // Nền badge "+12.5%" xanh nhạt
val GreenText    = Color(0xFF2E7D32)   // Màu chữ badge xanh lá
// ── Palette chính – Trà Sữa NL ───────────────────────────────────────────────
val MauNauDam   = Color(0xFF3E2723)   // nâu rất đậm  – header, nền tối
val MauNau      = Color(0xFF4E342E)   // nâu đậm      – nút chính, accent
val MauNauVua   = Color(0xFF795548)   // nâu vừa      – secondary
val MauNauNhat  = Color(0xFFF5F0EB)   // nâu rất nhạt – nền trang
val MauKem      = Color(0xFFD7CCC8)   // kem nhạt      – divider, placeholder

val MauXanh     = Color(0xFF4A7C59)   // xanh lá      – confirmed, success
val MauVang     = Color(0xFFF59E0B)   // vàng hổ phách – hot badge, ready
val MauDo       = Color(0xFFEF4444)   // đỏ           – error, cancel
val MauCam      = Color(0xFFF97316)   // cam           – delayed, warning

// ── Màu Material roles (dùng trong Theme.kt) ──────────────────────────────────
val Primary        = MauNau
val OnPrimary      = Color.White
val PrimaryContainer    = Color(0xFF7B5E57)
val OnPrimaryContainer  = Color(0xFFFFECE9)

val Secondary      = MauXanh
val OnSecondary    = Color.White
val SecondaryContainer  = Color(0xFFB2DFDB)
val OnSecondaryContainer = MauNauDam

val Tertiary       = MauVang
val OnTertiary     = MauNauDam
val TertiaryContainer   = Color(0xFFFFF9C4)
val OnTertiaryContainer = Color(0xFF3E2000)

val Background     = MauNauNhat
val OnBackground   = MauNauDam
val Surface        = Color.White
val OnSurface      = MauNauDam
val SurfaceVariant = Color(0xFFEDE7E1)
val OnSurfaceVariant    = Color(0xFF6D4C41)

val Error          = MauDo
val OnError        = Color.White
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer    = Color(0xFF410002)

val Outline        = MauKem
val OutlineVariant = Color(0xFFD7CCC8)