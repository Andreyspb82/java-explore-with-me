package ru.practicum.ewm.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.request.enums.Status;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {

    //@NotEmpty
    private List<Long> requestIds = new ArrayList<>();

  //  @NotEmpty
    private String status;

    @Override
    public String toString() {
        return "EventRequestStatusUpdateRequest{" +
                "requestIds=" + requestIds +
                ", status='" + status + '\'' +
                '}';
    }
}
