package eureca.capstone.project.admin.transaction_feed.repository;

import eureca.capstone.project.admin.transaction_feed.entity.TransactionFeed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionFeedRepository extends JpaRepository<TransactionFeed, Long> {

}
