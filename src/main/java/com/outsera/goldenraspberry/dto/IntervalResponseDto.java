package com.outsera.goldenraspberry.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IntervalResponseDto {

    private List<ProducerIntervalDto> min;

    private List<ProducerIntervalDto> max;
}
