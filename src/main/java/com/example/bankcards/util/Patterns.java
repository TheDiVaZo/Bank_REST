package com.example.bankcards.util;

import org.intellij.lang.annotations.RegExp;

public enum Patterns {;
    // Приложение будет работать только с российскими мобильными телефонами. Код '+7' будет подразумеваться изначально, поэтому смысла его хранить нет.
    public static final @RegExp String PHONE_NUMBER = "[0-9]{10}";

    public static final @RegExp String NUMBER_4_CARD = "[0-9]{4}";

    public static final @RegExp String NUMBER_CARD = "[0-9]{16}";
}
