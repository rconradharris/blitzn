package com.barleysoft.blitzn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class ClockButton extends Button {
	private boolean isFlipped = false;

	public ClockButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		String fontLocation = context.getString(R.string.clock_font);
		Typeface face = Typeface.createFromAsset(context.getAssets(),
				fontLocation);
		setTypeface(face);
	}

	void setIsFlipped(boolean isFlipped) {
		this.isFlipped = isFlipped;
	}

	void setIsActivated(boolean isActivated) {
		if (isActivated) {
			setClickable(true);
			setBackgroundColor(Color.BLACK);
		} else {
			setBackgroundColor(Color.TRANSPARENT);
			setClickable(false);
		}
	}

	public void setLost() {
		setBackgroundColor(Color.RED);
		setClickable(false);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isFlipped) {
			Rect r = canvas.getClipBounds();
			canvas.rotate(180, r.exactCenterX(), r.exactCenterY());
		}
		super.onDraw(canvas);
	}

}
