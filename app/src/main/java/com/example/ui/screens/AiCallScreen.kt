package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.ui.components.AudioReadoutButton
import com.example.ui.theme.AgriGreen
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.TrendDownRed
import com.example.ui.viewmodel.CallDialogMessage
import com.example.ui.viewmodel.CallStatus
import com.example.ui.viewmodel.KisanViewModel

@Composable
fun AiCallScreen(
    viewModel: KisanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val callStatus by viewModel.callStatus.collectAsState()
    val durationSec by viewModel.callDurationSec.collectAsState()
    val messages by viewModel.callMessages.collectAsState()

    var manualQuestion by remember { mutableStateOf("") }

    val formattedDuration = remember(durationSec) {
        val mins = durationSec / 60
        val secs = durationSec % 60
        String.format("%02d:%02d", mins, secs)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_call_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // IVR & Call Center Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(2.dp, if (callStatus == CallStatus.CONNECTED) AgriGreen else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AgriGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = AppStrings.get("call_number", language),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AgriGreen
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = Color(0xFF673AB7).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "✨ Gemini 3.8 Flash",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF512DA8)
                                )
                            }

                            Surface(
                                color = HarvestGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🔊 AI వాయిస్: తెలుగు",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE65100)
                                )
                            }

                            if (callStatus == CallStatus.CONNECTED) {
                                Surface(
                                    color = HarvestGold.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = formattedDuration,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Call Pulsing Avatar
                    CallAvatarAnimation(callStatus = callStatus)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = AppStrings.get("ai_call_title", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val statusLabel = when (callStatus) {
                        CallStatus.IDLE -> if (language == AppLanguage.TELUGU) "ఉచిత వాయిస్ కాల్ సిద్ధంగా ఉంది" else "Ready for free voice call"
                        CallStatus.DIALING -> if (language == AppLanguage.TELUGU) "కాల్ కలుపుతోంది..." else "Dialing..."
                        CallStatus.CONNECTED -> AppStrings.get("call_status_connected", language)
                        CallStatus.LISTENING -> AppStrings.get("call_status_listening", language)
                        CallStatus.SPEAKING -> AppStrings.get("call_status_speaking", language)
                        CallStatus.ENDED -> if (language == AppLanguage.TELUGU) "కాల్ ముగిసింది" else "Call Ended"
                    }

                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (callStatus == CallStatus.CONNECTED) AgriGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Call Action Button
                    if (callStatus == CallStatus.IDLE || callStatus == CallStatus.ENDED) {
                        Button(
                            onClick = { viewModel.startCall() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreen),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("start_call_button")
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = AppStrings.get("start_call", language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.endCall() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TrendDownRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("end_call_button")
                        ) {
                            Icon(imageVector = Icons.Default.CallEnd, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = AppStrings.get("end_call", language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Quick Spoken Prompts for Low-Literacy Farmers
        item {
            Column {
                Text(
                    text = if (language == AppLanguage.TELUGU) "రైతులకు సాధారణ ప్రశ్నలు (నొక్కి మాట్లాడండి):" else "Quick Voice Questions for Farmers:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val sampleQuestions = listOf(
                    Pair("వరంగల్ మార్కెట్లో తేజ మిర్చి ధర ఎంత ఉంది?", "What is Teja chilli price in Warangal?"),
                    Pair("పత్తిలో గులాబీ రంగు పురుగు నివారణకు ఏం చేయాలి?", "How to control pink bollworm in cotton?"),
                    Pair("రాబోయే 3 రోజుల్లో వర్షం పడుతుందా?", "Will it rain in the next 3 days?"),
                    Pair("కోల్డ్ స్టోరేజీ అద్దె మరియు నిల్వ ఖర్చులు ఎంత?", "What are cold storage charges and capacity?")
                )

                sampleQuestions.forEach { (qTe, qEn) ->
                    Surface(
                        onClick = {
                            if (callStatus == CallStatus.IDLE || callStatus == CallStatus.ENDED) {
                                viewModel.startCall()
                            }
                            viewModel.askAiAssistant(qTe, qEn)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(AgriGreen.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = AgriGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (language == AppLanguage.TELUGU) qTe else qEn,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Custom Question Input Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = manualQuestion,
                        onValueChange = { manualQuestion = it },
                        placeholder = { Text(AppStrings.get("speak_now", language), fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_call_custom_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (manualQuestion.isNotBlank()) {
                                if (callStatus == CallStatus.IDLE || callStatus == CallStatus.ENDED) {
                                    viewModel.startCall()
                                }
                                viewModel.askAiAssistant(manualQuestion, manualQuestion)
                                manualQuestion = ""
                            }
                        },
                        modifier = Modifier
                            .background(AgriGreen, CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }

        // Live Call Conversation Dialog Transcript
        item {
            Text(
                text = if (language == AppLanguage.TELUGU) "కాల్ సంభాషణ వివరాలు:" else "Live Call Conversation:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(messages, key = { it.id }) { msg ->
            CallMessageBubble(
                message = msg,
                language = language,
                onReplayAudio = {
                    viewModel.readAloudInTelugu(msg.textTe)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CallAvatarAnimation(callStatus: CallStatus) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (callStatus == CallStatus.CONNECTED || callStatus == CallStatus.SPEAKING) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(90.dp),
        contentAlignment = Alignment.Center
    ) {
        if (callStatus == CallStatus.CONNECTED || callStatus == CallStatus.SPEAKING) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pulseScale)
                    .background(AgriGreen.copy(alpha = 0.2f), CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .size(68.dp)
                .background(
                    if (callStatus == CallStatus.CONNECTED) AgriGreen else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (callStatus) {
                    CallStatus.SPEAKING -> Icons.Default.GraphicEq
                    CallStatus.LISTENING -> Icons.Default.Mic
                    CallStatus.CONNECTED -> Icons.Default.HeadsetMic
                    else -> Icons.Default.HeadsetMic
                },
                contentDescription = null,
                tint = if (callStatus == CallStatus.CONNECTED) Color.White else AgriGreen,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
fun CallMessageBubble(
    message: CallDialogMessage,
    language: AppLanguage,
    onReplayAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.isUser
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isUser) AgriGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = bubbleColor,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isUser) Icons.Default.RecordVoiceOver else Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = if (isUser) AgriGreen else HarvestGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUser) {
                                if (language == AppLanguage.TELUGU) "రైతు" else "Farmer"
                            } else {
                                if (language == AppLanguage.TELUGU) "కిసాన్ AI అసిస్టెంట్" else "Kisan AI Assistant"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isUser) AgriGreen else Color(0xFFE65100)
                        )
                        if (!isUser && message.isGeminiPowered) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFEDE7F6),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "✨ Gemini 3.8 Flash",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF512DA8)
                                )
                            }
                        }
                    }

                    if (!isUser) {
                        IconButton(onClick = onReplayAudio, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Replay",
                                tint = AgriGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (language == AppLanguage.TELUGU) message.textTe else message.textEn,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (message.keyActionItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = if (language == AppLanguage.TELUGU) "సిఫార్సు చేసిన ముఖ్యమైన చర్యలు:" else "Key Action Steps:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AgriGreen
                            )
                            message.keyActionItems.forEach { action ->
                                Text(
                                    text = "• $action",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
