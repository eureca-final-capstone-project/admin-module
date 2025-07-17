package eureca.capstone.project.admin.auth.constant;

public final class TokenConstant {
    // 1시간
    public static final long accessTokenValidity = 60 * 60 * 1000L;
    // 2주 (ms)
    public static final long refreshTokenValidity = 14 * 24 * 60 * 60 * 1000L;
    // (second)
    public static final int REFRESH_TOKEN_MAX_AGE_SEC = (int) (refreshTokenValidity / 1000);
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
}
