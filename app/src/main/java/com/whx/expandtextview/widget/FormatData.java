package com.whx.expandtextview.widget;

import com.whx.expandtextview.widget.parser.IParser;

import java.util.List;

/**
 * 记录可以点击的内容 和 位置
 */
public class FormatData {
    private String formattedContent;
    private List<PositionData> positionData;

    public String getFormattedContent() {
        return formattedContent;
    }

    public void setFormattedContent(String formattedContent) {
        this.formattedContent = formattedContent;
    }

    public List<PositionData> getPositionData() {
        return positionData;
    }

    public void setPositionData(List<PositionData> positionData) {
        this.positionData = positionData;
    }

    public static class PositionData {
        private int start;
        private int end;
        private String url;
        private IParser mParser;

        public PositionData(int start, int end, String url, IParser parser) {
            this.start = start;
            this.end = end;
            this.url = url;
            this.mParser = parser;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public IParser getParser() {
            return mParser;
        }

        public void setParser(IParser mParser) {
            this.mParser = mParser;
        }
    }
}
