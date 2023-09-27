package com.archko.subtitle;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.archko.subtitle.model.Subtitle;
import com.archko.subtitle.model.Time;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author AveryZhong.
 */

public class DefaultSubtitleEngine implements SubtitleEngine {
    private static final String TAG = DefaultSubtitleEngine.class.getSimpleName();
    private static final int MSG_REFRESH = 0x888;
    private static final int REFRESH_INTERVAL = 100;

    @Nullable
    private HandlerThread mHandlerThread;
    @Nullable
    private Handler mUIHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (isPlaying()) {
                long position = mMediaPlayer.getCurrentPosition();
                Message message = Message.obtain(mWorkHandler);
                message.obj = position;
                mWorkHandler.sendMessage(message);
            } else {
                mUIHandler.sendEmptyMessageDelayed(MSG_REFRESH, 1000L);
            }
        }
    };
    private Handler mWorkHandler;
    @Nullable
    private List<Subtitle> mSubtitles;
    private UIRenderTask mUIRenderTask;
    private ISubtitlePlayer mMediaPlayer;
    private OnSubtitlePreparedListener mOnSubtitlePreparedListener;
    private OnSubtitleChangeListener mOnSubtitleChangeListener;

    public DefaultSubtitleEngine() {

    }

    @Override
    public void bindToMediaPlayer(ISubtitlePlayer mediaPlayer) {
        mMediaPlayer = mediaPlayer;
    }

    @Override
    public void setSubtitlePath(Context context, String videoName, final String path) {
        reset();
        if (TextUtils.isEmpty(path)) {
            Log.w(TAG, "loadSubtitleFromRemote: path is null.");
            return;
        }
        initWorkThread();

        SubtitleLoader.loadSubtitle(path, new SubtitleLoader.Callback() {
            @Override
            public void onSuccess(final SubtitleLoadSuccessResult subtitleLoadSuccessResult) {
                if (subtitleLoadSuccessResult == null) {
                    Log.d(TAG, "onSuccess: subtitleLoadSuccessResult is null.");
                    return;
                }
                if (subtitleLoadSuccessResult.timedTextObject == null) {
                    Log.d(TAG, "onSuccess: timedTextObject is null.");
                    return;
                }
                final TreeMap<Integer, Subtitle> captions = subtitleLoadSuccessResult.timedTextObject.captions;
                if (captions == null) {
                    Log.d(TAG, "onSuccess: captions is null.");
                    return;
                }
                mSubtitles = new ArrayList<>(captions.values());
                setSubtitleDelay(SubtitleHelper.getTimeDelay());
                notifyPrepared();

                String subtitlePath = subtitleLoadSuccessResult.subtitlePath;
                if (subtitlePath.startsWith("http://") || subtitlePath.startsWith("https://")) {
                    String subtitleFileCacheDir = context.getExternalCacheDir().getAbsolutePath() + "/zimu/" + MD5.string2MD5(videoName) + "/";
                    File cacheDir = new File(subtitleFileCacheDir);
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    String subtitleFile = subtitleFileCacheDir + subtitleLoadSuccessResult.fileName;
                    File cacheSubtitleFile = new File(subtitleFile);
                    boolean writeResult = FileUtils.writeSimple(subtitleLoadSuccessResult.content.getBytes(), cacheSubtitleFile);
                    Log.d(TAG, "writeResult subtitle:" + writeResult);
                }
            }

            @Override
            public void onError(final Exception exception) {
                Log.e(TAG, "onError: " + exception.getMessage());
                stop();
            }
        });
    }

    @Override
    public void setSubtitleDelay(Integer milliseconds) {
        if (milliseconds == 0) {
            return;
        }
        if (mSubtitles == null || mSubtitles.isEmpty()) {
            return;
        }
        List<Subtitle> thisSubtitles = mSubtitles;
        mSubtitles = null;
        for (int i = 0; i < thisSubtitles.size(); i++) {
            Subtitle subtitle = thisSubtitles.get(i);
            Time start = subtitle.start;
            Time end = subtitle.end;
            start.mseconds += milliseconds;
            end.mseconds += milliseconds;
            if (start.mseconds <= 0) {
                start.mseconds = 0;
            }
            if (end.mseconds <= 0) {
                end.mseconds = 0;
            }
            subtitle.start = start;
            subtitle.end = end;
        }
        mSubtitles = thisSubtitles;
    }

    private static String playSubtitleCacheKey;

    public void setPlaySubtitleCacheKey(String cacheKey) {
        playSubtitleCacheKey = cacheKey;
    }

    public String getPlaySubtitleCacheKey() {
        return playSubtitleCacheKey;
    }

    @Override
    public void reset() {
        stop();
        mSubtitles = null;
        mUIRenderTask = null;
    }

    @Override
    public void start() {
        Log.d(TAG, "start: ");
        if (mMediaPlayer == null) {
            Log.w(TAG, "MediaPlayer is not bind, You must bind MediaPlayer to "
                    + SubtitleEngine.class.getSimpleName()
                    + " before start() method be called,"
                    + " you can do this by call " +
                    "bindToMediaPlayer(MediaPlayer mediaPlayer) method.");
            return;
        }
        stop();
        if (mUIHandler != null) {
            mUIHandler.sendEmptyMessageDelayed(MSG_REFRESH, REFRESH_INTERVAL);
        }
    }

    @Override
    public void pause() {
        stop();
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public void stop() {
        if (mWorkHandler != null) {
            mWorkHandler.removeMessages(MSG_REFRESH);
        }
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void destroy() {
        Log.d(TAG, "destroy: ");
        stopWorkThread();
        reset();

    }

    private boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    private void initWorkThread() {
        stopWorkThread();
        mHandlerThread = new HandlerThread("SubtitleFindThread");
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper(),
                msg -> {
                    try {
                        long delay = REFRESH_INTERVAL;
                        long position = (long) msg.obj;
                        Subtitle subtitle = SubtitleFinder.find(position, mSubtitles);
                        notifyRefreshUI(subtitle);
                        if (subtitle != null) {
                            delay = subtitle.end.mseconds - position;
                        }
                        if (mUIHandler != null) {
                            mUIHandler.sendEmptyMessageDelayed(MSG_REFRESH, delay);
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                    return true;
                });
    }

    private void stopWorkThread() {
        if (mWorkHandler != null) {
            mWorkHandler.removeCallbacksAndMessages(null);
            mWorkHandler = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }
    }

    private void notifyRefreshUI(final Subtitle subtitle) {
        if (mUIRenderTask == null) {
            mUIRenderTask = new UIRenderTask(mOnSubtitleChangeListener);
        }
        mUIRenderTask.execute(subtitle);
    }

    private void notifyPrepared() {
        if (mOnSubtitlePreparedListener != null) {
            mOnSubtitlePreparedListener.onSubtitlePrepared(mSubtitles);
        }
    }

    @Override
    public void setOnSubtitlePreparedListener(final OnSubtitlePreparedListener listener) {
        mOnSubtitlePreparedListener = listener;
    }

    @Override
    public void setOnSubtitleChangeListener(final OnSubtitleChangeListener listener) {
        mOnSubtitleChangeListener = listener;
    }

}
