package com.exchange_simulator.service;

import com.exchange_simulator.dto.user.UserCreateRequestDto;
import com.exchange_simulator.dto.user.UserResponseDto;
import com.exchange_simulator.entity.User;
import com.exchange_simulator.exceptionHandler.exceptions.database.UserAlreadyExistsException;
import com.exchange_simulator.exceptionHandler.exceptions.database.UserNotFoundException;
import com.exchange_simulator.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        String role = userData.getRole().toUpperCase();
        return userRepository.save(new User(userData.getUsername(), userData.getEmail(),
                password, role));
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
    public User findUserByIdWithLock(Long userId) {
        return userRepository.findByIdWithLock(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    public void updateFunds(User user, BigDecimal amount){
        if(user.getFunds().add(amount).compareTo(BigDecimal.ZERO) < 0){
            user.setFunds(BigDecimal.ZERO);
        }else{
            user.setFunds(user.getFunds().add(amount));
        }
        userRepository.save(user);
    }
}
