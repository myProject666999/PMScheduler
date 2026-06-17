package com.pm.scheduler.controller;

import com.pm.scheduler.common.result.R;
import com.pm.scheduler.service.DashboardService;
import com.pm.scheduler.vo.DashboardStatsVO;
import com.pm.scheduler.vo.ReminderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/reminders")
    public R<List<ReminderVO>> getReminders() {
        return R.ok(dashboardService.getReminders());
    }

    @GetMapping("/stats")
    public R<DashboardStatsVO> getStats() {
        return R.ok(dashboardService.getStats());
    }

    @PostMapping("/refresh-reminders")
    public R<Void> refreshReminders() {
        dashboardService.refreshReminders();
        return R.ok();
    }
}
