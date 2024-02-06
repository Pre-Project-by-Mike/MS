package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.mapper.UserMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WithMockUser(username = "mike", password = "qwe", authorities = "ROLE_MODERATOR")
public class TestUserController extends BaseIntegrationTest {
    @MockBean
    private Keycloak keycloak;
    @Mock
    private UsersResource usersResource;
    @Mock
    private UserResource userResource;
    @Mock
    private UserResponse userResponse;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private MappingsRepresentation mappingsRepresentation;
    @Mock
    private Response response;
    @Mock
    private RealmResource realmResource;
    private final UserRequest userRequest = new UserRequest("user1", "user1@gmail.com", "user1", "user1", "user1");
    private final UserRequest badUserRequest = new UserRequest("", "baduser1@gmail.com", "baduser1", "baduser1", "baduser1");
    @Mock
    private UserRepresentation userRepresentation;
    @Mock
    private List<RoleRepresentation> userRoles;
    @Mock
    private List<GroupRepresentation> userGroups;
    private final UUID id = UUID.randomUUID();
    @Mock
    private UserMapper userMapper;
    private String tokenUri = "http://backend-keycloak-auth:8080/auth/realms/ITM/protocol/openid-connect/token";

    public String getAccessTokenString() throws JSONException, IOException {
        return getJSONResponse().get("access_token").toString();
    }
    public JSONObject getJSONResponse() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded")
                , "username=mike" +
                        "&password=qwe&client_id=backend-gateway-client&grant_type=password&client_secret" +
                        "=QfWtB8cFbPgqgyTxjdIUN4L6TDrxK1dZ");
        Request request = new Request.Builder()
                .url(tokenUri)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        com.squareup.okhttp.Response response = client.newCall(request).execute();
        return new JSONObject(response.body().string());
    }

    @Test
    public void testGetAccessToken() throws JSONException, IOException {
        Assertions.assertNotNull(getAccessTokenString());
        Assertions.assertInstanceOf(String.class, getAccessTokenString());
    }

    @Test
    public void helloTest() throws Exception {
        mvc.perform(get("/api/users/hello"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(content().string(containsString("mike")));
    }

    @Test
    public void handleInvalidArgumentTest() throws Exception {
        mvc.perform(requestWithContent(post("/api/users"), badUserRequest))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    public void createTest() throws Exception {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);

        mvc.perform(requestWithContent(post("/api/users"), userRequest))
                .andExpect(status().is2xxSuccessful())
                .andDo(print());
    }

    @Test
    public void badCreateTest() throws Exception {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());

        mvc.perform(requestWithContent(post("/api/users"), userRequest))
                .andExpect(status().is5xxServerError())
                .andDo(print());
    }

    @Test
    public void getUserByIdTest() throws Exception {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(String.valueOf(id))).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRepresentation);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);
        when(mappingsRepresentation.getRealmMappings()).thenReturn(userRoles);
        when(userResource.groups()).thenReturn(userGroups);
        when(userMapper.userRepresentationToUserResponse(userRepresentation, userRoles, userGroups)).thenReturn(userResponse);

        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void badGetUserByIdTest() throws Exception {
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(String.valueOf(id))).thenThrow(new BackendResourcesException("test exception", HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(get("/api/users/{id}", id))
                .andExpect(status().is5xxServerError())
                .andDo(print());
    }

}