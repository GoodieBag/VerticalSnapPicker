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
    private int defaultTextColor, selectedTextColor, highlightLineColor;
    private int highlightLineThickness = 1;
    private int highlightLineMargin = 10;
    private int textSize = 14;
    VerticalSnapPickerListener mListener = null;

    int[][] states;
    int[] colors;
    ColorStateList textColorList;

    View previousSelected = null;
    private boolean paddingAdded = false;

    List<TextItem> textItems = new ArrayList<>();

    MarginLayoutParams lp;
    int padding;
    int startX, startY, endX, endY;
    Paint paint = new Paint();
    List<TextView> textViews = new ArrayList<>();


    private Handler handler;
    private boolean mIsFling, mIsScrolling, mIsSnapping;
    private LinearLayout parentLinearLayout;
    private Runnable mSnapRunner;
    private int mSelectedIndex = -1;
    private int mOffset = itemHeight;

    public VerticalSnapPicker(Context context) {
        this(context, null);
    }

    public VerticalSnapPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalSnapPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        itemHeight = (int) (itemHeight * getResources().getDisplayMetrics().density);
        handler = new Handler();
        mSnapRunner = new Runnable() {
            @Override
            public void run() {
                Log.d("SCROLLVIEW", "SNAP FROM Runner");
                snap();
            }
        };
        initAttributes(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        parentLinearLayout = new LinearLayout(context);
        parentLinearLayout.setOrientation(LinearLayout.VERTICAL);
        paint.setColor(highlightLineColor);
        paint.setStrokeWidth(highlightLineThickness);
        this.addView(parentLinearLayout);
        ViewGroup.LayoutParams lp = parentLinearLayout.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        parentLinearLayout.setLayoutParams(lp);
        //parentLinearLayout.setPadding(0,ITEM_HEIGHT,0,ITEM_HEIGHT);

    }

    private void initAttributes(Context context, AttributeSet attrs, int defStyleAttr) {

        if (attrs != null) {
            final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VerticalSnapPicker, defStyleAttr, 0);
            itemHeight = (int) array.getDimension(R.styleable.VerticalSnapPicker_item_height, itemHeight);
            mOffset = (int) array.getDimension(R.styleable.VerticalSnapPicker_item_vertical_offset, itemHeight);
            defaultTextColor = array.getColor(R.styleable.VerticalSnapPicker_default_text_color, Color.BLACK);
            selectedTextColor = array.getColor(R.styleable.VerticalSnapPicker_selected_text_color, defaultTextColor);
            highlightLineColor = array.getColor(R.styleable.VerticalSnapPicker_highlight_line_color, selectedTextColor);
            highlightLineThickness = (int) array.getDimension(R.styleable.VerticalSnapPicker_highlight_line_thickness, (int) (highlightLineThickness * DENSITY));
            highlightLineMargin = (int) array.getDimension(R.styleable.VerticalSnapPicker_highlight_line_side_margin, (int) (highlightLineMargin * DENSITY));
            textSize = (int) array.getDimension(R.styleable.VerticalSnapPicker_text_size, textSize);
            array.recycle();
        }

        states = new int[][]{
                new int[]{android.R.attr.state_selected}, // enabled
                new int[]{-android.R.attr.state_selected}, // disabled
        };

        colors = new int[]{
                selectedTextColor,
                defaultTextColor
        };

        textColorList = new ColorStateList(states, colors);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(mOffset*2+itemHeight, MeasureSpec.EXACTLY));
        //setMeasuredDimension(getMeasuredWidth(), NUMBER_VIEWS_VISIBLE*ITEM_HEIGHT);
        padding = (getMeasuredHeight() - itemHeight) / 2;

        Log.d("MEASURE", getMeasuredHeight() + " - " + itemHeight);
        if (getMeasuredHeight() > 600) {
            Log.d("AJHGDhja", "kjshdkad");
        }

        if (!paddingAdded) {
            View v;

            if (parentLinearLayout.getChildCount() > 0) {
                v = parentLinearLayout.getChildAt(0);
                if (v.getLayoutParams() instanceof MarginLayoutParams) {
                    lp = (MarginLayoutParams) v.getLayoutParams();
                    lp.setMargins(lp.leftMargin, padding, lp.rightMargin, lp.bottomMargin);
                    v.setLayoutParams(lp);
                }
                v = parentLinearLayout.getChildAt(parentLinearLayout.getChildCount() - 1);
                if (v.getLayoutParams() instanceof MarginLayoutParams) {
                    lp = (MarginLayoutParams) v.getLayoutParams();
                    lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, padding);
                    v.setLayoutParams(lp);
                }
                paddingAdded = true;
            }
        }
    }


    @Override
    public void fling(int velocityY) {
        /*super.fling(velocityY);
        handler.removeCallbacks(mSnapRunner);
        Log.d("SCROLLVIEW", "Fling");
        mIsFling = true;*/

    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        mIsScrolling = true;
        if (mIsFling) {
            if (Math.abs(y - oldY) < 2 || y == 0) {
                //Log.d("SCROLLVIEW", "Ended: " + Math.abs(y - oldY) + " " + (y >= getMeasuredHeight()) + " " + y);
                mIsFling = false;
                Log.d("SCROLLVIEW", "SNAP FROM Fling");
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
                    handler.postDelayed(mSnapRunner, 50);
                }

        }
        return super.onTouchEvent(e);
    }

    private void snap() {
        if (previousSelected != null) {
            previousSelected.setSelected(false);
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
        if (mSelectedIndex != 0 || mSelectedIndex <= textItems.size()) {
            TextView textView;
            textView = textViews.get(mSelectedIndex);
            textView.setSelected(true);
        }
        previousSelected = textViews.get(mSelectedIndex);
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
        //setScrollY(mSelectedIndex * ITEM_HEIGHT);
        Log.d("SCROLLVIEW", "ScrollY: " + getScrollY() + " " + itemHeight);
    }


    private void snapTo(int index) {
        if (index < 0 || index >= textItems.size())
            return;


        if (previousSelected != null) {
            previousSelected.setSelected(false);
        }

        mIsScrolling = false;
        mIsFling = false;

        if (mIsSnapping)
            return;

        mIsSnapping = true;
        int cst = getScrollY();
        mSelectedIndex = index;
        final int stt = mSelectedIndex * itemHeight;
//        if (mListener != null) {
//            mListener.onSnap(mSelectedIndex);
//        }
        if (mSelectedIndex != 0 || mSelectedIndex <= textItems.size()) {
            TextView textView;
            textView = textViews.get(mSelectedIndex);
            textView.setSelected(true);
        }
        previousSelected = textViews.get(mSelectedIndex);
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
        //setScrollY(mSelectedIndex * ITEM_HEIGHT);
        Log.d("SCROLLVIEW", "ScrollY: " + getScrollY() + " " + itemHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        startX = highlightLineMargin;
        startY = getScrollY() + padding;
        endX = startX + canvas.getWidth() - 2 * startX;
        endY = startY;

        canvas.drawLine(startX, startY, endX, endY, paint);
        canvas.drawLine(startX, startY + itemHeight, endX, endY + itemHeight, paint);


    }

    public List<TextItem> getList() {
        return textItems;
    }

    public void setList(List<TextItem> list) {
        this.textItems = list;
        parentLinearLayout.removeAllViews();
        paddingAdded = false;
        for (int i = 0; i < textItems.size(); i++) {
            TextItem item = textItems.get(i);
            TextView text = new TextView(getContext());
            text.setText(item.getText());
            Typeface typeface = Typeface.createFromAsset(getResources().getAssets(), item.getFont());
            text.setTypeface(typeface);
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            text.setTextColor(textColorList);
            text.setGravity(Gravity.CENTER);
            parentLinearLayout.addView(text);
            text.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
            textViews.add(text);
        }
        textViews.get(0).setSelected(true);
        previousSelected = textViews.get(0);
    }

    public static class TextItem {
        private String text;
        private String font;

        public TextItem(String text, String font) {
            this.text = text;
            this.font = font;
        }

        public String getText() {
            return text;
        }

        public String getFont() {
            return font;
        }
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    public int getDefaultTextColor() {
        return defaultTextColor;
    }

    public void setDefaultTextColor(int defaultTextColor) {
        this.defaultTextColor = defaultTextColor;
        invalidate();
    }

    public int getSelectedTextColor() {
        return selectedTextColor;
    }

    public void setSelectedTextColor(int selectedTextColor) {
        this.selectedTextColor = selectedTextColor;
        invalidate();
    }

    public int getHighlightLineColor() {
        return highlightLineColor;
    }

    public void setHighlightLineColor(int highlightLineColor) {
        this.highlightLineColor = highlightLineColor;
        invalidate();
    }

    public int getHighlightLineThickness() {
        return highlightLineThickness;
    }

    public void setHighlightLineThickness(int highlightLineThickness) {
        this.highlightLineThickness = highlightLineThickness;
        invalidate();
    }

    public int getHighlightLineMargin() {
        return highlightLineMargin;
    }

    public void setHighlightLineMargin(int highlightLineMargin) {
        this.highlightLineMargin = highlightLineMargin;
        invalidate();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
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
}
