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
    private double hubLatitude;
    private double hubLongitude;
    private double restaurant1Latitude;
    private double restaurant1Longitude;
    private double restaurant1PreparationTime;
    private double restaurant2Latitude;
    private double restaurant2Longitude;
    private double restaurant2PreparationTime;
    private double customer1Latitude;
    private double customer1Longitude;
    private double customer2Latitude;
    private double customer2Longitude;
}