package com.example.dsa_duel.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dsa_duel.R

@Composable
fun LoginRegisterToggle(
    isLogin: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val neonCyan = Color(0xFF00C2FF)
    val neonPurple = Color(0xFF8F67FF)
    val darkBg = Color(0xFF05050C)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A1C)),
        border = BorderStroke(1.dp, Color(0xFF202038))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Login Side
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onToggle(true) }
                    .background(
                        if (isLogin) {
                            Brush.horizontalGradient(
                                colors = listOf(neonPurple.copy(alpha = 0.15f), Color.Transparent)
                            )
                        } else {
                            SolidColor(Color.Transparent)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RETURNING PLAYER",
                        fontSize = 9.sp,
                        color = if (isLogin) neonPurple else Color.Gray,
                        fontFamily = FontFamily(Font(R.font.rajdhani_bold)),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "LOGIN",
                        fontSize = 20.sp,
                        color = if (isLogin) Color.White else Color.Gray.copy(alpha = 0.5f),
                        fontFamily = FontFamily(Font(R.font.rajdhani_bold)),
                        letterSpacing = 2.sp
                    )
                    if (isLogin) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(2.dp)
                                .background(neonPurple)
                        )
                    }
                }
            }

            // VS Section (The Duel Divider)
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .background(darkBg),
                contentAlignment = Alignment.Center
            ) {
                // Glow behind VS
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                            )
                        )
                )
                Text(
                    text = "VS",
                    fontSize = 22.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily(Font(R.font.rajdhani_bold)),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Register Side
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onToggle(false) }
                    .background(
                        if (!isLogin) {
                            Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, neonCyan.copy(alpha = 0.15f))
                            )
                        } else {
                            SolidColor(Color.Transparent)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NEW CHALLENGER",
                        fontSize = 9.sp,
                        color = if (!isLogin) neonCyan else Color.Gray,
                        fontFamily = FontFamily(Font(R.font.rajdhani_bold)),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "REGISTER",
                        fontSize = 20.sp,
                        color = if (!isLogin) Color.White else Color.Gray.copy(alpha = 0.5f),
                        fontFamily = FontFamily(Font(R.font.rajdhani_bold)),
                        letterSpacing = 2.sp
                    )
                    if (!isLogin) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(2.dp)
                                .background(neonCyan)
                        )
                    }
                }
            }
        }
    }
}
