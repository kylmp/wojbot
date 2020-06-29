package stunts.wojbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import stunts.wojbot.entity.TwitterAccount;
import stunts.wojbot.entity.TwitterKeywords;

import java.util.List;
import java.util.Optional;

@Repository
public interface TwitterKeywordsRepo extends CrudRepository<TwitterKeywords, String> {

    Optional<List<TwitterKeywords>> findAllByUserId(TwitterAccount userId);

    Optional<TwitterKeywords> findByUserIdAndKeyword(TwitterAccount userId, String keyword);

    int deleteAllByKeyword(String keyword);

}
