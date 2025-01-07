package com.flectomanager.gui;

import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class CustomPasswordField extends TextField {

    private static final char BULLET = '\u2022';
    private boolean isMasked = true;
    private StringBuilder password = new StringBuilder();

    public CustomPasswordField() {
        super();
    }

    @Override
    public void replaceText(int start, int end, String text) {
        password.replace(start, end, text);

        if (isMasked) {
            super.replaceText(0, super.getText().length(), getMaskedText());
        } else {
            super.replaceText(start, end, text);
        }

        positionCaret(start + text.length());
    }

    @Override
    public void replaceSelection(String replacement) {
        int start = getSelection().getStart();
        int end = getSelection().getEnd();
        replaceText(start, end, replacement);
    }

    @Override
    public void copy() {
        String textToCopy;
        if (isMasked) {
            textToCopy = password.toString().substring(getSelection().getStart(), getSelection().getEnd());
        } else {
            textToCopy = super.getSelectedText();
        }

        if (textToCopy.length() > 0) {
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(textToCopy);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        }
    }

    private String getMaskedText() {
        return String.valueOf(BULLET).repeat(password.length());
    }

    public void toggleMask() {
        int caretPosition = getCaretPosition();
        isMasked = !isMasked;
        if (isMasked) {
            super.replaceText(0, super.getText().length(), getMaskedText());
        } else {
            super.replaceText(0, super.getText().length(), password.toString());
        }
        positionCaret(caretPosition);
    }

    public String getPassword() {
        return password.toString();
    }
}
