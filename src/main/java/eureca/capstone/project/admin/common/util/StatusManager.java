package eureca.capstone.project.admin.common.util;


import eureca.capstone.project.admin.common.entity.Status;
import eureca.capstone.project.admin.common.exception.custom.StatusNotFoundException;
import eureca.capstone.project.admin.common.repository.StatusRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StatusManager {

    private final StatusRepository statusRepository;
    private Map<String, Map<String, Status>> statusCache;

    @PostConstruct
    public void init() {
        statusCache = statusRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Status::getDomain,
                        Collectors.toMap(Status::getCode, Function.identity())
                ));
    }

    /**
     * 도메인과 상태 이름을 통해 Status 객체를 반환하는 메서드
     * @param domain 상태가 속한 도메인 (예: "USER", "PAY")
     * @param code 상태 이름 (예: "PENDING", "ACTIVE")
     * @return Status 객체
     */
    public Status getStatus(String domain, String code) {
        return Optional.ofNullable(statusCache.get(domain))
                .map(domainMap -> domainMap.get(code))
                .orElseThrow(StatusNotFoundException::new);
    }
}
