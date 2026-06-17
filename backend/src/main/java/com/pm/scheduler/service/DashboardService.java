package com.pm.scheduler.service;

import com.pm.scheduler.vo.DashboardStatsVO;
import com.pm.scheduler.vo.ReminderVO;

import java.util.List;

public interface DashboardService {

    List<ReminderVO> getReminders();

    DashboardStatsVO getStats();

    void refreshReminders();
}
