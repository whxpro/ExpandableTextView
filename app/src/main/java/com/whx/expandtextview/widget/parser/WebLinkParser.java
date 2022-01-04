package com.whx.expandtextview.widget.parser;

import static androidx.core.util.PatternsCompat.AUTOLINK_WEB_URL;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;

import androidx.annotation.ColorInt;

import com.whx.expandtextview.widget.CenterImageSpan;
import com.whx.expandtextview.widget.FormatData;
import com.whx.expandtextview.widget.UUIDUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebLinkParser implements IParser {
    public static String TEXT_WEB_LINK = "网页链接";
    public static final String IMAGE_TARGET = "图";
    public static final String TARGET = IMAGE_TARGET + TEXT_WEB_LINK;

    private final Pattern pattern;
    /**
     * 是否需要转换url成网页链接四个字
     */
    private boolean mNeedConvertUrl = true;
    private Drawable mLinkDrawable;
    /**
     * 链接的字体颜色
     */
    private @ColorInt
    int mLinkTextColor;

    private OnLinkClickListener mLinkClickListener;

    public WebLinkParser() {
        pattern = AUTOLINK_WEB_URL;

    }

    public void setNeedConvertUrl(boolean needConvertUrl) {
        this.mNeedConvertUrl = needConvertUrl;
    }

    public void setWebLinkIcon(Drawable drawable) {
        mLinkDrawable = drawable;
    }

    public void setLinkTextColor(@ColorInt int color) {
        mLinkTextColor = color;
    }

    public void setOnLinkClickListener(OnLinkClickListener clickListener) {
        mLinkClickListener = clickListener;
    }

    @Override
    public String parse(String content, List<FormatData.PositionData> positionData, Map<String, String> convert) {
        int start;
        int end = 0;
        int temp = 0;
        Matcher matcher = pattern.matcher(content);
        StringBuilder newResult = new StringBuilder();
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            newResult.append(content, temp, start);
            if (mNeedConvertUrl) {
                //将匹配到的内容进行统计处理
                positionData.add(new FormatData.PositionData(newResult.length() + 1, newResult.length() + 2 + TARGET.length(), matcher.group(), this));
                newResult.append(" ").append(TARGET).append(" ");
            } else {
                String result = matcher.group();
                String key = UUIDUtils.getUuid(result.length());
                positionData.add(new FormatData.PositionData(newResult.length(), newResult.length() + 2 + key.length(), result, this));
                convert.put(key, result);
                newResult.append(" ").append(key).append(" ");
            }
            temp = end;
        }
        newResult.append(content.substring(end));
        return newResult.toString();
    }

    @Override
    public Object getSpan(SpannableStringBuilder content, FormatData.PositionData positionData, int fitPos, boolean shouldExpand, boolean currentLineLess) {
        if (shouldExpand) {
            if (positionData.getStart() < fitPos) {
                if (mLinkDrawable != null) {
                    CenterImageSpan imageSpan = new CenterImageSpan(mLinkDrawable, ImageSpan.ALIGN_BASELINE);
                    //设置链接图标
                    content.setSpan(imageSpan, positionData.getStart(), positionData.getStart() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }
                //设置链接文字样式
                int endPosition = positionData.getEnd();
                if (currentLineLess) {
                    if (fitPos > positionData.getStart() + 1 && fitPos < positionData.getEnd()) {
                        endPosition = fitPos;
                    }
                }
                if (positionData.getStart() + 1 < fitPos) {
                    addUrl(content, positionData, endPosition);
                }
            }
        } else {
            if (mLinkDrawable != null) {
                CenterImageSpan imageSpan = new CenterImageSpan(mLinkDrawable, ImageSpan.ALIGN_BASELINE);
                //设置链接图标
                content.setSpan(imageSpan, positionData.getStart(), positionData.getStart() + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
            addUrl(content, positionData, positionData.getEnd());
        }
        return null;
    }

    /**
     * 添加链接的span
     */
    private void addUrl(SpannableStringBuilder ssb, final FormatData.PositionData data, int endPosition) {
        ssb.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (mLinkClickListener != null) {
                    mLinkClickListener.onLinkClick(data.getUrl());
                } else {
                    //如果没有设置监听 则调用默认的打开浏览器显示连接
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri url = Uri.parse(data.getUrl());
                    intent.setData(url);
//                    mContext.startActivity(intent);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(mLinkTextColor);
                ds.setUnderlineText(false);
            }
        }, data.getStart() + 1, endPosition, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    @Override
    public int level() {
        return 100;
    }
}
