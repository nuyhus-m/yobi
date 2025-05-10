package com.S209.yobi.domain.measures.Mapper;

import com.S209.yobi.DTO.responseDTO.GraphPointDTO;
import com.S209.yobi.DTO.responseDTO.TotalHealthResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class HealthMapperNative {
    public TotalHealthResponseDTO totalHealthResponseDTO(Integer clientId, List<Object[]> measures){

        Map<String, List<GraphPointDTO>> bodyComposition = new LinkedHashMap<>();
        Map<String, List<GraphPointDTO>> bloodPressure = new LinkedHashMap<>();
        Map<String, List<GraphPointDTO>> stress = new LinkedHashMap<>();

        bodyComposition.put("bfp", new ArrayList<>());
        bodyComposition.put("bmr", new ArrayList<>());
        bodyComposition.put("ecf", new ArrayList<>());
        bodyComposition.put("protein", new ArrayList<>());

        bloodPressure.put("sbp", new ArrayList<>());
        bloodPressure.put("dbp", new ArrayList<>());

        stress.put("stressValue", new ArrayList<>());

        for(Object[] measure : measures){

            LocalDate date = ((java.sql.Date) measure[0]).toLocalDate();

            if(measure[1] != null) bodyComposition.get("bfp").add(new GraphPointDTO(date, (Number) measure[1]));
            if(measure[2] != null) bodyComposition.get("bmr").add(new GraphPointDTO(date, (Number) measure[2]));
            if(measure[3] != null) bodyComposition.get("ecf").add(new GraphPointDTO(date, (Number) measure[3]));
            if(measure[4] != null) bodyComposition.get("protein").add(new GraphPointDTO(date, (Number) measure[4]));
            if(measure[5] != null) bloodPressure.get("sbp").add(new GraphPointDTO(date, (Number) measure[5]));
            if(measure[6] != null) bloodPressure.get("dbp").add(new GraphPointDTO(date, (Number) measure[6]));
            if(measure[7] != null) stress.get("stressValue").add(new GraphPointDTO(date, (Number) measure[7]));

        }

        return TotalHealthResponseDTO.builder()
                .clientId(clientId)
                .bodyComposition(bodyComposition)
                .bloodPressure(bloodPressure)
                .stress(stress)
                .build();


    }
}
