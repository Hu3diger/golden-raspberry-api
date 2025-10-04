package com.outsera.goldenraspberry.service;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.outsera.goldenraspberry.dto.IntervalResponseDto;
import com.outsera.goldenraspberry.dto.ProducerIntervalDto;
import com.outsera.goldenraspberry.entity.MovieEntity;
import com.outsera.goldenraspberry.repository.MovieRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private static final Pattern PRODUCER_SPLIT_PATTERN = Pattern.compile(",|\\sand\\s");
    private static final int MIN_RECORD_LENGTH = 4;

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @PostConstruct
    @Transactional
    public void loadMoviesFromCsv() {
        try (InputStreamReader reader = new InputStreamReader(
                new ClassPathResource("movielist.csv").getInputStream());
             CSVReader csvReader = createCsvReader(reader)) {

            List<MovieEntity> movies = csvReader.readAll().stream()
                    .filter(this::isValidRecord)
                    .map(this::parseMovieRecord)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            movieRepository.saveAll(movies);

        } catch (IOException | CsvException e) {
            throw new RuntimeException("Error loading CSV file", e);
        }
    }

    private CSVReader createCsvReader(InputStreamReader reader) {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .build();

        return new CSVReaderBuilder(reader)
                .withCSVParser(parser)
                .build();
    }

    private boolean isValidRecord(String[] record) {
        return record.length >= MIN_RECORD_LENGTH
                && record[0] != null
                && !record[0].trim().isEmpty();
    }

    private MovieEntity parseMovieRecord(String[] record) {
        try {
            MovieEntity movie = new MovieEntity();
            movie.setYear(Integer.parseInt(record[0].trim()));
            movie.setTitle(safeGet(record, 1));
            movie.setStudios(safeGet(record, 2));
            movie.setProducers(safeGet(record, 3));
            movie.setWinner(safeGet(record, 4));
            return movie;
        } catch (NumberFormatException e) {
            System.err.println("Skipping line with invalid year: " + String.join(";", record));
            return null;
        }
    }

    private String safeGet(String[] record, int index) {
        return (record.length > index && record[index] != null)
                ? record[index].trim()
                : "";
    }

    public List<MovieEntity> getMovies(Integer year, Boolean winner) {
        if (year != null && winner != null) {
            return movieRepository.findByYearAndWinner(year, winner ? "yes" : "no");
        } else if (year != null) {
            return movieRepository.findByYear(year);
        } else if (winner != null) {
            return winner ? movieRepository.findWinners() : movieRepository.findNonWinners();
        }
        return movieRepository.findAll();
    }

    public MovieEntity getMovieById(Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    public List<Integer> getYears() {
        return movieRepository.findDistinctYears();
    }

    public IntervalResponseDto getProducerIntervals() {
        List<MovieEntity> winners = movieRepository.findWinners();

        if (winners.isEmpty()) {
            return new IntervalResponseDto(List.of(), List.of());
        }

        IntervalTrackerService tracker = new IntervalTrackerService();
        Map<String, List<Integer>> producerYears = new HashMap<>();

        for (MovieEntity movie : winners) {
            processMovieProducers(movie, producerYears, tracker);
        }

        return buildIntervalResponse(tracker);
    }

    private void processMovieProducers(
            MovieEntity movie,
            Map<String, List<Integer>> producerYears,
            IntervalTrackerService tracker) {

        String[] producers = PRODUCER_SPLIT_PATTERN.split(movie.getProducers());

        for (String producer : producers) {
            producer = producer.trim();
            if (producer.isEmpty()) continue;

            processProducer(producer, movie.getYear(), producerYears, tracker);
        }
    }

    private void processProducer(
            String producer,
            int year,
            Map<String, List<Integer>> producerYears,
            IntervalTrackerService tracker) {

        List<Integer> years = producerYears.computeIfAbsent(producer, k -> new ArrayList<>());
        int insertPos = insertYearSorted(years, year);

        if (years.size() > 1 && insertPos > 0) {
            ProducerIntervalDto intervalDto = createIntervalDto(producer, years, insertPos, year);
            tracker.updateMinMax(intervalDto);
        }
    }

    private int insertYearSorted(List<Integer> years, int year) {
        int insertPos = Collections.binarySearch(years, year);
        if (insertPos < 0) {
            insertPos = -(insertPos + 1);
        }
        years.add(insertPos, year);
        return insertPos;
    }

    private ProducerIntervalDto createIntervalDto(String producer, List<Integer> years, int insertPos, int currentYear) {
        int previousYear = years.get(insertPos - 1);
        int interval = currentYear - previousYear;
        return new ProducerIntervalDto(producer, interval, previousYear, currentYear);
    }

    private IntervalResponseDto buildIntervalResponse(IntervalTrackerService tracker) {
        return tracker.hasIntervals()
                ? new IntervalResponseDto(tracker.getMinIntervals(), tracker.getMaxIntervals())
                : new IntervalResponseDto(List.of(), List.of());
    }
}