package com.mybudget.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics")
@Api(tags = "Statistic API", description = "통계와 관련된 API")
@RestController
public class StatisticController {
}
