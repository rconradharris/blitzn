package com.barleysoft.blitzn;

import com.barleysoft.runningmean.RunningMeanWindow;
import com.barleysoft.runningmean.WindowTooSmall;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class PitchFlipListener implements SensorEventListener {
	// States
	public static final int UP = 0;
	public static final int SIDE = 1;
	public static final int DOWN = 2;

	// Parameters
	private static final int WINDOW_SIZE = 10;

	// Notice that DOWN -> UP is preferred over UP -> DOWN
	private static final int DOWN_THRESHOLD = 20; // degrees
	private static final int UP_THRESHOLD = 40; //

	private int mState = UP;
	private RunningMeanWindow mWindow = new RunningMeanWindow(WINDOW_SIZE);

	private Context mContext;
	private OnPitchFlipListener mPitchFlipListener;
	private Sensor mOrientSensor;
	private SensorManager mSensorMgr;

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
		case UP:
			return "UP";
		case DOWN:
			return "DOWN";
		case SIDE:
			return "SIDE";
		default:
			return "FACE_UKNOWN";
		}
	}

	public void onSensorChanged(SensorEvent event) {
		// Since we only care about 0 (UP) and 180 (DOWN)
		// we can just take abs
		float pitch = Math.abs(event.values[1]);
		int curState;
		float meanPitch;
		try {
			meanPitch = mWindow.addAndComputeMean(pitch);
			if (meanPitch < UP_THRESHOLD)
				curState = UP;
			else if ((180 - pitch) < DOWN_THRESHOLD)
				curState = DOWN;
			else
				curState = SIDE;
		} catch (WindowTooSmall e) {
			// Return while we accrue readings
			return;
		}

		boolean flip = false;
		if ((mState == DOWN) && (curState == UP)) {
			// DOWN -> UP
			flip = true;
			mState = UP;
		} else if ((mState == UP) && (curState == DOWN)) {
			// UP -> DOWN
			flip = true;
			mState = DOWN;
		}

		String prettyState = getPrettyState(mState);
		String prettyCurState = getPrettyState(curState);
		String meanPitchStr = String.valueOf(meanPitch);
		String pitchStr = String.valueOf(pitch);
		String logMsg = "";
		logMsg += "mState=" + prettyState;
		logMsg += " state=" + prettyCurState;
		logMsg += " meanPitch=" + meanPitchStr;
		logMsg += " pitch=" + pitchStr;
		Log.i("PitchFlipListener", logMsg);

		if (flip) {
			Log.i("PitchFlipListener", "pitch flipped");
			mPitchFlipListener.onPitchFlip(curState);
		}

	}

}
