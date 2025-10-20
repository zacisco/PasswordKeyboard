package com.zac.pswdKb;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.Objects;

/**
 * Created by zac on 12.10.17.
 */

public class pswdKb extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
	private KeyboardView mKeyboardView;
	private Keyboard mKeyboard;
	private boolean isCapsOn = false;
	private KEYS_TYPE mCurrentLocale;
	private InputMethodManager inputMethodManager;
	private int pushCount = 0;
	private Keyboard.Key shiftKey = null;

	private enum KEYS_TYPE {
		SYMBOLS, ENGLISH
	}

	@Override
	public View onCreateInputView() {
		inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		mKeyboardView = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
		mCurrentLocale = KEYS_TYPE.ENGLISH;
		mKeyboard = getKeyboard(mCurrentLocale);
		mKeyboard.setShifted(isCapsOn);
		mKeyboardView.setKeyboard(mKeyboard);
		mKeyboardView.setOnKeyboardActionListener(this);
		return mKeyboardView;
	}

	private Keyboard getKeyboard(KEYS_TYPE locale) {
		if (Objects.requireNonNull(locale) == KEYS_TYPE.SYMBOLS) {
			return new Keyboard(this, R.xml.keys_definition_symols);
		}
		return new Keyboard(this, R.xml.keys_definition);
	}

	private void handleShift(boolean capsOn) {
		isCapsOn = capsOn;
		mKeyboard.setShifted(isCapsOn);
		mKeyboardView.invalidateAllKeys();
	}

	private void resetShift() {
		shiftKey.icon = getResources().getDrawable(R.drawable.ic_upper_24dp);
		pushCount = 0;
		handleShift(false);
	}

	private void handleLanguageSwitch() {
		if (inputMethodManager != null)
//			inputMethodManager.switchToNextInputMethod(getToken(), false); // open next input method
			inputMethodManager.showInputMethodPicker(); // open dialog for select input method
	}

	private void handleSymbolsSwitch() {
		if (mCurrentLocale != KEYS_TYPE.SYMBOLS) {
			mKeyboard = getKeyboard(KEYS_TYPE.SYMBOLS);
			mCurrentLocale = KEYS_TYPE.SYMBOLS;
		} else {
			mKeyboard = getKeyboard(KEYS_TYPE.ENGLISH);
			mCurrentLocale = KEYS_TYPE.ENGLISH;
			mKeyboard.setShifted(isCapsOn);
		}
		mKeyboardView.setKeyboard(mKeyboard);
		mKeyboardView.invalidateAllKeys();
	}

	/*
	* Using for "next" switch input method. See handleLanguageSwitch() method also
	 */
//	private IBinder getToken() {
//		final Dialog dialog = getWindow();
//		if (dialog == null)
//			return null;
//		final Window window = dialog.getWindow();
//		if (window == null)
//			return null;
//		return window.getAttributes().token;
//	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		if (primaryCode != 0) {
			InputConnection ic = getCurrentInputConnection();

			switch (primaryCode) {
				case Keyboard.KEYCODE_DELETE:
					ic.deleteSurroundingText(1, 0);
					break;
				case Keyboard.KEYCODE_SHIFT:
					if (shiftKey == null) {
						shiftKey = mKeyboard.getKeys().get(mKeyboard.getShiftKeyIndex());
					}
					switch (pushCount) {
						case 0:
							pushCount++;
							shiftKey.icon = getResources().getDrawable(R.drawable.ic_upper_tmp_24dp);
							handleShift(true);
							shiftKey.on = false;
							break;
						case 1:
							pushCount++;
							shiftKey.icon = getResources().getDrawable(R.drawable.ic_upper_24dp);
							handleShift(true);
							break;
						default:
							resetShift();
					}
					break;
				case Keyboard.KEYCODE_DONE:
					ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
					break;
				case Keyboard.KEYCODE_ALT:
					handleSymbolsSwitch();
					break;
				case Keyboard.KEYCODE_MODE_CHANGE:
					handleLanguageSwitch();
					break;
				default:
					char code = (char) primaryCode;
					if (isCapsOn) {
						if (Character.isLetter(code)) {
							code = Character.toUpperCase(code);
						} else if (mCurrentLocale != KEYS_TYPE.SYMBOLS) {
							if (Character.isDigit(code)) {
								if (code == '1')
									code = 33;
								else if (code == '2') {
									code = 64;
								} else if (code == '3') {
									code = 35;
								} else if (code == '4') {
									code = 36;
								} else if (code == '5') {
									code = 37;
								} else if (code == '6') {
									code = 38;
								} else if (code == '7') {
									code = 42;
								} else if (code == '8') {
									code = 94;
								} else if (code == '9') {
									code = 40;
								} else if (code == '0') {
									code = 41;
								}
							} else if (code == '-') {
								code = 95;
							} else if (code == '=') {
								code = 43;
							}
						}
						if (pushCount == 1) {
							resetShift();
						}
					}
					ic.commitText(String.valueOf(code), 1);
			}
		}
	}

	@Override
	public void onPress(int primaryCode) {
		switch (primaryCode) {
			case Keyboard.KEYCODE_DELETE:
			case Keyboard.KEYCODE_SHIFT:
			case Keyboard.KEYCODE_DONE:
			case Keyboard.KEYCODE_ALT:
			case Keyboard.KEYCODE_MODE_CHANGE:
				mKeyboardView.setPreviewEnabled(false);
				break;
		}
	}

	@Override
	public void onRelease(int primaryCode) {
		mKeyboardView.setPreviewEnabled(true);
	}

	@Override
	public void onText(CharSequence text) {
		if (text != null) {
			InputConnection ic = getCurrentInputConnection();
			if (isCapsOn) {
				if (text.equals("`"))
					text = "~";
				else if (text.equals("["))
					text = "{";
				else if (text.equals("]"))
					text = "}";
				else if (text.equals(";"))
					text = ":";
				else if (text.equals("'"))
					text = "\"";
				else if (text.equals(","))
					text = "<";
				else if (text.equals("."))
					text = ">";
				else
					text = String.valueOf(Character.toUpperCase(text.charAt(0)));

				if (pushCount == 1) {
					resetShift();
				}
			}
			ic.commitText(text, 1);
		}
	}

	@Override
	public void swipeDown() {
	}

	@Override
	public void swipeLeft() {
	}

	@Override
	public void swipeRight() {
	}

	@Override
	public void swipeUp() {
	}
}
