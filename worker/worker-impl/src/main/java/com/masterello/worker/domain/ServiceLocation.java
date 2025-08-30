package com.masterello.worker.domain;

import com.masterello.commons.core.json.Patchable;
import com.masterello.user.domain.CityConverter;
import com.masterello.user.value.City;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceLocation {

    @Patchable
    @Column(name = "online")
    private boolean online = false;

    @ElementCollection
    @Patchable
    @CollectionTable(
            name = "worker_location_cities",
            joinColumns = @JoinColumn(name = "worker_id")
    )
    @Column(name = "city")
    @Convert(converter = CityConverter.class)
    private List<City> cities;
}
