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

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @PostConstruct
    public void loadMoviesFromCsv() {
        try {
            ClassPathResource resource = new ClassPathResource("movielist.csv");
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());

            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(';')
                    .build();

            CSVReader reader = new CSVReaderBuilder(inputStreamReader)
                    .withCSVParser(parser)
                    .build();

            List<String[]> records = reader.readAll();
            reader.close();

            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                if (record.length >= 4) {
                    try {
                        if (record[0] == null || record[0].trim().isEmpty()) {
                            continue;
                        }

                        int year = Integer.parseInt(record[0].trim());
                        MovieEntity movie = new MovieEntity();
                        movie.setYear(year);
                        movie.setTitle(safeGet(record, 1));
                        movie.setStudios(safeGet(record, 2));
                        movie.setProducers(safeGet(record, 3));
                        movie.setWinner(safeGet(record, 4));

                        movieRepository.save(movie);
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping line with invalid year: " + String.join(";", record));
                        continue;
                    }
                }
            }

        } catch (IOException | CsvException e) {
            throw new RuntimeException("Error loading CSV file", e);
        }
    }

    private String safeGet(String[] record, int index) {
        if (record.length > index && record[index] != null) {
            return record[index].trim();
        }
        return "";
    }

    public List<MovieEntity> getMovies(Integer year, Boolean winner) {
        if (year != null && winner != null) {
            return movieRepository.findByYearAndWinner(year, winner ? "yes" : "no");
        } else if (year != null) {
            return movieRepository.findByYear(year);
        } else if (winner != null) {
            return winner ? movieRepository.findWinners() : movieRepository.findNonWinners();
        } else {
            return movieRepository.findAll();
        }
    }

    public MovieEntity getMovieById(Long id) {
        return movieRepository.findById(id).orElse(null);
    }

    public List<MovieEntity> getWinners() {
        return movieRepository.findWinners();
    }

    public List<Integer> getYears() {
        return movieRepository.findDistinctYears();
    }

    public IntervalResponseDto getProducerIntervals() {
        List<MovieEntity> winners = movieRepository.findWinners();

        // 1. Group by Producer by collecting all winning years.
        Map<String, List<Integer>> producerWins = getProducersYears(winners);

        // 2. Generate all possible intervals for each producer.
        List<ProducerIntervalDto> intervals = calculateAllIntervals(producerWins);

        if (intervals.isEmpty()) {
            return new IntervalResponseDto(List.of(), List.of());
        }

        // 3. Find min and max intervals.
        return filterMinMaxIntervals(intervals);
    }

    private Map<String, List<Integer>> getProducersYears(List<MovieEntity> winners) {
        return winners.stream()
                .flatMap(movie -> {
                    String[] producerArray = movie.getProducers().split(",|\\sand\\s");

                    return java.util.Arrays.stream(producerArray)
                            .map(String::trim)
                            .filter(producer -> !producer.isEmpty())
                            .map(producer -> Map.entry(producer, movie.getYear()));
                })
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    private List<ProducerIntervalDto> calculateAllIntervals(Map<String, List<Integer>> producerWins) {
        return producerWins.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .flatMap(entry -> {
                    String producer = entry.getKey();
                    List<Integer> years = entry.getValue();

                    years.sort(Comparator.naturalOrder());

                    return IntStream.range(1, years.size())
                            .mapToObj(i -> new ProducerIntervalDto(
                                    producer,
                                    years.get(i) - years.get(i-1),
                                    years.get(i-1),
                                    years.get(i)
                            ));
                })
                .collect(Collectors.toList());
    }

    private IntervalResponseDto filterMinMaxIntervals(List<ProducerIntervalDto> intervals) {

        Comparator<ProducerIntervalDto> intervalComparator =
                Comparator.comparingInt(ProducerIntervalDto::getInterval);

        int minInterval = intervals.stream()
                .min(intervalComparator)
                .orElseThrow()
                .getInterval();

        int maxInterval = intervals.stream()
                .max(intervalComparator)
                .orElseThrow()
                .getInterval();

        List<ProducerIntervalDto> minIntervals = intervals.stream()
                .filter(i -> i.getInterval() == minInterval)
                .collect(Collectors.toList());

        List<ProducerIntervalDto> maxIntervals = intervals.stream()
                .filter(i -> i.getInterval() == maxInterval)
                .collect(Collectors.toList());

        return new IntervalResponseDto(minIntervals, maxIntervals);
    }
}
