# newUIparcel

一个免费、开源、无广告、不联网的快递取件码管理工具，基于 Material 3 磨砂玻璃主题。

## ✨ 功能特性

- 🎨 **磨砂玻璃主题** — 半透明卡片 + 渐变背景，时尚美观
- 🖼️ **自定义背景图** — 支持本地图片，可调整模糊度、遮罩、位置
- 🌈 **取件码多色引擎** — 不同取件码自动分配不同颜色，高辨识度
- 📏 **自定义解析规则** — 支持地址规则 / 取件码正则 / 忽略关键词
- 📱 **桌面小组件** — 多种尺寸，锁屏/桌面随时查看
- 🔔 **第三方 App 通知监听** — 帮微信朋友取快递更方便
- 👴 **老人模式** — 大字体、高对比度，方便长辈使用
- 🌓 **暗色模式** — 自动跟随系统，可以手动切换
- 🆔 **淘宝 / 拼多多身份码** — 一键唤起
- 📦 **地址归类** — 合并相同地址的包裹

## 📥 下载

前往 [Releases](https://github.com/chenran0408/newUIparcel/releases) 页面下载最新 APK。

> 注意：安装前可能需要允许"安装未知来源应用"。

## ⚠️ 华为 / 鸿蒙用户必看

由于华为 / 鸿蒙系统的「应用管控」机制，可能会阻止本 App 读取短信。请按以下步骤手动解除：

1. 打开手机 **设置** → **安全** → **应用管控**
2. 在管控应用列表中找到本 App
3. 点击 App → **解除管控**

解除后即可正常读取短信。如仍无法读取，请使用「通知监听」功能作为替代方案。

## 🛠️ 构建

```bash
# 克隆仓库
git clone https://github.com/chenran0408/newUIparcel.git

# 使用 Android Studio 打开 parcel-main 目录
# 或用命令行构建
cd parcel-main
./gradlew assembleRelease
```

## 📋 技术栈

- Kotlin + Jetpack Compose
- Material 3 Design
- Coil 图片加载
- Room 数据库
- WorkManager 后台任务
- kotlinx-serialization

## 🔗 原项目

本项目基于 [shareven/parcel](https://github.com/shareven/parcel) 二次开发，感谢原作者的开源贡献。

## 📄 许可

沿用原项目 (https://github.com/shareven/parcel/blob/master/LICENSE) 许可证。
