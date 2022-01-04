package com.whx.expandtextview.widget.parser;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.ColorInt;

import com.whx.expandtextview.widget.FormatData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionParser implements IParser {
    public static final String regexp_mention = "@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}";

    private final Pattern pattern;
    /**
     * @ 文字的颜色
     */
    private int mMentionTextColor;

    private OnLinkClickListener mLinkClickListener;

    public MentionParser() {
        pattern = Pattern.compile(regexp_mention, Pattern.CASE_INSENSITIVE);
    }

    public void setMentionTextColor(@ColorInt int color) {
        mMentionTextColor = color;
    }

    public void setOnLinkClickListener(OnLinkClickListener clickListener) {
        mLinkClickListener = clickListener;
    }

    @Override
    public String parse(String content, List<FormatData.PositionData> positionData, Map<String, String> convert) {
        Matcher matcher = pattern.matcher(content);
        List<FormatData.PositionData> datasMention = new ArrayList<>();
        while (matcher.find()) {
            //将匹配到的内容进行统计处理
            datasMention.add(new FormatData.PositionData(matcher.start(), matcher.end(), matcher.group(), this));
        }
        positionData.addAll(0, datasMention);
        return content;
    }

    @Override
    public Object getSpan(SpannableStringBuilder content, FormatData.PositionData positionData, int fitPos, boolean shouldExpand, boolean currentLineLess) {
        if (shouldExpand) {
            if (positionData.getStart() < fitPos) {
                int endPosition = positionData.getEnd();
                if (currentLineLess) {
                    if (fitPos < positionData.getEnd()) {
                        endPosition = fitPos;
                    }
                }
                addMention(content, positionData, endPosition);
            }
        } else {
            addMention(content, positionData, positionData.getEnd());
        }
        return null;
    }

    /**
     * 添加@用户的Span
     */
    private void addMention(SpannableStringBuilder ssb, final FormatData.PositionData data, int endPosition) {
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (mLinkClickListener != null)
                    mLinkClickListener.onLinkClick(data.getUrl());
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(mMentionTextColor);
                ds.setUnderlineText(false);
            }
        }, data.getStart(), endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    @Override
    public int level() {
        return 0;
    }
}
