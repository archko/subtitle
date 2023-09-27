package com.archko.subtitle;

import android.content.Context;

import com.archko.subtitle.model.Subtitle;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author AveryZhong.
 */

public interface SubtitleEngine {

    /**
     * 设置字幕路径，加载字幕
     *
     * @param videoName 视频的名字,由它的md5创建存储目录,取出来的时候就有列表了
     * @param path      字幕路径（本地路径或者是远程路径）
     */
    void setSubtitlePath(Context context, String videoName, String path);

    /**
     * 字幕延时
     *
     * @param milliseconds
     */
    void setSubtitleDelay(Integer milliseconds);

    void setPlaySubtitleCacheKey(String cacheKey);

    String getPlaySubtitleCacheKey();

    /**
     * 开启字幕刷新任务
     */
    void start();

    /**
     * 暂停
     */
    void pause();

    /**
     * 恢复
     */
    void resume();

    /**
     * 停止字幕刷新任务
     */
    void stop();

    /**
     * 重置
     */
    void reset();

    /**
     * 销毁字幕
     */
    void destroy();

    /**
     * 绑定AbstractPlayer
     *
     * @param mediaPlayer mediaPlayer
     */
    void bindToMediaPlayer(ISubtitlePlayer mediaPlayer);

    /**
     * 设置字幕准备完成监接口
     *
     * @param listener OnSubtitlePreparedListener
     */
    void setOnSubtitlePreparedListener(OnSubtitlePreparedListener listener);

    /**
     * 设置字幕改变监听接口
     *
     * @param listener OnSubtitleChangeListener
     */
    void setOnSubtitleChangeListener(OnSubtitleChangeListener listener);

    /**
     * 字幕准备完成监接口
     */
    interface OnSubtitlePreparedListener {
        void onSubtitlePrepared(@Nullable List<Subtitle> subtitles);
    }

    /**
     * 字幕改变监听接口
     */
    interface OnSubtitleChangeListener {
        void onSubtitleChanged(@Nullable Subtitle subtitle);
    }

}