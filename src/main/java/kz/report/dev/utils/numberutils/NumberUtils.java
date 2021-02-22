package kz.report.dev.utils.numberutils;

public class NumberUtils {
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isDouble(String str) {
        if (str == null) {
            return false;
        }

        int length = str.length();
        if (length == 0) {
            return false;
        }

        return true;
    }

    public static int parseInt(String str) {
        int res = 0;
        if (isInteger(str)) {
            res = Integer.parseInt(str);
        }
        return res;
    }

    public static double parseDouble(String str) {
        double res = 0;
        if (isDouble(str)) {
            res = Double.parseDouble(str);
        }
        return res;
    }
}
