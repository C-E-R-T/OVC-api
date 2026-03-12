package com.example.ovcbackend.user.favorite.controller;

import com.example.ovcbackend.global.commonResponse.OkResponse;
import com.example.ovcbackend.global.security.jwt.CustomUserDetails;
import com.example.ovcbackend.user.favorite.dto.FavoriteResponse;
import com.example.ovcbackend.user.favorite.exception.FavoriteBadRequestException;
import com.example.ovcbackend.user.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User favorite controller", description = "사용자 찜(favorites) 관련 api")
@RestController
@RequestMapping("/api/users/me/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "찜 목록 조회", description = "현재 로그인한 사용자의 찜 목록을 조회합니다.")
    public ResponseEntity<OkResponse<List<FavoriteResponse>>> getFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        List<FavoriteResponse> responses = favoriteService.getFavorites(getCurrentUserId(userDetails));
        return ResponseEntity.ok(OkResponse.success(responses, request.getRequestURI()));
    }

    @PostMapping("/{certId}")
    @Operation(summary = "찜 추가", description = "현재 로그인한 사용자의 찜 목록에 자격증을 추가합니다.")
    public ResponseEntity<OkResponse<Void>> addFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long certId,
            HttpServletRequest request
    ) {
        favoriteService.addFavorite(getCurrentUserId(userDetails), certId);
        return ResponseEntity.ok(OkResponse.success("찜이 추가되었습니다.", request.getRequestURI()));
    }

    @DeleteMapping("/{certId}")
    @Operation(summary = "찜 삭제", description = "현재 로그인한 사용자의 찜 목록에서 자격증을 삭제합니다.")
    public ResponseEntity<OkResponse<Void>> removeFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long certId,
            HttpServletRequest request
    ) {
        favoriteService.removeFavorite(getCurrentUserId(userDetails), certId);
        return ResponseEntity.ok(OkResponse.success("찜이 삭제되었습니다.", request.getRequestURI()));
    }

    private Long getCurrentUserId(CustomUserDetails userDetails) {
        // SecurityContext가 비정상인 경우를 방어
        if (userDetails == null || userDetails.getUser() == null || userDetails.getUser().getId() == null) {
            throw new FavoriteBadRequestException("유효한 사용자 정보를 찾을 수 없습니다.");
        }
        return userDetails.getUser().getId();
    }
}
