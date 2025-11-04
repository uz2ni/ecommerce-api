package com.example.ecommerceapi.domain.repository;

import com.example.ecommerceapi.domain.entity.User;

import java.util.List;

public interface UserRepository {

    List<User> getAllUsers();

    User getUser(Integer userId);

    Integer getBalance(Integer userId);

    void updateBalance(Integer userId, Integer newBalance);

}
