package com.acsendo.api.customreports.dto;

import java.util.List;

public class QuestionDetailDTO {
	
	
	private Long id;
	private String name;
	private Double result;
	private Integer countQuestions;
	private Integer countResponses;
	
	
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
	
	public Double getResult() {
		return result;
	}
	
	public void setResult(Double result) {
		this.result = result;
	}
	
	public Integer getCountQuestions() {
		return countQuestions;
	}
	
	public void setCountQuestions(Integer countQuestions) {
		this.countQuestions = countQuestions;
	}
	
	public Integer getCountResponses() {
		return countResponses;
	}
	
	public void setCountResponses(Integer countResponses) {
		this.countResponses = countResponses;
	}

}
