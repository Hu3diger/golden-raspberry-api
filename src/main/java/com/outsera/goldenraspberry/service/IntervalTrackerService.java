package com.outsera.goldenraspberry.service;

import com.outsera.goldenraspberry.dto.ProducerIntervalDto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
class IntervalTrackerService {
    private int minInterval = Integer.MAX_VALUE;
    private int maxInterval = Integer.MIN_VALUE;
    private final List<ProducerIntervalDto> minIntervals = new ArrayList<>();
    private final List<ProducerIntervalDto> maxIntervals = new ArrayList<>();

    public void updateMinMax(ProducerIntervalDto intervalDto) {
        int interval = intervalDto.getInterval();

        if (interval < minInterval) {
            minInterval = interval;
            minIntervals.clear();
            minIntervals.add(intervalDto);
        } else if (interval == minInterval) {
            minIntervals.add(intervalDto);
        }

        if (interval > maxInterval) {
            maxInterval = interval;
            maxIntervals.clear();
            maxIntervals.add(intervalDto);
        } else if (interval == maxInterval) {
            maxIntervals.add(intervalDto);
        }
    }

    public boolean hasIntervals() {
        return !minIntervals.isEmpty() && !maxIntervals.isEmpty();
    }
}

