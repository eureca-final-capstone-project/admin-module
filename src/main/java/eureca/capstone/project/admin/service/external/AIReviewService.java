package eureca.capstone.project.admin.service.external;

import eureca.capstone.project.admin.dto.request.AIReviewRequestDto;
import eureca.capstone.project.admin.dto.response.AIReviewResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class AIReviewService {

    private final ChatClient chatClient;

    public AIReviewService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Mono<AIReviewResponseDto> requestReview(AIReviewRequestDto requestDto) {
        // 1. LLM의 응답을 AIReviewResponseDto.class 형태로 파싱하기 위한 OutputParser 생성
        var outputConverter = new BeanOutputConverter<>(AIReviewResponseDto.class);

        // 2. LLM에게 보낼 시스템 프롬프트 정의
        String systemPrompt = """
            당신은 커뮤니티 게시글의 신고 내용을 검토하는 전문 콘텐츠 관리자입니다.
            게시글의 내용과 신고 유형, 신고 사유를 종합적으로 검토하여 신고의 타당성을 판단해야 합니다.
            
            판단 기준:
            - ACCEPT: 신고 내용이 명백히 타당하고 게시글이 규정을 위반했을 경우
            - REJECT: 신고 내용이 타당하지 않거나 게시글에 문제가 없는 경우
            - PENDING: 판단이 애매하거나 추가적인 검토가 필요한 경우

            응답은 반드시 다음 JSON 형식에 맞춰서만 생성해야 합니다. 다른 설명은 절대 추가하지 마세요.
            {format}
            """;

        // 3. 사용자 프롬프트(실제 데이터)를 담을 맵 생성
        Map<String, Object> userPromptMap = Map.of(
                "title", requestDto.getTitle(),
                "content", requestDto.getContent(),
                "reportType", requestDto.getReportType(),
                "reportReason", requestDto.getReportContent()
        );

        // 4. ChatClient를 사용하여 LLM 호출
        ChatResponse response = chatClient.prompt()
                .system(p -> p.text(systemPrompt).param("format", outputConverter.getFormat()))
                .user(p -> p.text("""
                    다음 게시글과 신고 내용을 검토해 주세요:
                    
                    - 게시글 제목: {title}
                    - 게시글 내용: {content}
                    - 신고 유형: {reportType}
                    - 신고 사유: {reportReason}
                    """).params(userPromptMap))
                .call()
                .chatResponse();

        // 5. LLM의 응답을 파싱하여 DTO 객체로 변환 후 Mono로 감싸서 반환
        AIReviewResponseDto reviewResponseDto = outputConverter.convert(response.getResult().getOutput().getText());
        log.info("AI 판단 결과: {} {}", reviewResponseDto.getResult(), reviewResponseDto.getConfidence());
        return Mono.just(reviewResponseDto);
    }
}