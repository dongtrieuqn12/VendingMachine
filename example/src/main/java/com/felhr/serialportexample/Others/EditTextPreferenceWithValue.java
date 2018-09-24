package com.felhr.serialportexample.Others;

import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

/**
 * Created by Ho Dong Trieu on 09/21/2018
 */
public class EditTextPreferenceWithValue extends EditTextPreference {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public EditTextPreferenceWithValue(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    public EditTextPreferenceWithValue(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public EditTextPreferenceWithValue(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public EditTextPreferenceWithValue(Context context) {
        super(context);
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        setSummary(text);
    }
}
