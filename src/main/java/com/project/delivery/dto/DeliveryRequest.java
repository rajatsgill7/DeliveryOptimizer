package com.project.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {
    private double amanLat;
    private double amanLon;
    private double r1Lat;
    private double r1Lon;
    private double pt1;
    private double r2Lat;
    private double r2Lon;
    private double pt2;
    private double c1Lat;
    private double c1Lon;
    private double c2Lat;
    private double c2Lon;
}
