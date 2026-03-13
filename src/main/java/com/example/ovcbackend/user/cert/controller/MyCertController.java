package com.example.ovcbackend.user.cert.controller;

import com.example.ovcbackend.global.commonResponse.OkResponse;
import com.example.ovcbackend.global.security.jwt.CustomUserDetails;
import com.example.ovcbackend.user.cert.dto.MyCertRequest;
import com.example.ovcbackend.user.cert.dto.MyCertResponse;
import com.example.ovcbackend.user.cert.exception.MyCertBadRequestException;
import com.example.ovcbackend.user.cert.service.MyCertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User cert controller", description = "사용자 취득 자격증 관련 api")
@RestController
@RequestMapping("/api/users/me/certs")
@RequiredArgsConstructor
public class MyCertController {

    private final MyCertService myCertService;

    @GetMapping
    @Operation(summary = "취득 자격증 목록 조회")
    public ResponseEntity<OkResponse<List<MyCertResponse>>> getMyCerts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        List<MyCertResponse> response = myCertService.getMyCerts(getCurrentUserId(userDetails));
        return ResponseEntity.ok(OkResponse.success(response, request.getRequestURI()));
    }

    @PostMapping("/{certId}")
    @Operation(summary = "취득 자격증 등록")
    public ResponseEntity<OkResponse<Void>> addMyCert(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long certId,
            @RequestBody MyCertRequest body,
            HttpServletRequest request
    ) {
        myCertService.addMyCert(getCurrentUserId(userDetails), certId, body);
        return ResponseEntity.ok(OkResponse.success("자격증이 등록되었습니다.", request.getRequestURI()));
    }

    @DeleteMapping("/{certId}")
    @Operation(summary = "취득 자격증 삭제")
    public ResponseEntity<OkResponse<Void>> removeMyCert(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long certId,
            HttpServletRequest request
    ) {
        myCertService.removeMyCert(getCurrentUserId(userDetails), certId);
        return ResponseEntity.ok(OkResponse.success("자격증이 삭제되었습니다.", request.getRequestURI()));
    }

    private Long getCurrentUserId(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null || userDetails.getUser().getId() == null) {
            throw new MyCertBadRequestException("유효한 사용자 정보를 찾을 수 없습니다.");
        }
        return userDetails.getUser().getId();
    }
}
