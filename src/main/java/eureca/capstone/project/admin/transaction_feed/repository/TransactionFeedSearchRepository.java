package eureca.capstone.project.admin.transaction_feed.repository;

import eureca.capstone.project.admin.transaction_feed.document.TransactionFeedDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TransactionFeedSearchRepository extends ElasticsearchRepository<TransactionFeedDocument, Long> {
}
