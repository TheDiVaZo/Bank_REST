package com.example.bankcards.view;

public enum View {;
    public static sealed class Public permits Owner, Admin {}
    public static final class Owner extends Public {}
    public static final class Admin extends Public {}
}
