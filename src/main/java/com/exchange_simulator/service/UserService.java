package com.exchange_simulator.service;

import com.exchange_simulator.dto.user.UserCreateRequestDto;
import com.exchange_simulator.dto.user.UserResponseDto;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void createSampleUser(){
        var user = new User("Joshua", "email@mail.email");
        userRepository.save(user);
    }

    public User createUser(UserCreateRequestDto userData){
        return userRepository.save(new User(userData.getName(), userData.getEmail()));
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
                user.getName(),
                user.getEmail(),
                user.getFunds()
        );
    }
}
