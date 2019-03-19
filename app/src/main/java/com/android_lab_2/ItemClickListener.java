package com.android_lab_2;

import android.view.View;

// required interface for recyclerView to be able to respond to click events
public interface ItemClickListener {
    void onClick(View view, int position, boolean isClick);
}
