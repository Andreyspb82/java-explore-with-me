package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.LocationModel;

public interface LocationRepository extends JpaRepository<LocationModel, Long> {
}
