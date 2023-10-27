package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByEventId(long eventId);

    ParticipationRequest findByIdAndRequesterId(long requestId, long userId);

    @Query(value = "select * from requests r " +
            "where r.requester_id = ?1 " +
            "and r.event_id in(select e.id from events e where e.initiator_id <> ?1)", nativeQuery = true)
    List<ParticipationRequest> findByUserIdForOtherUserEvents(long userId);


    @Query(value = "select * from requests r where r.event_id = ?1 and r.status = ?2 and r.id in (?3)", nativeQuery = true)
    List<ParticipationRequest> findByEventIdAndStatusAndId(long eventId, String status, List<Long> ids);
}
