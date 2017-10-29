package in.goodiebag.verticalsnappicker;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pavan on 30/06/17.
 */

public class VerticalSnapPicker extends ScrollView {
    private final float DENSITY = getContext().getResources().getDisplayMetrics().density;

    private int itemHeight = 100;
    @ColorInt
    private int mDefaultTextColor, mSelectedTextColor, mHighlightLineColor;
    private int mHighlightLineThickness = 1;
    private int mHighlightLineMargin = 10;
    private int mTextSize = 14;
    private VerticalSnapPickerListener mListener = null;

    private View mPreviousSelected = null;
    private boolean mPaddingAdded = false;

    private List<TextItem> mTextItems = new ArrayList<>();

    private int mPadding;
    private Paint mHighlightPaint;


    private Handler mHandler;
    private boolean mIsFling, mIsScrolling, mIsSnapping;
    private LinearLayout mParentLinearLayout;
    private Runnable mSnapRunner;
    private int mSelectedIndex = -1;
    private int mOffset;

    public VerticalSnapPicker(Context context) {
        this(context, null);
    }

    public VerticalSnapPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalSnapPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        itemHeight = (int) (itemHeight * getResources().getDisplayMetrics().density);
        mHandler = new Handler();
        mSnapRunner = new Runnable() {
            @Override
            public void run() {
                snap();
            }
        };
        initAttributes(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mParentLinearLayout = new LinearLayout(context);
        mParentLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mHighlightPaint=new Paint();
        mHighlightPaint.setColor(mHighlightLineColor);
        mHighlightPaint.setStrokeWidth(mHighlightLineThickness);
        this.addView(mParentLinearLayout);
        ViewGroup.LayoutParams lp = mParentLinearLayout.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParentLinearLayout.setLayoutParams(lp);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {

        if (attrs != null) {
            final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VerticalSnapPicker, defStyleAttr, 0);
            itemHeight = (int) array.getDimension(R.styleable.VerticalSnapPicker_item_height, itemHeight);
            mOffset = (int) array.getDimension(R.styleable.VerticalSnapPicker_item_vertical_offset, itemHeight);
            mDefaultTextColor = array.getColor(R.styleable.VerticalSnapPicker_default_text_color, Color.BLACK);
            mSelectedTextColor = array.getColor(R.styleable.VerticalSnapPicker_selected_text_color, mDefaultTextColor);
            mHighlightLineColor = array.getColor(R.styleable.VerticalSnapPicker_highlight_line_color, mSelectedTextColor);
            mHighlightLineThickness = (int) array.getDimension(R.styleable.VerticalSnapPicker_highlight_line_thickness, (int) (mHighlightLineThickness * DENSITY));
            mHighlightLineMargin = (int) array.getDimension(R.styleable.VerticalSnapPicker_highlight_line_side_margin, (int) (mHighlightLineMargin * DENSITY));
            mTextSize = (int) array.getDimension(R.styleable.VerticalSnapPicker_text_size, mTextSize);
            array.recycle();
        }
    }

    private ColorStateList getTextColorList() {

        return new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_selected}, // enabled
                new int[]{}, // disabled
        }, new int[]{
                this.mSelectedTextColor,
                this.mDefaultTextColor
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mOffset * 2 + itemHeight, MeasureSpec.EXACTLY));
        //setMeasuredDimension(getMeasuredWidth(), NUMBER_VIEWS_VISIBLE*ITEM_HEIGHT);
        mPadding = (getMeasuredHeight() - itemHeight) / 2;

        Log.d("MEASURE", getMeasuredHeight() + " - " + itemHeight);
        if (getMeasuredHeight() > 600) {
            Log.d("AJHGDhja", "kjshdkad");
        }

        if (!mPaddingAdded) {
            View v;

            if (mParentLinearLayout.getChildCount() > 0) {
                v = mParentLinearLayout.getChildAt(0);
                MarginLayoutParams lp;
                if (v.getLayoutParams() instanceof MarginLayoutParams) {
                    lp = (MarginLayoutParams) v.getLayoutParams();
                    lp.setMargins(lp.leftMargin, mPadding, lp.rightMargin, lp.bottomMargin);
                    v.setLayoutParams(lp);
                }
                v = mParentLinearLayout.getChildAt(mParentLinearLayout.getChildCount() - 1);
                if (v.getLayoutParams() instanceof MarginLayoutParams) {
                    lp = (MarginLayoutParams) v.getLayoutParams();
                    lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, mPadding);
                    v.setLayoutParams(lp);
                }
                mPaddingAdded = true;
            }
        }
    }


    @Override
    public void fling(int velocityY) {
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        mIsScrolling = true;
        if (mIsFling) {
            if (Math.abs(y - oldY) < 2 || y == 0) {
                mIsFling = false;
                snap();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.d("SCROLLVIEW", "UP");
                if (mIsScrolling) {
                    mHandler.postDelayed(mSnapRunner, 50);
                }

        }
        return super.onTouchEvent(e);
    }

    private void snap() {
        if (mPreviousSelected != null) {
            mPreviousSelected.setSelected(false);
        }
        mIsScrolling = false;
        mIsFling = false;
        if (mIsSnapping)
            return;
        mIsSnapping = true;
        int cst = getScrollY();
        mSelectedIndex = Math.round(((float) cst) / itemHeight);
        final int stt = mSelectedIndex * itemHeight;
        if (mListener != null) {
            mListener.onSnap(mSelectedIndex);
        }
        if (mSelectedIndex != 0 || mSelectedIndex <= mTextItems.size()) {
            TextView textView;
            textView = (TextView) mParentLinearLayout.getChildAt(mSelectedIndex);
            textView.setSelected(true);
        }
        mPreviousSelected = mParentLinearLayout.getChildAt(mSelectedIndex);
        ValueAnimator animator = ValueAnimator.ofInt(cst, stt);
        animator.setDuration(Math.abs(stt - cst) * 3);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                setScrollY(value);
                if (value == stt) {
                    mIsSnapping = false;
                }
            }
        });
        animator.start();
    }


    private void snapTo(int index) {
        if (index < 0 || index >= mTextItems.size())
            return;


        if (mPreviousSelected != null) {
            mPreviousSelected.setSelected(false);
        }

        mIsScrolling = false;
        mIsFling = false;

        if (mIsSnapping)
            return;

        mIsSnapping = true;
        int cst = getScrollY();
        mSelectedIndex = index;
        final int stt = mSelectedIndex * itemHeight;
        if (mSelectedIndex != 0 || mSelectedIndex <= mTextItems.size()) {
            TextView textView;
            textView = (TextView) mParentLinearLayout.getChildAt(mSelectedIndex);
            textView.setSelected(true);
        }
        mPreviousSelected = mParentLinearLayout.getChildAt(mSelectedIndex);
        ;
        ValueAnimator animator = ValueAnimator.ofInt(cst, stt);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();
                setScrollY(value);
                if (value == stt) {
                    mIsSnapping = false;
                }
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int startX = mHighlightLineMargin;
        int startY = getScrollY() + mPadding;
        int endX = startX + getWidth() - 2 * startX;
        int endY = startY;

        mHighlightPaint.setColor(mHighlightLineColor);
        canvas.drawLine(startX, startY, endX, endY, mHighlightPaint);
        canvas.drawLine(startX, startY + itemHeight, endX, endY + itemHeight, mHighlightPaint);
    }

    public List<TextItem> getList() {
        return mTextItems;
    }

    public void setList(@NonNull List<TextItem> list) {
        setList(list, 0);
    }

    public void setList(@NonNull List<TextItem> list, int selectedIndex) {
        this.mTextItems = list;
        mSelectedIndex = -1;
        mParentLinearLayout.removeAllViews();
        mPaddingAdded = false;
        for (int i = 0; i < mTextItems.size(); i++) {
            TextItem item = mTextItems.get(i);
            TextView text = new TextView(getContext());
            mParentLinearLayout.addView(text);
            setupTextView(text, item);
        }
        if (list.size() > 0) {
            setSelectedIndex(selectedIndex < list.size() && selectedIndex > 0 ? selectedIndex : 0);
        }
    }

    private void reload() {
        int selected = mSelectedIndex;
        setList(mTextItems, selected);
    }

    private void setupTextView(TextView text, TextItem item) {
        text.setText(item.getText());
        if (item.getFont() != null) {
            try {
                Typeface typeface = Typeface.createFromAsset(getResources().getAssets(), item.getFont());
                text.setTypeface(typeface);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        text.setTextColor(getTextColorList());
        text.setGravity(Gravity.CENTER);
        text.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
        reload();
        requestLayout();
    }

    public int getDefaultTextColor() {
        return mDefaultTextColor;
    }

    public void setDefaultTextColor(@ColorInt int defaultTextColor) {
        this.mDefaultTextColor = defaultTextColor;
        reload();
    }

    public int getSelectedTextColor() {
        return mSelectedTextColor;
    }

    public void setSelectedTextColor(@ColorInt int selectedTextColor) {
        this.mSelectedTextColor = selectedTextColor;
        reload();
    }

    public int getHighlightLineColor() {
        return mHighlightLineColor;
    }

    public void setHighlightLineColor(int highlightLineColor) {
        this.mHighlightLineColor = highlightLineColor;
        invalidate();
    }

    public int getHighlightLineThickness() {
        return mHighlightLineThickness;
    }

    public void setHighlightLineThickness(int highlightLineThickness) {
        this.mHighlightLineThickness = highlightLineThickness;
        invalidate();
    }

    public int getHighlightLineMargin() {
        return mHighlightLineMargin;
    }

    public void setHighlightLineMargin(int highlightLineMargin) {
        this.mHighlightLineMargin = highlightLineMargin;
        invalidate();
    }

    public int getTextSize() {
        return mTextSize;
    }

    public void setTextSize(int textSize) {
        this.mTextSize = textSize;
        for (int i = 0; i < mParentLinearLayout.getChildCount(); i++) {
            setupTextView((TextView) mParentLinearLayout.getChildAt(i), mTextItems.get(i));
        }
    }

    public int getVerticalOffset() {
        return mOffset;
    }

    public void setVerticalOffset(int mOffset) {
        this.mOffset = mOffset;
        requestLayout();
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int mSelectedIndex) {
        snapTo(mSelectedIndex);
    }

    public void setOnSnapListener(VerticalSnapPickerListener mListener) {
        this.mListener = mListener;
    }

    public interface VerticalSnapPickerListener {
        void onSnap(int position);
    }


    public static class TextItem {
        private String text;
        private String font;

        public TextItem(@NonNull String text) {
            this.text = text;
        }

        public TextItem(@NonNull String text, @NonNull String font) {
            this(text);
            this.font = font;
        }

        public String getText() {
            return text;
        }

        public String getFont() {
            return font;
        }
    }
}
