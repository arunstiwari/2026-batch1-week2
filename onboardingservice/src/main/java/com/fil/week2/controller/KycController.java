package com.fil.week2.controller;

import com.fil.week2.dto.*;
import com.fil.week2.service.KycService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class KycController {
    private final KycService kycService;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @PostMapping("/customers/{customerId}/kyc")
    public ResponseEntity<KycSubmissionResponse> submit(@PathVariable("customerId") Long customerId,
                                                        @Valid @RequestBody KycSubmissionRequest request){

        KycSubmissionResponse response = kycService.submit(customerId, request);
        return ResponseEntity
                .created(URI.create("/customers/"+customerId+"/kyc"))
                .body(response) ;
    }

    @GetMapping("/customers/{customerId}/kyc")
    public KycResponse findByCustomer(@PathVariable Long customerId) {
        return kycService.findByCustomer(customerId);
    }

    @GetMapping("/customers/{customerId}/kyc/history")
    public List<KycResponse> findHistory(@PathVariable Long customerId) {
        return kycService.findHistory(customerId);
    }

    // A decision is an event, so it POSTs to its own sub-resource - like a deposit
    @PostMapping("/customers/{customerId}/kyc/approval")
    public KycResponse approve(@PathVariable Long customerId,
                               @Valid @RequestBody KycApprovalRequest request) {
        return kycService.approve(customerId, request);
    }

    @PostMapping("/customers/{customerId}/kyc/rejection")
    public KycResponse reject(@PathVariable Long customerId,
                              @Valid @RequestBody KycRejectionRequest request) {
        return kycService.reject(customerId, request);
    }
}
