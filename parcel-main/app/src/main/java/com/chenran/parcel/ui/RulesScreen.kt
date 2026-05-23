package com.chenran.parcel.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.chenran.parcel.ui.theme.Primary
import com.chenran.parcel.util.clearAllCustomPatterns
import com.chenran.parcel.util.clearCustomPattern
import com.chenran.parcel.util.getCustomList
import com.chenran.parcel.viewmodel.ParcelViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    onCallback: () -> Unit
) {
    var listAddr by remember { mutableStateOf(mutableListOf<String>()) }
    var listCode by remember { mutableStateOf(mutableListOf<String>()) }
    var listIgnoreKeywords by remember { mutableStateOf(mutableListOf<String>()) }

    fun getDate() {
        listAddr = getCustomList(context, "address").toMutableList()
        listCode = getCustomList(context, "code").toMutableList()
        listIgnoreKeywords = getCustomList(context, "ignoreKeywords").toMutableList()
    }

    fun onDelete(key: String, pattern: String) {
        clearCustomPattern(context, key, pattern, viewModel)

        getDate()
        onCallback()
    }

    LaunchedEffect(Unit) {
        getDate()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("规则列表", color = Color.White) },
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
                            clearAllCustomPatterns(context, viewModel)
                            onCallback()
                            navController.navigate("home")
                        }
                    ) {
                        Text("清除所有规则", color = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_rule?message=") },
                containerColor = Primary.copy(alpha = 0.9f),
                contentColor = Color.White,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "手动添加规则")
            }
        }
    ) { innerPadding ->
        val cardColor = glassBaseColor().copy(alpha = 0.65f)
        val onCardColor = glassOnCardColor()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "自定义地址规则",
                    style = MaterialTheme.typography.titleSmall,
                    color = onCardColor
                )
            }
            items(listAddr) { address ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        SelectionContainer {
                            Text(
                                text = address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = onCardColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = { onDelete("address", address) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "删除规则",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Text(
                    text = "自定义取件码规则",
                    style = MaterialTheme.typography.titleSmall,
                    color = onCardColor
                )
            }
            items(listCode) { code ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = code,
                            style = MaterialTheme.typography.bodyMedium,
                            color = onCardColor,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = { onDelete("code", code) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "删除规则",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Text(
                    text = "不解析关键词",
                    style = MaterialTheme.typography.titleSmall,
                    color = onCardColor
                )
            }
            items(listIgnoreKeywords) { keyword ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = keyword,
                            style = MaterialTheme.typography.bodyMedium,
                            color = onCardColor,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            modifier = Modifier.size(36.dp),
                            onClick = { onDelete("ignoreKeywords", keyword) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "删除关键词",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}