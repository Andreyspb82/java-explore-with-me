package ru.practicum.ewm.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
//@Builder
//@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventUserRequest {
    public UpdateEventAdminRequest(Long id, String annotation, Long category, String description,
                                   String eventDate, Location location, Boolean paid, Long participantLimit,
                                   Boolean requestModeration, String stateAction, String title) {
        super(id, annotation, category, description, eventDate, location, paid, participantLimit,
                requestModeration, stateAction, title);
    }



    //    private Long id;
//
//    private String annotation;
//
//    private Long category;
//
//    private String description;
//
//    private String eventDate;
//
//    private Location location;
//
//    private Boolean paid;
//
//    private Long participantLimit;
//
//    private Boolean requestModeration;
//
//    private String stateAction;
//
//    private String title;
//
//    @Override
//    public String toString() {
//        return "UpdateEventAdminRequest{" +
//                "id=" + id +
//                ", annotation='" + annotation + '\'' +
//                ", category=" + category +
//                ", description='" + description + '\'' +
//                ", eventDate='" + eventDate + '\'' +
//                ", location=" + location +
//                ", paid=" + paid +
//                ", participantLimit=" + participantLimit +
//                ", requestModeration=" + requestModeration +
//                ", stateAction='" + stateAction + '\'' +
//                ", title='" + title + '\'' +
//                '}';
//    }
}
