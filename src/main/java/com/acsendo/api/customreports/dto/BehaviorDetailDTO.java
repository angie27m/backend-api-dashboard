package com.acsendo.api.customreports.dto;

import java.util.List;
import java.util.Map;

public class BehaviorDetailDTO {
	
	private Integer id;
	
	private String behavior;
	
	private Double total;

	private Map<String, String> relations;
	
	private List<FilterDTO> comments;
	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getBehavior() {
		return behavior;
	}

	public void setBehavior(String behavior) {
		this.behavior = behavior;
	}

	public Double getTotal() {
		return total;
	}

	public void setTotal(Double total) {
		this.total = total;
	}

	public Map<String, String> getRelations() {
		return relations;
	}

	public void setRelations(Map<String, String> relations) {
		this.relations = relations;
	}

	public List<FilterDTO> getComments() {
		return comments;
	}

	public void setComments(List<FilterDTO> comments) {
		this.comments = comments;
	}	
	
	
}
