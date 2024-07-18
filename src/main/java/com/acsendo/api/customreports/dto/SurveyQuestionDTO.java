package com.acsendo.api.customreports.dto;

import java.util.List;
import java.util.Map;

public class SurveyQuestionDTO {
	
	private Long id;
	
	private String question;
	
	private String type;
	
	private String questionCode;
	
	private List<ResponseDTO> optionsResponses;
	
	private List<Map<String, String>> responsesData;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getQuestionCode() {
		return questionCode;
	}

	public void setQuestionCode(String questionCode) {
		this.questionCode = questionCode;
	}

	public List<ResponseDTO> getOptionsResponses() {
		return optionsResponses;
	}

	public void setOptionsResponses(List<ResponseDTO> optionsResponses) {
		this.optionsResponses = optionsResponses;
	}

	public List<Map<String, String>> getResponsesData() {
		return responsesData;
	}

	public void setResponsesData(List<Map<String, String>> responsesData) {
		this.responsesData = responsesData;
	}


}
