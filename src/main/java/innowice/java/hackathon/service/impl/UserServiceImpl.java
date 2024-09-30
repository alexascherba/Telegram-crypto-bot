package innowice.java.hackathon.service.impl;

import innowice.java.hackathon.repository.UserRepository;
import innowice.java.hackathon.entity.User;
import innowice.java.hackathon.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User saveUser(Long chatId, String userName) {
        User user = new User();
        user.setUserName(userName);
        user.setChatId(chatId);
        return save(user);
    }

    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public User findByChatId(Long chatId) {
        return userRepository.findByChatId(chatId);
    }
}
