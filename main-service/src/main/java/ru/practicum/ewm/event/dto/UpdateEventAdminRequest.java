package ru.practicum.ewm.event.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventUserRequest {
    public UpdateEventAdminRequest(Long id, String annotation, Long category, String description,
                                   String eventDate, Location location, Boolean paid, Long participantLimit,
                                   Boolean requestModeration, String stateAction, String title) {
        super(id, annotation, category, description, eventDate, location, paid, participantLimit,
                requestModeration, stateAction, title);
    }
}
