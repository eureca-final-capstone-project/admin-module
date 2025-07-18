package eureca.capstone.project.admin.report.service.external;

import eureca.capstone.project.admin.report.dto.request.AIReviewRequestDto;
import eureca.capstone.project.admin.report.dto.response.AIReviewResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@Slf4j
public class AIReviewService {

    private final ChatClient chatClient;
    private final String systemPromptTemplate;

    public AIReviewService(@Qualifier("reportReviewClient") ChatClient chatClient,
                           @Value("classpath:prompts/report-review-system.txt") Resource systemPromptResource) {
        this.chatClient = chatClient;
        try {
            this.systemPromptTemplate = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);
            log.info("리뷰 프롬프트 로딩 성공");
        } catch (IOException e) {
            log.error("리뷰 프롬프트 로딩 실패");
            throw new UncheckedIOException("리뷰 프롬프트 로딩 실패", e);
        }
    }

    public AIReviewResponseDto requestReview(AIReviewRequestDto requestDto) {
        var outputConverter = new BeanOutputConverter<>(AIReviewResponseDto.class);

        // 1. 수동으로 시스템 프롬프트의 {format} 부분을 채워서 완성된 문자열을 만듭니다.
        String finalSystemPrompt = systemPromptTemplate.replace("{format}", outputConverter.getFormat());

        Map<String, Object> userPromptMap = Map.of(
                "title", requestDto.getTitle(),
                "content", requestDto.getContent(),
                "reportType", requestDto.getReportType(),
                "reportReason", requestDto.getReportContent()
        );

        // 2. ChatClient를 사용하여 LLM 호출
        AIReviewResponseDto response = chatClient.prompt()
                // 3. 완성된 finalSystemPrompt를 전달합니다. (템플릿 처리 X)
                .system(finalSystemPrompt)
                .user(p -> p.text("""
                    다음 게시글과 신고 내용을 검토해 주세요:
                    
                    - 게시글 제목: {title}
                    - 게시글 내용: {content}
                    - 신고 유형: {reportType}
                    - 신고 사유: {reportReason}
                    """).params(userPromptMap))
                .call()
                .entity(AIReviewResponseDto.class);

        log.info("AI 판단 결과: {}, 이유: [{}], 신뢰도: {}", response.getResult(), response.getReason(), response.getConfidence());
        return response;
    }
}