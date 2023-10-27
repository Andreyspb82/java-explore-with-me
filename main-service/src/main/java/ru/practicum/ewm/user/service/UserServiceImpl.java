package ru.practicum.ewm.user.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@Data
public class UserServiceImpl implements UserService {

    public final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequest newUser) {
        return UserMapper.mapToUserDto(userRepository.save(UserMapper.mapToUser(newUser)));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, PageRequest page) {
        if (ids.isEmpty()) {
            return UserMapper.mapToUsersDto(userRepository.findAllWithPage(page));
        }
        return UserMapper.mapToUsersDto(userRepository.findByUsersIdWithPage(ids, page));

    }

    @Override
    public void removeUserById(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with Id =" + userId + " does not exist");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public User getUserByIdForService(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with Id =" + userId + " does not exist");
        }
        return userRepository.findById(userId).get();
    }
}
