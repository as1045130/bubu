# 📷 Android 图片浏览器 (Android Image Viewer)

一个基于 **Jetpack Compose** 和 **Material 3** 开发的现代化 Android 图片浏览应用。

## ✨ 功能特性

- 🖼️ **图片画廊** — 网格布局展示设备中的所有图片，支持按文件夹分类浏览
- 🔍 **全屏浏览** — 点击图片进入全屏模式，沉浸式观看体验
- 🤏 **手势缩放** — 支持双指捏合缩放（最高 5 倍）和拖拽平移
- 👆 **左右滑动** — 在全屏模式下左右滑动切换上一张/下一张图片
- 📂 **文件夹筛选** — 按文件夹（相册）筛选图片
- ℹ️ **图片详情** — 查看文件名、大小、尺寸、拍摄日期等详细信息
- 📤 **分享功能** — 将图片分享到其他应用
- 🗑️ **删除功能** — 直接在应用内删除不需要的图片
- 🌓 **深色模式** — 自动适配系统深色/浅色主题
- 🎨 **动态颜色** — Android 12+ 支持 Material You 动态取色

## 🛠️ 技术栈

| 技术 | 说明 |
|------|------|
| **Kotlin** | 100% Kotlin 编写 |
| **Jetpack Compose** | 声明式 UI 框架 |
| **Material 3** | Material Design 最新设计规范 |
| **Coil** | 高效的图片加载库 |
| **Navigation Compose** | 类型安全的导航 |
| **MediaStore API** | 访问设备媒体文件 |

## 📱 系统要求

- Android 7.0 (API 24) 及以上
- 存储权限（用于读取图片）

## 🚀 构建与运行

### 使用 Android Studio

1. 克隆仓库：
   ```bash
   git clone https://github.com/YOUR_USERNAME/AndroidImageViewer.git
   ```

2. 用 **Android Studio Hedgehog (2024.1.1)** 或更高版本打开项目

3. 等待 Gradle 同步完成

4. 连接 Android 设备或启动模拟器，点击 **Run**

### 使用命令行

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 安装到已连接的设备
./gradlew installDebug
```

## 📂 项目结构

```
AndroidImageViewer/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/imageviewer/
│   │   │   ├── MainActivity.kt          # 入口 Activity
│   │   │   ├── ui/
│   │   │   │   ├── ImageViewerApp.kt    # 导航图
│   │   │   │   ├── theme/               # 主题配置
│   │   │   │   ├── screens/
│   │   │   │   │   ├── GalleryScreen.kt # 图片画廊页
│   │   │   │   │   └── ViewerScreen.kt  # 全屏浏览页
│   │   │   │   └── components/
│   │   │   │       └── ImageCard.kt     # 图片卡片组件
│   │   │   └── util/
│   │   │       └── PermissionHelper.kt  # 权限工具
│   │   ├── res/                         # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts                     # 项目级构建配置
├── settings.gradle.kts
└── gradle.properties
```

## 📝 权限说明

- `READ_EXTERNAL_STORAGE` (Android 12 及以下) — 读取外部存储中的图片
- `READ_MEDIA_IMAGES` (Android 13+) — 读取媒体图片文件

## 📄 许可证

MIT License

---

*此项目作为 AI 编程实践任务开发*
