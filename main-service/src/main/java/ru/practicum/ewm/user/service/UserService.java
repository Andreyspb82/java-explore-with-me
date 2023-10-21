package ru.practicum.ewm.user.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface UserService {

    public UserDto createUser(NewUserRequest newUser);

    public List<UserDto> getUsers(List<Long> ids, PageRequest page);

    public void removeUserById(long userId);

    public User getUserByIdForService(long userId);
}
