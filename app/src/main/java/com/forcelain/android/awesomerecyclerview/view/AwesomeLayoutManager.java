package com.forcelain.android.awesomerecyclerview.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

public class AwesomeLayoutManager extends RecyclerView.LayoutManager {

    public enum Orientation {VERTICAL, HORIZONTAL}

    private static final float SCALE_THRESHOLD_PERCENT = 0.66f;
    private static final float ITEM_HEIGHT_PERCENT = 0.75f;
    private SparseArray<View> viewCache = new SparseArray<>();
    private Orientation orientation = Orientation.VERTICAL;
    private int mAnchorPos;

    public AwesomeLayoutManager(Context context) {

    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        View anchorView = getAnchorView();
        mAnchorPos = anchorView != null ? getPosition(anchorView) : 0;
        if (orientation != null){
            this.orientation = orientation;
        }
        requestLayout();
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);
        fill(recycler);
        mAnchorPos = 0;
    }

    private void fill(RecyclerView.Recycler recycler) {

        View anchorView = getAnchorView();
        viewCache.clear();
        for (int i = 0, cnt = getChildCount(); i < cnt; i++) {
            View view = getChildAt(i);
            int pos = getPosition(view);
            viewCache.put(pos, view);
        }

        for (int i = 0; i < viewCache.size(); i++) {
            detachView(viewCache.valueAt(i));
        }

        switch (orientation) {

            case VERTICAL:
                fillUp(anchorView, recycler);
                fillDown(anchorView, recycler);
                break;
            case HORIZONTAL:
                fillLeft(anchorView, recycler);
                fillRight(anchorView, recycler);
                break;
        }
        
        updateViewScale();
    }

    private void fillUp(@Nullable View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        } else {
            anchorPos = mAnchorPos;
        }

        boolean fillUp = true;
        int pos = anchorPos - 1;
        int viewBottom = anchorTop;
        int viewHeight = (int) (getHeight() * ITEM_HEIGHT_PERCENT);
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.AT_MOST);
        while (fillUp && pos >= 0){
            View view = viewCache.get(pos);
            if (view == null){
                view = recycler.getViewForPosition(pos);
                addView(view, 0);
                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                int decoratedMeasuredHeight = getDecoratedMeasuredHeight(view);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                layoutDecorated(view, 0, viewBottom - decoratedMeasuredHeight, decoratedMeasuredWidth, viewBottom);
            } else {
                attachView(view);
                viewCache.remove(pos);
            }
            viewBottom = getDecoratedTop(view);
            fillUp = (viewBottom > 0);
            pos--;
        }
    }

    private void fillDown(@Nullable View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos;
        int anchorTop = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorTop = getDecoratedTop(anchorView);
        } else {
            anchorPos = mAnchorPos;
        }

        int pos = anchorPos;
        boolean fillDown = true;
        int height = getHeight();
        int viewTop = anchorTop;
        int itemCount = getItemCount();
        int viewHeight = (int) (getHeight() * ITEM_HEIGHT_PERCENT);
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.AT_MOST);

        while (fillDown && pos < itemCount){
            View view = viewCache.get(pos);
            if (view == null){
                view = recycler.getViewForPosition(pos);
                addView(view);
                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                int decoratedMeasuredHeight = getDecoratedMeasuredHeight(view);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                layoutDecorated(view, 0, viewTop, decoratedMeasuredWidth, viewTop + decoratedMeasuredHeight);
            } else {
                attachView(view);
                viewCache.remove(pos);
            }
            viewTop = getDecoratedBottom(view);
            fillDown = viewTop <= height;
            pos++;
        }
    }

    private void fillLeft(@Nullable View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos;
        int anchorLeft = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorLeft = getDecoratedLeft(anchorView);
        } else {
            anchorPos = mAnchorPos;
        }

        boolean fillLeft = true;
        int pos = anchorPos - 1;
        int viewRight = anchorLeft;
        int width = getWidth();
        int height = getHeight();
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);
        while (fillLeft && pos >= 0){
            View view = viewCache.get(pos);
            if (view == null){
                view = recycler.getViewForPosition(pos);
                addView(view, 0);
                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                int decoratedMeasuredHeight = getDecoratedMeasuredHeight(view);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                layoutDecorated(view, viewRight - decoratedMeasuredWidth, 0, viewRight, decoratedMeasuredHeight);
            } else {
                attachView(view);
                viewCache.remove(pos);
            }
            viewRight = getDecoratedLeft(view);
            fillLeft = (viewRight > 0);
            pos--;
        }
    }

    private void fillRight(View anchorView, RecyclerView.Recycler recycler) {
        int anchorPos;
        int anchorLeft = 0;
        if (anchorView != null){
            anchorPos = getPosition(anchorView);
            anchorLeft = getDecoratedLeft(anchorView);
        } else {
            anchorPos = mAnchorPos;
        }

        int pos = anchorPos;
        boolean fillRight = true;
        int viewLeft = anchorLeft;
        int itemCount = getItemCount();
        int width = getWidth();
        int height = getHeight();
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);

        while (fillRight && pos < itemCount){
            View view = viewCache.get(pos);
            if (view == null){
                view = recycler.getViewForPosition(pos);
                addView(view);
                measureChildWithDecorationsAndMargin(view, widthSpec, heightSpec);
                int decoratedMeasuredHeight = getDecoratedMeasuredHeight(view);
                int decoratedMeasuredWidth = getDecoratedMeasuredWidth(view);
                layoutDecorated(view, viewLeft, 0, viewLeft + decoratedMeasuredWidth, decoratedMeasuredHeight);
            } else {
                attachView(view);
                viewCache.remove(pos);
            }
            viewLeft = getDecoratedRight(view);
            fillRight = viewLeft <= width;
            pos++;
        }
    }

    private void updateViewScale() {
        int childCount = getChildCount();
        int height = getHeight();
        int thresholdPx = (int) (height * SCALE_THRESHOLD_PERCENT);
        for (int i = 0; i < childCount; i++) {
            float scale = 1f;
            View view = getChildAt(i);
            int viewTop = getDecoratedTop(view);
            if (viewTop >= thresholdPx){
                int delta = viewTop - thresholdPx;
                scale = (height - delta) / (float)height;
                scale = Math.max(scale, 0);
            }
            view.setPivotX(view.getHeight()/2);
            view.setPivotY(view.getHeight() / -2);
            view.setScaleX(scale);
            view.setScaleY(scale);
        }
    }

    private View getAnchorView() {
        int childCount = getChildCount();
        HashMap<Integer, View> viewsOnScreen = new HashMap<>();
        Rect mainRect = new Rect(0, 0, getWidth(), getHeight());
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            int top = getDecoratedTop(view);
            int bottom = getDecoratedBottom(view);
            int left = getDecoratedLeft(view);
            int right = getDecoratedRight(view);
            Rect viewRect = new Rect(left, top, right, bottom);
            boolean intersect = viewRect.intersect(mainRect);
            if (intersect){
                int square = viewRect.width() * viewRect.height();
                viewsOnScreen.put(square, view);
            }
        }
        if (viewsOnScreen.isEmpty()){
            return null;
        }
        Integer maxSquare = null;
        for (Integer square : viewsOnScreen.keySet()) {
            if (maxSquare == null){
                maxSquare = square;
            } else {
                maxSquare = Math.max(maxSquare, square);
            }
        }
        return viewsOnScreen.get(maxSquare);
    }

    @Override
    public boolean canScrollVertically() {
        return orientation == Orientation.VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return orientation == Orientation.HORIZONTAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int delta = scrollHorizontallyInternal(dx);
        offsetChildrenHorizontal(-delta);
        fill(recycler);
        return delta;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int delta = scrollVerticallyInternal(dy);
        offsetChildrenVertical(-delta);
        fill(recycler);
        return delta;
    }

    private int scrollVerticallyInternal(int dy) {
        int childCount = getChildCount();
        int itemCount = getItemCount();
        if (childCount == 0){
            return 0;
        }

        final View topView = getChildAt(0);
        final View bottomView = getChildAt(childCount - 1);

        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
        if (viewSpan <= getHeight()) {
            return 0;
        }

        int delta = 0;
        if (dy < 0){
            View firstView = getChildAt(0);
            int firstViewAdapterPos = getPosition(firstView);
            if (firstViewAdapterPos > 0){
                delta = dy;
            } else {
                int viewTop = getDecoratedTop(firstView);
                delta = Math.max(viewTop, dy);
            }
        } else if (dy > 0){
            View lastView = getChildAt(childCount - 1);
            int lastViewAdapterPos = getPosition(lastView);
            if (lastViewAdapterPos < itemCount - 1){
                delta = dy;
            } else {
                int viewBottom = getDecoratedBottom(lastView);
                int parentBottom = getHeight();
                delta = Math.min(viewBottom - parentBottom, dy);
            }
        }
        return delta;
    }

    private int scrollHorizontallyInternal(int dx) {
        int childCount = getChildCount();
        int itemCount = getItemCount();
        if (childCount == 0){
            return 0;
        }

        final View leftView = getChildAt(0);
        final View rightView = getChildAt(childCount - 1);

        int viewSpan = getDecoratedRight(rightView) - getDecoratedLeft(leftView);
        if (viewSpan <= getWidth()) {
            return 0;
        }

        int delta = 0;
        if (dx < 0){
            View firstView = getChildAt(0);
            int firstViewAdapterPos = getPosition(firstView);
            if (firstViewAdapterPos > 0){
                delta = dx;
            } else {
                int viewLeft = getDecoratedLeft(firstView);
                delta = Math.max(viewLeft, dx);
            }
        } else if (dx > 0){
            View lastView = getChildAt(childCount - 1);
            int lastViewAdapterPos = getPosition(lastView);
            if (lastViewAdapterPos < itemCount - 1){
                delta = dx;
            } else {
                int viewRight = getDecoratedRight(lastView);
                delta = Math.min(viewRight - getWidth(), dx);
            }
        }
        return delta;
    }

    private void measureChildWithDecorationsAndMargin(View child, int widthSpec, int heightSpec) {
        Rect decorRect = new Rect();
        calculateItemDecorationsForChild(child, decorRect);
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        widthSpec = updateSpecWithExtra(widthSpec, lp.leftMargin + decorRect.left,
                lp.rightMargin + decorRect.right);
        heightSpec = updateSpecWithExtra(heightSpec, lp.topMargin + decorRect.top,
                lp.bottomMargin + decorRect.bottom);
        child.measure(widthSpec, heightSpec);
    }

    private int updateSpecWithExtra(int spec, int startInset, int endInset) {
        if (startInset == 0 && endInset == 0) {
            return spec;
        }
        final int mode = View.MeasureSpec.getMode(spec);
        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(
                    View.MeasureSpec.getSize(spec) - startInset - endInset, mode);
        }
        return spec;
    }
}
