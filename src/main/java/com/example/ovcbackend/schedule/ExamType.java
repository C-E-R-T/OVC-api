package com.example.ovcbackend.schedule;

import lombok.Getter;

@Getter
public enum ExamType {
    WRITTEN("필기"),
    PRACTICAL("실기"),
    ETC("기타");

    private final String description;

    ExamType(String description){
        this.description = description;
    }
}
