package com.gcc.app.service.integration.workload;

import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "workload-service", url = "${workload.service.url}")
public interface WorkloadClient {

    @PostMapping("/api/workload")
    void processWorkload(@RequestBody TrainerWorkloadRequestDto request);

    @GetMapping("/api/workload/{username}")
    TrainerSummaryResponseDto getTrainerSummary(@PathVariable("username") String username);
}