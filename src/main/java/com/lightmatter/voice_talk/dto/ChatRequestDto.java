package com.lightmatter.voice_talk.dto;

public class ChatRequestDto {

    /**
     * {
     * 	"content":"人活着的意义是什么呢，如何让自己对生活的充满希望",
     * 	"languageType":10
     * }
     */
    private String content;

    // 中文=1； 英文 = 2
    private int languageType;

    public ChatRequestDto() {
    }

    public ChatRequestDto(String content, int languageType) {
        this.content = content;
        this.languageType = languageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getLanguageType() {
        return languageType;
    }

    public void setLanguageType(int languageType) {
        this.languageType = languageType;
    }

    @Override
    public String toString() {
        return "ChatRequestDto{" +
                "content='" + content + '\'' +
                ", languageType=" + languageType +
                '}';
    }
}
