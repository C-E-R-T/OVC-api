package com.example.ovcbackend.user.cert.service;

import com.example.ovcbackend.user.cert.dto.MyCertRequest;
import com.example.ovcbackend.user.cert.dto.MyCertResponse;

import java.util.List;

public interface MyCertService {
    List<MyCertResponse> getMyCerts(Long userId);

    void addMyCert(Long userId, Long certId, MyCertRequest request);

    void removeMyCert(Long userId, Long certId);
}
