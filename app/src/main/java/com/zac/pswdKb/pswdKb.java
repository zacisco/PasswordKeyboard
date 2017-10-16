package com.zac.pswdKb;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by zac on 12.10.17.
 */

public class pswdKb extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
	private KeyboardView mKeyboardView;
	private Keyboard mKeyboard;
	private boolean isCapsOn = false;
	private KEYS_TYPE mCurrentLocale;
	private InputMethodManager inputMethodManager;

	private enum KEYS_TYPE {
		SYMBOLS, ENGLISH
	}

	@Override
	public View onCreateInputView() {
		inputMethodManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
		mKeyboardView = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
		mCurrentLocale = KEYS_TYPE.ENGLISH;
		mKeyboard = getKeyboard(mCurrentLocale);
		mKeyboard.setShifted(isCapsOn);
		mKeyboardView.setKeyboard(mKeyboard);
		mKeyboardView.setOnKeyboardActionListener(this);
		return mKeyboardView;
	}

	private Keyboard getKeyboard(KEYS_TYPE locale) {
		switch (locale) {
			case SYMBOLS:
				return new Keyboard(this, R.xml.keys_definition_symols);
			default:
				return new Keyboard(this, R.xml.keys_definition);
		}
	}

	private void handleShift() {
		isCapsOn = !isCapsOn;
		mKeyboard.setShifted(isCapsOn);
		mKeyboardView.invalidateAllKeys();
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
					handleShift();
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
					if (Character.isLetter(code) && isCapsOn)
						code = Character.toUpperCase(code);

					ic.commitText(String.valueOf(code), 1);
					break;
			}
		}
	}

	@Override
	public void onPress(int primaryCode) {
	}

	@Override
	public void onRelease(int primaryCode) {
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
			}
			ic.commitText(text, 0);
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
