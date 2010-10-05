package com.barleysoft.blitzn;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class PitchFlipListener implements SensorEventListener {
	public static final int FACE_UNKNOWN = 0;
	public static final int FACE_UP = 1;
	public static final int FACE_DOWN = 2;

	private static final int PITCH_FLIP_THRESHOLD = 20; // degrees
	private static final int TIME_THRESHOLD = 100; // ms

	private Context mContext;
	private OnPitchFlipListener mPitchFlipListener;
	private Sensor mOrientSensor;
	private SensorManager mSensorMgr;
	private long mLastTime;
	private int mLastState;

	public interface OnPitchFlipListener {
		public void onPitchFlip(int state);
	}

	public PitchFlipListener(Context context) {
		mContext = context;
		resume();
	}

	public void setOnPitchFlipListener(OnPitchFlipListener listener) {
		mPitchFlipListener = listener;
	}

	public void resume() {
		mSensorMgr = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorMgr == null) {
			throw new UnsupportedOperationException("Sensors not supported");
		}

		mOrientSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		boolean supported = mSensorMgr.registerListener(this, mOrientSensor,
				SensorManager.SENSOR_DELAY_GAME);

		if (!supported) {
			mSensorMgr.unregisterListener(this, mOrientSensor);
			throw new UnsupportedOperationException("Orientation not supported");
		}
	}

	public void pause() {
		if (mSensorMgr != null) {
			mSensorMgr.unregisterListener(this, mOrientSensor);
			mSensorMgr = null;
			mOrientSensor = null;
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public String getPrettyState(int state) {
		switch (state) {
		case FACE_UP:
			return "FACE UP";
		case FACE_DOWN:
			return "FACE DOWN";
		default:
			return "FACE_UKNOWN";
		}
	}

	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		long now = System.currentTimeMillis();

		if ((now - mLastTime) < TIME_THRESHOLD)
			return;

		float pitch = event.values[1];

		int state;
		if (Math.abs(180 + pitch) < PITCH_FLIP_THRESHOLD)
			state = FACE_DOWN;
		else if (Math.abs(0 - pitch) < PITCH_FLIP_THRESHOLD)
			state = FACE_UP;
		else
			state = FACE_UNKNOWN;

		if ((mLastState == FACE_UNKNOWN) && (state != FACE_UNKNOWN))
			mLastState = state;

		String prettyState = getPrettyState(state);
		String prettyLastState = getPrettyState(mLastState);
		Log.i("PitchFlipListener", "Pitch: " + String.valueOf(pitch) + " Last: " + prettyLastState + " Current: " 
				+ prettyState);

		if ((state != FACE_UNKNOWN) && (mLastState != FACE_UNKNOWN)
				&& (state != mLastState)) {

			// FACE_UP -> FACE_DOWN or FACE_DOWN -> FACE_UP
			if (mPitchFlipListener != null) {
				Log.i("PitchFlipListener", "pitch flip detected: "
						+ prettyState);
				mPitchFlipListener.onPitchFlip(state);
			}
			mLastState = state;
		}

		mLastTime = now;

	}

}
