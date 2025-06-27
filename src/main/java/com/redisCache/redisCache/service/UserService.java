package com.redisCache.redisCache.service;

import com.redisCache.redisCache.entity.User;
import com.redisCache.redisCache.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        // Fetch the user from the database. The result will be cached.
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @CacheEvict(value = "users", key = "#user.id")
    public User saveOrUpdateUser(User user) {
        // Save the user to the database and evict cache
        return userRepository.save(user);
    }

    @CacheEvict(value = "users", key = "#id")
    public void deleteUserById(Long id) {
        // Delete the user from the database and evict the cache
        userRepository.deleteById(id);
    }
}