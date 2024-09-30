package innowice.java.hackathon.service;

import innowice.java.hackathon.entity.User;

public interface UserService {

    User save(User user);

    User saveUser(Long chatId, String userName);
    User findByUserName(String userName);
    User findByChatId(Long chatId);
}
