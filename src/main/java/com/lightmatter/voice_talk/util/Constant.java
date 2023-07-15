package com.lightmatter.voice_talk.util;

import java.util.HashMap;
import java.util.Map;

public class Constant {


    public static final  String CHINESE_PREFIX = "请用中文回复我。";
    public static final  String ENGLISH_PREFIX = "请用英文回复我。";
    public static final String PREFIX = "下面是我的对你说的话：";

    public static final String ROLE_0 = "";

    public static final String ROLE_1 = "" +
            "你作为一名心理医生，用口语化的方式为大家解决生活、心理出现的各种问题，用口语化的方式跟大家沟通交流。可以加一些语气词。下面是一个人的对你说的话：";

    public static final String ROLE_2 = "" +
            "你作为一个技术专家给我进行相关技术指导，以沟通交流的方式，以及口语化方式给我交流。可以适当的使用一些修辞。";

    public static final String ROLE_3 = "" +
            "你作为我的好朋友，当我遇到生活中的一些事情的时候，你以口语化的方式跟我交流，可以用比较幽默的口吻。";

    public static Map<String,String> ROLE_MAP = new HashMap<>();

    static {
        ROLE_MAP.put("不定义角色", ROLE_0);
        ROLE_MAP.put("心理医生", ROLE_1);
        ROLE_MAP.put("技术专家", ROLE_2);
        ROLE_MAP.put("好朋友", ROLE_3);
    }

    public static final String LANGUAGE_ENGLISH = "英语";
    public static final String LANGUAGE_CHINESE = "中文(普通话)";

    public static String SELECTED_ROLE = "好朋友";
    public static String SELECTED_LANGUAGE = LANGUAGE_CHINESE;
    public static boolean SELECTED_IS_PLAY_VOICE = true;

    public static final String IS_PLAY_VOICE_KEY = "isPlayVoice";
    public static final String LANGUAGE_KEY = "language";
    public static final String ROLE_KEY = "role";
    public static final String MY_ROLE_MAP_KEY = "myOwnRoleMap";

}
