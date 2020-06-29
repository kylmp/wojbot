package stunts.wojbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import stunts.wojbot.entity.TwitterAccount;
import stunts.wojbot.entity.TwitterKeywords;
import stunts.wojbot.entity.WojBotUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface WojBotUserRepo extends CrudRepository<WojBotUser, Long> {
}
