package com.whx.expandtextview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.whx.expandtextview.widget.ExpandableTextView;
import com.whx.expandtextview.widget.StatusType;
import com.whx.expandtextview.widget.parser.MentionParser;
import com.whx.expandtextview.widget.parser.WebLinkParser;

public class MainActivity extends AppCompatActivity {

    private ExpandableTextView mExpandTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String content = "    我所认识的中国，http://www.baidu.com 强大、友好 --习大大。@奥特曼 “一带一路”经济带带动了沿线国家的经济发展，促进我国与他国的友好往来和贸易发展，可谓“双赢”，Github地址。 自古以来，中国以和平、友好的面孔示人。汉武帝派张骞出使西域，开辟 #丝绸之路，增进与西域各国的友好往来。http://www.baidu.com 胡麻、胡豆、香料等食材也随之传入中国，汇集于 #中华美食。@RNG 漠漠古道，驼铃阵阵，这条路奠定了“一带一路”的基础，让世界认识了中国。";
        mExpandTv = findViewById(R.id.test_tv);
        mExpandTv.setContent(content);

        mExpandTv.setNeedRealExpandOrFold(false);
        mExpandTv.setExpandOrContractClickListener(type -> {
            if (type.equals(StatusType.STATUS_FOLD)) {
                Toast.makeText(this, "收回操作，不真正触发收回操作", Toast.LENGTH_SHORT).show();

            } else {
                if (mExpandTv.getMaxLines() == 5) {
                    mExpandTv.setExpand(true);
                } else {
                    mExpandTv.setMaxLines(5);
                }
            }
        });

        WebLinkParser parser = new WebLinkParser();
        parser.setLinkTextColor(Color.parseColor("#FF6200"));
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.mipmap.link, null);
        drawable.setBounds(0, 0, 30, 30); //必须设置图片大小，否则不显示
        parser.setWebLinkIcon(drawable);
        mExpandTv.addParser(parser);

        MentionParser mentionParser = new MentionParser();
        mentionParser.setMentionTextColor(Color.parseColor("#FF6200"));
        mExpandTv.addParser(mentionParser);

        findViewById(R.id.fold_btn).setOnClickListener(v -> {
            mExpandTv.setExpand(false);
        });

        findViewById(R.id.to_list).setOnClickListener(v -> {
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        });
    }

    public int dp2px(double dip) {
        Context context = this;
        if (context != null && context.getResources() != null && context.getResources().getDisplayMetrics() != null) {
            double density = (context.getResources().getDisplayMetrics()).density;
            return (int) (density * dip + 0.5D);
        }
        return 0;
    }
}