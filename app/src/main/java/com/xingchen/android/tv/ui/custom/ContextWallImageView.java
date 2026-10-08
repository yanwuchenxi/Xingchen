package com.xingchen.android.tv.ui.custom;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/** 详情页视频海报背景：按宽高比居中裁剪铺满 */
public class ContextWallImageView extends AppCompatImageView {

    private final Matrix matrix = new Matrix();

    public ContextWallImageView(Context context) {
        this(context, null);
    }

    public ContextWallImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContextWallImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        updateMatrix();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateMatrix();
    }

    private void updateMatrix() {
        Drawable drawable = getDrawable();
        int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (drawable == null || viewWidth <= 0 || viewHeight <= 0) return;
        int dw = drawable.getIntrinsicWidth();
        int dh = drawable.getIntrinsicHeight();
        if (dw <= 0 || dh <= 0) return;
        float viewRatio = viewWidth / (float) viewHeight;
        float drawableRatio = dw / (float) dh;
        boolean narrow = drawableRatio < Math.min(1.2f, viewRatio * 0.75f);
        float scale;
        float dx;
        float dy;
        if (narrow) {
            scale = viewWidth / (float) dw;
            dx = 0f;
            dy = 0f;
        } else {
            scale = Math.max(viewWidth / (float) dw, viewHeight / (float) dh);
            dx = (viewWidth - dw * scale) * 0.5f;
            dy = (viewHeight - dh * scale) * 0.5f;
        }
        matrix.reset();
        matrix.setScale(scale, scale);
        matrix.postTranslate(Math.round(dx) + getPaddingLeft(), Math.round(dy) + getPaddingTop());
        setImageMatrix(matrix);
    }
}
