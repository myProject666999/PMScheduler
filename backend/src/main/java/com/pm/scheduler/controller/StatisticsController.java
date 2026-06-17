package com.pm.scheduler.controller;

import com.pm.scheduler.common.result.R;
import com.pm.scheduler.service.StatisticsService;
import com.pm.scheduler.vo.CompletionStatVO;
import com.pm.scheduler.vo.MonthlyTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/completion/person")
    public R<List<CompletionStatVO>> completionByPerson(@RequestParam Integer year, @RequestParam Integer month) {
        return R.ok(statisticsService.completionByPerson(year, month));
    }

    @GetMapping("/completion/equipment")
    public R<List<CompletionStatVO>> completionByEquipment(@RequestParam Integer year, @RequestParam Integer month) {
        return R.ok(statisticsService.completionByEquipment(year, month));
    }

    @GetMapping("/monthly-trend")
    public R<List<MonthlyTrendVO>> monthlyTrend() {
        return R.ok(statisticsService.monthlyTrend());
    }
}
