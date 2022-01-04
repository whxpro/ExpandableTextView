package com.whx.expandtextview;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.whx.expandtextview.widget.ExpandableTextView;
import com.whx.expandtextview.widget.StatusType;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = 20;
            }
        });
        recyclerView.setAdapter(new MAdapter());
    }

    static class Data {
        public String content;
        public boolean isExpand;

        Data(String content, boolean isExpand) {
            this.content = content;
            this.isExpand = isExpand;
        }
    }

    static class MAdapter extends RecyclerView.Adapter<MHolder> {
        String content = "    我所认识的中国，http://www.baidu.com 强大、友好 --习大大。@奥特曼 “一带一路”经济带带动了沿线国家的经济发展，促进我国与他国的友好往来和贸易发展，可谓“双赢”，\nGithub地址。 自古以来，中国以和平、友好的面孔示人。汉武帝派张骞出使西域，开辟 #丝绸之路，增进与西域各国的友好往来。http://www.baidu.com 胡麻、胡豆、香料等食材也随之传入中国，汇集于 #中华美食。@RNG 漠漠古道，驼铃阵阵，这条路奠定了“一带一路”的基础，让世界认识了中国。";
        List<Data> data = new ArrayList<>();

        MAdapter() {
            for (int i = 0; i < 20; i++) {
                data.add(new Data(content, false));
            }
        }
        @NonNull
        @Override
        public MHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MHolder(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull MHolder holder, int position) {
            holder.bindData(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    static class MHolder extends RecyclerView.ViewHolder {
        public MHolder(ViewGroup parent) {
            super(new ExpandableTextView(parent.getContext()));
            ExpandableTextView view = (ExpandableTextView) itemView;
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setCanExpend(true);
            view.setCanFold(true);
            view.setMaxLines(3);
            view.setBackgroundColor(Color.WHITE);
            view.setExpandBtnColor(Color.parseColor("#247FFF"));
            view.setFoldBtnColor(Color.parseColor("#FF2900"));
//            view.setPadding(20, 20, 20, 20);
        }

        public void bindData(Data data) {
            ExpandableTextView view = ((ExpandableTextView) itemView);
            view.setStatus(data.isExpand ? StatusType.STATUS_EXPAND : StatusType.STATUS_FOLD);
            view.setContent(data.content);
            view.setExpandOrContractClickListener(status -> {
                data.isExpand = status == StatusType.STATUS_EXPAND;
            });
        }
    }
}
