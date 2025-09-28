package com.outsera.goldenraspberry.controller;

import com.outsera.goldenraspberry.entity.MovieEntity;
import com.outsera.goldenraspberry.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<MovieEntity>> getAllMovies(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Boolean winner) {
        List<MovieEntity> movies = movieService.getMovies(year, winner);
        return ResponseEntity.ok(movies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieEntity> getMovieById(@PathVariable Long id) {
        MovieEntity movie = movieService.getMovieById(id);
        return movie != null ? ResponseEntity.ok(movie) : ResponseEntity.notFound().build();
    }

    @GetMapping("/years")
    public ResponseEntity<List<Integer>> getYears() {
        List<Integer> years = movieService.getYears();
        return ResponseEntity.ok(years);
    }
}
