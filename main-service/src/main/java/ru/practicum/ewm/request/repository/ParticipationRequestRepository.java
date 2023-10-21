package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.request.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {


    //  @Query("select new ru.practicum.ewm.request.dto.ParticipationRequestDto  " +
//          "from EndpointHit eh " +
//          "where eh.created > ?1 and eh.created < ?2 and eh.uri in ?3 " +
//          "group by eh.app, eh.uri " +
//          "order by count(eh.ip) desc")
    // не работает
//@Query("select new ru.practicum.ewm.request.dto.ParticipationRequestDto(pr.id, pr.created, pr.event_id, pr.user_id, pr.status) from ParticipationRequest pr where pr.event_id = ?1")
    List<ParticipationRequest> findByEventId(long eventId);


    ParticipationRequest findByIdAndRequesterId(long requestId, long userId);


    @Query(value = "select * from requests r " +
            "where r.requester_id = ?1 " +
            "and r.event_id in(select e.id from events e where e.initiator_id <> ?1)", nativeQuery = true)
    List<ParticipationRequest> findByUserIdForOtherUserEvents (long userId);

  //  @Query(value = "select count(r.id) from requests r where r.status = 'PENDING' and r.event_id = ?1;", nativeQuery = true)

    //@Query(value = "select * from requests r where  r.event_id = ?1", nativeQuery = true)
    @Query(value = "select count(r.id) from requests r where r.status = 'CONFIRMED' and r.event_id = ?1", nativeQuery = true)
    Long countRequestStatusConfirmedForEventId(long eventId);

    @Query(value = "select * from requests r where r.event_id = ?1 and r.status = ?2 and r.id in (?3)", nativeQuery = true)
    List<ParticipationRequest> findByEventIdAndStatusAndId (long eventId, String status, List<Long> ids);


}
