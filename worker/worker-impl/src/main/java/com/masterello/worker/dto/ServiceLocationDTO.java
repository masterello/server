package com.masterello.worker.dto;

import com.masterello.user.value.City;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceLocationDTO {

    private boolean online;
    private List<City> cities;
}
