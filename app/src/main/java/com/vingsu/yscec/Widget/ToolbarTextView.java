package com.vingsu.yscec.Widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ToolbarTextView extends TextView {

    private Context mContext;
    private Point size;

    public ToolbarTextView(Context context) {
        super(context);
        this.mContext = context;
        size = new Point();
    }

    public ToolbarTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        size = new Point();
    }

    public ToolbarTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
        size = new Point();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = this.getWidth();
        int parentLeft = ((RelativeLayout)this.getParent()).getLeft();

        if(width > 0 && parentLeft > 0){
            Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            display.getSize(size);
            int screenWidth = size.x;
            int centerAbs = screenWidth/2 - width/2 - parentLeft;
            this.setLeft(centerAbs);
        }
    }
}
