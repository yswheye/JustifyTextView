package com.heye.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

public class JustifyTextView extends AppCompatTextView {
    private static final String TAG = "_haha";
    private float mLineY;
    private int mViewWidth;
    private float lineHeight;
    private TextPaint mPaint;
    private int lineCount;
    private boolean ellipsizeEnd;
    private boolean measured;
    private int maxLines = -1;

    public JustifyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Layout layout = getLayout();
        mPaint = getPaint();
        mPaint.setTextSize(getTextSize());
        mPaint.setColor(getCurrentTextColor());
        mPaint.drawableState = getDrawableState();
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        int textHeight = (int) (Math.ceil(fm.descent - fm.ascent));
        lineHeight = (int) (textHeight * layout.getSpacingMultiplier() + layout.getSpacingAdd());
        /**
         * 设置过maxLines，先通过MAX_VALUE测量出实际全部显示内容的行数
         */
        if (!measured && (getMaxLines() != Integer.MAX_VALUE || getMaxLines() != 0)) {
            if (maxLines == -1) {
                maxLines = getMaxLines();
            }
            measured = true;
            setMaxLines(Integer.MAX_VALUE);
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
            return;
        }
        //获取实际全部显示内容的行数
        lineCount = layout.getLineCount();
        Log.d(TAG, "onMeasure1: max = " + maxLines + ", lines = " + lineCount);
        //设置过maxLines属性
        if (maxLines != Integer.MAX_VALUE && maxLines < lineCount) {
            ellipsizeEnd = true;
            lineCount = maxLines;
        }
        Log.d(TAG, "onMeasure2: max = " + maxLines + ", lines = " + lineCount + "， ellipsizeEnd = " + ellipsizeEnd);
        int viewHeight = (int) (lineHeight * lineCount - Math.ceil(fm.bottom - fm.leading));
        setMeasuredDimension(getMeasuredWidth(), viewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mViewWidth = getMeasuredWidth();
        String text = getText().toString();
        mLineY = getTextSize();
        Layout layout = getLayout();

        // layout.getLayout()在4.4.3出现NullPointerException
        if (layout == null) {
            return;
        }

        for (int i = 0; i < lineCount; i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
            String line = text.substring(lineStart, lineEnd);
            //处理ellipsize end ...
            if (ellipsizeEnd) {
                if (i == lineCount - 1 && line.length() > 2) {
                    line = line.substring(0, line.length() - 1) + "...";
                }
            }
            Log.d(TAG, (i+1) + " lineCount = " + lineCount + ", line = " + line + "，mLineY = " + mLineY);
            if (needScale(line) && (ellipsizeEnd || i < lineCount - 1)) {
                drawScaledText(canvas, line, width);
            } else {
                canvas.drawText(line, 0, mLineY, mPaint);
            }
            mLineY += lineHeight;
        }
    }

    private void drawScaledText(Canvas canvas, String line, float lineWidth) {
        float x = 0;
        if (isFirstLineOfParagraph(line)) {
            String blanks = "  ";
            canvas.drawText(blanks, x, mLineY, getPaint());
            float bw = StaticLayout.getDesiredWidth(blanks, getPaint());
            x += bw;

            line = line.substring(3);
        }

        int gapCount = line.length() - 1;
        int i = 0;
        if (line.length() > 2 && line.charAt(0) == 12288 && line.charAt(1) == 12288) {
            String substring = line.substring(0, 2);
            float cw = StaticLayout.getDesiredWidth(substring, getPaint());
            canvas.drawText(substring, x, mLineY, getPaint());
            x += cw;
            i += 2;
        }

        float d = (mViewWidth - lineWidth) / gapCount;
        for (; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, getPaint());
            canvas.drawText(c, x, mLineY, getPaint());
            x += cw + d;
        }
    }

    private boolean isFirstLineOfParagraph(String line) {
        return line.length() > 3 && line.charAt(0) == ' ' && line.charAt(1) == ' ';
    }

    private boolean needScale(String line) {
        if (line == null || line.length() == 0) {
            return false;
        } else {
            return line.charAt(line.length() - 1) != '\n';
        }
    }

}
