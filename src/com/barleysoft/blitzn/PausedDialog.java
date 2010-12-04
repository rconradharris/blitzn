package com.barleysoft.blitzn;

import android.app.AlertDialog;
import android.content.Context;
import android.view.MotionEvent;

public class PausedDialog extends AlertDialog {
	protected PausedDialog(Context context) {
		super(context);
		setIcon(android.R.drawable.ic_dialog_alert);
		setTitle(R.string.paused);
	}

	public boolean onTouchEvent(MotionEvent event) {
		// User can touch anywhere on the screen to dismiss the paused dialog
		dismiss();
		return super.onTouchEvent(event);
	}
}
