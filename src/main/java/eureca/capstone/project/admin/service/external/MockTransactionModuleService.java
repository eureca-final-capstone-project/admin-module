package eureca.capstone.project.admin.service.external;

import eureca.capstone.project.admin.dto.external.TransactionFeedDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Profile("local-test") // "local-test" 프로필에서만 활성화
public class MockTransactionModuleService extends TransactionModuleService {

    // WebClient를 사용하지 않으므로 생성자도 간단하게 변경
    public MockTransactionModuleService() {
        super(null); // 부모 클래스의 생성자를 호출하지만, 사용하지 않으므로 null 전달
    }

    @Override
    public Mono<TransactionFeedDto> getFeedDetails(Long transactionFeedId) {
        System.out.println("====== MockTransactionModuleService is called! ======");
        // 실제 통신 대신, 미리 만들어둔 가짜 데이터를 즉시 반환합니다.
        TransactionFeedDto fakeDto = new TransactionFeedDto(
                "LG U+ 500메가 팔게요",
                "싸게 팝니다 가져가세요"
        );
        return Mono.just(fakeDto);
    }
}
