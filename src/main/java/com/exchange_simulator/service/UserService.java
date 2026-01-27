package com.exchange_simulator.service;

import com.exchange_simulator.dto.user.UserCreateRequestDto;
import com.exchange_simulator.dto.user.UserResponseDto;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.exceptionHandler.exceptions.database.UserAlreadyExistsException;
import com.exchange_simulator.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserCreateRequestDto userData){
        if(userRepository.existsByUsername(userData.getUsername())){
            throw new UserAlreadyExistsException("User with username '" + userData.getUsername() + "' already exists");
        }
        if(userRepository.existsByEmail(userData.getEmail())){
            throw new UserAlreadyExistsException("User with email '" + userData.getEmail() + "' already exists");
        }
        String password = passwordEncoder.encode(userData.getPassword());
        return userRepository.save(new User(userData.getUsername(), userData.getEmail(),
                password, userData.getRole()));
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }

    public static UserResponseDto getDto(User user){
        return new UserResponseDto(
                user.getId(),
                user.getUpdatedAt(),
                user.getCreatedAt(),
                user.getUsername(),
                user.getEmail(),
                user.getFunds()
        );
    }
}
