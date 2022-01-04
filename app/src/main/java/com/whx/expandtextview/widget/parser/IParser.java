package com.whx.expandtextview.widget.parser;

import android.text.SpannableStringBuilder;

import androidx.annotation.Nullable;

import com.whx.expandtextview.widget.FormatData;

import java.util.List;
import java.util.Map;

public interface IParser {
    @Nullable
    String parse(String content, List<FormatData.PositionData> positionData, Map<String, String> convert);

    @Nullable
    Object getSpan(SpannableStringBuilder content, FormatData.PositionData positionData, int fitPos, boolean shouldExpand, boolean currentLineLess);

    int level();
}
