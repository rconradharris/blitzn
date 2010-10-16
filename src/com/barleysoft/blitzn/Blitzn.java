package com.barleysoft.blitzn;

import com.barleysoft.blitzn.ChessClock.DelayMode;
import com.barleysoft.blitzn.ChessClock.Player;
import com.barleysoft.motion.PitchFlipListener;
import com.barleysoft.motion.ShakeListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class Blitzn extends Activity {

	// Constants
	public static final String PREFS_NAME = "BlitznPrefs";
	public static final int ACTIVITY_SET_TIME = 0;

	// Member variables
	private ChessClock mChessClock;
	private ClockButton mPlayer1ClockButton;
	private ClockButton mPlayer2ClockButton;

	private ShakeListener mShakeListener;
	private PitchFlipListener mPitchFlipListener;

	private Handler mTimerHandler = new Handler();
	private AlertDialog mPausedDialog;


	// Configurations
	private long mDuration = 5 * 1 * 1000L;
	private boolean mShakeEnabled = true;
	private boolean mFlipEnabled = true;
	private boolean mSoundEnabled = true;
	private boolean mShowIntroDialog = true;
	private boolean mTimePressureWarningEnabled = true;
	private DelayMode mDelayMode = DelayMode.NODELAY;
	private long mDelayTime = 0L;

	private void initializeMainWindow() {
		// Go full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);
		mPausedDialog = createPausedDialog();
		if (mShowIntroDialog) {
			showIntroDialogBox();
		}
	}
	
	private ClockButton initializeClockButton(final Player player, int resId, boolean isFlipped, ChessPlayer chessPlayer) {
		ClockButton button = (ClockButton) findViewById(resId);
		button.setIsFlipped(isFlipped);
		button.setChessPlayer(chessPlayer);
		OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				activatePlayer(player);
			}
		};
		button.setOnClickListener(clickListener);
		button.initialize();
		return button;
	}

	void initializeChessClock() {
		mChessClock = new BlitznChessClock();
		mChessClock.setDuration(mDuration);
		mChessClock.setDelayMode(mDelayMode);
		mChessClock.setDelayTime(mDelayTime);
		
		ChessPlayer chessPlayer1 = new BlitznChessPlayer();
		ChessPlayer chessPlayer2 = new BlitznChessPlayer();
		
		mChessClock.setChessPlayer(Player.ONE, chessPlayer1);
		mChessClock.setChessPlayer(Player.TWO, chessPlayer2);
		
		mPlayer1ClockButton = initializeClockButton(Player.ONE, R.id.player1Clock, true, chessPlayer1);
		mPlayer2ClockButton = initializeClockButton(Player.TWO, R.id.player2Clock, false, chessPlayer2);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		restorePreferences();
		initializeChessClock();
		installShakeListener();
		installPitchFlipListener();
		initializeMainWindow();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if ((mShakeListener != null) && !mShakeListener.isListening()) {
			mShakeListener.resume();
		}

		if ((mPitchFlipListener != null) && !mPitchFlipListener.isListening()) {
			mPitchFlipListener.resume();
		}

		// When a paused game is resumed, we don't want to immediately start the
		// clock,
		// lest the player is not ready. Rather, we re-emit the paused dialog
		// box. Since the
		// clock was placed into the PAUSED state in onPause, we're in a
		// consistent state.
		if (mChessClock.isPaused() && !mPausedDialog.isShowing()) {
			mPausedDialog.show();
		}
	}

	@Override
	protected void onPause() {
		if (mShakeListener != null) {
			mShakeListener.pause();
		}
		
		if (mPitchFlipListener != null) {
			mPitchFlipListener.pause();
		}

		// Don't show the dialog box on a system initiated pause
		if (mChessClock.isStarted()) {
			pauseClock(false);
		}

		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (mChessClock.isStarted()) {
			showExitDialog();
		} else {
			super.onBackPressed();
		}
	}

	private AlertDialog createPausedDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.paused).create();
		alertDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						if (mChessClock.isPaused()) {
							unPauseClock();
						}
					}
				});
		return alertDialog;
	}

	private void showIntroDialogBox() {
		// Ask the user if they want to quit
		new AlertDialog.Builder(this)
				// .setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.intro_title).setMessage(R.string.intro_text)
				.setNegativeButton(R.string.ok, null).show();
		mShowIntroDialog = false;
	}

	void setDelayMethodFromInt(int delayMethod) {
		// TODO(sirp): these methods should go away and be replaced with something more elegant
		switch (delayMethod) {
		case 1:
			mDelayMode = DelayMode.FISCHER;
		case 2:
			mDelayMode = DelayMode.BRONSTEIN;
		default:
			mDelayMode = DelayMode.NODELAY;
		}
	}

	int getDelayMethodAsInt() {
		switch (mDelayMode) {
		case FISCHER:
			return 1;
		case BRONSTEIN:
			return 2;
		default:
			return 0;
		}
	}

	void restorePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		mDuration = settings.getLong("duration", 5 * 60 * 1000L);
		setDelayMethodFromInt(settings.getInt("delayMethod", 0));
		mDelayTime = settings.getLong("delay", 0L);
		mShakeEnabled = settings.getBoolean("shakeEnabled", true);
		mFlipEnabled = settings.getBoolean("flipEnabled", true);
		mSoundEnabled = settings.getBoolean("soundEnabled", true);
		mShowIntroDialog = settings.getBoolean("showIntroDialog", true);
		mTimePressureWarningEnabled = settings.getBoolean(
				"mTimePressureWarningEnabled", true);
	}

	void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("duration", mDuration);
		editor.putLong("delay", mDelayTime);
		editor.putInt("delayMethod", getDelayMethodAsInt());
		editor.putBoolean("shakeEnabled", mShakeEnabled);
		editor.putBoolean("flipEnabled", mFlipEnabled);
		editor.putBoolean("soundEnabled", mSoundEnabled);
		editor.putBoolean("mTimePressureWarningEnabled",
				mTimePressureWarningEnabled);
		editor.commit();
	}

	void installShakeListener() {
		final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// TODO(sirp): add configuration setting for shake
		mShakeListener = new ShakeListener(this);
		mShakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
			public void onShake() {
				if (mShakeEnabled && !mChessClock.isPaused() && !mChessClock.isReady()) {
					vibe.vibrate(100);
					resetClock();
				}
			}
		});
	}

	void installPitchFlipListener() {
		final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// TODO(sirp): add configuration setting for shake
		mPitchFlipListener = new PitchFlipListener(this);
		mPitchFlipListener
				.setOnPitchFlipListener(new PitchFlipListener.OnPitchFlipListener() {
					public void onPitchFlip(PitchFlipListener.State state) {
						if (mFlipEnabled && mChessClock.isStarted()) {
							if ((state == PitchFlipListener.State.UP)
									&& mChessClock.isPaused()) {
								Log.i("Blitzn", "flip detected, unpausing");
								vibe.vibrate(100);
								unPauseClock();
							} else if ((state == PitchFlipListener.State.DOWN)
									&& !mChessClock.isPaused()) {
								Log.i("Blitzn", "flip detected, pausing");
								vibe.vibrate(100);
								pauseClock(true);
							}
						}
					}
				});
	}

	void startClockTimer() {
		mTimerHandler.removeCallbacks(updateTimeTask);
		// TODO(sirp): perhaps CLOCK_RESOLUTION should be defined in the app and passed into
		// the ChessClock object
		mTimerHandler.postDelayed(updateTimeTask, mChessClock.getClockResolution());
	}

	void stopClockTimer() {
		mTimerHandler.removeCallbacks(updateTimeTask);
	}

	void activatePlayer(Player player) {
		if (mChessClock.isReady()) {
			startClockTimer();
			setKeepScreenOn(true);
		}
		mChessClock.activatePlayer(player);
		if (player == Player.ONE) {
			mPlayer1ClockButton.activate();
			mPlayer2ClockButton.deactivate();
		} else {
			mPlayer1ClockButton.deactivate();
			mPlayer2ClockButton.activate();
		}
	}

	void resetClock() {
		setKeepScreenOn(false);
		stopClockTimer();
		mChessClock.reset();
		mPlayer1ClockButton.reset();
		mPlayer2ClockButton.reset();
	}

	void toggleClock() {
		switch (mClockState) {
		case PLAYER1_RUNNING:
			activateClock(Player.TWO);
			break;
		case PLAYER2_RUNNING:
			activateClock(Player.ONE);
			break;
		default:
			// throw ClockStateException("wrong state");
		}
	}

	void pauseClock(boolean showDialog) {
		// There are two types of pauses:
		// a) Player initiated, where the player flips the phone over
		// or hits pause from the menu
		// b) Where the system pauses the clock because the clock is
		// no longer visible (hit the settings page or the home button)
		//
		// For a) we want to display a paused dialog; however, for b)
		// we do not.
		switch (mClockState) {
		case PLAYER1_RUNNING:
			stopClockTimer();
			mClockState = ClockState.PLAYER1_PAUSED;
			if (showDialog)
				mPausedDialog.show();
			break;
		case PLAYER2_RUNNING:
			stopClockTimer();
			mClockState = ClockState.PLAYER2_PAUSED;
			if (showDialog)
				mPausedDialog.show();
			break;
		default:
			// throw ClockStateException("wrong state");
		}
	}

	void unPauseClock() {
		switch (mClockState) {
		case PLAYER1_PAUSED:
			startClockTimer();
			mClockState = ClockState.PLAYER1_RUNNING;
			if (mPausedDialog.isShowing())
				mPausedDialog.dismiss();
			break;
		case PLAYER2_PAUSED:
			startClockTimer();
			mClockState = ClockState.PLAYER2_RUNNING;
			if (mPausedDialog.isShowing())
				mPausedDialog.dismiss();
			break;
		default:
			// throw ClockStateException("wrong state");
		}
	}

	void setKeepScreenOn(boolean keepScreenOn) {
		View layout = (View) findViewById(R.id.mainLayout);
		layout.setKeepScreenOn(keepScreenOn);
	}

	void stopClock() {
		setKeepScreenOn(false);
		stopClockTimer();
		mClockState = ClockState.STOPPED;

		boolean player1Lost = hasPlayerLost(Player.ONE);
		boolean player2Lost = hasPlayerLost(Player.TWO);

		if (player1Lost && player2Lost) {
			Log.e("Blitzn", "both players lost, we have a problem!");
		}

		if (player1Lost) {
			mPlayer1ClockButton.setLost();
		}

		if (player2Lost) {
			mPlayer2ClockButton.setLost();
		}
		playGameOverSound();
	}


	void tickForPlayer(Player player) {
		if (isPlayerUnderTimePressure(player) && mTimePressureWarningEnabled) {
			if (((mClockTicks * CLOCK_RESOLUTION) % TIME_PRESSURE_SIREN_INTERVAL) == 0) {
				playTimePressureSiren();
			}
		}

		mClockTicks++;

		if (player == Player.ONE) {
			mPlayer1TimeLeft -= CLOCK_RESOLUTION;
		} else {
			mPlayer2TimeLeft -= CLOCK_RESOLUTION;
		}
	}

	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			// Check for either clock expiring
			if (hasPlayerLost(Player.ONE) || hasPlayerLost(Player.TWO)) {
				stopClock();
				return;
			}

			switch (mClockState) {
			case PLAYER1_RUNNING:
				if (mDelayContext.shouldClockTickForPlayer(Player.ONE))
					tickForPlayer(Player.ONE);
				mDelayContext.tickForPlayer(Player.ONE);
				break;
			case PLAYER2_RUNNING:
				if (mDelayContext.shouldClockTickForPlayer(Player.TWO))
					tickForPlayer(Player.TWO);
				mDelayContext.tickForPlayer(Player.TWO);
				break;
			}

			updateClockDisplays();

			// Reschedule the next tick
			long nextUpdate = SystemClock.uptimeMillis() + CLOCK_RESOLUTION;
			mTimerHandler.postAtTime(this, nextUpdate);
		}
	};

	private void updateClockDisplays() {
		updateClockForPlayer(Player.ONE);
		updateClockForPlayer(Player.TWO);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// Back button on SetTime sub-Activity is overridden to bundle the
		// results and RETURN_OK
		Bundle extras = intent.getExtras();
		long old_duration = mDuration;
		mDuration = extras.getLong("durationMinutes") * 60 * 1000;

		// NOTE(sirp): setDelayMethodFromInt has the side-effect of clearing the
		// delay so we must store the old delay before calling it
		long oldDelay = mDelayContext.getDelay();

		int oldDelayMethod = getDelayMethodAsInt();
		setDelayMethodFromInt(extras.getInt("delayMethod"));

		mDelayContext.setDelay(extras.getLong("delaySeconds") * 1000);

		mShakeEnabled = extras.getBoolean("shakeEnabled");
		mFlipEnabled = extras.getBoolean("flipEnabled");
		mSoundEnabled = extras.getBoolean("soundEnabled");
		mTimePressureWarningEnabled = extras
				.getBoolean("timePressureWarningEnabled");

		// Only reset the clock if we changed something related to time-keeping
		if ((old_duration != mDuration)
				|| (oldDelay != mDelayContext.getDelay())
				|| (oldDelayMethod != getDelayMethodAsInt())) {
			resetClock();
		}
	}

	private void setTime() {
		Intent setTimeIntent = new Intent(this, SetTime.class);
		setTimeIntent.putExtra("delayMethod", getDelayMethodAsInt());
		setTimeIntent.putExtra("durationMinutes", mDuration / 60 / 1000);
		setTimeIntent.putExtra("delaySeconds", mDelayContext.getDelay() / 1000);
		setTimeIntent.putExtra("shakeEnabled", mShakeEnabled);
		setTimeIntent.putExtra("flipEnabled", mFlipEnabled);
		setTimeIntent.putExtra("soundEnabled", mSoundEnabled);
		setTimeIntent.putExtra("timePressureWarningEnabled",
				mTimePressureWarningEnabled);
		startActivityForResult(setTimeIntent, ACTIVITY_SET_TIME);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.resetMenu:
			resetClock();
			return true;
		case R.id.settingsMenu:
			setTime();
			return true;
		case R.id.pauseMenu:
			pauseClock(true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem pauseMenu = menu.findItem(R.id.pauseMenu);
		pauseMenu.setEnabled(isGameInProgress() && !isGamePaused());

		MenuItem resetMenu = menu.findItem(R.id.resetMenu);
		resetMenu.setEnabled((mClockState != ClockState.READY)
				&& !isGamePaused());

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onStop() {
		super.onStop();
		savePreferences();
	}

	private void showExitDialog() {
		// Ask the user if they want to quit
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.quit)
				.setMessage(R.string.leave_in_progress)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

								// Stop the activity
								Blitzn.this.finish();
							}
						}).setNegativeButton(R.string.no, null).show();
	}


	public boolean isDelayEnabled() {
		return mDelayContext.isDelayEnabled();
	}


	public long getTimeLeftForPlayer(Player player) {
		return (player == Player.ONE) ? mPlayer1TimeLeft : mPlayer2TimeLeft;
	}

	public void adjustTimeLeftForPlayer(Player player, long adjustment) {
		if (player == Player.ONE)
			mPlayer1TimeLeft += adjustment;
		else
			mPlayer2TimeLeft += adjustment;
	}

}