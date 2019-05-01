package com.vingsu.yscec.Widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class TimeTableItem extends Button {

    private static int customMaxHeight = 0;

    public static int getCustomMaxHeight(){
        return customMaxHeight;
    }

    public TimeTableItem(Context context) {
        super(context);
    }

    public TimeTableItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeTableItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setCustomMaxHeight(this.getHeight());
        this.setHeight(customMaxHeight);
    }

    private void setCustomMaxHeight(int i){
        if(i > customMaxHeight){
            customMaxHeight = i;
        }
    }
}
