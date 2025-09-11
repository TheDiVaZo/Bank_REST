package com.example.bankcards.util;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import java.util.regex.Pattern;

public enum CardUtil {;
    public static final IntUnaryOperator CHAR_TO_INT = ch -> ch-'0';
    private static final Pattern cardNumberPattern = Pattern.compile(Patterns.NUMBER_CARD);
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    public static String generateCardNumber() {
        int[] digits = new int[16];
        int controlNumIndex = 15;

        for (int i = 0; i < 15; i++) {
            digits[i] = random.nextInt(10);
        }
        int sum = 0;
        for (int i = 14; i >= 0; i--) {
            int digit = digits[i];
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }

        digits[controlNumIndex] = (10 - (sum % 10)) % 10;

        StringBuilder cardNumber = new StringBuilder();
        for (int digit : digits) {
            cardNumber.append(digit);
        }

        return cardNumber.toString();
    }

    public static boolean cardIsValid(String number) {
        if (!cardNumberPattern.matcher(number).matches()) return false;
        AtomicInteger atomicInteger = new AtomicInteger(1);
        int controlSum = number.chars().map(ch -> {
            int digit = CHAR_TO_INT.applyAsInt(ch);
            int modifiedDigit = digit * 2;
            if (modifiedDigit > 9) modifiedDigit -= 9;
            return atomicInteger.getAndIncrement() % 2 != 0 ? modifiedDigit : digit;
        }).sum();
        return controlSum % 10 == 0;
    }
}
