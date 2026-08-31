package com.example.mathia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import java.text.SimpleDateFormat
import java.util.*

enum class ContestType(val displayName: String, val category: String) {
    PRECISION("Carrera de Precisión", "Precisión"),
    CONSTANCY("Maestros de la Suma", "Constancia"),
    IMPROVEMENT("Superación Matemática", "Mejora"),
    MASTERY("Desafío de Divisiones", "Dominio")
}

data class WeeklyContest(
    val id: String,
    val title: String,
    val type: ContestType,
    val description: String,
    val startDate: String,
    val endDate: String,
    val rewardXP: Int,
    val rewardStars: Int
)

@Composable
fun WeeklyContestCard(
    contest: WeeklyContest = getActiveWeeklyContest(),
    onParticipate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.PrimaryBlue.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(AppColors.PrimaryBlue, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CONCURSO SEMANAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.PrimaryBlue
                        )
                        Text(
                            text = contest.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMain
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = AppColors.Coral.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = contest.type.category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Coral
                    )
                }
            }

            Text(
                text = contest.description,
                fontSize = 13.sp,
                color = AppColors.TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Vence: ${contest.endDate}",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onParticipate,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
                    shape = RoundedCornerShape(50.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "¡Participar!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

fun getActiveWeeklyContest(): WeeklyContest {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    
    // Set to Monday of current week
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val startStr = sdf.format(cal.time)
    
    // Set to Sunday of current week
    cal.add(Calendar.DAY_OF_WEEK, 6)
    val endStr = sdf.format(cal.time)

    return WeeklyContest(
        id = "contest_current",
        title = "Desafío de Divisiones",
        type = ContestType.MASTERY,
        description = "¡Demuestra tu dominio resolviendo la mayor cantidad de divisiones exactas esta semana!",
        startDate = startStr,
        endDate = endStr,
        rewardXP = 100,
        rewardStars = 25
    )
}
