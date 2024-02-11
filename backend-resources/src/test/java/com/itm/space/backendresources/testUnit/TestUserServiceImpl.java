package com.itm.space.backendresources.testUnit;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestUserServiceImpl {
    @Autowired
    Keycloak keycloak;
    @Autowired
    UserService service;
    @Test
    public void testGetUserById() {
        UserResponse userResponse = service.getUserById(UUID.fromString("595674a5-66a0-448b-8f8e-d85a891c87f5"));
        Assertions.assertNotNull(userResponse);
        Assertions.assertInstanceOf(UserResponse.class, userResponse);
        Assertions.assertEquals("Mikhail",userResponse.getFirstName());
    }

    @Test
    public void testCreateUser() {
        UsersResource users = keycloak.realm("ITM").users();
        if (users.count() > 1) {
            for (int i = 1; i < users.count(); i++) {
                String id = users.list().get(i).getId();
                users.delete(id);
            }
        }
        Assertions.assertEquals(1, users.count());
        UserRequest userRequest = new UserRequest("petr", "sendersignal@gmail.com", "zxc", "Petia", "West");
        service.createUser(userRequest);
        Assertions.assertEquals(2, users.count());
    }
}
