package com.whx.expandtextview.widget.parser;

import android.text.SpannableStringBuilder;

import com.whx.expandtextview.widget.FormatData;

import java.util.List;
import java.util.Map;

public class TagParser implements IParser {
    @Override
    public String parse(String content, List<FormatData.PositionData> positionData, Map<String, String> convert) {
        return null;
    }

    @Override
    public Object getSpan(SpannableStringBuilder content, FormatData.PositionData positionData, int fitPos,  boolean shouldExpand, boolean currentLineLess) {
        return null;
    }

    @Override
    public int level() {
        return 0;
    }
}
