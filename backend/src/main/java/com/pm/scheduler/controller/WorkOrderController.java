package com.pm.scheduler.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.scheduler.common.result.R;
import com.pm.scheduler.dto.WorkOrderDispatchDTO;
import com.pm.scheduler.dto.WorkOrderExecuteDTO;
import com.pm.scheduler.dto.WorkOrderQueryDTO;
import com.pm.scheduler.dto.WorkOrderReviewDTO;
import com.pm.scheduler.service.WorkOrderService;
import com.pm.scheduler.vo.WorkOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/work-order")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping("/page")
    public R<Page<WorkOrderVO>> page(WorkOrderQueryDTO queryDTO) {
        return R.ok(workOrderService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public R<WorkOrderVO> getById(@PathVariable Long id) {
        return R.ok(workOrderService.getById(id));
    }

    @PostMapping("/dispatch/{id}")
    public R<Void> dispatch(@PathVariable Long id, @Valid @RequestBody WorkOrderDispatchDTO dto) {
        workOrderService.dispatch(id, dto, 1L);
        return R.ok();
    }

    @PostMapping("/execute/{id}")
    public R<Void> execute(@PathVariable Long id, @RequestBody WorkOrderExecuteDTO dto) {
        workOrderService.execute(id, dto);
        return R.ok();
    }

    @PostMapping("/review/{id}")
    public R<Void> review(@PathVariable Long id, @Valid @RequestBody WorkOrderReviewDTO dto) {
        workOrderService.review(id, dto, 1L);
        return R.ok();
    }
}
