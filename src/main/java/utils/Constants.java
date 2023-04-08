package utils;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final int MINIMUM_PASSWORD_LENGTH = 6;

    public static final int MINIMUM_USERNAME_LENGTH = 3;

    public static final int SALT_SIZE = 8;

    public static final int INITIAL_TOKEN_COUNT = 100;

    public static final int WORD_SIZE = 5;

    public static final int MAX_GUESS_COUNT = 6;

    public static final String ALPHA_NUMERIC_STRING = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
}
