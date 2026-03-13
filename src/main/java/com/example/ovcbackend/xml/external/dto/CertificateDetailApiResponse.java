package com.example.ovcbackend.xml.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

import java.util.List;

@JacksonXmlRootElement(localName = "response")
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificateDetailApiResponse {
    @JacksonXmlProperty(localName = "body")
    private Body body;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        private List<CertDetailItemDto> items;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CertDetailItemDto {
        @JacksonXmlProperty(localName = "contents") // XML의 <contents> 태그
        private String contents;

        @JacksonXmlProperty(localName = "infogb")   // XML의 <infogb> 태그
        private String infogb;
    }
}
