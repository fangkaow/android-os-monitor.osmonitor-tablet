package com.eolwral.osmonitor.tablet.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class TimeSeekBar extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	  
    private SeekBar mSeekBar;
    private TextView mSplashText, mValueText;
    private Context mContext;

    private String mDialogMessage;
    private int mDefault, mMax, mValue = 0;

    public TimeSeekBar(Context context, AttributeSet attrs,  int defStyle) {
        super (context, attrs, defStyle);

        mContext = context;
        mDialogMessage = attrs.getAttributeValue("http://schemas.android.com/apk/res/android","dialogMessage");
        mDefault = Integer.parseInt(attrs.getAttributeValue("http://schemas.android.com/apk/res/android","defaultValue"));
        mMax = Integer.parseInt(attrs.getAttributeValue("http://schemas.android.com/apk/res/android","max"));
     }

    public TimeSeekBar(Context context, AttributeSet attrs) {
        this (context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.preference.DialogPreference#onCreateDialogView()
     */
    @Override
    protected View onCreateDialogView() {
        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        mSplashText = new TextView(mContext);
        if (mDialogMessage != null)
            mSplashText.setText(mDialogMessage);
        layout.addView(mSplashText);

        mValueText = new TextView(mContext);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(18);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(mValueText, params);

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this );
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if (shouldPersist())
            mValue = getPersistedInt(mDefault)-1;

        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);
        return layout;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.preference.DialogPreference#onBindDialogView(android.view.View)
     */
    @Override
    protected void onBindDialogView(View v) {
        super .onBindDialogView(v);
        mSeekBar.setMax(mMax);
        mSeekBar.setProgress(mValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.preference.Preference#onSetInitialValue(boolean,
     * java.lang.Object)
     */
    @Override
    protected void onSetInitialValue(boolean restore,
            Object defaultValue) {
        super .onSetInitialValue(restore, defaultValue);

        if (restore)
            mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
        else
            mValue = (Integer) defaultValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android
     * .widget.SeekBar, int, boolean)
     */
    public void onProgressChanged(SeekBar seek, int value,
            boolean fromTouch) {
        String t = String.valueOf(value+1);
        mValueText.setText(t);
        if (shouldPersist())
            persistInt(value+1);
        callChangeListener(new Integer(value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android
     * .widget.SeekBar)
     */
    public void onStartTrackingTouch(SeekBar seek) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android
     * .widget.SeekBar)
     */
    public void onStopTrackingTouch(SeekBar seek) {
    }

    public void setMax(int max) {
        mMax = max;
    }

    public int getMax() {
        return mMax;
    }

    public void setProgress(int progress) {
        mValue = progress;
        if (mSeekBar != null)
            mSeekBar.setProgress(progress);
    }

    public int getProgress() {
        return mValue;
    }
}