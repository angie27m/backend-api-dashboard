package com.acsendo.api.customreports.dto;

import java.util.Date;

import com.acsendo.api.survey.enumerations.SurveyState;
import com.fasterxml.jackson.annotation.JsonFormat;

public class SurveyEvaluationDTO {
	
	private Long id;
	
	private String name;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date createdDate;
	
	private SurveyState surveyState;
	
	private Double responseRate;
		
	private long countQuestions;
	
	private long countParticipants;
	
	private long countParticipantsFinished;
	
	private String averageTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public SurveyState getSurveyState() {
		return surveyState;
	}

	public void setSurveyState(SurveyState surveyState) {
		this.surveyState = surveyState;
	}

	public Double getResponseRate() {
		return responseRate;
	}

	public void setResponseRate(Double responseRate) {
		this.responseRate = responseRate;
	}

	public long getCountQuestions() {
		return countQuestions;
	}

	public void setCountQuestions(long countQuestions) {
		this.countQuestions = countQuestions;
	}

	public long getCountParticipants() {
		return countParticipants;
	}

	public void setCountParticipants(long countParticipants) {
		this.countParticipants = countParticipants;
	}

	public long getCountParticipantsFinished() {
		return countParticipantsFinished;
	}

	public void setCountParticipantsFinished(long countParticipantsFinished) {
		this.countParticipantsFinished = countParticipantsFinished;
	}

	public String getAverageTime() {
		return averageTime;
	}

	public void setAverageTime(String averageTime) {
		this.averageTime = averageTime;
	}
	
}
	