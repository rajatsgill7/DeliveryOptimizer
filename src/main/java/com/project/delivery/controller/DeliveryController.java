package com.project.delivery.controller;

import com.project.delivery.dto.DeliveryRequest;
import com.project.delivery.services.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/optimize")
class DeliveryController {

    @Autowired
    DeliveryService deliveryService;

    private static final Logger logger = LoggerFactory.getLogger(DeliveryController.class);

    @PostMapping
    public Mono<String> optimizeRoute(@RequestBody DeliveryRequest request) {
        logger.info("Received request to optimize route.");

        return deliveryService.processRequest(request);

    }

}
