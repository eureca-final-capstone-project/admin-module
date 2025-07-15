package eureca.capstone.project.admin.repository;

import eureca.capstone.project.admin.domain.transaction_feed.entity.TransactionFeed;
import eureca.capstone.project.admin.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionFeedRepository extends JpaRepository<TransactionFeed, Long> {

}
