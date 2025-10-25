package com.municipal.controllers;

import com.municipal.dtos.UserDTO;
import com.municipal.dtos.UserInputDTO;
import com.municipal.services.UserService;

import java.util.List;

/**
 * Facade for retrieving user information while keeping UI code unaware of HTTP details.
 */
public class UserController {

    private final UserService userService;

    public UserController() {
        this(new UserService());
    }

    public UserController(UserService userService) {
        this.userService = userService;
    }

    public List<UserDTO> loadUsers(String bearerToken) {
        return userService.findAll(bearerToken);
    }

    public UserDTO createUser(UserInputDTO input, String bearerToken) {
        return userService.create(input, bearerToken);
    }

    public UserDTO updateUser(Long id, UserInputDTO input, String bearerToken) {
        return userService.update(id, input, bearerToken);
    }

    public void deleteUser(Long id, String bearerToken) {
        userService.delete(id, bearerToken);
    }
}
