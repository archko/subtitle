package com.archko.subtitle;

/**
 * 抽象的播放器，继承此接口扩展自己的播放器
 * Created by Doikki on 2017/12/21.
 */
public interface ISubtitlePlayer {

    /**
     * 是否正在播放
     */
    boolean isPlaying();

    /**
     * 获取当前播放的位置
     */
    long getCurrentPosition();

    /**
     * 获取视频总时长
     */
    long getDuration();

}
