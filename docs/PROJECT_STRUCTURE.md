# 香合源码结构说明

对应版本2.1.4。以下路径相对于工程根目录，不包含构建生成的文件。



下列路径均相对于 `工程根目录/`。Kotlin路径完整列出，所有其他当前项目文件也在后续表中列出；不包括本文自身。

### 4.1 正式Kotlin源文件

| 文件位置 | 作用 |
|---|---|
| `app/src/main/java/com/example/xiangshouzhengdao/App.kt` | 读取语言偏好并应用应用级Locale。 |
| `app/src/main/java/com/example/xiangshouzhengdao/data/local/AppDatabase.kt` | Room单例、7表注册、版本4和3到4迁移。 |
| `app/src/main/java/com/example/xiangshouzhengdao/data/local/Converters.kt` | 字符串列表、整数列表与JSON文本的Room转换。 |
| `app/src/main/java/com/example/xiangshouzhengdao/data/local/Dao.kt` | 用户、预置资料、记录、收藏与缓存的SQL访问接口。 |
| `app/src/main/java/com/example/xiangshouzhengdao/data/local/DataRepository.kt` | 账号操作、预置数据事务导入、用户记录、收藏和缓存统一访问。 |
| `app/src/main/java/com/example/xiangshouzhengdao/data/local/Entities.kt` | 7个持久化实体及字段约束。 |
| `app/src/main/java/com/example/xiangshouzhengdao/data/local/EntityExtensions.kt` | 实体和领域对象互转，编解码记录及配方JSON。 |
| `app/src/main/java/com/example/xiangshouzhengdao/data/model/DataModels.kt` | 药材、问题、分数、结果、配方与记录等领域模型和体质配置。 |
| `app/src/main/java/com/example/xiangshouzhengdao/engine/PrescriptionEngine.kt` | 融合节气与体质方、应用禁忌、分配整数占比和匹配产品。 |
| `app/src/main/java/com/example/xiangshouzhengdao/engine/ScoreEngine.kt` | 按题组计算转化分，判断和排序九型体质。 |
| `app/src/main/java/com/example/xiangshouzhengdao/engine/SurveyEngine.kt` | 答题位置、答案、前后导航、恢复和完成状态。 |
| `app/src/main/java/com/example/xiangshouzhengdao/i18n/Loc.kt` | 代码中生成的中英文展示文案和稳定键映射。 |
| `app/src/main/java/com/example/xiangshouzhengdao/notification/NotificationHelper.kt` | 通知渠道、权限检查与节气通知内容。 |
| `app/src/main/java/com/example/xiangshouzhengdao/notification/SolarTermScheduler.kt` | 安排、取消和立即触发节气检查。 |
| `app/src/main/java/com/example/xiangshouzhengdao/receiver/SolarTermReceiver.kt` | 响应节气检查、重启与更新广播，通知并安排后续检查。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/auth/AuthActivity.kt` | 本地登录、注册和游客入口。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/calendar/SolarCalendarActivity.kt` | 按年节气日历、当前/下一节气和日期倒计时；另定义SolarTermInfo。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/calendar/SolarTermCalendarAdapter.kt` | 日历条目的日期、三候与描述。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/custom/CustomPrescriptionActivity.kt` | 按体质和场景筛选材料、应用禁用规则，展示定制说明。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/herb/HerbAdapter.kt` | 药材卡片、后台图片请求及复用身份校验。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/herb/HerbDetailActivity.kt` | 药材概览、百科请求、失败兜底与重试。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/herb/HerbListActivity.kt` | 药材资产装载、分类搜索与列表。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/history/HistoryActivity.kt` | 当前用户历史列表、查看与删除管理。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/history/HistoryAdapter.kt` | 历史列表绑定及管理状态刷新。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/home/HomeFragment.kt` | 节气天气、六功能入口和最近测评。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/knowledge/BodyTypeDetailActivity.kt` | 按体质标识展示说明长文。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/knowledge/BodyTypeKnowledgeActivity.kt` | 体质知识与君臣佐使说明的导航入口。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/knowledge/BodyTypeListActivity.kt` | 九种体质入口卡片与详情参数。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/knowledge/JunChenZuoShiActivity.kt` | 君臣佐使科普页面。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/main/MainActivity.kt` | 三个底部页签及首页Fragment容器，通知授权。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/profile/AboutActivity.kt` | 应用版本和协议/隐私入口。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/profile/EditProfileActivity.kt` | 昵称、密码和头像修改，拍照/选择/保存图片。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/profile/MyPrescriptionsActivity.kt` | 当前用户收藏列表，内部定义FavoriteAdapter。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/profile/PrescriptionDetailActivity.kt` | 展示已收藏香方快照和取消收藏。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/profile/PrivacyPolicyActivity.kt` | 隐私政策资源展示。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/profile/ProfileFragment.kt` | 用户状态、头像、资料、收藏、档案、设置及退出入口。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/profile/SettingsActivity.kt` | 中英文切换及返回主页。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/profile/UserAgreementActivity.kt` | 用户协议资源展示。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/result/ResultActivity.kt` | 体质报告、雷达图、配方展示、记录收藏与报告图片保存。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/solar/SolarPrescriptionActivity.kt` | 当前节气方和全部24节气方列表。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/solar/SolarPrescriptionAdapter.kt` | 节气方列表显示与选中回调。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/solar/SolarPrescriptionDetailActivity.kt` | 节气主题、功效和君药详情。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/splash/SplashActivity.kt` | 系统启动屏接入、定位权限、初始化和登录路由。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/survey/SurveyActivity.kt` | 声明选择、题库装载、用户隔离进度、提交结果。 |
| `app/src/main/java/com/example/xiangshouzhengdao/ui/survey/SurveyFragment.kt` | 简版与完整版问卷入口。 |
| `app/src/main/java/com/example/xiangshouzhengdao/utils/SolarTermUtil.kt` | 固定月日节气表、按年日期、当前/下一节气与季节主题。 |
| `app/src/main/java/com/example/xiangshouzhengdao/utils/WeatherUtil.kt` | 后台城市反查、Open-Meteo天气请求及天气代码解析。 |

### 4.2 测试源码

这里只记录源码的作用，不要求其他AI重新执行测试。

| 文件位置 | 作用 |
|---|---|
| `app/src/androidTest/java/com/example/xiangshouzhengdao/ExampleInstrumentedTest.kt` | 设备侧包名检查模板；不是完整业务UI测试。 |
| `app/src/test/java/com/example/xiangshouzhengdao/ExampleUnitTest.kt` | 基础算术模板单元测试。 |
| `app/src/test/java/com/example/xiangshouzhengdao/SolarTermUtilTest.kt` | 节气日期排序、跨年、倒计时与闰年回归源码。 |
| `app/src/test/java/com/example/xiangshouzhengdao/engine/EngineRegressionTest.kt` | 真实题库与配方的计分、禁忌、比例及稳定性回归源码。 |

### 4.3 工程配置与入口注册

| 文件位置 | 作用 |
|---|---|
| `.gitignore` | 根版本管理忽略规则。 |
| `app/.gitignore` | 应用模块忽略规则。 |
| `app/build.gradle.kts` | 应用版本、SDK范围、ViewBinding、构建类型和依赖。 |
| `app/proguard-rules.pro` | 应用混淆规则文件，当前release未启用压缩混淆。 |
| `app/src/main/AndroidManifest.xml` | Application、23个Activity、Receiver、Provider和权限注册。 |
| `app/src/main/ic_launcher-playstore.png` | 商店图标源素材，不等同于当前自适应桌面图标。 |
| `build.gradle.kts` | 根工程插件与KSP版本声明。 |
| `gradle.properties` | Gradle JVM参数、Kotlin规范和KSP兼容设置。 |
| `gradle/gradle-daemon-jvm.properties` | Gradle守护进程Java工具链约定。 |
| `gradle/libs.versions.toml` | 集中管理库和插件版本及别名。 |
| `gradle/wrapper/gradle-wrapper.jar` | wrapper启动所需工具文件，属于工程配置，不是APP编译产物。 |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle分发版本、下载位置及wrapper配置。 |
| `gradlew` | 类Unix系统Gradle wrapper入口脚本。 |
| `gradlew.bat` | Windows Gradle wrapper入口脚本。 |
| `local.properties` | 本机SDK位置，目前指向D:/AndroidSDK；不是业务源码且迁移机器需调整。 |
| `settings.gradle.kts` | 项目名、依赖仓库、仅引入app模块。 |

### 4.4 预置数据文件

| 文件位置 | 作用 |
|---|---|
| `app/src/main/assets/herbs.json` | 中文材料库，当前61条；包含新增的鹅梨汁炼蜜复合材料。 |
| `app/src/main/assets/herbs_en.json` | 61条同标识材料的英文展示数据。 |
| `app/src/main/assets/body_prescriptions.json` | 9种体质基准方及角色比例、产品和禁忌说明。 |
| `app/src/main/assets/solar_prescriptions.json` | 24节气日期、君药、主题和功效。 |
| `app/src/main/assets/questions.json` | 中文完整版54题，按bodyType分组。 |
| `app/src/main/assets/questions_en.json` | 英文完整版54题，题号和体质元数据已对齐中文。 |
| `app/src/main/assets/questions_quick.json` | 中文简版12题。 |
| `app/src/main/assets/questions_quick_en.json` | 英文简版12题。 |

### 4.5 全部Android资源文件

布局和资源不是可忽略附件；它们决定页面结构、语言、图标与运行配置。

| 文件位置 | 作用 |
|---|---|
| `app/src/main/res/anim/nav_fade_in.xml` | 底部页签进入动画。 |
| `app/src/main/res/anim/nav_fade_out.xml` | 底部页签退出动画。 |
| `app/src/main/res/anim/slide_in_right.xml` | 新页面从右侧进入动画。 |
| `app/src/main/res/anim/slide_out_left.xml` | 旧页面向左退出动画。 |
| `app/src/main/res/drawable/bg_blob_green.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_blob_warm.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_body_type_badge.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_circle.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_glass.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_glass_dock.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_hero.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_hero_chip.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_hero_glow.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_icon_tile.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_logo.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_main_gradient.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_nav_icon.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_product_tag.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/bg_tag.xml` | 背景、形状或装饰资源，具体使用位置由布局引用决定。 |
| `app/src/main/res/drawable/empty_splash.xml` | 普通主题使用的空启动图可绘制资源。 |
| `app/src/main/res/drawable/ic_about.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_arrow_right.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_assessment.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_back.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_calendar.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_close.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_doc.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_download.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_edit.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_empty_herb.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_herb.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_history.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_home.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_knowledge.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_lock.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_notification.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_person.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_prescription.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_search.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_settings.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_solar.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_star.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/ic_test.xml` | 界面矢量图标，具体功能按资源名和布局引用识别。 |
| `app/src/main/res/drawable/launcher_foreground.xml` | 桌面图标前景安全区包装，引用完整logo并留白。 |
| `app/src/main/res/drawable/launcher_monochrome.xml` | 桌面主题单色图层的安全区包装。 |
| `app/src/main/res/drawable/progress_horizontal.xml` | 答题横向进度条。 |
| `app/src/main/res/drawable/ripple_soft.xml` | 轻量触摸波纹。 |
| `app/src/main/res/drawable/splash_brand_icon.xml` | 系统启动屏专用透明画布与居中缩小原图。 |
| `app/src/main/res/drawable/weather_cloudy.xml` | 天气状态图标，HomeFragment以明确资源ID选择。 |
| `app/src/main/res/drawable/weather_drizzle.xml` | 天气状态图标，HomeFragment以明确资源ID选择。 |
| `app/src/main/res/drawable/weather_fog.xml` | 天气状态图标，HomeFragment以明确资源ID选择。 |
| `app/src/main/res/drawable/weather_rain.xml` | 天气状态图标，HomeFragment以明确资源ID选择。 |
| `app/src/main/res/drawable/weather_sleet.xml` | 天气状态图标，HomeFragment以明确资源ID选择。 |
| `app/src/main/res/drawable/weather_snow.xml` | 天气状态图标，HomeFragment以明确资源ID选择。 |
| `app/src/main/res/drawable/weather_sunny.xml` | 天气状态图标，HomeFragment以明确资源ID选择。 |
| `app/src/main/res/drawable/weather_thunder.xml` | 天气状态图标，HomeFragment以明确资源ID选择。 |
| `app/src/main/res/drawable/weather_unknown.xml` | 天气状态图标，HomeFragment以明确资源ID选择。 |
| `app/src/main/res/drawable-nodpi/bg_avatar.png` | 默认头像位图。 |
| `app/src/main/res/drawable-nodpi/club_logo.png` | 合作标志。 |
| `app/src/main/res/drawable-nodpi/ic_launcher_fg.png` | 主题单色图层使用的前景位图素材。 |
| `app/src/main/res/drawable-nodpi/logo.png` | 完整品牌原图。 |
| `app/src/main/res/layout/activity_about.xml` | 关于与版本布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_auth.xml` | 登录注册布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_body_type_detail.xml` | 体质详情布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_body_type_knowledge.xml` | 体质知识入口布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_body_type_list.xml` | 九型体质列表布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_custom_prescription.xml` | 香方定制布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_edit_profile.xml` | 修改资料布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_herb_detail.xml` | 药材详情与百科布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_herb_list.xml` | 药材搜索分类列表布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_history.xml` | 测评历史布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_jun_chen_zuo_shi.xml` | 君臣佐使说明布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_main.xml` | 主容器与底部导航布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_my_prescriptions.xml` | 收藏香方列表布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_prescription_detail.xml` | 收藏香方快照详情布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_privacy_policy.xml` | 隐私政策布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_result.xml` | 结果雷达图与配方报告布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_settings.xml` | 语言设置布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_solar_calendar.xml` | 节气日历布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_solar_prescription.xml` | 节气方列表布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_solar_prescription_detail.xml` | 节气方详情布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_splash.xml` | 启动路由与授权等待页面布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_survey.xml` | 逐题作答布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/activity_user_agreement.xml` | 用户协议布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/choosingavatarmenu.xml` | 头像操作菜单布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/dialog_view_avatar.xml` | 头像大图与保存布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/fragment_home.xml` | 首页布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/fragment_profile.xml` | 个人中心布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/fragment_survey.xml` | 两种测评入口布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/item_favorite_prescription.xml` | 收藏列表条目布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/item_herb.xml` | 报告中的材料占比条目布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/item_herb_card.xml` | 药材资料卡片布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/item_history.xml` | 测评历史条目布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/item_solar_prescription.xml` | 节气方列表条目布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/layout/item_solar_term_calendar.xml` | 节气日历条目布局；对应ViewBinding由构建生成。 |
| `app/src/main/res/mipmap-anydpi/ic_launcher.xml` | 自适应默认桌面图标，组合背景、前景与单色图层。 |
| `app/src/main/res/mipmap-anydpi/ic_launcher_round.xml` | 自适应圆形桌面图标，组合背景、前景与单色图层。 |
| `app/src/main/res/values/colors.xml` | 应用颜色定义。 |
| `app/src/main/res/values/lint_strings_engine.xml` | 默认中文格式化展示资源，替代Kotlin中的直接文本拼接；不是独立业务模块。 |
| `app/src/main/res/values/lint_strings_platform.xml` | 默认中文格式化展示资源，替代Kotlin中的直接文本拼接；不是独立业务模块。 |
| `app/src/main/res/values/lint_strings_ui.xml` | 默认中文格式化展示资源，替代Kotlin中的直接文本拼接；不是独立业务模块。 |
| `app/src/main/res/values/startup_theme.xml` | 系统SplashScreen启动主题，指定专用图标和后续主题。 |
| `app/src/main/res/values/strings.xml` | 默认中文通用字符串、法律长文、界面与错误提示。 |
| `app/src/main/res/values/themes.xml` | 常规主题、控件样式与文字样式。 |
| `app/src/main/res/values-en/lint_strings_engine.xml` | 英文格式化展示资源，替代Kotlin中的直接文本拼接；不是独立业务模块。 |
| `app/src/main/res/values-en/lint_strings_platform.xml` | 英文格式化展示资源，替代Kotlin中的直接文本拼接；不是独立业务模块。 |
| `app/src/main/res/values-en/lint_strings_ui.xml` | 英文格式化展示资源，替代Kotlin中的直接文本拼接；不是独立业务模块。 |
| `app/src/main/res/values-en/strings.xml` | 英文通用字符串、法律长文、界面与错误提示。 |
| `app/src/main/res/values-v31/themes.xml` | Android12以上主题覆盖。 |
| `app/src/main/res/xml/backup_rules.xml` | 传统应用备份规则。 |
| `app/src/main/res/xml/data_extraction_rules.xml` | 新版数据提取和备份规则。 |
| `app/src/main/res/xml/file_paths.xml` | FileProvider允许共享的私有文件路径。 |

### 4.6 原始设计资料

| 文件位置 | 用途与边界 |
|---|---|
| `docs/design/A-传统药香标准化资料库.docx` | 原始需求参考，只能作为背景；与当前实现有差异时，技术文档描述当前软件并明确差异。 |
| `docs/design/A-香合 APP 九型中医体质测评问卷.docx` | 原始需求参考，只能作为背景；与当前实现有差异时，技术文档描述当前软件并明确差异。 |

