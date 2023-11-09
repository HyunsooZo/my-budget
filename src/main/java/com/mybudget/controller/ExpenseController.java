package com.mybudget.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/expenses")
@Api(tags = "Expense API", description = "지출과 관련된 API")
@RestController
public class ExpenseController {
}
