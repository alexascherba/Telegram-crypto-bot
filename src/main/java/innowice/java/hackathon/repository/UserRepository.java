package innowice.java.hackathon.repository;

import innowice.java.hackathon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserName(String userName);
    User findByChatId(Long chatId);
}
