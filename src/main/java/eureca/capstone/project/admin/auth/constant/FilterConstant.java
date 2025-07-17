package eureca.capstone.project.admin.auth.constant;

public final class FilterConstant {
    public static final String[] whiteList = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error",
            "/favicon.ico",
            "/v3/api-docs/**"
    };

    public static final String[] blackList = {
        "/**"
    };

    public static final String REFRESH_PATH = "api/admin/auth/reissue";
}
