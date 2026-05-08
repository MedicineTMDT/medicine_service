package com.ryo.prescription.repository.httpclient;

import com.ryo.prescription.dto.request.DrugList;
import com.ryo.prescription.dto.response.APIResponse;
import com.ryo.prescription.dto.response.DrugResponse;
import com.ryo.prescription.dto.response.PrescriptionInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "medicine-service", url = "${app.services.profile}")
public interface MedicineClient {
    @GetMapping(value = "/drugs/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    APIResponse<DrugResponse> findById(@PathVariable Integer id);

    @GetMapping(value = "/drug-interactions/get-drug-interaction-preview", produces = MediaType.APPLICATION_JSON_VALUE)
    APIResponse<PrescriptionInfo> getPrescriptionReview( @RequestBody DrugList drugList);
}
