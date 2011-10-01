package net.londatiga.android;

import com.eolwral.osmonitor.tablet.R;

import android.content.Context;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import android.widget.ImageView;
import android.widget.TextView;

import android.view.Gravity;
import android.view.LayoutInflater;

import android.view.View;
import android.view.View.OnClickListener;

import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

/**
 * Quickaction window.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
public class QuickAction extends PopupWindows {
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private LayoutInflater inflater;
	private ViewGroup mTrack;
	private OnActionItemClickListener mListener;
	
	private int mChildPos;
	
	/**
	 * Constructor.
	 * 
	 * @param context Context
	 */
	public QuickAction(Context context) {
		super(context);
		
		inflater 	= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setRootViewId(R.layout.quickaction);
		mChildPos		= 0;
	}
	
	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id
	 */
	public void setRootViewId(int id) {
		mRootView	= (ViewGroup) inflater.inflate(id, null);
		mTrack 		= (ViewGroup) mRootView.findViewById(R.id.tracks);

		mArrowDown 	= (ImageView) mRootView.findViewById(R.id.arrow_down);
		mArrowUp 	= (ImageView) mRootView.findViewById(R.id.arrow_up);

		setContentView(mRootView);
	}
	

	/**
	 * Add action item
	 * 
	 * @param action  {@link ActionItem}
	 */
	public void addActionItem(ActionItem action) {
		
		String title 	= action.getTitle();
		Drawable icon 	= action.getIcon();
		
		View container	= (View) inflater.inflate(R.layout.action_item, null);
		
		ImageView img 	= (ImageView) container.findViewById(R.id.iv_icon);
		TextView text 	= (TextView) container.findViewById(R.id.tv_title);
		
		if (icon != null) 
			img.setImageDrawable(icon);
		else
			img.setVisibility(View.GONE);
		
		if (title != null)
			text.setText(title);
		else
			text.setVisibility(View.GONE);
		
		final int pos =  mChildPos;
		
		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if (mListener != null){
					mListener.onItemClick(pos);
				}
					
				dismiss();
			}
		});
		
		container.setFocusable(true);
		container.setClickable(true);
			 
		mTrack.addView(container, mChildPos+1);
		
		mChildPos++;
	}
	
	public void setOnActionItemClickListener(OnActionItemClickListener listener) {
		mListener = listener;
	}
	
	/**
	 * Show popup mWindow
	 */
	public void show (View anchor) {
		preShow();

		int[] location 		= new int[2];
		
		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());

		mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		int rootWidth 		= mRootView.getMeasuredWidth();
		int rootHeight 		= mRootView.getMeasuredHeight();

		int screenWidth 	= mWindowManager.getDefaultDisplay().getWidth();

		int xPos 			= (screenWidth - rootWidth) / 2;
		int yPos	 		= anchorRect.top - rootHeight;

		boolean onTop		= true;
		
		// display on bottom
		if (rootHeight > anchor.getTop()) {
			yPos 	= anchorRect.bottom;
			onTop	= false;
		}

		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX());
		
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}

	/**
	 * Show arrow
	 * 
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);
        
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
        
        param.leftMargin = requestedX - arrowWidth / 2;
      
        hideArrow.setVisibility(View.INVISIBLE);
    }
	
	/**
	 * Listener for item click
	 *
	 */
	public interface OnActionItemClickListener {
		public abstract void onItemClick(int pos);
	}
}