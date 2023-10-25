package ru.practicum.ewm.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.dto.Location;
import ru.practicum.ewm.event.model.LocationModel;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationMapper {

    public static LocationModel mapToLocationModel(Location location) {
        return LocationModel.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    public static Location mapToLocation(LocationModel locationModel) {
        return Location.builder()
                .lat(locationModel.getLat())
                .lon(locationModel.getLon())
                .build();
    }
}
