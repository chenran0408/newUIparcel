package com.chenran.parcel.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.chenran.parcel.ui.theme.glassBaseColor
import com.chenran.parcel.ui.theme.glassOnCardColor

fun getAppVersionName(context: Context): String {
    try {
        val packageManager = context.packageManager
        val packageName = context.packageName
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        return ("版本：" + packageInfo.versionName)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return "未知版本"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val context = LocalContext.current
    val newUrl = "https://github.com/chenran0408/newUIparcel"
    val originalUrl = "https://github.com/shareven/parcel"

    val cardColor = glassBaseColor().copy(alpha = 0.65f)
    val onCardColor = glassOnCardColor()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("关于", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = Color.White)
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "newUIparcel",
                style = MaterialTheme.typography.headlineLarge,
                color = onCardColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "本版开源地址",
                        style = MaterialTheme.typography.titleMedium,
                        color = onCardColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, newUrl.toUri())
                            context.startActivity(intent)
                        }
                    ) {
                        Text(newUrl, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "原版开源地址",
                        style = MaterialTheme.typography.titleMedium,
                        color = onCardColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, originalUrl.toUri())
                            context.startActivity(intent)
                        }
                    ) {
                        Text(originalUrl, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                getAppVersionName(context),
                style = MaterialTheme.typography.bodyLarge,
                color = onCardColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "这是一个免费、开源、无广告、不联网，追求简洁的app，不收集任何个人信息。\n\n本app会自动解析收到的短信，并从中提取出地址和取件码信息，可以展示到桌面卡片上（支持暗色模式）。\n\n您可以添加自定义规则来改进解析效果。\n\n还支持监听第三方app通知，自动保存取件码消息，帮微信朋友取快递更方便了。\n\n打开监听通知权限，还能实现后台进程保活，实时更新桌面卡片。\n\n桌面卡片添加：一般是藏在全部卡片-最底部的插件或者安卓小组件里面\n\n欢迎下载和使用！有问题或建议请提issue！",
                style = MaterialTheme.typography.bodyLarge,
                color = onCardColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
