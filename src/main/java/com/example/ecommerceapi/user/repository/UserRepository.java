package com.example.ecommerceapi.user.repository;

import com.example.ecommerceapi.user.entity.User;

import java.util.List;

public interface UserRepository {

    List<User> findAll();

    User findById(Integer userId);

    Integer findBalanceById(Integer userId);

    void updateBalance(Integer userId, Integer newBalance);

}
