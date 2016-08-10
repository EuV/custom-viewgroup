package ru.yandex.yamblz.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class LightweightHorizontalLayout extends ViewGroup {
    private static final String TAG = LightweightHorizontalLayout.class.getSimpleName();
    private static final String ERROR_MSG = " supports at most one child with layout_width set to 'match_parent'!";

    private static final int NONE = -1;

    private int[] childrenWidth;
    private int[] childrenHeight;
    private int childMaxHeight;

    public LightweightHorizontalLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMeasured = 0;
        int specialChildIndex = NONE;
        int limitlessSpec = MeasureSpec.makeMeasureSpec(0, UNSPECIFIED);

        childrenWidth = new int[getChildCount()];
        childrenHeight = new int[childrenWidth.length];

        for (int i = 0; i < childrenWidth.length; i++) {
            View child = getChildAt(i);

            if (child.getVisibility() == GONE) continue;

            child.measure(limitlessSpec, limitlessSpec);

            childrenWidth[i] = computeChildSize(child, true);
            if (childrenWidth[i] == MATCH_PARENT) {
                if (specialChildIndex != NONE) throw new IllegalArgumentException(TAG + ERROR_MSG);
                specialChildIndex = i;
            } else {
                widthMeasured += childrenWidth[i];
            }

            childrenHeight[i] = computeChildSize(child, false);
            childMaxHeight = Math.max(childrenHeight[i], childMaxHeight);
        }

        int horizontalPadding = getPaddingLeft() + getPaddingRight();
        int contentMaxWidth = MeasureSpec.getSize(widthMeasureSpec) - horizontalPadding;

        widthMeasured = Math.min(widthMeasured, contentMaxWidth);

        if (specialChildIndex != NONE) {
            childrenWidth[specialChildIndex] = contentMaxWidth - widthMeasured;
            widthMeasured = contentMaxWidth;
        }

        int verticalPadding = getPaddingTop() + getPaddingBottom();
        int contentMaxHeight = MeasureSpec.getSize(heightMeasureSpec) - verticalPadding;

        int heightMeasured;
        if (MeasureSpec.getMode(heightMeasureSpec) == EXACTLY) {
            heightMeasured = childMaxHeight = contentMaxHeight;
        } else {
            heightMeasured = Math.min(childMaxHeight, contentMaxHeight);
        }

        setMeasuredDimension(widthMeasured + horizontalPadding, heightMeasured + verticalPadding);
    }


    private int computeChildSize(View child, boolean isWidth) {
        LayoutParams params = child.getLayoutParams();
        int size = isWidth ? params.width : params.height;

        switch (size) {
            case MATCH_PARENT:
                return MATCH_PARENT;

            case WRAP_CONTENT:
                return isWidth ? child.getMeasuredWidth() : child.getMeasuredHeight();

            default:
                return size;
        }
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int drawn = getPaddingLeft();
        int topPadding = getPaddingTop();

        for (int i = 0; i < this.getChildCount(); i++) {
            int width = childrenWidth[i];

            int height = childrenHeight[i];
            if (height == MATCH_PARENT) {
                height = childMaxHeight;
            }

            getChildAt(i).layout(drawn, topPadding, drawn + width, height + topPadding);

            drawn += width;
        }
    }
}
