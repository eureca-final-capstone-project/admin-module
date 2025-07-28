package eureca.capstone.project.admin.common.constant;

import java.util.List;
import java.util.Map;

public final class RestrictionConst {
    public static final Long RES_WRITE_7DAYS = 1L;
    public static final Long RES_FOREVER = 2L;
    public static final Long RES_TRANSACTION = 3L;
    public static final Long RES_WRITE_1DAYS = 4L;

    // 제재 타입에 해당하는 권한리스트
    public static final Map<Long, List<String>> RTYPE_TO_AUTHORITIES = Map.of(
            RES_TRANSACTION , List.of("TRANSACTION", "WRITE") // 거래제한 + 쓰기제한
    );
}
