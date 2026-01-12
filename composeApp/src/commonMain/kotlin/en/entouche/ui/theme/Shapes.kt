package en.entouche.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val EntoucheShapes = Shapes(
    // Extra small - for small chips and indicators
    extraSmall = RoundedCornerShape(8.dp),

    // Small - for buttons and input fields
    small = RoundedCornerShape(12.dp),

    // Medium - for cards and dialogs
    medium = RoundedCornerShape(16.dp),

    // Large - for bottom sheets and large cards
    large = RoundedCornerShape(24.dp),

    // Extra large - for full-screen dialogs
    extraLarge = RoundedCornerShape(32.dp)
)

// Custom shapes for specific components
object EntoucheCustomShapes {
    val Pill = RoundedCornerShape(50)
    val TopSheet = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    val BottomSheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val CardSmall = RoundedCornerShape(12.dp)
    val CardMedium = RoundedCornerShape(16.dp)
    val CardLarge = RoundedCornerShape(24.dp)
    val Button = RoundedCornerShape(12.dp)
    val ButtonPill = RoundedCornerShape(50)
    val TextField = RoundedCornerShape(12.dp)
    val Chip = RoundedCornerShape(8.dp)
    val FAB = RoundedCornerShape(20.dp)
    val CircularFAB = RoundedCornerShape(50)
}
