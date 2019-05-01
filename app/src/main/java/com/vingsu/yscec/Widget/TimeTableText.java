package com.vingsu.yscec.Widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class TimeTableText extends TextView {
    public TimeTableText(Context context) {
        super(context);
    }

    public TimeTableText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeTableText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDisplayHint(int hint) {
        this.setHeight(TimeTableItem.getCustomMaxHeight());
        super.onDisplayHint(hint);
    }
}
