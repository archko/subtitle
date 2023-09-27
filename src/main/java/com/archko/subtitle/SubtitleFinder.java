package com.archko.subtitle;

import com.archko.subtitle.model.Subtitle;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * @author AveryZhong.
 */

public class SubtitleFinder {
    private SubtitleFinder() {
        throw new AssertionError("No instance for you");
    }

    @Nullable
    public static Subtitle find(long position, List<Subtitle> subtitles) {
        if (subtitles == null || subtitles.isEmpty()) {
            return null;
        }
        int start = 0;
        int end = subtitles.size() - 1;
        while (start <= end) {
            int middle = (start + end) / 2;
            Subtitle middleSubtitle = subtitles.get(middle);
            if (position < middleSubtitle.start.mseconds) {
                if (position > middleSubtitle.end.mseconds) {
                    return middleSubtitle;
                }
                end = middle - 1;
            } else if (position > middleSubtitle.end.mseconds) {
                if (position < middleSubtitle.start.mseconds) {
                    return middleSubtitle;
                }
                start = middle + 1;
            } else if (position >= middleSubtitle.start.mseconds
                    && position <= middleSubtitle.end.mseconds) {
                return middleSubtitle;
            }
        }
        return null;
    }

}