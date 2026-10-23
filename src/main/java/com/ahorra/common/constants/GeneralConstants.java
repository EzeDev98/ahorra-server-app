package com.ahorra.common.constants;

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

    private GeneralConstants() {
        throw new UnsupportedOperationException("General constant class cannot be instantiated");
    }
}
