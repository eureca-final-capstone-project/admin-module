package eureca.capstone.project.admin.common.component;

import eureca.capstone.project.admin.report.entity.ReportType;
import eureca.capstone.project.admin.report.entity.RestrictionType;
import eureca.capstone.project.admin.report.repository.ReportTypeRepository;
import eureca.capstone.project.admin.report.repository.RestrictionTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitComponent {
    private final ReportTypeRepository reportTypeRepository;
    private final RestrictionTypeRepository restrictionTypeRepository;

    @PostConstruct
    @Transactional
    public void init() {
        initReportType();
        initRestrictionType();
    }

    private void initReportType() {
        insertIfNotExists(1L, "욕설 및 비속어 포함");
        insertIfNotExists(2L, "주제 불일치");
        insertIfNotExists(3L, "음란 내용 포함");
        insertIfNotExists(4L, "기타");
    }

    private void insertIfNotExists(Long id, String explanation) {
        if (!reportTypeRepository.existsById(id)) {
            ReportType entity = ReportType.builder()
                    .reportTypeId(id)
                    .explanation(explanation)
                    .type(explanation)
                    .build();
            reportTypeRepository.save(entity);
        }
    }

    private void initRestrictionType() {
        insertRestrictionTypeIfNotExists(1L, "게시글 작성 제한", 7);
        insertRestrictionTypeIfNotExists(2L, "영구 제한", -1);
        insertRestrictionTypeIfNotExists(3L, "신고 제한", 30);
        insertRestrictionTypeIfNotExists(4L, "게시글 작성 제한(1일)", 1);
    }

    private void insertRestrictionTypeIfNotExists(Long id, String content, Integer duration) {
        if (!restrictionTypeRepository.existsById(id)) {
            RestrictionType entity = RestrictionType.builder()
                    .restrictionTypeId(id)
                    .content(content)
                    .duration(duration)
                    .build();
            restrictionTypeRepository.save(entity);
        }
    }
}

