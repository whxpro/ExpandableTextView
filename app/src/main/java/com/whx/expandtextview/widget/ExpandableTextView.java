package com.whx.expandtextview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import com.whx.expandtextview.R;
import com.whx.expandtextview.widget.parser.IParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * 一个支持展开 收起 网页链接 和 @用户 点击识别 的TextView
 */
public class ExpandableTextView extends AppCompatTextView {
    private static final int DEF_MAX_LINE = 3;
    public static String TEXT_FOLD = "收起";
    public static String TEXT_EXPEND = "展开";
    public static final String Space = " ";

    public static final String DEFAULT_CONTENT = "";

    private static int retryTime = 0;

    private TextPaint mPaint;

    private boolean linkHit;

    /**
     * 记录当前的状态
     */
    private StatusType mStatus = StatusType.STATUS_FOLD;

    private FormatData mFormatData;

    /**
     * 计算的layout
     */
    private DynamicLayout mDynamicLayout;

    // hide状态下，展示多少行开始省略
    private int mMaxLines = Integer.MAX_VALUE;

    private int currentLines;

    private int mWidth;

    private Drawable mMoreArrow;

    /**
     * 展开或者收回事件监听
     */
    private OnExpandOrFoldClickListener expandOrContractClickListener;

    /**
     * 点击展开或者收回按钮的时候 是否真的执行操作
     */
    private boolean needRealExpandOrFold = true;

    /**
     * 是否需要收起
     */
    private boolean mNeedFold = true;

    /**
     * 是否需要展开功能
     */
    private boolean mNeedExpend = true;

    /**
     * 是否需要永远将展开或收回显示在最右边
     */
    private boolean mNeedAlwaysShowRight = false;

    /**
     * 是否需要动画 默认开启动画
     */
    private boolean mNeedAnimation = false;

    private int mLineCount;

    private CharSequence mContent;

    /**
     * 展开文字的颜色
     */
    private int mExpandBtnColor;

    /**
     * 收起的文字的颜色
     */
    private int mFoldBtnColor;

    /**
     * 展开的文案
     */
    private String mExpandBtnString;
    /**
     * 收起的文案
     */
    private String mFoldBtnString;

    /**
     * 在收回和展开前面添加的内容
     */
    private String mEndExpandContent;

    /**
     * 在收回和展开前面添加的内容的字体颜色
     */
    private int mEndExpandTextColor;

    //是否AttachedToWindow
    private boolean isAttached;

    private final PriorityQueue<IParser> mParserList = new PriorityQueue<>(6, (o1, o2) -> o2.level() - o1.level());

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        setMovementMethod(LocalLinkMovementMethod.getInstance());
        addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                if (!isAttached)
                    doSetContent();
                isAttached = true;
            }

            @Override
            public void onViewDetachedFromWindow(View v) {

            }
        });
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        //适配英文版
        TEXT_FOLD = context.getString(R.string.social_contract);
        TEXT_EXPEND = context.getString(R.string.social_expend);


        if (attrs != null) {
            TypedArray a =
                    getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView,
                            defStyleAttr, 0);

            mMaxLines = a.getInt(R.styleable.ExpandableTextView_etv_max_line, DEF_MAX_LINE);
            mNeedExpend = a.getBoolean(R.styleable.ExpandableTextView_etv_show_expand, true);
            mNeedFold = a.getBoolean(R.styleable.ExpandableTextView_etv_show_fold, false);
            mNeedAnimation = a.getBoolean(R.styleable.ExpandableTextView_etv_need_animation, true);

            mNeedAlwaysShowRight = a.getBoolean(R.styleable.ExpandableTextView_etv_always_showright, false);

            mFoldBtnString = a.getString(R.styleable.ExpandableTextView_etv_fold_text);
            mExpandBtnString = a.getString(R.styleable.ExpandableTextView_etv_expand_text);

            mExpandBtnColor = a.getColor(R.styleable.ExpandableTextView_etv_expand_color,
                    Color.parseColor("#247FFF"));
            mEndExpandTextColor = a.getColor(R.styleable.ExpandableTextView_etv_end_color,
                    Color.parseColor("#999999"));
            mFoldBtnColor = a.getColor(R.styleable.ExpandableTextView_etv_fold_color,
                    Color.parseColor("#999999"));

            currentLines = mMaxLines;
            a.recycle();
        }
        if (TextUtils.isEmpty(mExpandBtnString)) {
            mExpandBtnString = TEXT_EXPEND;
        }
        if (TextUtils.isEmpty(mFoldBtnString)) {
            mFoldBtnString = TEXT_FOLD;
        }
        mPaint = getPaint();
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mMoreArrow = ResourcesCompat.getDrawable(getResources(), R.mipmap.shop_ic_note_content_more_arr, null);
        if (mMoreArrow != null) {
            mMoreArrow.setBounds(0, 0, 24, 24);
        }
    }

    /**
     * 设置内容
     */
    public void setContent(final String content) {
        mContent = content;
        if (isAttached)
            doSetContent();
    }

    public void addParser(IParser parser) {
        mParserList.add(parser);
    }

    public void setExpand(boolean expand) {
        if (expand) {
            action(StatusType.STATUS_EXPAND);
        } else {
            action(StatusType.STATUS_FOLD);
        }
    }

    /**
     * 实际设置内容的
     */
    private void doSetContent() {
        if (mContent == null) {
            return;
        }
        currentLines = mMaxLines;

        if (mWidth <= 0) {
            if (getWidth() > 0)
                mWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        }

        if (mWidth <= 0) {
            if (retryTime > 10) {
                setText(DEFAULT_CONTENT);
            }
            this.post(() -> {
                retryTime++;
                setContent(mContent.toString());
            });
        } else {
            // 将内容设置到控件中
            setText(getRealContent(mContent.toString()));
        }
    }

    private SpannableStringBuilder getRealContent(CharSequence content) {
        // 处理给定的数据，识别模式串
        mFormatData = formatData(content.toString());
        // 用来计算内容的大小
        mDynamicLayout =
                new DynamicLayout(mFormatData.getFormattedContent(), mPaint, mWidth, Layout.Alignment.ALIGN_NORMAL, 1.2f, 0.0f,
                        true);

        // 获取行数
        mLineCount = mDynamicLayout.getLineCount();

        if (onGetLineCountListener != null) {
            onGetLineCountListener.onGetLineCount(mLineCount, mLineCount > mMaxLines);
        }

        if (!mNeedExpend || mLineCount <= mMaxLines) {
            // 不需要展开功能 直接处理链接模块
            return dealLink(mFormatData, false);
        } else {
            return dealLink(mFormatData, true);
        }
    }

    /**
     * 对传入的数据进行正则匹配并处理
     */
    private FormatData formatData(String content) {
        FormatData formatData = new FormatData();
        if (mParserList.isEmpty() || content == null) {        // 如果无解析器，返回元数据
            formatData.setFormattedContent(content);
            return formatData;
        }
        List<FormatData.PositionData> data = new ArrayList<>();
        Map<String, String> convert = new HashMap<>();
        String dealContent = content;

        for (IParser parser : mParserList) {
            String c = parser.parse(dealContent, data, convert);
            if (!TextUtils.isEmpty(c)) {
                dealContent = c;
            }
        }

        if (!convert.isEmpty() && dealContent != null) {        // 如果处理过程中有替换操作
            for (Map.Entry<String, String> entry : convert.entrySet()) {
                dealContent = dealContent.replaceAll(entry.getKey(), entry.getValue());
            }
        }
        formatData.setFormattedContent(dealContent);
        formatData.setPositionData(data);
        return formatData;
    }

    /**
     * 设置最后的收起文案
     */
    private String getExpandEndContent() {
        if (TextUtils.isEmpty(mEndExpandContent)) {
            return String.format(Locale.getDefault(), "  %s",
                    mFoldBtnString);
        } else {
            return String.format(Locale.getDefault(), "  %s  %s",
                    mEndExpandContent, mFoldBtnString);
        }
    }

    /**
     * 设置展开的文案
     */
    private String getHideEndContent() {
        if (TextUtils.isEmpty(mEndExpandContent)) {
            return String.format(Locale.getDefault(), mNeedAlwaysShowRight ? "...  %s" : "...  %s",
                    mExpandBtnString);
        } else {
            return String.format(Locale.getDefault(), mNeedAlwaysShowRight ? "...  %s  %s" : "...  %s  %s",
                    mEndExpandContent, mExpandBtnString);
        }
    }

    /**
     * 处理文字中的链接、@等
     */
    private SpannableStringBuilder dealLink(FormatData formatData, boolean expandable) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        //获取存储的状态
        if (mStatus != null) {
            boolean isHide = mStatus == StatusType.STATUS_FOLD;

            if (isHide) {
                currentLines = mMaxLines;
            } else {
                currentLines = mMaxLines + ((mLineCount - mMaxLines));
            }
        }
        //处理折叠操作
        if (expandable) {
            if (currentLines < mLineCount && currentLines > 0) {
                int index = currentLines - 1;
                int endPosition = mDynamicLayout.getLineEnd(index);
                int startPosition = mDynamicLayout.getLineStart(index);
                float lineWidth = mDynamicLayout.getLineWidth(index);

                Log.e("----------", "index: " + index + ", endPos: " + endPosition + ", startPos:" + startPosition + ", lineWidth:" + lineWidth + ", view w:" + getWidth() + ", real w: " + mWidth);
                String endString = getHideEndContent();

                //计算原内容被截取的位置下标
                int fitPosition =
                        getFitPosition(endString, endPosition, startPosition, mWidth, mPaint.measureText(endString), 0);
                Log.e("----------", "fit position: " + fitPosition);

                String substring = formatData.getFormattedContent().substring(0, fitPosition);
                if (substring.endsWith("\n")) {
                    substring = substring.substring(0, substring.length() - "\n".length());
                }
                ssb.append(substring);

                if (mNeedAlwaysShowRight) {
                    //计算一下最后一行有没有充满
                    /*float lastLineWidth = 0;
                    for (int i = 0; i < index; i++) {
                        lastLineWidth += mDynamicLayout.getLineWidth(i);
                    }

                    lastLineWidth = lastLineWidth / (index);*/
                    float emptyWidth = mWidth - lineWidth - mPaint.measureText(endString);
                    if (emptyWidth > 0) {
                        float measureText = mPaint.measureText(Space);
                        int count = 0;
                        while (measureText * count < emptyWidth) {
                            count++;
                        }
                        Log.e("-------", "count: " + count);
                        count = count - 1;
                        for (int i = 0; i < count; i++) {
                            ssb.append(Space);
                        }
                    }
                }

                //在被截断的文字后面添加 "展开"
                ssb.append(endString);

                int expendLength = TextUtils.isEmpty(mEndExpandContent) ? 0 : 2 + mEndExpandContent.length();
                ssb.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        if (needRealExpandOrFold) {
                            action(StatusType.STATUS_EXPAND);
                        }
                        if (expandOrContractClickListener != null) {
                            expandOrContractClickListener.onClick(StatusType.STATUS_EXPAND);
                        }
                    }

                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setColor(mExpandBtnColor);
                        ds.setUnderlineText(false);
                    }
                }, ssb.length() - mExpandBtnString.length() - expendLength, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                if (mMoreArrow != null) {
                    ssb.setSpan(new CenterImageSpan(mMoreArrow, ImageSpan.ALIGN_BASELINE), ssb.length() - 1, ssb.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
            } else {
                ssb.append(formatData.getFormattedContent());
                if (mNeedFold) {
                    String endString = getExpandEndContent();

                    if (mNeedAlwaysShowRight) {
                        //计算一下最后一行有没有充满
                        int index = mDynamicLayout.getLineCount() - 1;
                        float lineWidth = mDynamicLayout.getLineWidth(index);
                        float lastLineWidth = 0;
                        for (int i = 0; i < index; i++) {
                            lastLineWidth += mDynamicLayout.getLineWidth(i);
                        }
                        lastLineWidth = lastLineWidth / (index);
                        float emptyWidth = lastLineWidth - lineWidth - mPaint.measureText(endString);
                        if (emptyWidth > 0) {
                            float measureText = mPaint.measureText(Space);
                            int count = 0;
                            while (measureText * count < emptyWidth) {
                                count++;
                            }
                            count = count - 1;
                            for (int i = 0; i < count; i++) {
                                ssb.append(Space);
                            }
                        }
                    }

                    ssb.append(endString);

                    int expendLength = TextUtils.isEmpty(mEndExpandContent) ? 0 : 2 + mEndExpandContent.length();
                    ssb.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            if (needRealExpandOrFold) {
                                action(StatusType.STATUS_FOLD);
                            }
                            if (expandOrContractClickListener != null) {
                                expandOrContractClickListener.onClick(StatusType.STATUS_FOLD);
                            }
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setColor(mFoldBtnColor);
                            ds.setUnderlineText(false);
                        }
                    }, ssb.length() - mFoldBtnString.length() - expendLength, ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    if (!TextUtils.isEmpty(mEndExpandContent)) {
                        ssb.append(mEndExpandContent);
                        ssb.setSpan(new ForegroundColorSpan(mEndExpandTextColor), ssb.length() - mEndExpandContent.length(), ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        } else {
            ssb.append(formatData.getFormattedContent());
            if (!TextUtils.isEmpty(mEndExpandContent)) {
                ssb.append(mEndExpandContent);
                ssb.setSpan(new ForegroundColorSpan(mEndExpandTextColor), ssb.length() - mEndExpandContent.length(), ssb.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }

        List<FormatData.PositionData> positionData = formatData.getPositionData();
        if (positionData != null) {
            for (FormatData.PositionData data : positionData) {
                if (ssb.length() >= data.getEnd()) {
                    int fitPosition = ssb.length() - getHideEndContent().length();
                    data.getParser().getSpan(ssb, data, fitPosition, mNeedExpend && expandable, currentLines < mLineCount);
                }
            }
        }
        //清除链接点击时背景效果
        setHighlightColor(Color.TRANSPARENT);
        Log.e("---------", ssb.toString());
        return ssb;
    }

    /**
     * 执行展开和收回的动作
     */
    private void action(StatusType status) {
        if (status == mStatus) {
            return;
        }
        mStatus = status;
        boolean isHide = currentLines < mLineCount;

        if (mNeedAnimation) {
            // 待实现动画
            if (isHide) {
                currentLines = mMaxLines + ((mLineCount - mMaxLines));
            } else {
                currentLines = mMaxLines;
            }
            setText(getRealContent(mContent));
        } else {
            if (isHide) {
                currentLines = mMaxLines + ((mLineCount - mMaxLines));
            } else {
                currentLines = mMaxLines;
            }
            setText(getRealContent(mContent));
        }
    }

    /**
     * 计算原内容被裁剪的长度
     *
     * @param endPosition   指定行最后文字的位置
     * @param startPosition 指定行文字开始的位置
     * @param lineWidth     指定行文字的宽度
     * @param endStringWith 最后添加的文字的宽度
     * @param offset        偏移量
     */
    private int getFitPosition(String endString, int endPosition, int startPosition, float lineWidth,
                               float endStringWith, float offset) {
        // 最后一行需要添加的文字的字数
        int position = (int) ((lineWidth - (endStringWith + offset)) * (endPosition - startPosition)
                / lineWidth);

        if (position <= endString.length()) return endPosition;

        // 计算最后一行需要显示的正文的长度
        float measureText = mPaint.measureText(
                (mFormatData.getFormattedContent().substring(startPosition, startPosition + position)));

        Log.e("---------", "measure text: " + measureText);
        // 如果最后一行需要显示的正文的长度比最后一行的长减去“展开”文字的长度要短就可以了  否则加个空格继续算
        if (measureText <= lineWidth - endStringWith) {
            return startPosition + position;
        } else {
            return getFitPosition(endString, endPosition, startPosition, lineWidth, endStringWith, offset + mPaint.measureText(Space));
        }
    }

    /**
     * 绑定状态
     */
    public void setStatus(StatusType status) {
        mStatus = status;
    }

    public static class LocalLinkMovementMethod extends LinkMovementMethod {
        static LocalLinkMovementMethod sInstance;


        public static LocalLinkMovementMethod getInstance() {
            if (sInstance == null)
                sInstance = new LocalLinkMovementMethod();

            return sInstance;
        }

        @Override
        public boolean onTouchEvent(TextView widget,
                                    Spannable buffer, MotionEvent event) {
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = buffer.getSpans(
                        off, off, ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }

                    if (widget instanceof ExpandableTextView) {
                        ((ExpandableTextView) widget).linkHit = true;
                    }
                    return true;
                } else {
                    Selection.removeSelection(buffer);
                    Touch.onTouchEvent(widget, buffer, event);
                    return false;
                }
            }
            return Touch.onTouchEvent(widget, buffer, event);
        }
    }

    boolean dontConsumeNonUrlClicks = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        linkHit = false;
        boolean res = super.onTouchEvent(event);

        if (dontConsumeNonUrlClicks)
            return linkHit;

        //防止选择复制的状态不消失
        if (action == MotionEvent.ACTION_UP) {
            this.setTextIsSelectable(false);
        }

        return res;
    }

    public interface OnGetLineCountListener {
        /**
         * lineCount 预估可能占有的行数
         * canExpand 是否达到可以展开的条件
         */
        void onGetLineCount(int lineCount, boolean canExpand);
    }

    private OnGetLineCountListener onGetLineCountListener;

    public OnGetLineCountListener getOnGetLineCountListener() {
        return onGetLineCountListener;
    }

    public void setOnGetLineCountListener(OnGetLineCountListener onGetLineCountListener) {
        this.onGetLineCountListener = onGetLineCountListener;
    }

    public interface OnExpandOrFoldClickListener {
        void onClick(StatusType type);
    }

    public boolean canFold() {
        return mNeedFold;
    }

    public void setCanFold(boolean canFold) {
        this.mNeedFold = canFold;
    }

    public boolean canExpand() {
        return mNeedExpend;
    }

    public void setCanExpend(boolean canExpend) {
        this.mNeedExpend = canExpend;
    }

    public boolean isNeedAnimation() {
        return mNeedAnimation;
    }

    public void setNeedAnimation(boolean mNeedAnimation) {
        this.mNeedAnimation = mNeedAnimation;
    }

    public void setMaxLines(int maxLines) {
        if (this.mMaxLines != maxLines) {
            this.mMaxLines = maxLines;
            doSetContent();
        }
    }

    public int getMaxLines() {
        return mMaxLines;
    }

    public void setExpandBtnColor(int expandBtnColor) {
        this.mExpandBtnColor = expandBtnColor;
    }

    public void setFoldBtnColor(int foldBtnColor) {
        this.mFoldBtnColor = foldBtnColor;
    }

    public void setExpandBtnString(String expandBtnString) {
        this.mExpandBtnString = expandBtnString;
    }

    public void setFoldBtnString(String foldBtnString) {
        this.mFoldBtnString = foldBtnString;
    }

    public void setAlwaysShowRight(boolean alwaysShowRight) {
        this.mNeedAlwaysShowRight = alwaysShowRight;
    }

    public void setNeedRealExpandOrFold(boolean needRealExpandOrFold) {
        this.needRealExpandOrFold = needRealExpandOrFold;
    }

    public void setExpandOrContractClickListener(OnExpandOrFoldClickListener expandOrContractClickListener) {
        this.expandOrContractClickListener = expandOrContractClickListener;
    }
}
