package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.event.model.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    List<Event> findByInitiatorId(long userId, PageRequest page);

    Event findByInitiatorIdAndId(long userId, long eventId);


    @Query(value = "select * from events e" +
            " where e.id = ?1 and e.state = ?2", nativeQuery = true)
    Event findByIdAndState (long eventId, String state);


//    @Query(value = "select * from events e" +
//            " where e.id in ?1 ", nativeQuery = true)
//    List<Event> findByIds (List<Long> eventsId);


//    @Query(value = "select * from events e" +
//            " where e.state in ?1", nativeQuery = true)
//    List<Event> findByState (List<String> state);







//    @Query(value = "select * from events e" +
//            " where e.user_id = ?1 and e.id = ?2", nativeQuery = true)
//    Event findByUserIdAndId(long userId, long eventId);



}
