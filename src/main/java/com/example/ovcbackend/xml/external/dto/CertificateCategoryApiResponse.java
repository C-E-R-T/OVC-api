package com.example.ovcbackend.xml.external.dto;


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "response")
public class CertificateCategoryApiResponse {
    @JacksonXmlProperty(localName = "body")
    private Body body;

    @Data
    public static class Body {
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        private List<CertItemDto> items;
    }

    @Data
    public static class CertItemDto {
        @JacksonXmlProperty(localName = "jmcd")
        private String certId;       // 종목코드

        @JacksonXmlProperty(localName = "jmfldnm")
        private String name;         // 종목명

        @JacksonXmlProperty(localName = "obligfldcd")
        private String categoryCode; // 대직무분야코드

        @JacksonXmlProperty(localName = "obligfldnm")
        private String categoryName; // 대직무분야명
    }
}
