package kz.report.dev.utils;

import java.math.BigDecimal;
import java.util.*;

public class Converter {

    private enum Level {UNITS, TENS, HUNDREDS, MILLIONS, BILLIONS};

    private static String[] units = {"ноль","один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"};
    private static String[] upperUnits = {"Один", "Два", "Три", "Четыре", "Пять", "Шесть", "Семь", "Восемь", "Девять"};
    private static String[] range10_19 = {"десять", "одинадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестьнадцать", "семьнадцать", "восемьнадцать", "девятнадцать"};
    private static String[] upperRange10_19 = {"Десять", "Одинадцать", "Двенадцать", "Тринадцать", "Четырнадцать", "Пятнадцать", "Шестьнадцать", "Семьнадцать", "Восемьнадцать", "Девятнадцать"};
    private static String[] tens = {"двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};
    private static String[] upperTens = {"Двадцать", "Тридцать", "Сорок", "Пятьдесят", "Шестьдесят", "Семьдесят", "Восемьдесят", "Девяносто"};
    private static String[] hundreds = {"сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "деятьсот"};
    private static String[] upperHundreds = {"Сто", "Двести", "Триста", "Четыреста", "Пятьсот", "Шестьсот", "Семьсот", "Восемьсот", "Девятьсот"};
    private static String[] thousands = {"тысяча", "тысячи",  "тысяч",};
    private static String[] millions = {"миллион", "миллиона", "миллионов"};
    private static String[] billions = {"миллиард", "миллиарда", "миллиардов"};



    public static String convert(String d) {
        if (Objects.isNull(d)) return "";
        String[] parts = d.split(",");
        return convert(parts);
    }
    public static String convert(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) return "";
        String units = BigDecimal.valueOf(d).toString();
        String[] parts = units.split("\\.");
        return convert(parts);
    }

    private static String convert(String[] parts) {
        Queue<Character> wholePart = new ArrayDeque<>();
        for (int i = 0; i < parts[0].length(); i++) {
            wholePart.add(parts[0].charAt(i));
        }
        String result = "";
        if (wholePart.size() >= 10) {
            if (wholePart.size() == 10) {
                result = recursiveRead(wholePart, 1, Level.BILLIONS);
            } else if (wholePart.size() == 11) {
                result = recursiveRead(wholePart, 2, Level.BILLIONS);
            } else if (wholePart.size() == 12){
                result = recursiveRead(wholePart, 3, Level.BILLIONS);
            }
        } else if (wholePart.size() >= 7) {
            if (wholePart.size() == 7) {
                result = recursiveRead(wholePart, 1, Level.MILLIONS);
            } else if (wholePart.size() == 8) {
                result = recursiveRead(wholePart, 2, Level.MILLIONS);
            } else {
                result = recursiveRead(wholePart, 3, Level.MILLIONS);
            }
        } else if (wholePart.size() >= 4) {
            if (wholePart.size() == 4) {
                result = recursiveRead(wholePart, 1, Level.HUNDREDS);
            } else if (wholePart.size() == 5) {
                result = recursiveRead(wholePart, 2, Level.HUNDREDS);
            } else {
                result = recursiveRead(wholePart, 3, Level.HUNDREDS);
            }
        } else if (wholePart.size() >= 1) {
            if (wholePart.size() == 1) {
                result = recursiveRead(wholePart, 1, Level.TENS);
            } else if (wholePart.size() == 2) {
                result = recursiveRead(wholePart, 2, Level.TENS);
            } else {
                result = recursiveRead(wholePart, 3, Level.TENS);
            }
        }
        if (parts.length == 1 || parts[1] == null || parts[1].length() == 0) {
            return result + "тенге 0 тиын";
        }
        return result + "тенге " + parts[1] + " тиын";
    }

    private static String recursiveRead(final Queue<Character> c, int cnt, Level level) {
        int _ch1;
        int _ch2;
        int _ch3;
        String result= "";
        if (cnt == 1) {
            _ch1 = Character.getNumericValue(c.poll());
            result = units[_ch1];
            result += " " + getPostfix(_ch1, level);
        } else if (cnt == 2) {
            _ch1 = Character.getNumericValue(c.poll());
            _ch2 = Character.getNumericValue(c.poll());
            if (_ch1 == 1) {
                result = range10_19[_ch2];
                result += " " + getPostfix(_ch2, level);
            } else {
                result = tens[_ch1 - 2];
                if (_ch2 == 0) {
                    result += " " + getPostfix(_ch2, level);
                } else {
                    result += " " + units[_ch2];
                    result += " " + getPostfix(_ch2, level);
                }
            }
        } else {
            _ch1 = Character.getNumericValue(c.poll());
            _ch2 = Character.getNumericValue(c.poll());
            _ch3 = Character.getNumericValue(c.poll());
            if (_ch1 > 0) {
                result = hundreds[_ch1 - 1];
            }
            if (_ch2 > 0) {
                if (_ch2 > 1) {
                    result +=  " " + tens[_ch2 - 2];
                } else {
                    result += " " + range10_19[_ch3];
                }
            }
            if (_ch3 > 0) {
                if (_ch2 == 0 || _ch2 > 1) {
                    result += " " + units[_ch3];
                }
            }
            if (!(_ch1 == 0 && _ch2 == 0 && _ch3 == 0)) {
                result += " " + getPostfix(_ch3, level);
            }

        }
        if (Level.TENS == level) {
            return result;
        } else if (Level.BILLIONS == level) {
            return result + " " + recursiveRead(c, 3, Level.MILLIONS);
        } else if (Level.MILLIONS == level) {
            return result + " " + recursiveRead(c, 3, Level.HUNDREDS);
        } else if (Level.HUNDREDS == level) {
            return result + " " + recursiveRead(c, 3, Level.TENS);
        }
        return result;
    }

    private static String getPostfix(int lastDigit, Level level) {
        if (Level.BILLIONS == level) {
            if (lastDigit == 0 || lastDigit > 4) {
                return billions[2];
            } else if (lastDigit > 1) {
                return billions[1];
            } else {
                return billions[0];
            }
        } else if (Level.MILLIONS == level) {
            if (lastDigit == 0 || lastDigit > 4) {
                return millions[2];
            } else if (lastDigit > 1) {
                return millions[1];
            } else {
                return millions[0];
            }
        } else if (Level.HUNDREDS == level) {
            if (lastDigit == 0 || lastDigit > 4) {
                return thousands[2];
            } else if (lastDigit > 1) {
                return thousands[1];
            } else {
                return thousands[0];
            }
        }
        return " ";
    }
}
