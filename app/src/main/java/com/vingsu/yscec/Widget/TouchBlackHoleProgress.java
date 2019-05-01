package com.vingsu.yscec.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class TouchBlackHoleProgress extends RelativeLayout {

    private boolean touch_disabled = true;

    public TouchBlackHoleProgress(Context context) {
        super(context);
        init(context);
    }

    public TouchBlackHoleProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TouchBlackHoleProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context c){
        ProgressBar pb = new ProgressBar(c);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(CENTER_IN_PARENT,TRUE);
        pb.setLayoutParams(lp);
        addView(pb);
    }

    public void disableTouch(boolean b){
        this.touch_disabled = b;
    }

    @Override
    public void setVisibility(int v) {
        super.setVisibility(v);
        if(v == GONE || v == INVISIBLE){
            disableTouch(false);
        }else if(v == VISIBLE){
            disableTouch(true);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touch_disabled;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

}
