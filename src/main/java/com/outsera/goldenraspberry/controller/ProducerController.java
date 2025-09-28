package com.outsera.goldenraspberry.controller;

import com.outsera.goldenraspberry.dto.IntervalResponseDto;
import com.outsera.goldenraspberry.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/producers")
public class ProducerController {

    private final MovieService movieService;

    @Autowired
    public ProducerController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/intervals")
    public ResponseEntity<IntervalResponseDto> getProducerIntervals() {
        IntervalResponseDto response = movieService.getProducerIntervals();
        return ResponseEntity.ok(response);
    }
}