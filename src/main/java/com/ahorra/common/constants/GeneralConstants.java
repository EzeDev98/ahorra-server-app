package com.ahorra.common.constants;

import java.math.BigDecimal;
import java.time.Duration;

public class GeneralConstants {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String APP_NAME = "Ahorra";
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    public static final String REPLACED_BY_NEW_LOGIN = "REPLACED_BY_NEW_LOGIN";
    public static final String LOGOUT = "LOGOUT";
    public static final String ADMIN_ACTION = "ADMIN_ACTION";
    public static final int RESET_PASSWORD_RATE_LIMIT_DURATION = 24;
    public static final int MAX_RESET_PASSWORD_REQUEST = 5;
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000000");
    public static final int MAX_CUSTOM_PERIOD_DAYS = 366;
    public static final int PIN_LENGTH = 6;
    public static final int MAX_PIN_ATTEMPTS = 5;
    public static final int PIN_LOCK_MINUTES = 15;
    public static final int PIN_RESET_WINDOW_MINUTES =  10;
    public static final String TOOL_NAME = "record_entry";
    public static final int MAX_TEXT_LENGTH = 2000;
    public static final int MAX_CLIENT_REQUEST_ID_LENGTH = 100;
    public static final int MAX_VENDOR_LENGTH = 255;
    public static final BigDecimal MAX_TOTAL = new BigDecimal("1000000000000");
    public static final int MAX_ITEMS = 50;
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    private GeneralConstants() {
        throw new UnsupportedOperationException("General constant class cannot be instantiated");
    }
}
