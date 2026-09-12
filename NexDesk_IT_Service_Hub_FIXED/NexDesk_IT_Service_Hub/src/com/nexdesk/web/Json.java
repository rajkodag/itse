package com.nexdesk.web;

public final class Json {

    private Json() {
    }

    public static String esc(String s) {

        if (s == null) {
            return "";
        }

        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    public static String str(
            String key,
            String value) {

        return "\"" +
                esc(key) +
                "\":\"" +
                esc(value) +
                "\"";
    }

    public static String num(
            String key,
            int value) {

        return "\"" +
                esc(key) +
                "\":" +
                value;
    }

    public static String nullableNum(
            String key,
            Integer value) {

        return "\"" +
                esc(key) +
                "\":" +
                (value == null
                        ? "null"
                        : value.toString());
    }
}