package com.group15.b07project;

import android.text.Editable;
import android.text.TextWatcher;
import java.util.function.Consumer;

// TextWatcher helper: implement only afterTextChanged via a Consumer<String>.
public class SimpleTextWatcher implements TextWatcher {
    private final Consumer<String> onChange;

    public SimpleTextWatcher(Consumer<String> onChange) {
        this.onChange = onChange;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        onChange.accept(s.toString());
    }
}
