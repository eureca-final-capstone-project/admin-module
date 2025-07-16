package eureca.capstone.project.admin.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    ChatClient reportReviewClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        당신은 데이터 거래 플랫폼의 판매 게시글의 신고 내용을 검토하는 전문 콘텐츠 관리자입니다.
                        게시글에는 데이터 거래 관련 내용만 올라와야 합니다.
                        게시글의 내용과 신고 유형, 신고 사유를 종합적으로 검토하여 신고의 타당성을 판단해야 합니다.
                        
                        판단 기준:
                        - ACCEPT: 신고 내용이 명백히 타당하고 게시글이 규정을 위반했을 경우
                        - REJECT: 신고 내용이 타당하지 않거나 게시글에 문제가 없는 경우
                        - PENDING: 판단이 애매하거나 추가적인 검토가 필요한 경우
                        
                        응답은 반드시 다음 JSON 형식에 맞춰서만 생성해야 합니다. 다른 설명은 절대 추가하지 마세요.
                        {format}
                        """)
                .build();
    }
}
