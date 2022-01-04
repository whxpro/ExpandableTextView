package com.whx.expandtextview.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

/**
 * 自定义ImageSpan 让Image 在行内居中显示
 */
public class CenterImageSpan extends ImageSpan {
    private final Drawable drawable;

    public CenterImageSpan(Drawable d, int verticalAlignment) {
        super(d, verticalAlignment);
        this.drawable = d;
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text,
                     int start, int end, float x,
                     int top, int y, int bottom, @NonNull Paint paint) {
        // image to draw
        Drawable b = getDrawable();
        // font metrics of text to be replaced
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        int transY = (y + fm.descent + y + fm.ascent) / 2
                - b.getBounds().bottom / 2;
        canvas.save();
        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }
}
