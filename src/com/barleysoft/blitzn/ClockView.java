package com.barleysoft.blitzn;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;


public class ClockView extends TextView {
	private boolean isFlipped = false;

	public ClockView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	void setIsFlipped(boolean isFlipped) {
		this.isFlipped = isFlipped;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Rect r = canvas.getClipBounds();
		if (isFlipped) {
			canvas.rotate(180, r.exactCenterX(), r.exactCenterY());
		}
		super.onDraw(canvas);
	}

}
