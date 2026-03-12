package com.example.ovcbackend.user.favorite.service;

import com.example.ovcbackend.user.favorite.dto.FavoriteResponse;

import java.util.List;

public interface FavoriteService {
    List<FavoriteResponse> getFavorites(Long userId);

    void addFavorite(Long userId, Long certId);

    void removeFavorite(Long userId, Long certId);
}
