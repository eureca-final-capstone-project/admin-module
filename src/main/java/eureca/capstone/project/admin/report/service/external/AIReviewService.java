package eureca.capstone.project.admin.report.service.external;

import eureca.capstone.project.admin.report.dto.request.AIReviewRequestDto;
import eureca.capstone.project.admin.report.dto.response.AIReviewResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class AIReviewService {

    private final ChatClient chatClient;

    public AIReviewService(@Qualifier("reportReviewClient") ChatClient reportReviewClient) {
        this.chatClient = reportReviewClient;
    }

    public AIReviewResponseDto requestReview(AIReviewRequestDto requestDto) {
        var outputConverter = new BeanOutputConverter<>(AIReviewResponseDto.class);

        Map<String, Object> userPromptMap = Map.of(
                "title", requestDto.getTitle(),
                "content", requestDto.getContent(),
                "reportType", requestDto.getReportType(),
                "reportReason", requestDto.getReportContent()
        );

        // 4. ChatClient를 사용하여 LLM 호출
        AIReviewResponseDto response = chatClient.prompt()
                .system(p -> p.param("format", outputConverter.getFormat()))
                .user(p -> p.text("""
                    다음 게시글과 신고 내용을 검토해 주세요:
                    
                    - 게시글 제목: {title}
                    - 게시글 내용: {content}
                    - 신고 유형: {reportType}
                    - 신고 사유: {reportReason}
                    """).params(userPromptMap))
                .call()
                .entity(AIReviewResponseDto.class);

        log.info("AI 판단 결과: {} {}", response.getResult(), response.getConfidence());
        return response;
    }
}