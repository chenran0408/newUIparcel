package com.chenran.parcel.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.chenran.parcel.MainActivity
import com.chenran.parcel.R
import com.chenran.parcel.model.ParcelData
import com.chenran.parcel.ui.theme.FailPink
import com.chenran.parcel.ui.theme.GlassCard
import com.chenran.parcel.ui.theme.GlassChip
import com.chenran.parcel.ui.theme.GlassSurface
import com.chenran.parcel.ui.theme.SuccessGreen
import com.chenran.parcel.ui.theme.glassButtonColor
import com.chenran.parcel.ui.theme.glassOnCardColor
import com.chenran.parcel.ui.theme.glassOnCardVariantColor
import com.chenran.parcel.ui.theme.glassSheetColor
import com.chenran.parcel.ui.theme.LocalIsDarkTheme
import com.chenran.parcel.ui.theme.pickupCodeBackgroundColor
import com.chenran.parcel.ui.theme.pickupCodeBackgroundColorDark
import com.chenran.parcel.ui.theme.pickupCodeColor
import com.chenran.parcel.ui.theme.pickupCodeColorDark
import com.chenran.parcel.util.addCompletedIds
import com.chenran.parcel.util.addLog
import com.chenran.parcel.util.exportRulesToJson
import com.chenran.parcel.util.formatPickupCode
import com.chenran.parcel.util.getAddressMappings
import com.chenran.parcel.util.getSortByLocker
import com.chenran.parcel.util.importRulesFromJson
import com.chenran.parcel.util.removeCompletedId
import com.chenran.parcel.util.saveAddressMapping
import com.chenran.parcel.util.saveIndex
import com.chenran.parcel.util.saveSortByLocker
import com.chenran.parcel.util.BackgroundManager
import com.chenran.parcel.util.ThemeManager
import com.chenran.parcel.util.WallpaperSettings
import com.chenran.parcel.viewmodel.ParcelViewModel
import com.chenran.parcel.ui.components.TagDialog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    hasPermission: Boolean,
    onCallBack: () -> Unit,
    updateAllWidget: () -> Unit,
    isSeniorMode: Boolean,
    onSeniorModeChanged: (Boolean) -> Unit,
    onWallpaperChanged: (String) -> Unit = {},
    themeMode: Int,
    onThemeModeChanged: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var showCompleted by remember { mutableStateOf(getShowCompleted(context)) }
    var showCodeTime by remember { mutableStateOf(getShowCodeTime(context)) }
    var isHorizontalLayout by remember { mutableStateOf(getHorizontalLayout(context)) }
    var sortByLocker by remember { mutableStateOf<Boolean>(getSortByLocker(context)) }

    var tempWallpaperUri by remember { mutableStateOf("") }
    var showWallpaperAdjust by remember { mutableStateOf(false) }

    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            tempWallpaperUri = uri.toString()
            showWallpaperAdjust = true
        }
    }

    val importRulesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() } ?: ""
                importRulesFromJson(context, viewModel, json)
                (context as MainActivity).readAndParseSms()
                addLog(context, "规则导入成功")
            } catch (e: Exception) {
                addLog(context, "规则导入失败: ${e.message}")
            }
        }
    }

    val exportRulesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val json = exportRulesToJson(context)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(json.toByteArray(Charsets.UTF_8))
                }
                addLog(context, "规则导出成功")
            } catch (e: Exception) {
                addLog(context, "规则导出失败: ${e.message}")
            }
        }
    }

    val timeFilterOptions = listOf(
        "全部", "今天", "近2天", "近3天", "近4天", "近5天",
        "近6天", "近7天", "近8天", "近9天", "近10天",
    )

    val selectedTimeFilterIndex by viewModel.timeFilterIndex.collectAsState()
    val failedData by viewModel.failedMessages.collectAsState()
    val successData by viewModel.successSmsData.collectAsState()
    val onCardColor = glassOnCardColor()
    val onCardVariantColor = glassOnCardVariantColor()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "取件码",
                        style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    Surface(
                        onClick = { showBottomSheet = true },
                        shape = RoundedCornerShape(20.dp),
                        color = glassButtonColor(),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
                    ) {
                        Text(
                            text = timeFilterOptions[selectedTimeFilterIndex],
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    Surface(
                        onClick = { navController.navigate("success_sms") },
                        shape = RoundedCornerShape(20.dp),
                        color = SuccessGreen.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, SuccessGreen.copy(alpha = 0.3f)),
                    ) {
                        Text(
                            text = successData.size.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        onClick = { navController.navigate("fail_sms") },
                        shape = RoundedCornerShape(20.dp),
                        color = FailPink.copy(alpha = 0.2f),
                        border = BorderStroke(0.5.dp, FailPink.copy(alpha = 0.3f)),
                    ) {
                        Text(
                            text = failedData.size.toString(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            color = FailPink,
                            style = if (isSeniorMode) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "菜单",
                                modifier = Modifier.size(if (isSeniorMode) 32.dp else 24.dp),
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            modifier = Modifier
                                .background(
                                    glassSheetColor(),
                                    RoundedCornerShape(16.dp)
                                )
                                .then(if (isSeniorMode) Modifier.fillMaxWidth(0.8f) else Modifier),
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            val menuItems = listOf(
                                if (sortByLocker) "取消按柜号排序" else "按柜号排序",
                                if (isHorizontalLayout) "切换为纵向地址" else "切换为横向地址",
                                if (showCompleted) "隐藏已取件的码" else "显示已取件的码",
                                if (showCodeTime) "隐藏时间" else "显示时间",
                                "添加自定义取件短信",
                                "地址归类",
                                "规则列表",
                                "导出规则",
                                "导入规则",
                                "查看日志",
                                "监听第三方app通知",
                                "淘宝身份码",
                                "拼多多身份码",
                                if (isSeniorMode) "关闭老人模式" else "开启老人模式",
                                "主题模式: ${ThemeManager.modeLabel(themeMode)}",
                                "设置背景图",
                                "清除背景图",
                                "关于",
                            )
                            menuItems.forEachIndexed { idx, text ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text,
                                            style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                            color = onCardColor
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        when (idx) {
                                            0 -> {
                                                val new = !sortByLocker
                                                saveSortByLocker(context, new)
                                                sortByLocker = new
                                                (context as MainActivity).readAndParseSms()
                                            }
                                            1 -> {
                                                val new = !isHorizontalLayout
                                                saveHorizontalLayout(context, new)
                                                isHorizontalLayout = new
                                            }
                                            2 -> {
                                                val new = !showCompleted
                                                saveShowCompleted(context, new)
                                                showCompleted = new
                                            }
                                            3 -> {
                                                val new = !showCodeTime
                                                saveShowCodeTime(context, new)
                                                showCodeTime = new
                                            }
                                            4 -> navController.navigate("add_custom_sms/ ")
                                            5 -> navController.navigate("address_group")
                                            6 -> navController.navigate("rules")
                                            7 -> exportRulesLauncher.launch("parcel_rules.json")
                                            8 -> importRulesLauncher.launch(arrayOf("application/json"))
                                            9 -> navController.navigate("logs")
                                            10 -> navController.navigate("use_notification")
                                            11 -> openTaobaoIdentityEntry(context)
                                            12 -> openPddIdentityEntry(context)
                                            13 -> onSeniorModeChanged(!isSeniorMode)
                                            14 -> {
                                                val next = (themeMode + 1) % 3
                                                onThemeModeChanged(next)
                                            }
                                            15 -> wallpaperPickerLauncher.launch(arrayOf("image/*"))
                                            16 -> onWallpaperChanged("")
                                            17 -> navController.navigate("about")
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (hasPermission) {
                ParcelList(
                    context = context,
                    viewModel = viewModel,
                    navController = navController,
                    updateAllWidget = updateAllWidget,
                    showCompleted = showCompleted,
                    showCodeTime = showCodeTime,
                    isHorizontalLayout = isHorizontalLayout,
                    sortByLocker = sortByLocker,
                    isSeniorMode = isSeniorMode
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_empty_package),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "需要短信权限才能读取取件码",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onCallBack() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f)),
                    ) {
                        Text("获取短信权限")
                    }
                }
            }
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = glassSheetColor(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "选择时间范围",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onCardColor,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    timeFilterOptions.forEachIndexed { index, option ->
                        val isSelected = index == selectedTimeFilterIndex
                        GlassSurface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    saveIndex(context, index)
                                    viewModel.setTimeFilterIndex(index)
                                    (context as MainActivity).readAndParseSms()
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            showBottomSheet = false
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else Color.Transparent,
                        ) {
                            Text(
                                text = option,
                                textAlign = TextAlign.Start,
                                style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else onCardColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        if (showWallpaperAdjust && tempWallpaperUri.isNotBlank()) {
            WallpaperAdjustSheet(
                wallpaperUri = tempWallpaperUri,
                initialSettings = BackgroundManager.getWallpaperSettings(context),
                onDismiss = {
                    showWallpaperAdjust = false
                    tempWallpaperUri = ""
                },
                onApply = { settings ->
                    showWallpaperAdjust = false
                    onWallpaperChanged(tempWallpaperUri)
                    tempWallpaperUri = ""
                }
            )
        }
    }
}

@Composable
private fun PickupCodeChip(
    code: String,
    isCompleted: Boolean,
    isSeniorMode: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
) {
    val codeColor = if (isDarkTheme) pickupCodeColorDark(code) else pickupCodeColor(code)
    val bgColor = if (isDarkTheme) pickupCodeBackgroundColorDark(code) else pickupCodeBackgroundColor(code)
    val animatedBgColor by animateColorAsState(
        targetValue = if (isCompleted) Color.White.copy(alpha = if (isDarkTheme) 0.06f else 0.15f) else bgColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bgColor"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isCompleted) Color.White.copy(alpha = 0.35f) else codeColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "textColor"
    )

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        color = animatedBgColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, codeColor.copy(alpha = if (isCompleted) 0.1f else 0.25f)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "已取件",
                    modifier = Modifier.size(if (isSeniorMode) 24.dp else 18.dp),
                    tint = Color.White.copy(alpha = 0.35f)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = formatPickupCode(code),
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = animatedTextColor,
                style = if (isSeniorMode) MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ) else MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
            )
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun AddressCard(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    updateAllWidget: () -> Unit,
    showCompleted: Boolean,
    showCodeTime: Boolean,
    parcelData: ParcelData,
    expandedStates: androidx.compose.runtime.MutableState<MutableMap<String, Boolean>>,
    isExpanded: Boolean,
    sortByLocker: Boolean,
    isSeniorMode: Boolean,
) {
    val isAllCompleted = parcelData.smsDataList.find { !it.isCompleted } == null
    val isDarkTheme = LocalIsDarkTheme.current
    val onCardColor = glassOnCardColor()
    val onCardVariantColor = glassOnCardVariantColor()
    var showTagDialog by remember { mutableStateOf(false) }
    var showSmsDetail by remember { mutableStateOf<String?>(null) }
    val addressMappings = remember { getAddressMappings(context) }
    val currentTag = addressMappings[parcelData.address] ?: ""

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        cornerRadius = 20.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    GlassSurface(
                        onClick = {
                            navController.navigate("add_custom_sms/${parcelData.address}")
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = if (isDarkTheme) 0.12f else 0.4f),
                        modifier = Modifier.size(if (isSeniorMode) 40.dp else 32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "添加自定义取件码",
                                tint = onCardColor,
                                modifier = Modifier.size(if (isSeniorMode) 24.dp else 18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                expandedStates.value = expandedStates.value.toMutableMap().apply {
                                    put(parcelData.address, !isExpanded)
                                }
                            }
                        ) {
                            Text(
                                text = parcelData.address,
                                style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = onCardColor,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            GlassSurface(
                                onClick = { showTagDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                color = if (currentTag.isNotBlank()) SuccessGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = if (isDarkTheme) 0.08f else 0.2f),
                                modifier = Modifier.size(if (isSeniorMode) 32.dp else 24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (currentTag.isNotBlank()) currentTag.take(1) else "标",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (currentTag.isNotBlank()) SuccessGreen else onCardVariantColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (!isAllCompleted) {
                            Text(
                                text = "${parcelData.num} 件待取",
                                style = MaterialTheme.typography.labelSmall,
                                color = SuccessGreen,
                            )
                        }
                    }
                }

                GlassSurface(
                    onClick = {
                        if (parcelData.num > 0) {
                            val smsList = parcelData.smsDataList
                                .filterNot { it.isCompleted }
                                .map { it.sms }
                            addCompletedIds(context, viewModel, smsList)
                            updateAllWidget()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = if (parcelData.num > 0) SuccessGreen.copy(alpha = 0.15f)
                    else Color.White.copy(alpha = if (isDarkTheme) 0.06f else 0.15f),
                    enabled = parcelData.num > 0,
                    modifier = Modifier.size(if (isSeniorMode) 44.dp else 36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "标记取件",
                            tint = if (parcelData.num > 0) SuccessGreen
                            else onCardVariantColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(if (isSeniorMode) 28.dp else 20.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = if (showCompleted) (isExpanded || !isAllCompleted) else (!isAllCompleted),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    parcelData.smsDataList.forEach { smsData ->
                        if (!(((!isExpanded) && smsData.isCompleted) || ((!showCompleted) && smsData.isCompleted))) {
                            if (smsData.rawBody.isNotEmpty()) {
                                var expanded by remember { mutableStateOf(false) }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { expanded = !expanded },
                                        color = Color.White.copy(alpha = if (isDarkTheme) 0.06f else 0.12f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(0.5.dp, onCardColor.copy(alpha = 0.15f)),
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            Text(
                                                text = if (expanded) smsData.rawBody else smsData.rawBody.take(80) + if (smsData.rawBody.length > 80) "…" else "",
                                                style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
                                                color = onCardColor.copy(alpha = 0.85f),
                                                maxLines = if (expanded) Int.MAX_VALUE else 3,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (!expanded && smsData.rawBody.length > 80) {
                                                Text(
                                                    text = "点击展开",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = onCardVariantColor.copy(alpha = 0.5f),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        if (smsData.lockerNumber.isNotEmpty()) {
                                            GlassChip {
                                                Text(
                                                    text = smsData.lockerNumber,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = if (isSeniorMode) MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ) else MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = onCardColor
                                                )
                                            }
                                        }
                                        if (showCodeTime) {
                                            val sdf = remember(isSeniorMode) {
                                                SimpleDateFormat(
                                                    if (isSeniorMode) "MM-dd" else "MM-dd HH:mm",
                                                    Locale.getDefault()
                                                )
                                            }
                                            Text(
                                                text = sdf.format(Date(smsData.sms.timestamp)),
                                                style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelSmall,
                                                color = onCardVariantColor.copy(alpha = 0.6f)
                                            )
                                        }
                                        Surface(
                                            onClick = { showSmsDetail = smsData.sms.body },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White.copy(alpha = if (isDarkTheme) 0.08f else 0.15f),
                                        ) {
                                            Text(
                                                text = "原文",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = onCardVariantColor.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PickupCodeChip(
                                        code = smsData.code,
                                        isCompleted = smsData.isCompleted,
                                        isSeniorMode = isSeniorMode,
                                        isDarkTheme = isDarkTheme,
                                        onClick = {
                                            if (smsData.isCompleted) {
                                                removeCompletedId(context, viewModel, smsData.sms)
                                            } else {
                                                addCompletedIds(context, viewModel, listOf(smsData.sms))
                                            }
                                            updateAllWidget()
                                        }
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        if (smsData.lockerNumber.isNotEmpty()) {
                                            GlassChip {
                                                Text(
                                                    text = smsData.lockerNumber,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = if (isSeniorMode) MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ) else MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = onCardColor
                                                )
                                            }
                                        }
                                        if (showCodeTime) {
                                            val sdf = remember(isSeniorMode) {
                                                SimpleDateFormat(
                                                    if (isSeniorMode) "MM-dd" else "MM-dd HH:mm",
                                                    Locale.getDefault()
                                                )
                                            }
                                            Text(
                                                text = sdf.format(Date(smsData.sms.timestamp)),
                                                style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelSmall,
                                                color = onCardVariantColor.copy(alpha = 0.6f)
                                            )
                                        }
                                        Surface(
                                            onClick = { showSmsDetail = smsData.sms.body },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White.copy(alpha = if (isDarkTheme) 0.08f else 0.15f),
                                        ) {
                                            Text(
                                                text = "原文",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = onCardVariantColor.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTagDialog) {
        TagDialog(
            currentAddress = parcelData.address,
            currentTag = currentTag,
            existingMappings = addressMappings,
            onDismiss = { showTagDialog = false },
            onConfirm = { tag ->
                saveAddressMapping(context, parcelData.address, tag)
                showTagDialog = false
                (context as? MainActivity)?.readAndParseSms()
            },
            onRemove = {
                showTagDialog = false
                (context as? MainActivity)?.readAndParseSms()
            }
        )
    }

    if (showSmsDetail != null) {
        Dialog(
            onDismissRequest = { showSmsDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "完整短信",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showSmsDetail = null }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "关闭",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = showSmsDetail ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalParcelList(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    updateAllWidget: () -> Unit,
    showCompleted: Boolean,
    showCodeTime: Boolean,
    parcelsData: List<ParcelData>,
    expandedStates: androidx.compose.runtime.MutableState<MutableMap<String, Boolean>>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    sortByLocker: Boolean,
    isSeniorMode: Boolean,
) {
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { parcelsData.size }
    )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(pagerState.currentPage)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            divider = {},
        ) {
            parcelsData.forEachIndexed { index, data ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        val isSelected = pagerState.currentPage == index
                        GlassSurface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color.White.copy(alpha = 0.25f)
                            else Color.Transparent,
                            borderAlpha = if (isSelected) 0.2f else 0f,
                        ) {
                            Text(
                                text = data.address,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = if (isSelected) Color.White
                                else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { page ->
            val parcel = parcelsData[page]
            val isExpanded = expandedStates.value[parcel.address] ?: true
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isSeniorMode) 12.dp else 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    AddressCard(
                        context = context,
                        viewModel = viewModel,
                        navController = navController,
                        updateAllWidget = updateAllWidget,
                        showCompleted = showCompleted,
                        showCodeTime = showCodeTime,
                        parcelData = parcel,
                        expandedStates = expandedStates,
                        isExpanded = isExpanded,
                        sortByLocker = sortByLocker,
                        isSeniorMode = isSeniorMode,
                    )
                }
            }
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ParcelList(
    context: Context,
    viewModel: ParcelViewModel,
    navController: NavController,
    updateAllWidget: () -> Unit,
    showCompleted: Boolean,
    showCodeTime: Boolean,
    isHorizontalLayout: Boolean = false,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    sortByLocker: Boolean,
    isSeniorMode: Boolean,
) {
    val parcelsData by viewModel.parcelsData.collectAsState()
    val filteredParcelsData = if (showCompleted) parcelsData else parcelsData.filter { parcel ->
        parcel.smsDataList.any { !it.isCompleted }
    }
    val expandedStates = remember { mutableStateOf(mutableMapOf<String, Boolean>()) }
    var currentTabIndex by remember { mutableStateOf(selectedTabIndex) }
    val timeFilterIndex by viewModel.timeFilterIndex.collectAsState()

    LaunchedEffect(timeFilterIndex) {
        currentTabIndex = 0
    }

    if (isHorizontalLayout && filteredParcelsData.isNotEmpty()) {
        HorizontalParcelList(
            context = context,
            viewModel = viewModel,
            navController = navController,
            updateAllWidget = updateAllWidget,
            showCompleted = showCompleted,
            showCodeTime = showCodeTime,
            parcelsData = filteredParcelsData,
            expandedStates = expandedStates,
            selectedTabIndex = currentTabIndex,
            onTabSelected = {
                currentTabIndex = it
                onTabSelected(it)
            },
            sortByLocker = sortByLocker,
            isSeniorMode = isSeniorMode,
        )
        return
    }

    if (filteredParcelsData.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_empty_package),
                contentDescription = null,
                modifier = Modifier.size(if (isSeniorMode) 120.dp else 80.dp),
                tint = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "暂无取件码",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate("add_custom_sms/ ") },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(if (isSeniorMode) 28.dp else 20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "添加自定义取件短信",
                    style = if (isSeniorMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "您可以手动添加取件短信或取件码",
                style = if (isSeniorMode) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = if (isSeniorMode) 12.dp else 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(filteredParcelsData, key = { it.address }) { result ->
                val isExpanded = expandedStates.value[result.address] ?: true
                AddressCard(
                    context = context,
                    viewModel = viewModel,
                    navController = navController,
                    updateAllWidget = updateAllWidget,
                    showCompleted = showCompleted,
                    showCodeTime = showCodeTime,
                    parcelData = result,
                    expandedStates = expandedStates,
                    isExpanded = isExpanded,
                    sortByLocker = sortByLocker,
                    isSeniorMode = isSeniorMode,
                )
            }
        }
    }
}

private fun openTaobaoIdentityEntry(context: Context) {
    val lastmile =
        "https://pages-fast.m.taobao.com/wow/z/uniapp/1100333/last-mile-fe/m-end-school-tab/home"
    val candidates = listOf(
        "tbopen://m.taobao.com/tbopen/index.html?h5Url=" + Uri.encode(lastmile),
    )
    for (u in candidates) {
        try {
            val i = Intent(Intent.ACTION_VIEW, u.toUri())
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            return
        } catch (_: Exception) {
        }
    }
    try {
        val i = Intent(Intent.ACTION_VIEW, lastmile.toUri())
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.setClassName("com.taobao.taobao", "com.taobao.browser.BrowserActivity")
        context.startActivity(i)
        return
    } catch (_: Exception) {
    }
}

private fun openPddIdentityEntry(context: Context) {
    val pkg = "com.xunmeng.pinduoduo"
    val schemes = listOf(
        "pinduoduo://com.xunmeng.pinduoduo/mdkd/package",
        "pinduoduo://com.xunmeng.pinduoduo/",
        "pinduoduo://"
    )
    for (u in schemes) {
        try {
            val i = Intent(Intent.ACTION_VIEW, u.toUri())
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            return
        } catch (_: Exception) {
        }
    }
    try {
        val i = context.packageManager.getLaunchIntentForPackage(pkg)
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
            return
        }
    } catch (_: Exception) {
    }
}

private fun saveShowCompleted(context: Context, show: Boolean) {
    try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("show_completed_codes", show) }
    } catch (_: Exception) {
    }
}

private fun getShowCompleted(context: Context): Boolean {
    return try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.getBoolean("show_completed_codes", true)
    } catch (_: Exception) {
        true
    }
}

private fun saveShowCodeTime(context: Context, show: Boolean) {
    try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("show_code_time", show) }
    } catch (_: Exception) {
    }
}

private fun getShowCodeTime(context: Context): Boolean {
    return try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.getBoolean("show_code_time", true)
    } catch (_: Exception) {
        true
    }
}

private fun saveHorizontalLayout(context: Context, horizontal: Boolean) {
    try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("horizontal_layout", horizontal) }
    } catch (_: Exception) {
    }
}

private fun getHorizontalLayout(context: Context): Boolean {
    return try {
        val prefs = context.getSharedPreferences("parcel_prefs", Context.MODE_PRIVATE)
        prefs.getBoolean("horizontal_layout", false)
    } catch (_: Exception) {
        false
    }
}
