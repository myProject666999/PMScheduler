package com.pm.scheduler.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pm.scheduler.dto.WorkOrderDispatchDTO;
import com.pm.scheduler.dto.WorkOrderExecuteDTO;
import com.pm.scheduler.dto.WorkOrderQueryDTO;
import com.pm.scheduler.dto.WorkOrderReviewDTO;
import com.pm.scheduler.entity.WorkOrder;
import com.pm.scheduler.vo.WorkOrderVO;

public interface WorkOrderService {

    Page<WorkOrderVO> page(WorkOrderQueryDTO queryDTO);

    WorkOrderVO getById(Long id);

    WorkOrder create(WorkOrder workOrder);

    void dispatch(Long id, WorkOrderDispatchDTO dto, Long userId);

    void startExecute(Long id);

    void execute(Long id, WorkOrderExecuteDTO dto);

    void review(Long id, WorkOrderReviewDTO dto, Long userId);

    WorkOrder autoGenerate(Long equipmentId, Long standardId, String triggerType);
}
