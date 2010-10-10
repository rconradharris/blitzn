package com.barleysoft.motion;

import com.barleysoft.despiker.RunningMedianGenerator;
import com.barleysoft.despiker.NotEnoughData;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class PitchFlipListener implements SensorEventListener {
	public enum State {
		UP, DOWN
	}

	private enum AggregateState {
		UP, DOWN, SIDE
	}

	private enum PitchState {
		FORWARD, UP, BACKWARDS, DOWN, NOT_ENOUGH_DATA
	}

	private enum RollState {
		LEVEL, RIGHT, LEFT, NOT_ENOUGH_DATA
	}

	// Parameters
	// 25 was empirically derived on an HTC Incredible
	// 10 was too sensitive and 25 was too laggy
	private static final int WINDOW_SIZE = 15;

	// These are separate so that we can make UP or DOWN more sensitive
	private static final int ANGLE_THRESHOLD = 45; // degrees

	private State mState = State.UP;

	private RunningMedianGenerator mPitchWindow = new RunningMedianGenerator(
			WINDOW_SIZE);

	private RunningMedianGenerator mRollWindow = new RunningMedianGenerator(
			WINDOW_SIZE);

	private Context mContext;
	private OnPitchFlipListener mPitchFlipListener;
	private Sensor mOrientSensor;
	private SensorManager mSensorMgr;

	public interface OnPitchFlipListener {
		public void onPitchFlip(State mState);
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

	public boolean isListening() {
		return (mSensorMgr != null);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	private PitchState computeCurrentPitchState(float pitch) {
		final float FORWARD = 0.0f;
		final float UP = -90.0f;
		final float DOWN = 90.0f;

		float smoothedPitch;
		try {
			smoothedPitch = mPitchWindow.addAndCompute(pitch);
		} catch (NotEnoughData e) {
			// Return while we accrue readings
			return PitchState.NOT_ENOUGH_DATA;
		}

		Log.i("PitchFlipListner",
				"smoothedPitch=" + String.valueOf(smoothedPitch));
		PitchState state;
		if (Math.abs(FORWARD - smoothedPitch) < ANGLE_THRESHOLD)
			state = PitchState.FORWARD;
		else if (Math.abs(UP - smoothedPitch) <= ANGLE_THRESHOLD)
			state = PitchState.UP;
		else if (Math.abs(DOWN - smoothedPitch) <= ANGLE_THRESHOLD)
			state = PitchState.DOWN;
		else
			state = PitchState.BACKWARDS;

		return state;
	}

	private RollState computeCurrentRollState(float roll) {
		final float RIGHT = -90.0f;
		final float LEFT = 90.0f;

		float smoothedRoll;
		try {
			smoothedRoll = mRollWindow.addAndCompute(roll);
		} catch (NotEnoughData e) {
			// Return while we accrue readings
			return RollState.NOT_ENOUGH_DATA;
		}

		RollState state;
		if (Math.abs(RIGHT - smoothedRoll) < ANGLE_THRESHOLD)
			state = RollState.RIGHT;
		else if (Math.abs(LEFT - smoothedRoll) < ANGLE_THRESHOLD)
			state = RollState.LEFT;
		else
			state = RollState.LEVEL;

		return state;
	}

	public void onSensorChanged(SensorEvent event) {
		// Pitch
		float pitch = event.values[1];
		PitchState curPitchState = computeCurrentPitchState(pitch);
		// NOTE(sirp): should this be a try/except
		if (curPitchState == PitchState.NOT_ENOUGH_DATA)
			return;

		// Roll
		float roll = event.values[2];
		RollState curRollState = computeCurrentRollState(roll);
		// NOTE(sirp): should this be a try/except
		if (curRollState == RollState.NOT_ENOUGH_DATA)
			return;

		boolean pitchLevel = (curPitchState == PitchState.FORWARD)
				|| (curPitchState == PitchState.BACKWARDS);
		boolean rollLevel = (curRollState == RollState.LEVEL);

		// Aggregate state basically says if the either the phone is pitched
		// up or rolled, then consider that on it's side. Otherwise, we're
		// UP if pitch forward and DOWN pitch backwards
		AggregateState aggregateState;
		if (pitchLevel && rollLevel) {
			aggregateState = (curPitchState == PitchState.FORWARD) ? AggregateState.UP
					: AggregateState.DOWN;
		} else {
			aggregateState = AggregateState.SIDE;
		}

		boolean flip = false;
		if ((mState == State.DOWN) && (aggregateState == AggregateState.UP)) {
			// DOWN -> UP
			flip = true;
			mState = State.UP;
		} else if ((mState == State.UP)
				&& (aggregateState == AggregateState.DOWN)) {
			// UP -> DOWN
			flip = true;
			mState = State.DOWN;
		}

		String logMsg = "mState=" + mState;
		logMsg += " aggregateState=" + aggregateState;
		logMsg += " curPitchState=" + curPitchState;
		logMsg += " curRollState=" + curRollState;
		logMsg += " pitch=" + String.valueOf(pitch);
		logMsg += " roll=" + String.valueOf(roll);

		Log.i("PitchFlipListener", logMsg);

		if (flip) {
			Log.i("PitchFlipListener", "pitch flipped");
			mPitchFlipListener.onPitchFlip(mState);
		}

	}

}
