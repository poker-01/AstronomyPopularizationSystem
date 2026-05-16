package com.springboot.backendserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanetDto {

    private String id;
    private String name;
    private String nameEn;
    private String type;
    private String distanceFromSun;
    private String orbitalPeriod;
    private String diameter;
    private String moons;
    private String description;
    private String imageUrl;
    private String imageCredit;
    private Integer order;
}
