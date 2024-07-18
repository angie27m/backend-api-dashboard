package com.acsendo.api.customreports.dto;

import java.util.List;

public class QuestionCommentDTO {
	
	private String question;
	private List<String> comments;
	
	
	public QuestionCommentDTO() {
		super();
	}

	public QuestionCommentDTO(String question, List<String> comments) {
		super();
		this.question = question;
		this.comments = comments;
	}
	
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public List<String> getComments() {
		return comments;
	}
	public void setComments(List<String> comments) {
		this.comments = comments;
	}
	
	
}
