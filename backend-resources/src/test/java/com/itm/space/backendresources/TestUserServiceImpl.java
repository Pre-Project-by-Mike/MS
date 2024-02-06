package com.itm.space.backendresources;

import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestUserServiceImpl {
    @Autowired
    UserService service;
    @Test
    public void testGetUserById() {
        System.out.println(service.getUserById(UUID.fromString("595674a5-66a0-448b-8f8e-d85a891c87f5")));
    }
}
