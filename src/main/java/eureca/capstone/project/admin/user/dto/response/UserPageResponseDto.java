package eureca.capstone.project.admin.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserPageResponseDto {
    private List<UserResponseDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public UserPageResponseDto(Page<UserResponseDto> pageData) {
        this.content = pageData.getContent();
        this.page = pageData.getNumber();
        this.size = pageData.getSize();
        this.totalElements = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
    }
}
