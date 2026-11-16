<img src="app/src/main/res/drawable-nodpi/logo.png" width="96" alt="香合图标" />

# 香合 · 香守正道

以九型体质问卷和二十四节气为依据的原生 Android 香方应用。问卷计分、配方生成、账号和个人档案均在设备本地处理；天气、药材图片和百科使用外部服务。

当前版本 **2.1.4（versionCode 5）**，最低支持 Android 8.0（API 26）。

## 下载

在 [Releases](https://github.com/Kurumiiau/Xiangshouzhengdao/releases) 下载安装包。版本说明会标注安装包的构建类型；源码归档由 GitHub 自动提供。

## 已实现功能

- **体质测评**：12 题简版、54 题完整版；按九种体质计算转化分、排序并展示雷达图。
- **节气配方**：结合当前节气、主体质、轻度体质和用户声明，合并材料、处理禁忌并输出合计 100% 的整数比例。
- **资料查询**：61 条材料数据、9 套体质基准方、24 条节气方；药材分类、搜索及知识说明。
- **个人档案**：本地注册登录、游客使用、答题进度恢复、测评历史、香方收藏、昵称密码和头像管理。
- **日常使用**：节气日历及提醒、当地天气、中英文切换、报告图片保存、场景材料筛选和产品搜索跳转。

本应用使用确定性规则生成结果，当前没有接入 AI 模型或自建后端。内容用于养生香文化参考，不替代临床诊疗。

## 技术与工程

| 项目 | 当前配置 |
| --- | --- |
| 语言与界面 | Kotlin、Activity / Fragment、XML、ViewBinding |
| 本地存储 | Room 2.7.1、SQLite schema v4、SharedPreferences、私有图片文件 |
| 配置 | minSdk 26、compileSdk 37、targetSdk 37 |
| 构建 | Gradle 9.4.1、AGP 9.2.1、KSP 2.1.20-1.0.32、JDK 21 |
| 主要库 | Material Components、AndroidX、Gson、MPAndroidChart、ExifInterface |
| 应用标识 | com.example.xiangshouzhengdao |

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/xiangshouzhengdao/
│   ├── App.kt
│   ├── data/          # 模型、实体、DAO、数据库和仓库
│   ├── engine/        # 答题导航、计分、香方生成
│   ├── i18n/          # 代码生成文案的中英文映射
│   ├── notification/  # 提醒渠道和调度
│   ├── receiver/      # 节气检查、重启及更新广播
│   ├── ui/            # 页面、Fragment和列表适配器
│   └── utils/         # 节气日期和天气请求
├── assets/            # 8份JSON资产
└── res/               # 布局、语言、样式、图标和动画
```

完整文件清单见 [源码结构说明](docs/PROJECT_STRUCTURE.md)。原始设计资料位于 [docs/design](docs/design)；其中未实现的设想不代表当前软件具备的功能。

## 本地构建

1. 安装 Android Studio、JDK 21 和 Android SDK Platform 37、Build Tools 36.0.0。
2. 在工程根目录创建不提交版本管理的 `local.properties`，填写本机 SDK 路径，例如：

   ```properties
   sdk.dir=D:/AndroidSDK
   ```

3. 执行 Gradle wrapper：

   ```powershell
   # Windows
   .\gradlew.bat assembleDebug
   ```

   ```bash
   # macOS / Linux
   chmod +x gradlew
   ./gradlew assembleDebug
   ```

Debug APK 默认位于 `app/build/outputs/apk/debug/app-debug.apk`。构建缓存、安装包、本机路径和签名密钥均不提交到源码仓库。

正式发布请自行配置保管 release 签名密钥；当前工程没有配置生产签名。`assembleRelease` 的输出不应被当作已签名的正式安装包。

## 检查命令

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
```

测试源文件包括引擎回归、节气日期测试及模板测试。引擎回归中含 648 组配方组合检查。文件位置为 `app/src/test` 和 `app/src/androidTest`；命令仅供开发使用，不代表每个 Release 都已做真机验收。

## 数据与接口边界

- 账号是设备本地账号，没有云同步、账号找回或跨设备登录服务。
- 测评历史保留原始答卷、声明和体质结果；重新打开报告会结合当前节气生成香方。收藏详情展示保存时的配方快照。
- 定制页按体质、场景和声明筛选材料，不生成测评引擎式的比例配方，也不保存定制结果。
- 节气采用固定月日表，未实现逐年天文历法校正和长夏独立配方窗口。
- 数据库提供 v3 → v4 迁移；未确认的更旧版本迁移不会通过删库兜底。
- 天气使用 Open-Meteo；百科和图片依赖第三方网络服务，失败时可能显示本地说明或占位图。产品标签会打开淘宝搜索，不含应用内交易功能。

## 版本记录与许可

更新内容见 [CHANGELOG.md](CHANGELOG.md)。本仓库暂未指定开源许可证；第三方依赖的许可由各依赖项目分别提供。
