package eureca.capstone.project.admin.transaction_feed.repository;

import eureca.capstone.project.admin.transaction_feed.entity.SalesType;
import eureca.capstone.project.admin.transaction_feed.entity.TransactionFeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesTypeRepository extends JpaRepository<SalesType, Long> {
    Optional<SalesType> findByName(String name);
}
