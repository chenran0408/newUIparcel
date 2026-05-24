package com.chenran.parcel.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.chenran.parcel.ui.theme.glassBaseColor
import com.chenran.parcel.ui.theme.glassOnCardColor
import com.chenran.parcel.util.addCustomList
import com.chenran.parcel.viewmodel.ParcelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleScreen(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    message: String,
    onCallback: () -> Unit
) {
    var addressPattern by remember { mutableStateOf("") }
    var codePattern by remember { mutableStateOf("") }
    var lockerPattern by remember { mutableStateOf("") }
    var ignoreKeyword by remember { mutableStateOf("") }

    val cardColor = glassBaseColor().copy(alpha = 0.65f)
    val onCardColor = glassOnCardColor()
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = onCardColor,
        unfocusedTextColor = onCardColor,
        focusedBorderColor = onCardColor.copy(alpha = 0.6f),
        unfocusedBorderColor = onCardColor.copy(alpha = 0.3f),
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        cursorColor = onCardColor,
    )

    val hasMessage = message.isNotBlank()

    val isButtonEnabled = if (hasMessage) {
        (addressPattern.isNotEmpty() && message.contains(addressPattern))
                || (codePattern.isNotEmpty() && message.contains(codePattern))
                || lockerPattern.isNotBlank()
                || ignoreKeyword.isNotEmpty()
    } else {
        addressPattern.isNotBlank() || codePattern.isNotBlank() || lockerPattern.isNotBlank() || ignoreKeyword.isNotBlank()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("新增规则", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            navController.navigate("rules")
                        }
                    ) {
                        Text("规则列表", color = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    if (hasMessage) {
                        SelectionContainer {
                            Text(
                                text = message,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = onCardColor
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Column {
                        Text(
                            text = if (hasMessage) "复制短信中的 取件码 填入" else "输入取件码正则或取件码样例",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onCardColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = codePattern,
                            placeholder = { Text("选填", color = onCardColor.copy(alpha = 0.5f)) },
                            onValueChange = { codePattern = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "复制短信中的 地址 填入",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onCardColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = addressPattern,
                            placeholder = { Text("选填", color = onCardColor.copy(alpha = 0.5f)) },
                            onValueChange = { addressPattern = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "柜号(如: 7号柜, ①号柜)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onCardColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = lockerPattern,
                            placeholder = { Text("选填", color = onCardColor.copy(alpha = 0.5f)) },
                            onValueChange = { lockerPattern = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text(
                            text = "填入关键词，不解析短信",
                            style = MaterialTheme.typography.bodyMedium,
                            color = onCardColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ignoreKeyword,
                            placeholder = { Text("选填", color = onCardColor.copy(alpha = 0.5f)) },
                            onValueChange = { ignoreKeyword = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Button(
                            enabled = isButtonEnabled,
                            onClick = {
                                if (addressPattern.isNotBlank()) {
                                    addCustomList(context, "address", addressPattern)
                                    viewModel.addCustomAddressPattern(addressPattern)
                                    addressPattern = ""
                                }
                                if (codePattern.isNotBlank()) {
                                    val regexPattern = if (hasMessage) {
                                        val escapedCodePattern = java.util.regex.Pattern.quote(codePattern)
                                        val parts = message.split(codePattern, limit = 2)
                                        if (parts.size == 2) {
                                            java.util.regex.Pattern.quote(parts[0]) + """([\s\S]{2,})""" + java.util.regex.Pattern.quote(parts[1])
                                        } else {
                                            java.util.regex.Pattern.quote(message).replace(escapedCodePattern, """([\s\S]{2,})""")
                                        }
                                    } else {
                                        "(" + java.util.regex.Pattern.quote(codePattern) + ")"
                                    }
                                    addCustomList(context, "code", regexPattern)
                                    viewModel.addCustomCodePattern(regexPattern)
                                    codePattern = ""
                                }
                                if (lockerPattern.isNotBlank()) {
                                    val lockerRegex = "(" + java.util.regex.Pattern.quote(lockerPattern) + ")"
                                    addCustomList(context, "locker", lockerRegex)
                                    viewModel.addCustomLockerPattern(lockerRegex)
                                    lockerPattern = ""
                                }
                                if (ignoreKeyword.isNotBlank()) {
                                    addCustomList(context, "ignoreKeywords", ignoreKeyword)
                                    viewModel.addIgnoreKeyword(ignoreKeyword)
                                    ignoreKeyword = ""
                                }
                                onCallback()
                                navController.navigate("rules")
                            }
                        ) {
                            Text(text = "点击自动添加规则")
                        }
                    }
                }
            }
        }
    }
}
