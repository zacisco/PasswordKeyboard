package com.zac.pswdKb;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zac on 12.10.17.
 */

public class pswdKb extends InputMethodService implements KeyboardView.OnKeyboardActionListener {
    private KeyboardView mKeyboardView;
    private Keyboard mKeyboard;
    private KEYS_TYPE mCurrentLocale = KEYS_TYPE.ENGLISH;
    private ShiftState shiftState = ShiftState.OFF;
    private Keyboard.Key shiftKey;

    private static final Map<Character, Character> SHIFT_MAP = new HashMap<>();
    private static final Map<String, String> TEXT_SHIFT_MAP = new HashMap<>();

    static {
        SHIFT_MAP.put('1', '!');
        SHIFT_MAP.put('2', '@');
        SHIFT_MAP.put('3', '#');
        SHIFT_MAP.put('4', '$');
        SHIFT_MAP.put('5', '%');
        SHIFT_MAP.put('6', '^');
        SHIFT_MAP.put('7', '&');
        SHIFT_MAP.put('8', '*');
        SHIFT_MAP.put('9', '(');
        SHIFT_MAP.put('0', ')');
        SHIFT_MAP.put('-', '_');
        SHIFT_MAP.put('=', '+');

        TEXT_SHIFT_MAP.put("`", "~");
        TEXT_SHIFT_MAP.put("[", "{");
        TEXT_SHIFT_MAP.put("]", "}");
        TEXT_SHIFT_MAP.put(";", ":");
        TEXT_SHIFT_MAP.put("'", "\"");
        TEXT_SHIFT_MAP.put(",", "<");
        TEXT_SHIFT_MAP.put(".", ">");
    }

    private enum KEYS_TYPE {
        SYMBOLS, ENGLISH
    }

    private enum ShiftState {
        OFF,
        SHIFTED_ONCE,
        CAPS_LOCK
    }

    @Override
    public View onCreateInputView() {
        mKeyboardView = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        mKeyboard = getKeyboard(mCurrentLocale);
        setShiftKey();
        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.setOnKeyboardActionListener(this);
        mKeyboardView.setPreviewEnabled(false);
        return mKeyboardView;
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        if (primaryCode == 0 || getCurrentInputConnection() == null) return;

        InputConnection ic = getCurrentInputConnection();
        long eventTime = SystemClock.uptimeMillis();

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                handleGeneralKey(ic, eventTime, KeyEvent.KEYCODE_DEL);
                break;
            case Keyboard.KEYCODE_SHIFT:
                handleShiftKey();
                break;
            case Keyboard.KEYCODE_DONE:
                handleGeneralKey(ic, eventTime, KeyEvent.KEYCODE_ENTER);
                break;
            case Keyboard.KEYCODE_ALT:
                handleSymbolsSwitch();
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:
                handleLanguageSwitch();
                break;
            default:
                handleCharacterKey((char) primaryCode, ic);
        }
    }

    @Override
    public void onText(CharSequence text) {
        if (text == null) return;

        InputConnection ic = getCurrentInputConnection();
        if (shiftState != ShiftState.OFF) {
            if (TEXT_SHIFT_MAP.containsKey(text)) {
                text = TEXT_SHIFT_MAP.get(text);
            } else {
                text = String.valueOf(Character.toUpperCase(text.charAt(0)));
            }
            if (shiftState == ShiftState.SHIFTED_ONCE) {
                resetShift();
            }
        }
        ic.commitText(text, 1);
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
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

    private void handleCharacterKey(char primaryCode, InputConnection ic) {
        char code = primaryCode;
        if (shiftState != ShiftState.OFF) {
            if (Character.isLetter(code)) {
                code = Character.toUpperCase(code);
            } else if (mCurrentLocale != KEYS_TYPE.SYMBOLS && SHIFT_MAP.containsKey(code)) {
                code = SHIFT_MAP.get(code);
            }
            if (shiftState == ShiftState.SHIFTED_ONCE) {
                resetShift();
            }
        }
        ic.commitText(String.valueOf(code), 1);
    }

    private static void handleGeneralKey(InputConnection ic, long eventTime, int keycodeDel) {
        ic.sendKeyEvent(new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keycodeDel, 0, 0));
        ic.sendKeyEvent(new KeyEvent(eventTime + 1, eventTime + 1, KeyEvent.ACTION_UP, keycodeDel, 0, 0));
    }

    private void handleShiftKey() {
        switch (shiftState) {
            case OFF:
                shiftState = ShiftState.SHIFTED_ONCE;
                shiftKey.icon = getResources().getDrawable(getShiftIcon());
                shiftKey.on = false;
                break;
            case SHIFTED_ONCE:
                shiftState = ShiftState.CAPS_LOCK;
                break;
            default:
                resetShift();
        }
        mKeyboard.setShifted(shiftState != ShiftState.OFF);
        mKeyboardView.invalidateAllKeys();
    }

    private void handleSymbolsSwitch() {
        mCurrentLocale = mCurrentLocale == KEYS_TYPE.SYMBOLS ? KEYS_TYPE.ENGLISH : KEYS_TYPE.SYMBOLS;
        mKeyboard = getKeyboard(mCurrentLocale);

        setShiftKey();
        resetShift();

        mKeyboardView.setKeyboard(mKeyboard);
        mKeyboardView.invalidateAllKeys();
    }

    private void setShiftKey() {
        int shiftIndex = mKeyboard.getShiftKeyIndex();
        shiftKey = shiftIndex >= 0 ? mKeyboard.getKeys().get(shiftIndex) : null;
    }

    private void resetShift() {
        if (shiftKey == null) return;

        shiftState = ShiftState.OFF;
        shiftKey.icon = getResources().getDrawable(getShiftIcon());
        mKeyboard.setShifted(false);
        mKeyboardView.invalidateAllKeys();
    }

    private int getShiftIcon() {
        return shiftState == ShiftState.OFF ? R.drawable.ic_upper_24dp : R.drawable.ic_upper_tmp_24dp;
    }

    private Keyboard getKeyboard(KEYS_TYPE type) {
        return type == KEYS_TYPE.SYMBOLS
                ? new Keyboard(this, R.xml.keys_definition_symols)
                : new Keyboard(this, R.xml.keys_definition);
    }

    private void handleLanguageSwitch() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
//			inputMethodManager.switchToNextInputMethod(getToken(), false); // open next input method
            imm.showInputMethodPicker(); // open dialog for select input method
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
}
