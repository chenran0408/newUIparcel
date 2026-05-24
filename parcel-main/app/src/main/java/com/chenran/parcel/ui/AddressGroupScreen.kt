package com.chenran.parcel.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.chenran.parcel.ui.theme.GlassCard
import com.chenran.parcel.ui.theme.SuccessGreen
import com.chenran.parcel.ui.theme.glassOnCardColor
import com.chenran.parcel.ui.theme.glassOnCardVariantColor
import com.chenran.parcel.ui.components.TagDialog
import com.chenran.parcel.util.SmsParser
import com.chenran.parcel.util.SmsUtil
import com.chenran.parcel.util.getAddressMappings
import com.chenran.parcel.util.getCustomSmsByTimeFilter
import com.chenran.parcel.util.loadCustomRulesToParser
import com.chenran.parcel.util.removeAddressMapping
import com.chenran.parcel.util.saveAddressMapping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressGroupScreen(
    context: android.content.Context,
    navController: NavController,
    onCallback: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var allAddresses by remember { mutableStateOf(listOf<String>()) }
    var addressMappings by remember { mutableStateOf(mapOf<String, String>()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedAddresses = remember { mutableStateListOf<String>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var currentAddress by remember { mutableStateOf<String?>(null) }
    var currentTag by remember { mutableStateOf("") }
    val expandedTags = remember { mutableStateListOf<String>() }

    fun loadData() {
        scope.launch {
            isLoading = true
            try {
                val mappings = getAddressMappings(context)
                addressMappings = mappings
                val addresses = withContext(Dispatchers.IO) {
                    val allSms = SmsUtil.readAllSms(context)
                    val customSms = getCustomSmsByTimeFilter(context, 0)
                    val allMessages = allSms + customSms
                    val parser = SmsParser()
                    loadCustomRulesToParser(context, parser)

                    allMessages.mapNotNull { sms ->
                        val result = parser.parseSms(sms.body)
                        if (result.success) result.address else null
                    }.distinct().sorted()
                }
                allAddresses = addresses
                val tags = mappings.values.toSet().filter { it !in expandedTags }
                expandedTags.addAll(tags)
            } catch (e: Exception) {
                com.chenran.parcel.util.addLog(context, "加载地址列表失败: ${e.message}")
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadData() }

    fun saveMapping(originalAddress: String, tag: String) {
        saveAddressMapping(context, originalAddress, tag)
        addressMappings = getAddressMappings(context)
        onCallback()
    }

    fun removeMapping(originalAddress: String) {
        removeAddressMapping(context, originalAddress)
        addressMappings = getAddressMappings(context)
        onCallback()
    }

    val tagsWithAddresses = addressMappings.entries.groupBy({ it.value }, { it.key }).toSortedMap()
    val unmappedAddresses = allAddresses.filter { it !in addressMappings.keys }.sorted()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("地址归类", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    if (isSelectionMode && selectedAddresses.isNotEmpty()) {
                        IconButton(onClick = {
                            showAddDialog = true
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "合并选中")
                        }
                    }
                    if (!isSelectionMode) {
                        TextButton(onClick = {
                            isSelectionMode = true
                            currentAddress = null
                            currentTag = ""
                            selectedAddresses.clear()
                        }) {
                            Text("多选", color = Color.White)
                        }
                    } else {
                        TextButton(onClick = {
                            isSelectionMode = false
                            selectedAddresses.clear()
                        }) {
                            Text("取消", color = Color.White)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("加载中...", color = glassOnCardColor())
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                tagsWithAddresses.forEach { (tag, addresses) ->
                    item(key = "tag_$tag") {
                        val isExpanded = expandedTags.contains(tag)
                        Surface(
                            onClick = {
                                if (isExpanded) expandedTags.remove(tag) else expandedTags.add(tag)
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = SuccessGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = SuccessGreen.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${addresses.size}个地址",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SuccessGreen.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "收起" else "展开",
                                    tint = SuccessGreen.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    val isExpanded = expandedTags.contains(tag)
                    if (isExpanded) {
                        items(items = addresses.sorted(), key = { "addr_${tag}_$it" }) { addr ->
                            TagAddressItem(
                                address = addr,
                                isSelected = selectedAddresses.contains(addr),
                                isSelectionMode = isSelectionMode,
                                onItemClick = {
                                    if (isSelectionMode) {
                                        if (selectedAddresses.contains(addr)) selectedAddresses.remove(addr)
                                        else selectedAddresses.add(addr)
                                    }
                                },
                                onRemoveClick = { removeMapping(addr) }
                            )
                        }
                    }
                }

                if (unmappedAddresses.isNotEmpty()) {
                    item(key = "unmapped_header") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "未归类 (${unmappedAddresses.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = glassOnCardColor()
                            )
                        }
                    }
                    items(items = unmappedAddresses, key = { "unmapped_$it" }) { addr ->
                        UnmappedAddressItem(
                            address = addr,
                            isSelected = selectedAddresses.contains(addr),
                            isSelectionMode = isSelectionMode,
                            onItemClick = {
                                if (isSelectionMode) {
                                    if (selectedAddresses.contains(addr)) selectedAddresses.remove(addr)
                                    else selectedAddresses.add(addr)
                                }
                            },
                            onTagClick = {
                                selectedAddresses.clear()
                                selectedAddresses.add(addr)
                                currentAddress = addr
                                showAddDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TagDialog(
            title = "设置归类标签",
            currentTag = currentTag,
            currentAddress = currentAddress,
            existingMappings = addressMappings,
            selectedAddresses = selectedAddresses.toList(),
            onDismiss = {
                showAddDialog = false
                selectedAddresses.clear()
                isSelectionMode = false
            },
            onConfirm = { tag ->
                if (tag.isNotBlank()) {
                    selectedAddresses.forEach { addr ->
                        saveMapping(addr, tag)
                    }
                    showAddDialog = false
                    isSelectionMode = false
                    selectedAddresses.clear()
                    loadData()
                }
            }
        )
    }
}

@Composable
private fun TagAddressItem(
    address: String,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onItemClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val cardBorder = if (isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 3.dp, bottom = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(cardBorder)
            .clickable { onItemClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = glassOnCardColor(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (!isSelectionMode) {
                Surface(
                    onClick = onRemoveClick,
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Red.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = "移出",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun UnmappedAddressItem(
    address: String,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onItemClick: () -> Unit,
    onTagClick: () -> Unit,
) {
    val cardBorder = if (isSelected) {
        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(cardBorder)
            .clickable { onItemClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = glassOnCardColor(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (!isSelectionMode) {
                Surface(
                    onClick = onTagClick,
                    shape = RoundedCornerShape(6.dp),
                    color = SuccessGreen.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "归类",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
