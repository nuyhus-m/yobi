package com.S209.yobi.domain.report.service;

import com.S209.yobi.DTO.responseDTO.ReportDetailResponseDTO;
import com.S209.yobi.DTO.responseDTO.ReportListDTO;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.clients.repository.ClientRepository;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.report.entity.WeeklyReport;
import com.S209.yobi.domain.report.repository.ReportRepository;
import com.S209.yobi.domain.users.entity.User;
import com.S209.yobi.domain.users.repository.UserRepository;
import com.S209.yobi.exceptionFinal.ApiResponseCode;
import com.S209.yobi.exceptionFinal.ApiResponseDTO;
import com.S209.yobi.exceptionFinal.ApiResult;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final ClientRepository clientRepository;

    /**
     * 주간 보고서 리스트 불러오기
     */
    public ApiResult getReportList (int userId, int clientId) {

        // 존재하는 유저인지 & 존재하는 클라이언트인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (optionalClient.isEmpty()) {
            log.info("해당하는 클라이언트 없음, [clientId:{}]", clientId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_CLIENT);
        }

        // 리포트 리스트 반환
        List<WeeklyReport> WeeklyReports = reportRepository.findByClientId(clientId);
        ReportListDTO result = ReportListDTO.of(WeeklyReports);

        return result;

    }

    /**
     * 주간 보고서 단건 조회
     */
    public ApiResult getReportDetail (int userId, Long reportId){

        // 존재하는 유저인지 & 존재하는 리포트인지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Optional<WeeklyReport> optionalWeeklyReport = reportRepository.findById(reportId);
        if (optionalWeeklyReport.isEmpty()) {
            log.info("해당하는 리포트가 없음, [userId:{}, clientId:{}]", userId, reportId);
            return ApiResponseDTO.fail(ApiResponseCode.NOT_FOUND_REPORT);
        }

        // 존재하는 리포트가 있으면 반환하기
        WeeklyReport weeklyReport = optionalWeeklyReport.get();
        ReportDetailResponseDTO result = ReportDetailResponseDTO.of(weeklyReport);
        return result;

    }



}
