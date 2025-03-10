# Delivery Optimizer

This Spring Boot application helps a delivery executive determine the fastest way to deliver orders based on latitude and longitude coordinates.

## How to Run the Application

### Prerequisites
- Java 8+
- Maven
- cURL or Postman for API testing

### Running the Application for Different Profiles
Spring Boot supports different environments using profiles. The application supports **dev, test, and prod** profiles.

#### Run with Default Profile (Dev)
```
mvn clean spring-boot:run
```

#### Run with a Specific Profile
```
mvn spring-boot:run -Dspring-boot.run.profiles=dev  # Development Mode
mvn spring-boot:run -Dspring-boot.run.profiles=test # Testing Mode
mvn spring-boot:run -Dspring-boot.run.profiles=prod # Production Mode
```


## API Endpoint
### POST `/optimize`
This endpoint calculates the best delivery route given different locations.

## Sample cURL Requests

### Sample 1: Basic Test Case
```sh
curl -X POST "http://localhost:8080/optimize" \
     -H "Content-Type: application/json" \
     -d '{
           "amanLat": 12.934,
           "amanLon": 77.619,
           "r1Lat": 12.935,
           "r1Lon": 77.620,
           "pt1": 0.5,
           "r2Lat": 12.936,
           "r2Lon": 77.622,
           "pt2": 0.4,
           "c1Lat": 12.937,
           "c1Lon": 77.623,
           "c2Lat": 12.938,
           "c2Lon": 77.624
         }'
```

### Sample 2: Orders in a Different Area
```sh
curl -X POST "http://localhost:8080/optimize" \
     -H "Content-Type: application/json" \
     -d '{
           "amanLat": 13.050,
           "amanLon": 77.620,
           "r1Lat": 13.055,
           "r1Lon": 77.625,
           "pt1": 0.6,
           "r2Lat": 13.060,
           "r2Lon": 77.630,
           "pt2": 0.3,
           "c1Lat": 13.065,
           "c1Lon": 77.635,
           "c2Lat": 13.070,
           "c2Lon": 77.640
         }'
```

### Sample 3: Longer Distance Between Locations
```sh
curl -X POST "http://localhost:8080/optimize" \
     -H "Content-Type: application/json" \
     -d '{
           "amanLat": 12.900,
           "amanLon": 77.500,
           "r1Lat": 12.920,
           "r1Lon": 77.520,
           "pt1": 0.8,
           "r2Lat": 12.940,
           "r2Lon": 77.540,
           "pt2": 0.5,
           "c1Lat": 12.960,
           "c1Lon": 77.560,
           "c2Lat": 12.980,
           "c2Lon": 77.580
         }'
```

### Sample 4: Very Close Locations
```sh
curl -X POST "http://localhost:8080/optimize" \
     -H "Content-Type: application/json" \
     -d '{
           "amanLat": 12.940,
           "amanLon": 77.640,
           "r1Lat": 12.941,
           "r1Lon": 77.641,
           "pt1": 0.2,
           "r2Lat": 12.942,
           "r2Lon": 77.642,
           "pt2": 0.2,
           "c1Lat": 12.943,
           "c1Lon": 77.643,
           "c2Lat": 12.944,
           "c2Lon": 77.644
         }'
```

### Sample 5: Extreme Case - Spread Out Locations
```sh
curl -X POST "http://localhost:8080/optimize" \
     -H "Content-Type: application/json" \
     -d '{
           "amanLat": 12.500,
           "amanLon": 77.000,
           "r1Lat": 12.800,
           "r1Lon": 77.300,
           "pt1": 1.0,
           "r2Lat": 13.100,
           "r2Lon": 77.600,
           "pt2": 1.2,
           "c1Lat": 13.400,
           "c1Lon": 77.900,
           "c2Lat": 13.700,
           "c2Lon": 78.200
         }'
```

