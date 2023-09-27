package com.archko.subtitle.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

import com.archko.subtitle.DefaultSubtitleEngine;
import com.archko.subtitle.FileUtils;
import com.archko.subtitle.ISubtitlePlayer;
import com.archko.subtitle.SubtitleEngine;
import com.archko.subtitle.model.Subtitle;

import java.io.File;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author AveryZhong.
 */
@SuppressLint("AppCompatCustomView")
public class SimpleSubtitleView extends TextView
        implements SubtitleEngine, SubtitleEngine.OnSubtitleChangeListener,
        SubtitleEngine.OnSubtitlePreparedListener {

    private static final String EMPTY_TEXT = "";

    private SubtitleEngine mSubtitleEngine;

    public boolean isInternal = false;

    public boolean hasInternal = false;

    public SimpleSubtitleView(final Context context) {
        super(context);
        init();
    }

    public SimpleSubtitleView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleSubtitleView(final Context context, final AttributeSet attrs,
                              final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSubtitleEngine = new DefaultSubtitleEngine();
        mSubtitleEngine.setOnSubtitlePreparedListener(this);
        mSubtitleEngine.setOnSubtitleChangeListener(this);
    }

    @Override
    public void onSubtitlePrepared(@Nullable final List<Subtitle> subtitles) {
        start();
    }

    @Override
    public void onSubtitleChanged(@Nullable final Subtitle subtitle) {
        if (subtitle == null) {
            setText(EMPTY_TEXT);
            return;
        }
        String text = subtitle.content;
        text = text.replaceAll("(?:\\r\\n)", "<br />");
        text = text.replaceAll("(?:\\r)", "<br />");
        text = text.replaceAll("(?:\\n)", "<br />");
        text = text.replaceAll("\\{[\\s\\S]*\\}", "");
        setText(Html.fromHtml(text));
    }

    @Override
    public void setSubtitlePath(Context context, final String videoName, final String path) {
        isInternal = false;
        mSubtitleEngine.setSubtitlePath(context, videoName, path);
    }

    @Override
    public void setSubtitleDelay(Integer mseconds) {
        mSubtitleEngine.setSubtitleDelay(mseconds);
    }

    public void setPlaySubtitleCacheKey(String cacheKey) {
        mSubtitleEngine.setPlaySubtitleCacheKey(cacheKey);
    }

    public String getPlaySubtitleCacheKey() {
        return mSubtitleEngine.getPlaySubtitleCacheKey();
    }

    public void clearSubtitleCache(Context context) {
        String subtitleCacheKey = getPlaySubtitleCacheKey();
        if (subtitleCacheKey != null && subtitleCacheKey.length() > 0) {
            //CacheManager.delete(MD5.string2MD5(subtitleCacheKey), "");

            String subtitleFileCacheDir = context.getExternalCacheDir().getAbsolutePath() + "/zimu/" + subtitleCacheKey + "/";
            File file = new File(subtitleFileCacheDir);
            if (file.exists()) {
                FileUtils.recursiveDelete(file);
            }
        }
    }

    @Override
    public void reset() {
        mSubtitleEngine.reset();
    }

    @Override
    public void start() {
        mSubtitleEngine.start();
    }

    @Override
    public void pause() {
        mSubtitleEngine.pause();
    }

    @Override
    public void resume() {
        mSubtitleEngine.resume();
    }

    @Override
    public void stop() {
        mSubtitleEngine.stop();
    }

    @Override
    public void destroy() {
        mSubtitleEngine.destroy();
    }

    @Override
    public void bindToMediaPlayer(ISubtitlePlayer mediaPlayer) {
        mSubtitleEngine.bindToMediaPlayer(mediaPlayer);
    }

    @Override
    public void setOnSubtitlePreparedListener(final OnSubtitlePreparedListener listener) {
        mSubtitleEngine.setOnSubtitlePreparedListener(listener);
    }

    @Override
    public void setOnSubtitleChangeListener(final OnSubtitleChangeListener listener) {
        mSubtitleEngine.setOnSubtitleChangeListener(listener);
    }

    @Override
    protected void onDetachedFromWindow() {
        destroy();
        super.onDetachedFromWindow();
    }
}