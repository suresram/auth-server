package com.s2.spring.cloud.auth.server.user.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.s2.spring.cloud.auth.server.user.model.User;



public interface UserRepository extends MongoRepository<User, String> {

	User findByUsername(String username);

}