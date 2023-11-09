package com.mybudget.service;

import com.mybudget.repository.StatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StatisticService {
    private final StatisticRepository statisticRepository;
}
