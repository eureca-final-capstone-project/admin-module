package eureca.capstone.project.admin.service.external;

import eureca.capstone.project.admin.dto.external.TransactionFeedDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Profile("!local-test")
public class TransactionModuleService {

    private final WebClient webClient;

    // application.yml에 transaction-module의 주소를 설정합니다.
    @Value("${transaction-module.url}")
    private String transactionModuleUrl;

//    public TransactionModuleService(WebClient.Builder webClientBuilder) {
//        this.webClient = webClientBuilder.baseUrl(transactionModuleUrl).build();
//    }

    public TransactionModuleService(WebClient.Builder webClientBuilder) {
        // webClientBuilder가 null일 경우를 대비
        this.webClient = (webClientBuilder == null) ? null : webClientBuilder.baseUrl(transactionModuleUrl).build();
    }

    /**
     * transaction-module로부터 게시글 상세 정보를 조회합니다.
     * @param transactionFeedId 조회할 게시글 ID
     * @return 게시글 정보 Mono
     */
    public Mono<TransactionFeedDto> getFeedDetails(Long transactionFeedId) {
        return webClient.get()
                // transaction-module에 구현되어야 할 내부 API 엔드포인트
                .uri("/api/internal/feeds/{transactionFeedId}", transactionFeedId)
                .retrieve()
                .bodyToMono(TransactionFeedDto.class);
    }
}