package com.acsendo.api.customreports.dto;

import com.acsendo.api.climate.enumerations.SentimentType;

public class OpenQuestionResponseDTO {

	
	public Long id;
	public String response;
	public SentimentType sentimentType;
	public Long questionId;
	
	
	
	public OpenQuestionResponseDTO() {
		super();
	}

	public OpenQuestionResponseDTO(Long id, String response, SentimentType sentimentType, Long questionId) {
		super();
		this.id = id;
		this.response = response;
		this.sentimentType = sentimentType;
		this.questionId = questionId;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getResponse() {
		return response;
	}
	
	public void setResponse(String response) {
		this.response = response;
	}
	
	public SentimentType getSentimentType() {
		return sentimentType;
	}
	
	public void setSentimentType(SentimentType sentimentType) {
		this.sentimentType = sentimentType;
	}

	public Long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
	
	
}
