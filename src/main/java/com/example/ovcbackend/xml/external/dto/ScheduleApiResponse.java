package com.example.ovcbackend.xml.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;

import java.util.List;

@Getter
@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleApiResponse {

    @JacksonXmlProperty(localName = "header")
    private Header header;

    @JacksonXmlProperty(localName = "body")
    private Body body;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JacksonXmlProperty(localName = "resultCode")
        private String resultCode;

        @JacksonXmlProperty(localName = "resultMsg")
        private String resultMsg;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        private List<ScheduleItemDto> items;

        @JacksonXmlProperty(localName = "numOfRows")
        private Integer numOfRows;

        @JacksonXmlProperty(localName = "pageNo")
        private Integer pageNo;

        @JacksonXmlProperty(localName = "totalCount")
        private Integer totalCount;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScheduleItemDto {
        @JacksonXmlProperty(localName = "implYy")
        private String implYy;

        @JacksonXmlProperty(localName = "implSeq")
        private String implSeq;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "docRegStartDt")
        private String docRegStartDt;

        @JacksonXmlProperty(localName = "docRegEndDt")
        private String docRegEndDt;

        @JacksonXmlProperty(localName = "docExamStartDt")
        private String docExamStartDt;

        @JacksonXmlProperty(localName = "docExamEndDt")
        private String docExamEndDt;

        @JacksonXmlProperty(localName = "docPassDt")
        private String docPassDt;

        @JacksonXmlProperty(localName = "pracRegStartDt")
        private String pracRegStartDt;

        @JacksonXmlProperty(localName = "pracRegEndDt")
        private String pracRegEndDt;

        @JacksonXmlProperty(localName = "pracExamStartDt")
        private String pracExamStartDt;

        @JacksonXmlProperty(localName = "pracExamEndDt")
        private String pracExamEndDt;

        @JacksonXmlProperty(localName = "pracPassDt")
        private String pracPassDt;
    }
}
