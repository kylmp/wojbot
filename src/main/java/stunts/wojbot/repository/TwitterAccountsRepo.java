package stunts.wojbot.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import stunts.wojbot.entity.TwitterAccount;

import java.util.Optional;

@Repository
public interface TwitterAccountsRepo extends CrudRepository<TwitterAccount, String> {

    Optional<TwitterAccount> findByHandle(String handle);

    Optional<Iterable<TwitterAccount>> findAllByMuteEquals(boolean isMuted);

}
