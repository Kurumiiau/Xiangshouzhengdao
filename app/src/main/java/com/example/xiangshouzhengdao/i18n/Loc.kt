package com.example.xiangshouzhengdao.i18n

object Loc {

    @Volatile
    var lang: String = "zh"

    // 这里只翻译展示文案，体质ID和节气键仍用原值，别拿译文去查库。
    fun pick(en: String, zh: String): String = if (lang == "en") en else zh

    fun bodyShort(id: String): String = when (id) {
        "pinghe" -> pick("Balanced", "平和")
        "qixu" -> pick("Qi-Deficient", "气虚")
        "yangxu" -> pick("Yang-Deficient", "阳虚")
        "yinxu" -> pick("Yin-Deficient", "阴虚")
        "tanshi" -> pick("Phlegm-Damp", "痰湿")
        "shire" -> pick("Damp-Heat", "湿热")
        "xueyu" -> pick("Blood-Stasis", "血瘀")
        "qiyu" -> pick("Qi-Stagnation", "气郁")
        "tebing" -> pick("Allergic", "特禀")
        else -> bodyName(id)
    }

    fun bodyName(id: String): String = when (id) {
        "pinghe" -> pick("Balanced Constitution", "平和质")
        "qixu" -> pick("Qi-Deficient Constitution", "气虚质")
        "yangxu" -> pick("Yang-Deficient Constitution", "阳虚质")
        "yinxu" -> pick("Yin-Deficient Constitution", "阴虚质")
        "tanshi" -> pick("Phlegm-Dampness Constitution", "痰湿质")
        "shire" -> pick("Damp-Heat Constitution", "湿热质")
        "xueyu" -> pick("Blood-Stasis Constitution", "血瘀质")
        "qiyu" -> pick("Qi-Stagnation Constitution", "气郁质")
        "tebing" -> pick("Allergic (Special) Constitution", "特禀质")
        else -> id
    }

    fun bodyDesc(id: String): String = when (id) {
        "qixu" -> pick("fatigue, sweating on exertion, catching colds easily", "易疲惫、一动出汗、换季易感冒")
        "yangxu" -> pick("cold hands and feet, aversion to cold, diarrhea", "常年手脚冰凉、畏寒腹泻")
        "yinxu" -> pick("hot palms, dry mouth, night sweats, insomnia", "手心发热、口干盗汗、失眠烦躁")
        "tanshi" -> pick("heaviness, phlegm, sticky stools", "身体沉重、痰多、大便黏腻")
        "shire" -> pick("acne, bitter taste, itchy skin, prone to heat", "长痘口苦、皮肤瘙痒、易上火")
        "xueyu" -> pick("dull complexion, stabbing pains, menstrual clotting", "面色暗沉、身体刺痛、经期淤堵")
        "qiyu" -> pick("anxiety, difficulty falling asleep, chest tightness", "焦虑压抑、入睡困难、胸闷叹气")
        "tebing" -> pick("seasonal rhinitis, pollen and skin allergies", "换季鼻炎、花粉皮肤过敏")
        else -> ""
    }

    fun bodyAdvice(id: String): String = when (id) {
        "qixu" -> pick("boost qi and lift yang, guard against external pathogens", "补气升阳、固表防外感")
        "yangxu" -> pick("warm the spleen and kidneys, dispel cold", "温脾肾、散寒暖身")
        "yinxu" -> pick("nourish yin and subdue fire, calm the heart and aid sleep", "滋阴降火、清心助眠")
        "tanshi" -> pick("strengthen the spleen, resolve dampness and phlegm", "健脾祛湿、化解痰湿")
        "shire" -> pick("clear heat and dry dampness, soothe stagnation", "清热燥湿、疏解郁热")
        "xueyu" -> pick("move qi and invigorate blood, disperse stasis", "行气活血、散瘀淡斑")
        "qiyu" -> pick("soothe the liver and relieve depression, ease anxiety", "疏肝解郁、舒缓焦虑")
        "tebing" -> pick("open the nasal passages, guard against allergens", "宣通鼻窍、抵御过敏")
        else -> ""
    }

    fun roleName(role: String): String = when (role) {
        "君" -> pick("Monarch", "君")
        "臣" -> pick("Minister", "臣")
        "佐" -> pick("Assistant", "佐")
        "使" -> pick("Guide", "使")
        else -> role
    }

    fun solarTerm(name: String): String = when (name) {
        "立春" -> pick("Start of Spring", "立春")
        "雨水" -> pick("Rain Water", "雨水")
        "惊蛰" -> pick("Awakening of Insects", "惊蛰")
        "春分" -> pick("Spring Equinox", "春分")
        "清明" -> pick("Pure Brightness", "清明")
        "谷雨" -> pick("Grain Rain", "谷雨")
        "立夏" -> pick("Start of Summer", "立夏")
        "小满" -> pick("Grain Buds", "小满")
        "芒种" -> pick("Grain in Ear", "芒种")
        "夏至" -> pick("Summer Solstice", "夏至")
        "小暑" -> pick("Minor Heat", "小暑")
        "大暑" -> pick("Major Heat", "大暑")
        "立秋" -> pick("Start of Autumn", "立秋")
        "处暑" -> pick("End of Heat", "处暑")
        "白露" -> pick("White Dew", "白露")
        "秋分" -> pick("Autumn Equinox", "秋分")
        "寒露" -> pick("Cold Dew", "寒露")
        "霜降" -> pick("Frost's Descent", "霜降")
        "立冬" -> pick("Start of Winter", "立冬")
        "小雪" -> pick("Minor Snow", "小雪")
        "大雪" -> pick("Major Snow", "大雪")
        "冬至" -> pick("Winter Solstice", "冬至")
        "小寒" -> pick("Minor Cold", "小寒")
        "大寒" -> pick("Major Cold", "大寒")
        else -> name
    }

    fun seasonTheme(key: String): String = when (key) {
        "春疏肝" -> pick("Spring — Soothe the Liver", "春疏肝")
        "夏清心" -> pick("Summer — Clear the Heart", "夏清心")
        "秋润肺" -> pick("Autumn — Moisten the Lungs", "秋润肺")
        "冬温肾" -> pick("Winter — Warm the Kidneys", "冬温肾")
        else -> pick("Season-round Harmony", "四季调和")
    }

    fun heroHint(key: String): String = when (key) {
        "春疏肝" -> pick("Spring qi rises — a time to soothe the liver and smooth the qi", "春气生发,疏肝理气正当时")
        "夏清心" -> pick("Summer heat prevails — a time to clear the heart and cool the fire", "夏热养心,清心降火正当时")
        "秋润肺" -> pick("Autumn dryness sets in — a time to moisten the lungs and nourish yin", "秋燥当令,润肺养阴正当时")
        "冬温肾" -> pick("Winter cold seals the season — a time to warm the kidneys and guard yang", "冬寒封藏,温阳固本正当时")
        else -> pick("Follow the seasons, harmonize the self, and blend a fine incense", "顺天时,调人和,制一炉好香")
    }

    fun solarTheme(theme: String): String = when (theme) {
        "疏肝防疫" -> pick("Soothe Liver, Ward Off Epidemics", "疏肝防疫")
        "疏肝健脾" -> pick("Soothe Liver, Strengthen Spleen", "疏肝健脾")
        "疏肝理气" -> pick("Soothe Liver, Regulate Qi", "疏肝理气")
        "疏肝解郁" -> pick("Soothe Liver, Relieve Stagnation", "疏肝解郁")
        "清肝明目" -> pick("Clear Liver, Brighten Eyes", "清肝明目")
        "化湿疏肝" -> pick("Resolve Dampness, Soothe Liver", "化湿疏肝")
        "清心火" -> pick("Clear Heart Fire", "清心火")
        "清热解暑" -> pick("Clear Heat, Relieve Summer Heat", "清热解暑")
        "化湿解暑" -> pick("Resolve Dampness, Relieve Heat", "化湿解暑")
        "清暑安神" -> pick("Clear Heat, Calm the Mind", "清暑安神")
        "化暑湿" -> pick("Resolve Summer Dampness", "化暑湿")
        "清内热" -> pick("Clear Internal Heat", "清内热")
        "润肺清燥" -> pick("Moisten Lungs, Clear Dryness", "润肺清燥")
        "滋阴润燥" -> pick("Nourish Yin, Moisten Dryness", "滋阴润燥")
        "润肺养阴" -> pick("Moisten Lungs, Nourish Yin", "润肺养阴")
        "平肝明目" -> pick("Pacify Liver, Brighten Eyes", "平肝明目")
        "润燥祛风" -> pick("Moisten Dryness, Dispel Wind", "润燥祛风")
        "祛风清头" -> pick("Dispel Wind, Clear the Head", "祛风清头")
        "温肾纳气" -> pick("Warm Kidneys, Grasp Qi", "温肾纳气")
        "温阳散寒" -> pick("Warm Yang, Disperse Cold", "温阳散寒")
        "温中散寒" -> pick("Warm the Middle, Disperse Cold", "温中散寒")
        "温补元阳" -> pick("Warm and Tonify Primordial Yang", "温补元阳")
        "温肾助阳" -> pick("Warm Kidneys, Assist Yang", "温肾助阳")
        "温阳避寒" -> pick("Warm Yang, Ward Off Cold", "温阳避寒")
        else -> theme
    }

    fun solarEffect(term: String): String = when (term) {
        "立春" -> pick("Soothe the liver, regulate qi, ward off early-spring colds", "疏肝理气、抵御春寒流感")
        "雨水" -> pick("Soothe the liver, relieve stagnation, strengthen the spleen and resolve dampness", "疏肝解郁、健脾化湿")
        "惊蛰" -> pick("Soothe the liver and regulate qi, dispel spring drowsiness", "疏肝理气、化解春困")
        "春分" -> pick("Soothe the liver and relieve stagnation, ease spring restlessness", "疏肝解郁、舒缓春困烦躁")
        "清明" -> pick("Clear the liver and brighten the eyes, dispel wind and heat", "清肝明目、疏风散热")
        "谷雨" -> pick("Resolve late-spring dampness, soothe the liver and strengthen the spleen", "化解暮春湿浊、疏肝健脾")
        "立夏" -> pick("Clear heart fire, ease summer restlessness and insomnia", "清心火、缓解夏季心烦失眠")
        "小满" -> pick("Clear heat and relieve summer heat, dispel wind and heat", "清热解暑、疏风散热")
        "芒种" -> pick("Resolve dampness aromatically, relieve heat and vexation", "芳香化湿、解暑除烦")
        "夏至" -> pick("Clear summer damp-heat, calm the mind and relieve heat", "清暑湿热、安神解暑")
        "小暑" -> pick("Resolve summer dampness, relieve bodily stickiness", "化解暑湿、去除身体黏腻")
        "大暑" -> pick("Clear internal heat, ease summer night sweats and vexation", "清内热、改善暑天盗汗心烦")
        "立秋" -> pick("Moisten the lungs and clear dryness, ease autumn dry cough", "润肺清燥、缓解秋燥干咳")
        "处暑" -> pick("Nourish yin and moisten dryness, ease post-summer residual heat", "滋阴润燥、缓解暑后余热")
        "白露" -> pick("Moisten the lungs and nourish yin, ease autumn dryness", "润肺养阴、缓解秋燥")
        "秋分" -> pick("Pacify the liver and brighten the eyes, dispel wind and clear heat", "平肝明目、疏风清热")
        "寒露" -> pick("Moisten dryness and dispel wind, ease autumn nasal congestion", "润燥祛风、缓解秋凉鼻塞")
        "霜降" -> pick("Dispel wind and clear the head, ease autumn headaches", "祛风清头、改善秋燥头痛")
        "立冬" -> pick("Warm the kidneys and grasp qi, calm the mind in winter", "温肾纳气、冬日安神")
        "小雪" -> pick("Warm yang and disperse cold, ward off early cold", "温阳散寒、抵御初寒")
        "大雪" -> pick("Warm the middle and disperse cold, strengthen resistance to cold", "温中散寒、增强御寒")
        "冬至" -> pick("Warm and tonify primordial yang, ward off winter chills", "温补元阳、抵御寒冬畏寒")
        "小寒" -> pick("Warm the kidneys and assist yang, strengthen cold resistance", "温肾助阳、增强抗寒")
        "大寒" -> pick("Warm yang and ward off cold, guard against seasonal epidemics", "温阳避寒、冬春交替防疫")
        else -> pick("Season-round harmony, follow the solar terms to adjust your blend", "四季调和,顺时养生")
    }

    fun product(name: String): String = when (name) {
        "睡眠香包" -> pick("Sleep Sachet", "睡眠香包")
        "卧室香薰" -> pick("Bedroom Aromatherapy", "卧室香薰")
        "枕边香包" -> pick("Bedside Sachet", "枕边香包")
        "卧室睡眠香薰" -> pick("Bedroom Sleep Aromatherapy", "卧室睡眠香薰")
        "热敷香包" -> pick("Heat-Pack Sachet", "热敷香包")
        "车载香薰" -> pick("Car Aromatherapy", "车载香薰")
        "随身香珠" -> pick("Pocket Beads", "随身香珠")
        "室内插香" -> pick("Indoor Incense Sticks", "室内插香")
        "居家盘香" -> pick("Home Coil Incense", "居家盘香")
        "浴室香氛" -> pick("Bathroom Fragrance", "浴室香氛")
        "办公桌面香插" -> pick("Desk Incense Holder", "办公桌面香插")
        "随身香囊" -> pick("Pocket Sachet", "随身香囊")
        "桌面无火香插" -> pick("Flameless Desk Holder", "桌面无火香插")
        "随身清润香囊" -> pick("Refreshing Pocket Sachet", "随身清润香囊")
        "随身佩戴香珠" -> pick("Wearable Beads", "随身佩戴香珠")
        "睡前香包" -> pick("Bedtime Sachet", "睡前香包")
        "睡眠热敷包" -> pick("Sleep Heat Pack", "睡眠热敷包")
        "办公舒缓香薰" -> pick("Calming Office Aromatherapy", "办公舒缓香薰")
        "随身防疫香珠" -> pick("Protective Pocket Beads", "随身防疫香珠")
        "室内防敏香插" -> pick("Anti-Allergy Incense Holder", "室内防敏香插")
        "防疫香珠" -> pick("Protective Beads", "防疫香珠")
        "防敏香插" -> pick("Anti-Allergy Holder", "防敏香插")
        "办公香插" -> pick("Office Incense Holder", "办公香插")
        "无火香插" -> pick("Flameless Holder", "无火香插")
        "佩戴香珠" -> pick("Wearable Beads", "佩戴香珠")
        "四季随身香囊" -> pick("Season-round Pocket Sachet", "四季随身香囊")
        "居家线香" -> pick("Home Incense Sticks", "居家线香")
        else -> name
    }

    fun constraint(label: String): String = when (label) {
        "血瘀质/孕妇" -> pick("Blood-Stasis / Pregnancy", "血瘀质/孕妇")
        "血瘀质、孕妇" -> pick("Blood-Stasis, Pregnant", "血瘀质、孕妇")
        "阴虚火旺" -> pick("Yin Deficiency with Fire", "阴虚火旺")
        "湿热质" -> pick("Damp-Heat", "湿热质")
        "特禀重度过敏" -> pick("Severe Allergies", "特禀重度过敏")
        else -> label
    }

    fun pureTitle() = pick("Balanced Constitution", "平和体质")
    fun pureDesc() = pick("Your constitution is balanced and harmonious, with no notable deviation.", "您体质均衡平和,阴阳调和,无明显偏颇。")
    fun pureRec() = pick("A season-round harmonizing blend for daily care and home serenity.", "四季通用调和香,日常养护、居家安神。")
    fun pureTroubles() = pick("Well-balanced, no notable concerns", "体质平和,无明显困扰")

    fun singleDesc(name: String, desc: String) = pick("Your core constitution is $name, marked by $desc.", "您核心体质为${name},存在${desc}类身体困扰。")
    fun singleRec(name: String, advice: String) = pick("Prioritize the $name blend — $advice.", "优先匹配${name}专属药香,${advice}")

    fun compositeTitle(names: String) = pick("Combined Constitution: $names", "复合体质:$names")
    fun compositeDesc(names: String) = pick("You have a combined constitution of $names; the blend has been rebalanced by your scores.", "您属于复合偏颇体质:$names,AI已根据分值自动调整君臣药材配比。")
    fun compositeRec() = pick("A dual-effect blend addressing both needs.", "双效合香,兼顾双重调理需求。")

    fun mildTitle() = pick("Mild Imbalance", "轻度偏颇体质")
    fun mildDesc() = pick("Your constitution leans slightly off balance; mindful daily care is suggested.", "体质略有偏颇,建议日常调理预防。")
    fun mildRec() = pick("Periodic reassessment and mindful daily care are recommended.", "建议定期复测,日常注意养生调理。")
    fun mildTroubles(desc: String?) = desc ?: pick("A mild imbalance; mindful daily care is suggested", "体质略有偏颇,建议日常调理预防")

    fun noTrendTitle() = pick("No Clear Tendency", "体质倾向不明显")
    fun noTrendDesc() = pick("No notable imbalance was found; keep up your current lifestyle.", "本次测评未发现明显偏颇倾向,保持现有生活方式即可。")
    fun noTrendRec() = pick("A season-round harmonizing blend for daily care.", "四季通用调和香,日常养护。")
    fun noTrendTroubles() = pick("No notable concerns", "无明显困扰")

    fun totalEffect(solarThemeKey: String, names: String, rec: String): String =
        pick("In the season of $solarThemeKey, nurture your $names — $rec", "顺时$solarThemeKey,调理$names,$rec")
    fun totalEffectPeace(solarThemeKey: String): String =
        pick("In the season of $solarThemeKey, daily care to harmonize yin and yang", "顺时$solarThemeKey,日常养护,调和阴阳")

    fun contraRemoved(list: String) = pick("Removed: $list", "已禁用:$list")
    fun contraReduced(list: String) = pick("Reduced: $list", "已减量:$list")
    fun contraHeader() = pick("Formula adjusted for your condition: ", "已根据您的身体状况调整配方:")

    fun contraTitle(forbidden: String, reduced: String): String =
        pick("Formula adjusted for your condition: removed $forbidden; reduced $reduced.", "已根据您的身体状况调整配方:禁用$forbidden;减量$reduced。")
    fun contraReasonForbidden(c: String, list: String) = pick("$c: $list removed", "${c}:${list}禁用")
    fun contraReasonReduced(c: String, list: String) = pick("$c: $list reduced", "${c}:${list}减量")

    fun quickBadge() = pick("Quick", "简版")
    fun fullBadge() = pick("Full", "专业版")
    fun datePattern() = pick("EEEE, MMM d", "M月d日 EEEE")
    fun monthPattern() = pick("MMM", "M月")
    fun recordTag() = pick("Assessment", "测评")
    fun signatureBlend() = pick("Signature Blend", "专属香方")
    fun savedOn() = pick("Saved on", "收藏于")
    fun savedTime() = pick("Saved at", "收藏时间")

    fun dateLocale(): java.util.Locale = if (lang == "en") java.util.Locale.ENGLISH else java.util.Locale.SIMPLIFIED_CHINESE

    fun bodyBrief(id: String): String = when (id) {
        "pinghe" -> pick("Harmonious yin-yang and qi-blood; balanced build, rosy complexion, ample energy", "阴阳气血调和,体态适中,面色红润,精力充沛")
        "qixu" -> pick("Deficient primal qi; prone to fatigue, shortness of breath, and sweating", "元气不足,易疲乏,气短懒言,易出汗")
        "yangxu" -> pick("Deficient yang; aversion to cold, cold hands and feet", "阳气不足,畏寒怕冷,手足不温")
        "yinxu" -> pick("Depleted yin; dry mouth and throat, warm palms and soles", "阴液亏少,口燥咽干,手足心热")
        "tanshi" -> pick("Accumulated phlegm-dampness; plump build, full abdomen", "痰湿凝聚,形体肥胖,腹部肥满")
        "shire" -> pick("Lingering damp-heat; oily skin, prone to acne", "湿热内蕴,面垢油光,易生痤疮")
        "xueyu" -> pick("Sluggish blood; dull skin, purplish tongue", "血行不畅,肤色晦暗,舌质紫黯")
        "qiyu" -> pick("Stagnant qi; low mood, prone to worry", "气机郁滞,神情抑郁,忧虑脆弱")
        "tebing" -> pick("Congenital tendency; physiological sensitivity, allergies", "先天失常,生理缺陷,过敏反应")
        else -> id
    }

    fun herbCategory(name: String): String = when (name) {
        "木质定香类" -> pick("Woody Fixatives", "木质定香类")
        "温阳散寒类" -> pick("Warming Yang, Dispelling Cold", "温阳散寒类")
        "健脾化湿类" -> pick("Strengthening Spleen, Resolving Dampness", "健脾化湿类")
        "疏肝安神类" -> pick("Soothing Liver, Calming Mind", "疏肝安神类")
        "清润滋阴类" -> pick("Clearing Heat, Nourishing Yin", "清润滋阴类")
        "通鼻抗过敏类" -> pick("Opening Nasal Passages, Anti-Allergy", "通鼻抗过敏类")
        "活血通络类" -> pick("Moving Blood, Freeing Meridians", "活血通络类")
        "补气固本类" -> pick("Tonifying Qi, Securing the Root", "补气固本类")
        "清热燥湿类" -> pick("Clearing Heat, Drying Dampness", "清热燥湿类")
        "定香粘合剂" -> pick("Fixative Binders", "定香粘合剂")
        "理气开郁类" -> pick("Regulating Qi, Opening Stagnation", "理气开郁类")
        "滋阴降火类" -> pick("Nourishing Yin, Subduing Fire", "滋阴降火类")
        "祛风散寒类" -> pick("Dispelling Wind and Cold", "祛风散寒类")
        "开窍醒神类" -> pick("Opening Orifices, Awakening Mind", "开窍醒神类")
        "祛风解表类" -> pick("Dispelling Wind, Releasing Exterior", "祛风解表类")
        else -> name
    }

    fun solarThreePeriods(term: String): String = when (term) {
        "立春" -> pick("East wind thaws the ice; hibernating insects stir; fish rise beneath the ice", "东风解冻,蛰虫始振,鱼上冰")
        "雨水" -> pick("Otters sacrifice fish; wild geese return; plants begin to sprout", "獭祭鱼,鸿雁来,草木萌动")
        "惊蛰" -> pick("Peach blossoms open; orioles sing; hawks turn into doves", "桃始华,仓庚鸣,鹰化为鸠")
        "春分" -> pick("Swallows arrive; thunder sounds; lightning begins", "玄鸟至,雷乃发声,始电")
        "清明" -> pick("Paulownia blooms; field mice turn into quails; rainbows appear", "桐始华,田鼠化为鴽,虹始见")
        "谷雨" -> pick("Duckweed sprouts; cuckoos preen; hoopoes alight on mulberries", "萍始生,鸣鸠拂其羽,戴胜降于桑")
        "立夏" -> pick("Crickets chirp; earthworms emerge; royal melons sprout", "蝼蝈鸣,蚯蚓出,王瓜生")
        "小满" -> pick("Sow thistles flourish; tender grasses wither; wheat ripens", "苦菜秀,靡草死,麦秋至")
        "芒种" -> pick("Mantises hatch; shrikes sing; mockingbirds fall silent", "螳螂生,鵙始鸣,反舌无声")
        "夏至" -> pick("Deer shed antlers; cicadas sing; pinellia grows", "鹿角解,蜩始鸣,半夏生")
        "小暑" -> pick("Warm winds arrive; crickets shelter in walls; hawks learn to hunt", "温风至,蟋蟀居壁,鹰始挚")
        "大暑" -> pick("Rotting grass breeds fireflies; the soil steams; heavy rains fall", "腐草为萤,土润溽暑,大雨时行")
        "立秋" -> pick("Cool winds arrive; white dew forms; cold cicadas sing", "凉风至,白露生,寒蝉鸣")
        "处暑" -> pick("Hawks sacrifice birds; heaven and earth turn stern; grain ripens", "鹰乃祭鸟,天地始肃,禾乃登")
        "白露" -> pick("Wild geese come; swallows return; birds store winter fare", "鸿雁来,玄鸟归,群鸟养羞")
        "秋分" -> pick("Thunder falls silent; insects seal their burrows; waters begin to dry", "雷始收声,蛰虫坯户,水始涸")
        "寒露" -> pick("Geese arrive as guests; sparrows become clams in the deep; chrysanthemums yellow", "鸿雁来宾,雀入大水为蛤,菊有黄华")
        "霜降" -> pick("Wolves sacrifice their prey; leaves yellow and fall; insects bow down", "豺乃祭兽,草木黄落,蛰虫咸俯")
        "立冬" -> pick("Waters begin to freeze; the earth hardens; pheasants enter the deep as mirages", "水始冰,地始冻,雉入大水为蜃")
        "小雪" -> pick("Rainbows hide; yang ascends and yin descends; winter seals in", "虹藏不见,天气上升,闭塞而成冬")
        "大雪" -> pick("The ice bird stops singing; tigers begin to mate; iris sprouts", "鹖鴠不鸣,虎始交,荔挺出")
        "冬至" -> pick("Earthworms curl; deer shed antlers; springs stir", "蚯蚓结,麋角解,水泉动")
        "小寒" -> pick("Geese head north; magpies build nests; pheasants call", "雁北乡,鹊始巢,雉始雊")
        "大寒" -> pick("Hens hatch; raptors hunt fiercely; lakes freeze solid", "鸡乳,征鸟厉疾,水泽腹坚")
        else -> term
    }

    fun solarDesc(term: String): String = when (term) {
        "立春" -> pick("Spring begins; all things revive", "春季开始,万物复苏")
        "雨水" -> pick("Rains begin and gradually increase", "降雨开始,雨量渐增")
        "惊蛰" -> pick("Spring thunder wakes the hibernating insects", "春雷乍动,惊醒蛰虫")
        "春分" -> pick("Day and night are equal; mid-spring", "昼夜平分,春季中点")
        "清明" -> pick("Clear skies; grass and trees flourish", "天气晴朗,草木繁茂")
        "谷雨" -> pick("Rain nourishes the crops; spring draws to a close", "雨生百谷,春季结束")
        "立夏" -> pick("Summer begins; crops grow", "夏季开始,作物生长")
        "小满" -> pick("Grains swell but are not yet ripe", "麦类饱满,尚未成熟")
        "芒种" -> pick("Wheat ripens; summer sowing begins", "麦类成熟,夏种开始")
        "夏至" -> pick("Longest day; mid-summer", "白昼最长,夏季中点")
        "小暑" -> pick("The heat begins to set in", "气候开始炎热")
        "大暑" -> pick("The hottest time of the year", "一年中最热时期")
        "立秋" -> pick("Autumn begins; heat recedes and cool arrives", "秋季开始,暑去凉来")
        "处暑" -> pick("The summer heat ends; it turns cooler", "暑气结束,天气转凉")
        "白露" -> pick("It cools down; dew condenses white", "天气转凉,露凝而白")
        "秋分" -> pick("Day and night are equal; mid-autumn", "昼夜平分,秋季中点")
        "寒露" -> pick("The dew is cold, nearly freezing", "露水已寒,将要结冰")
        "霜降" -> pick("It grows colder; the first frost appears", "天气渐冷,初霜出现")
        "立冬" -> pick("Winter begins; all things are stored away", "冬季开始,万物收藏")
        "小雪" -> pick("Light snow begins to fall", "开始降雪,雪量小")
        "大雪" -> pick("Snows grow heavy; the ground is covered", "雪量增大,地面积雪")
        "冬至" -> pick("Shortest day; mid-winter", "白昼最短,冬季中点")
        "小寒" -> pick("The cold begins to bite", "气候开始寒冷")
        "大寒" -> pick("The coldest time of the year", "一年中最冷时期")
        else -> term
    }

    fun keyOfName(name: String): String? = listOf("pinghe","qixu","yangxu","yinxu","tanshi","shire","xueyu","qiyu","tebing").firstOrNull { bodyName(it) == name }

    fun bodyArticle(id: String): String = when (id) {
        "pinghe" -> pick(
            "A constitution in which yin-yang, qi and blood are in harmony: balanced build, rosy complexion, and ample energy.\n\nPhysique: well-proportioned and sturdy.\n\nTypical signs: lustrous skin and hair, bright eyes, clear nose, ruddy lips, rarely tired, tolerant of heat and cold, sound sleep, good appetite, regular bowels and urination.\n\nTemperament: easy-going and cheerful.\n\nTendencies: seldom falls ill.\n\nAdaptability: adapts well to natural and social environments.",
            "【总体特征】阴阳气血调和，以体态适中、面色红润、精力充沛等为主要特征。\n\n【形体特征】体形匀称健壮。\n\n【常见表现】面色、肤色润泽，头发稠密有光泽，目光有神，鼻色明润，嗅觉通利，唇色红润，不易疲劳，精力充沛，耐受寒热，睡眠良好，胃纳佳，二便正常，舌色淡红，苔薄白，脉和缓有力。\n\n【心理特征】性格随和开朗。\n\n【发病倾向】平素患病较少。\n\n【对外界环境适应能力】对自然环境和社会环境适应能力较强。")
        "qixu" -> pick(
            "A constitution marked by deficient primal qi: fatigue, shortness of breath and spontaneous sweating.\n\nPhysique: soft, flabby muscles.\n\nTypical signs: a low, weak voice; reluctance to speak; easy fatigue; low spirits; easy sweating; pale tongue with teeth marks; weak pulse.\n\nTemperament: introverted, dislikes risks.\n\nTendencies: prone to colds and organ prolapse; slow recovery.\n\nAdaptability: poor tolerance of wind, cold, summer heat and dampness.",
            "【总体特征】元气不足，以疲乏、气短、自汗等气虚表现为主要特征。\n\n【形体特征】肌肉松软不实。\n\n【常见表现】平素语音低弱，气短懒言，容易疲乏，精神不振，易出汗，舌淡红，舌边有齿痕，脉弱。\n\n【心理特征】性格内向，不喜冒险。\n\n【发病倾向】易患感冒、内脏下垂等病；病后康复缓慢。\n\n【对外界环境适应能力】不耐受风、寒、暑、湿邪。")
        "yangxu" -> pick(
            "A constitution marked by deficient yang: aversion to cold and cold hands and feet.\n\nPhysique: soft, flabby muscles.\n\nTypical signs: habitual chilliness, cold limbs, preference for warm food and drink, low spirits, pale puffy tongue, deep slow pulse.\n\nTemperament: quiet, reserved.\n\nTendencies: prone to phlegm-fluid retention, swelling and diarrhea; illness tends to turn cold.\n\nAdaptability: tolerates summer but not winter; easily affected by wind, cold and dampness.",
            "【总体特征】阳气不足，以畏寒怕冷、手足不温等虚寒表现为主要特征。\n\n【形体特征】肌肉松软不实。\n\n【常见表现】平素畏冷，手足不温，喜热饮食，精神不振，舌淡胖嫩，脉沉迟。\n\n【心理特征】性格多沉静、内向。\n\n【发病倾向】易患痰饮、肿胀、泄泻等病；感邪易从寒化。\n\n【对外界环境适应能力】耐夏不耐冬；易感风、寒、湿邪。")
        "yinxu" -> pick(
            "A constitution marked by depleted yin: dry mouth and throat, warm palms and soles.\n\nPhysique: slim build.\n\nTypical signs: warm palms and soles, dry mouth and throat, slightly dry nose, preference for cold drinks, dry stools, red tongue with little coating, thin rapid pulse.\n\nTemperament: quick-tempered, outgoing and active.\n\nTendencies: prone to consumptive disorders and insomnia; illness tends to turn hot.\n\nAdaptability: tolerates winter but not summer; intolerant of heat and dryness.",
            "【总体特征】阴液亏少，以口燥咽干、手足心热等虚热表现为主要特征。\n\n【形体特征】体形偏瘦。\n\n【常见表现】手足心热，口燥咽干，鼻微干，喜冷饮，大便干燥，舌红少津，脉细数。\n\n【心理特征】性情急躁，外向好动，活泼。\n\n【发病倾向】易患虚劳、失精、不寐等病；感邪易从热化。\n\n【对外界环境适应能力】耐冬不耐夏；不耐受暑、热、燥邪。")
        "tanshi" -> pick(
            "A constitution marked by accumulated phlegm-dampness: plump build, full abdomen, sticky mouth and greasy coating.\n\nPhysique: overweight with a soft, full abdomen.\n\nTypical signs: oily facial skin, heavy sweating, chest oppression, copious phlegm, sweet or sticky taste, preference for rich food, greasy coating, slippery pulse.\n\nTemperament: gentle, steady, patient.\n\nTendencies: prone to diabetes, stroke and chest bi-syndrome.\n\nAdaptability: poor tolerance of plum-rain season and damp environments.",
            "【总体特征】痰湿凝聚，以形体肥胖、腹部肥满、口黏苔腻等痰湿表现为主要特征。\n\n【形体特征】体形肥胖，腹部肥满松软。\n\n【常见表现】面部皮肤油脂较多，多汗且黏，胸闷，痰多，口甜腻或淡，喜食肥甘甜黏，苔腻，脉滑。\n\n【心理特征】性格温和、稳重，善于忍耐。\n\n【发病倾向】易患消渴、中风、胸痹等病。\n\n【对外界环境适应能力】对梅雨季节及湿重环境适应能力差。")
        "shire" -> pick(
            "A constitution marked by internal damp-heat: oily face, bitter taste, yellow greasy coating.\n\nPhysique: medium or slim.\n\nTypical signs: oily complexion, acne, bitter dry mouth, heavy body, sticky or dry stools, scanty yellow urine, red tongue with yellow greasy coating, slippery rapid pulse.\n\nTemperament: easily vexed and impatient.\n\nTendencies: prone to sores, jaundice and heat strangury.\n\nAdaptability: poor tolerance of late-summer damp-heat.",
            "【总体特征】湿热内蕴，以面垢油光、口苦、苔黄腻等湿热表现为主要特征。\n\n【形体特征】形体中等或偏瘦。\n\n【常见表现】面垢油光，易生痤疮，口苦口干，身重困倦，大便黏滞不畅或燥结，小便短黄，男性易阴囊潮湿，女性易带下增多，舌质偏红，苔黄腻，脉滑数。\n\n【心理特征】容易心烦急躁。\n\n【发病倾向】易患疮疖、黄疸、热淋等病。\n\n【对外界环境适应能力】对夏末秋初湿热气候，湿重或气温偏高环境较难适应。")
        "xueyu" -> pick(
            "A constitution marked by sluggish blood: dull complexion and purplish tongue.\n\nPhysique: either plump or thin.\n\nTypical signs: dull skin, pigmentation, easy bruising, dark lips, dark or stasis-spotted tongue, enlarged dark sublingual veins, choppy pulse.\n\nTemperament: easily vexed, forgetful.\n\nTendencies: prone to abdominal masses, pain and bleeding disorders.\n\nAdaptability: intolerant of cold.",
            "【总体特征】血行不畅，以肤色晦暗、舌质紫黯等血瘀表现为主要特征。\n\n【形体特征】胖瘦均见。\n\n【常见表现】肤色晦暗，色素沉着，容易出现瘀斑，口唇黯淡，舌黯或有瘀点，舌下络脉紫黯或增粗，脉涩。\n\n【心理特征】易烦，健忘。\n\n【发病倾向】易患癥瘕及痛证、血证等。\n\n【对外界环境适应能力】不耐受寒邪。")
        "qiyu" -> pick(
            "A constitution marked by stagnant qi: depressed mood and vulnerability to worry.\n\nPhysique: mostly slim.\n\nTypical signs: low mood, emotional fragility, fretfulness, pale-red tongue with thin white coating, wiry pulse.\n\nTemperament: introverted, sensitive, prone to overthinking.\n\nTendencies: prone to depression, plum-pit qi and other qi-stagnation disorders.\n\nAdaptability: poor tolerance of emotional stress and rainy weather.",
            "【总体特征】气机郁滞，以神情抑郁、忧虑脆弱等气郁表现为主要特征。\n\n【形体特征】形体瘦者为多。\n\n【常见表现】神情抑郁，情感脆弱，烦闷不乐，舌淡红，苔薄白，脉弦。\n\n【心理特征】性格内向不稳定，敏感多虑。\n\n【发病倾向】易患郁证、梅核气、百合病及脏躁等。\n\n【对外界环境适应能力】对精神刺激适应能力较差；不适应阴雨天气。")
        "tebing" -> pick(
            "A constitution marked by congenital tendencies: physiological sensitivities and allergic reactions.\n\nPhysique: usually no special features; some have congenital anomalies.\n\nTypical signs: asthma, hives, itchy throat, nasal congestion and sneezing; family history of allergies.\n\nTemperament: varies with the individual.\n\nTendencies: prone to asthma, urticaria, hay fever and drug allergies.\n\nAdaptability: poor; sensitive to allergens and seasonal changes.",
            "【总体特征】先天失常，以生理缺陷、过敏反应等为主要特征。\n\n【形体特征】过敏体质者一般无特殊；先天禀赋异常者或有畸形，或有生理缺陷。\n\n【常见表现】过敏体质者常见哮喘、风团、咽痒、鼻塞、喷嚏等；患遗传性疾病者有垂直遗传、先天性、家族性特征；患胎传性疾病者具有母体影响胎儿个体生长发育及相关疾病特征。\n\n【心理特征】随禀质不同情况各异。\n\n【发病倾向】过敏体质者易患哮喘、荨麻疹、花粉症及药物过敏等；遗传性疾病如血友病、先天愚型等；胎传性疾病如五迟（立迟、行迟、发迟、齿迟和语迟）、五软（头软、项软、手足软、肌肉软、口软）、解颅、胎惊等。\n\n【对外界环境适应能力】适应能力差，如过敏体质者对易致敏季节适应能力差，易引发宿疾。")
        else -> ""
    }

    fun junPrefix() = pick("Monarch herbs: ", "君药:")
    fun scene(name: String): String = when (name) {
        "日常养生" -> pick("Daily Wellness", "日常养生")
        "助眠安神" -> pick("Sleep & Calm", "助眠安神")
        "提神醒脑" -> pick("Refresh & Focus", "提神醒脑")
        "疏肝解郁" -> pick("Soothe Liver & Lift Mood", "疏肝解郁")
        "祛湿化浊" -> pick("Resolve Dampness", "祛湿化浊")
        "温阳散寒" -> pick("Warm Yang & Dispel Cold", "温阳散寒")
        else -> name
    }
    fun customHeader() = pick("Custom Blend", "定制香方")
    fun customBodyLabel() = pick("For constitution", "适用体质")
    fun customSceneLabel() = pick("For scene", "使用场景")
    fun customJun() = pick("Monarch herbs", "君药")
    fun customChen() = pick("Minister herbs", "臣药")
    fun customZuoShi() = pick("Assistant & Guide herbs", "佐使药")
    fun customAdjust() = pick("Adjust harmonizing herbs as needed", "根据具体情况酌加调和药材")
    fun customUsage() = pick("How to use", "用法建议")
    fun customUsage1() = pick("Grind the herbs into powder", "将上述药材研磨成粉")
    fun customUsage2() = pick("Blend by the Monarch-Minister-Assistant-Guide proportions", "按君臣佐使比例配伍")
    fun customUsage3() = pick("Use as a sachet, pouch, or incense", "可制成香囊、香包或熏香使用")
    fun customUsage4() = pick("Use in a well-ventilated space", "建议在通风良好的环境中使用")

    fun notifyTitle(term: String) = pick("Now: $term", "现在是:$term")
    fun notifyBody(seasonKey: String, term: String): String {
        val body = when (seasonKey) {
            "春疏肝" -> pick("Spring qi rises — soothe the liver and smooth the qi.", "春气生发,疏肝理气。")
            "夏清心" -> pick("Summer heat prevails — clear the heart and cool the fire.", "夏热养心,清心降火。")
            "秋润肺" -> pick("Autumn dryness sets in — moisten the lungs and nourish yin.", "秋燥养肺,润肺养阴。")
            "冬温肾" -> pick("Winter cold seals the season — warm the kidneys and tonify yang.", "冬寒养肾,温阳补肾。")
            else -> pick("Follow the seasons and nurture yourself.", "顺时调养。")
        }
        return pick("$body Reassess your constitution for a seasonal blend. ($term)", "${body}建议定期测评体质,顺应节气调香。(${term})")
    }
}