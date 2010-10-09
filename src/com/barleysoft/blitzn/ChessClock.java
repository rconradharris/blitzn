package com.barleysoft.blitzn;

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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;

public class ChessClock extends Activity {

	// Constants
	public enum PLAYER { ONE, TWO }
	public static int CLOCK_RESOLUTION = 100; // ms

	// Activities
	public static final int ACTIVITY_SET_TIME = 0;

	// Menu Items
	public static final int MENU_RESET = Menu.FIRST;
	public static final int MENU_SET_TIME = Menu.FIRST + 1;
	public static final int MENU_PAUSE = Menu.FIRST + 2;

	// Preference Key-Value Store
	public static final String PREFS_NAME = "BlitznPrefs";

	// Clock State Machine
	public static final int NOSTATE = 0;
	public static final int READY = 1;
	public static final int PLAYER1_RUNNING = 2;
	public static final int PLAYER2_RUNNING = 3;
	public static final int PLAYER1_PAUSED = 4;
	public static final int PLAYER2_PAUSED = 5;
	public static final int STOPPED = 6;

	// Member variables
	private int clockState = NOSTATE;

	private long player1TimeLeft = 0L;
	private long player2TimeLeft = 0L;

	private long player1IncrementLeft = 0L;
	private long player2IncrementLeft = 0L;

	private ClockButton player1Clock;
	private ClockButton player2Clock;

	private ShakeListener shakeListener;
	private PitchFlipListener pitchFlipListener;

	private Handler handler = new Handler();
	private AlertDialog pausedDialog;

	// Two players are used to get fast playback
	private MediaPlayer clicker1;
	private MediaPlayer clicker2;
	private MediaPlayer gameOverSoundPlayer;

	// Configurations
	private long duration = 5 * 1 * 1000L;
	private long increment = 0L;
	private boolean shakeEnabled = true;
	private boolean flipEnabled = true;
	private boolean soundEnabled = true;
	private boolean showIntroDialog = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Go full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		// Setup Player 1
		player1Clock = (ClockButton) findViewById(R.id.player1Clock);
		player1Clock.setIsFlipped(true);
		OnClickListener player1ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER.ONE);
			}
		};
		player1Clock
				.setOnClickListener((android.view.View.OnClickListener) player1ClickListener);

		// Setup Player 2
		player2Clock = (ClockButton) findViewById(R.id.player2Clock);
		OnClickListener player2ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER.TWO);
			}
		};

		player2Clock
				.setOnClickListener((android.view.View.OnClickListener) player2ClickListener);

		// Initialize everything
		pausedDialog = createPausedDialog();
		restorePreferences();
		initializeSound();
		installShakeListener();
		installPitchFlipListener();
		resetClock();
		if (showIntroDialog)
			showIntroDialogBox();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if ((shakeListener != null) && !shakeListener.isListening())
			shakeListener.resume();

		if ((pitchFlipListener != null) && !pitchFlipListener.isListening())
			pitchFlipListener.resume();

		// When a paused game is resumed, we don't want to immediately start the
		// clock,
		// lest the player is not ready. Rather, we re-emit the paused dialog
		// box. Since the
		// clock was placed into the PAUSED state in onPause, we're in a
		// consistent state.
		if (isGamePaused() && !pausedDialog.isShowing())
			pausedDialog.show();
	}

	@Override
	protected void onPause() {
		if (shakeListener != null)
			shakeListener.pause();
		if (pitchFlipListener != null)
			pitchFlipListener.pause();

		// Don't show the dialog box on a system initiated pause
		if (isGameInProgress())
			pauseClock(false);

		super.onPause();
	}

	@Override
	public void onBackPressed() {
		if (isGameInProgress())
			showExitDialog();
		else
			super.onBackPressed();
	}

	private AlertDialog createPausedDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.paused).create();
		alertDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						if (isGamePaused())
							unPauseClock();
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
		showIntroDialog = false;
	}

	private void initializeSound() {
		clicker1 = createMediaPlayer(R.raw.click1);
		clicker2 = createMediaPlayer(R.raw.click1);
		gameOverSoundPlayer = createMediaPlayer(R.raw.gameover);
	}

	private MediaPlayer createMediaPlayer(int resId) {
		MediaPlayer clicker = MediaPlayer.create(this, resId);
		// clicker will be null if sound is not supported on device
		if (clicker != null)
			clicker.setVolume(1.0f, 1.0f);
		return clicker;
	}

	void playClick() {
		if ((clicker1 == null) || !soundEnabled)
			return;

		// Fall-back to second clicker so that we can play clicks nearly
		// simultaneously
		if (clicker1.isPlaying())
			clicker2.start();
		else
			clicker1.start();
	}

	void playGameOverSound() {
		if ((gameOverSoundPlayer == null) || !soundEnabled)
			return;
		gameOverSoundPlayer.start();
	}

	void restorePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		duration = settings.getLong("duration", 5 * 60 * 1000L);
		increment = settings.getLong("increment", 0L);
		shakeEnabled = settings.getBoolean("shakeEnabled", true);
		flipEnabled = settings.getBoolean("flipEnabled", true);
		soundEnabled = settings.getBoolean("soundEnabled", true);
		showIntroDialog = settings.getBoolean("showIntroDialog", true);
	}

	void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong("duration", duration);
		editor.putLong("increment", increment);
		editor.putBoolean("shakeEnabled", shakeEnabled);
		editor.putBoolean("flipEnabled", flipEnabled);
		editor.putBoolean("soundEnabled", soundEnabled);
		editor.putBoolean("showIntroDialog", showIntroDialog);
		editor.commit();
	}

	void installShakeListener() {
		final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// TODO(sirp): add configuration setting for shake
		shakeListener = new ShakeListener(this);
		shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
			public void onShake() {
				if (shakeEnabled && !isGamePaused() && !isGameReady()) {
					vibe.vibrate(100);
					resetClock();
				}
			}
		});
	}

	void installPitchFlipListener() {
		final Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// TODO(sirp): add configuration setting for shake
		pitchFlipListener = new PitchFlipListener(this);
		pitchFlipListener
				.setOnPitchFlipListener(new PitchFlipListener.OnPitchFlipListener() {
					public void onPitchFlip(int state) {
						if (flipEnabled && isGameInProgress()) {
							if ((state == PitchFlipListener.UP)
									&& isGamePaused()) {
								Log.i("Blitzn", "flip detected, unpausing");
								vibe.vibrate(100);
								unPauseClock();
							} else if ((state == PitchFlipListener.DOWN)
									&& !isGamePaused()) {
								Log.i("Blitzn", "flip detected, pausing");
								vibe.vibrate(100);
								pauseClock(true);
							}
						}
					}
				});
	}

	void onPlayerClick(PLAYER which) {
		if (which == PLAYER.ONE) {
			switch (clockState) {
			case READY:
				initiateClock(PLAYER.ONE);
				break;
			case PLAYER1_RUNNING:
				toggleClock();
				break;
			}
		} else {
			switch (clockState) {
			case READY:
				initiateClock(PLAYER.TWO);
				break;
			case PLAYER2_RUNNING:
				toggleClock();
				break;
			}
		}

	}

	void initiateClock(PLAYER which) {
		if (clockState != READY) {
			// throw new ClockStateException("already started");
		}

		activateClock(which);

		handler.removeCallbacks(updateTimeTask);
		handler.postDelayed(updateTimeTask, 100);
		setKeepScreenOn(true);
	}

	void activateClock(PLAYER which) {
		if (which == PLAYER.ONE) {
			player1IncrementLeft = increment;
			player1Clock.setIsActivated(true);
			player2Clock.setIsActivated(false);
			playClick();
			clockState = PLAYER1_RUNNING;
		} else {
			player2IncrementLeft = increment;
			player2Clock.setIsActivated(true);
			player1Clock.setIsActivated(false);
			playClick();
			clockState = PLAYER2_RUNNING;
		}
	}

	void resetClock() {
		setKeepScreenOn(false);
		handler.removeCallbacks(updateTimeTask);

		player1TimeLeft = duration;
		player1IncrementLeft = increment;
		player1Clock.setIsActivated(true);

		player2TimeLeft = duration;
		player2IncrementLeft = increment;
		player2Clock.setIsActivated(true);

		updateClockDisplays();
		clockState = READY;
	}

	void toggleClock() {
		switch (clockState) {
		case PLAYER1_RUNNING:
			player1TimeLeft += player1IncrementLeft;
			activateClock(PLAYER.TWO);
			break;
		case PLAYER2_RUNNING:
			player2TimeLeft += player2IncrementLeft;
			activateClock(PLAYER.ONE);
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
		switch (clockState) {
		case PLAYER1_RUNNING:
			handler.removeCallbacks(updateTimeTask);
			clockState = PLAYER1_PAUSED;
			if (showDialog)
				pausedDialog.show();
			break;
		case PLAYER2_RUNNING:
			handler.removeCallbacks(updateTimeTask);
			clockState = PLAYER2_PAUSED;
			if (showDialog)
				pausedDialog.show();
			break;
		default:
			// throw ClockStateException("wrong state");
		}
	}

	void unPauseClock() {
		switch (clockState) {
		case PLAYER1_PAUSED:
			handler.removeCallbacks(updateTimeTask);
			handler.postDelayed(updateTimeTask, 100);
			clockState = PLAYER1_RUNNING;
			if (pausedDialog.isShowing())
				pausedDialog.dismiss();
			break;
		case PLAYER2_PAUSED:
			handler.removeCallbacks(updateTimeTask);
			handler.postDelayed(updateTimeTask, 100);
			clockState = PLAYER2_RUNNING;
			if (pausedDialog.isShowing())
				pausedDialog.dismiss();
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
		handler.removeCallbacks(updateTimeTask);
		clockState = STOPPED;

		boolean player1Lost = hasPlayerLost(PLAYER.ONE);
		boolean player2Lost = hasPlayerLost(PLAYER.TWO);

		if (player1Lost && player2Lost) {
			Log.e("Blitzn", "both players lost, we have a problem!");
		}

		if (player1Lost) {
			player1Clock.setLost();
		}

		if (player2Lost) {
			player2Clock.setLost();
		}
		playGameOverSound();
	}

	boolean hasPlayerLost(PLAYER which) {
		long timeLeft = (which == PLAYER.ONE) ? player1TimeLeft : player2TimeLeft;
		return (timeLeft < CLOCK_RESOLUTION);
	}

	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			// Check for either clock expiring
			if (hasPlayerLost(PLAYER.ONE) || hasPlayerLost(PLAYER.TWO)) {
				stopClock();
				return;
			}

			switch (clockState) {
			case PLAYER1_RUNNING:
				player1TimeLeft -= CLOCK_RESOLUTION;
				player1IncrementLeft -= CLOCK_RESOLUTION;
				if (player1IncrementLeft < 0)
					player1IncrementLeft = 0L;
				break;
			case PLAYER2_RUNNING:
				player2TimeLeft -= CLOCK_RESOLUTION;
				player2IncrementLeft -= CLOCK_RESOLUTION;
				if (player2IncrementLeft < 0)
					player2IncrementLeft = 0L;
				break;
			}

			updateClockDisplays();

			// Reschedule the next tick
			long nextUpdate = SystemClock.uptimeMillis() + CLOCK_RESOLUTION;
			handler.postAtTime(this, nextUpdate);
		}
	};

	private void updateClockDisplays() {
		updateClockForPlayer(player1Clock, player1TimeLeft);
		updateClockForPlayer(player2Clock, player2TimeLeft);
	}

	private void updateClockForPlayer(ClockButton clockView, long timeLeft) {
		int seconds = (int) timeLeft / 1000;
		int minutes = seconds / 60;
		seconds = seconds % 60;
		String clockText = String.format("%02d:%02d", minutes, seconds);
		clockView.setText(clockText);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// Back button on SetTime sub-Activity is overridden to bundle the
		// results and RETURN_OK
		Bundle extras = intent.getExtras();
		long old_duration = duration;
		duration = extras.getInt("durationMinutes") * 60 * 1000;

		long old_increment = increment;
		increment = extras.getInt("incrementSeconds") * 1000;

		shakeEnabled = extras.getBoolean("shakeEnabled");
		flipEnabled = extras.getBoolean("flipEnabled");
		soundEnabled = extras.getBoolean("soundEnabled");

		// Only reset the clock if we changed something related to time-keeping
		if ((old_duration != duration) || (old_increment != increment))
			resetClock();
	}

	private void setTime() {
		Intent setTimeIntent = new Intent(this, SetTime.class);
		Log.i("Blitzn", "Launching SetTime Intent");

		setTimeIntent.putExtra("durationMinutes", (int) duration / 60 / 1000);
		setTimeIntent.putExtra("incrementSeconds", (int) increment / 1000);
		setTimeIntent.putExtra("shakeEnabled", shakeEnabled);
		setTimeIntent.putExtra("flipEnabled", flipEnabled);
		setTimeIntent.putExtra("soundEnabled", soundEnabled);

		startActivityForResult(setTimeIntent, ACTIVITY_SET_TIME);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_RESET, 0, "Reset");
		menu.add(0, MENU_PAUSE, 0, "Pause");
		menu.add(0, MENU_SET_TIME, 0, "Settings");
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_RESET:
			resetClock();
			return true;
		case MENU_SET_TIME:
			setTime();
			return true;
		case MENU_PAUSE:
			pauseClock(true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		MenuItem pauseMenu = menu.findItem(MENU_PAUSE);
		pauseMenu.setEnabled(isGameInProgress() && !isGamePaused());

		MenuItem resetMenu = menu.findItem(MENU_RESET);
		resetMenu.setEnabled((clockState != READY) && !isGamePaused());

		return true;
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
								ChessClock.this.finish();
							}
						}).setNegativeButton(R.string.no, null).show();
	}

	public boolean isGameInProgress() {
		switch (clockState) {
		case NOSTATE:
		case READY:
		case STOPPED:
			return false;
		}
		return true;
	}

	public boolean isGamePaused() {
		switch (clockState) {
		case PLAYER1_PAUSED:
		case PLAYER2_PAUSED:
			return true;
		}
		return false;
	}

	public boolean isGameReady() {
		return (clockState == READY);
	}

}