package eureca.capstone.project.admin.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class AIConfig {

    @Bean
    ChatClient reportReviewClient(ChatClient.Builder builder) throws IOException {
        return builder.build();
    }
}
