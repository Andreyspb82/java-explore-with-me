package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.event.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findByInitiatorId(long userId, PageRequest page);

    Event findByInitiatorIdAndId(long userId, long eventId);

    @Query(value = "select * from events e" +
            " where e.id = ?1 and e.state = ?2 ", nativeQuery = true)
    Event findByIdAndState(long eventId, String state);
}
