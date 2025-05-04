package com.S209.yobi.schedules.controller;

import com.S209.yobi.exception.ApiResponseDTO;
import com.S209.yobi.schedules.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class SchedulesController {

    private final ScheduleService scheduleService;

    @Operation(summary = "단건 일정 조회", description = "scheduleId를 넘기면 단건 일정의 정보를 조회할 수 있습니다.")
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getSchedule(
            @PathVariable Integer scheduleId
    ) {
//        Map<String, Object> schedule = scheduleService.getSchedule(scheduleId);
//        return ResponseEntity.ok(ApiResponseDTO.success(schedule));
//    }
//}

        log.info("===== Controller: getSchedule 요청 받음, scheduleId: {} =====", scheduleId);

        try {
            Map<String, Object> schedule = scheduleService.getSchedule(scheduleId);
            log.info("Service에서 응답 받음: {}", schedule);

            ApiResponseDTO<Map<String, Object>> response = ApiResponseDTO.success(schedule);
            log.info("최종 응답 생성 완료: {}", response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Controller에서 에러 발생: ", e);
            throw e;
        } finally {
            log.info("===== Controller: getSchedule 종료 =====");
        }
    }

}