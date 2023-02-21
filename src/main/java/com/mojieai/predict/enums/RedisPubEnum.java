package com.mojieai.predict.enums;


import com.mojieai.predict.redis.refresh.handler.sub.*;

public enum RedisPubEnum {

    INI_CONFIG("INI_ONLINE_CHANNEL", "INI_ONLINE_CONFIRM_CHANNEL", IniSubHandler.class),
    ACTIVITY_INI_CONFIG("ACTIVITY_INI_ONLINE_CHANNEL", "ACTIVITY_INI_CONFIRM_CHANNEL", ActivityIniSubHandler.class),
    BANNER_CONFIG("BANNER_ONLINE_CHANNEL", "BANNER_ONLINE_CONFIRM_CHANNEL", BannerSubHandler.class),
    AWARD_INFO_CONFIG("AWARD_INFO_ONLINE_CHANNEL", "AWARD_INFO_ONLINE_CONFIRM_CHANNEL", AwardInfoSubHandler.class),
    PERIOD_CONFIG("PERIOD_ONLINE_CHANNEL", "PERIOD_ONLINE_CONFIRM_CHANNEL", PeriodSubHandler.class);
    
    public static final int CHANNEL_TYPE_ONLINE = 0;//线上
    public static final int CHANNEL_TYPE_CONFIRM = 1;//预发
    private String channel;
    private String channelConfirm;
    private Class<? extends RedisSubscribeHandler> handlerClass;

    RedisPubEnum(String channel, String channelConfirm, Class<? extends RedisSubscribeHandler> handlerClass) {
        this.channel = channel;
        this.channelConfirm = channelConfirm;
        this.handlerClass = handlerClass;
    }

    public static String getChannelOnlineByChannelKey(String channelKey) {
        for (RedisPubEnum r : RedisPubEnum.values()) {
            if (r.getChannel().startsWith(channelKey)) {
                return r.getChannel();
            }
        }
        return null;
    }

    public static String getChannelConfirmByChannelKey(String channelKey) {
        for (RedisPubEnum r : RedisPubEnum.values()) {
            if (r.getChannelConfirm().startsWith(channelKey)) {
                return r.getChannelConfirm();
            }
        }
        return null;
    }

    public static String getChannel(String channelKey, Integer type) {
        String channel = null;
        switch (type) {
            case CHANNEL_TYPE_ONLINE:
                channel = RedisPubEnum.getChannelOnlineByChannelKey(channelKey);
                break;
            case CHANNEL_TYPE_CONFIRM:
                channel = RedisPubEnum.getChannelConfirmByChannelKey(channelKey);
                break;
        }
        return channel;
    }

    public String getChannel() {
        return channel;
    }

    public Class<? extends RedisSubscribeHandler> getHandlerClass() {
        return handlerClass;
    }

    public String getChannelConfirm() {
        return channelConfirm;
    }
}
