package com.pm.scheduler.service;

import com.pm.scheduler.vo.CompletionStatVO;
import com.pm.scheduler.vo.MonthlyTrendVO;

import java.util.List;

public interface StatisticsService {

    List<CompletionStatVO> completionByPerson(Integer year, Integer month);

    List<CompletionStatVO> completionByEquipment(Integer year, Integer month);

    List<MonthlyTrendVO> monthlyTrend();
}
