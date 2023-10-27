package ru.practicum.ewm.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("from User")
    List<User> findAllWithPage(Pageable page);

    @Query("from User u where u.id in ?1")
    List<User> findByUsersIdWithPage(List<Long> ids, Pageable page);
}
