package com.orange.amaplike.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

import com.orange.amaplike.R;


/**
 * created by czh on 2018-01-29
 */
public class FloatActionBtnBehavior extends FloatingActionButton.Behavior {


    private int mTopLayoutHeight=0;

    public FloatActionBtnBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, FloatingActionButton child, int layoutDirection) {
//        for (int i=0;i<parent.getChildCount();i++){
//            View temp=parent.getChildAt(i);
//            if (temp.getId()== R.id.parent_toplayout){
//                mTopLayoutHeight=temp.findViewById(R.id.topLayout).getHeight();
//                break;
//            }
//        }
        mTopLayoutHeight=parent.findViewById(R.id.topLayout).getHeight();
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final FloatingActionButton child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency instanceof NestedScrollView;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        if (child.getTop()<mTopLayoutHeight+100){
            child.hide();
        }else {
            child.show();
        }
        return false;
    }
}