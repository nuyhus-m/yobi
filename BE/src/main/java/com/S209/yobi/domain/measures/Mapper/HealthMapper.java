package com.S209.yobi.domain.measures.Mapper;

import com.S209.yobi.DTO.responseDTO.GraphPointDTO;
import com.S209.yobi.DTO.responseDTO.TotalHealthResponseDTO;
import com.S209.yobi.domain.clients.entity.Client;
import com.S209.yobi.domain.measures.entity.BloodPressure;
import com.S209.yobi.domain.measures.entity.BodyComposition;
import com.S209.yobi.domain.measures.entity.Measure;
import com.S209.yobi.domain.measures.entity.Stress;
import com.S209.yobi.domain.users.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class HealthMapper {

    public TotalHealthResponseDTO toTotalHealthDTO(User user, Client client, List<Measure> measures){
        Map<String, List<GraphPointDTO>> bodyCompositionMap = new LinkedHashMap<>();
        Map<String, List<GraphPointDTO>> bloodPressureMap = new LinkedHashMap<>();
        Map<String, List<GraphPointDTO>> stressMap = new LinkedHashMap<>();

        // 각 항목별 시리즈 초기화
        bodyCompositionMap.put("bfp", new ArrayList<>());
        bodyCompositionMap.put("bmr", new ArrayList<>());
        bodyCompositionMap.put("ecf", new ArrayList<>());
        bodyCompositionMap.put("protein", new ArrayList<>());

        bloodPressureMap.put("sbp", new ArrayList<>());
        bloodPressureMap.put("dbp", new ArrayList<>());

        stressMap.put("stressValue", new ArrayList<>());


        // measure 순회를 돌면서 , 각 항목별로 추출하고 key값으로 date를 넣고 각 항목의 value에는 항목의 값을 빼서 넣기
        for(Measure measure : measures){
            LocalDate date = measure.getDate();

            // BodyComposition
            BodyComposition body = measure.getBody();
            if(body != null){
                float roundedBfp = Math.round(body.getBfp() * 10) / 10.0f;
                int roundedBmr = Math.round(body.getBmr());
                float roundedEcf = Math.round(body.getEcf() * 10) / 10.0f;
                float roundedProtein = Math.round(body.getProtein() * 10) / 10.0f;

                bodyCompositionMap.get("bfp").add(GraphPointDTO.builder().date(date).value(roundedBfp).build());
                bodyCompositionMap.get("bmr").add(GraphPointDTO.builder().date(date).value(roundedBmr).build());
                bodyCompositionMap.get("ecf").add(GraphPointDTO.builder().date(date).value(roundedEcf).build());
                bodyCompositionMap.get("protein").add(GraphPointDTO.builder().date(date).value(roundedProtein).build());
            }

            // BloodPressure
            BloodPressure blood = measure.getBlood();
            if(blood != null){
                float roundedSbp = Math.round(blood.getSbp() * 10) / 10.0f;
                float roundedDbp = Math.round(blood.getDbp() * 10) / 10.0f;

                bloodPressureMap.get("sbp").add(GraphPointDTO.builder().date(date).value(roundedSbp).build());
                bloodPressureMap.get("dbp").add(GraphPointDTO.builder().date(date).value(roundedDbp).build());
            }

            // Stress
            Stress stress = measure.getStress();
            if(stress != null){
                stressMap.get("stressValue").add(GraphPointDTO.builder().date(date).value(stress.getStressValue()).build());
            }


        }

        return TotalHealthResponseDTO.builder()
                .clientId(client.getId())
                .bodyComposition(bodyCompositionMap)
                .bloodPressure(bloodPressureMap)
                .stress(stressMap)
                .build();

    }
}
