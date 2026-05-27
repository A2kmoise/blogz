package codewithmoise.org.blogbackend.services;

import codewithmoise.org.blogbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public  AuthService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void createAccount(){}

    public void login(){}

    public void updateProfile(){}

    public void logout(){}
}
