package com.acsendo.api.customreports.dto;

import java.util.List;

import com.acsendo.api.goals.model.GoalTrackingPeriodType;

public class GoalTrackDTO {
	
	private GoalTrackingPeriodType periodType;
	
	private Integer countTracks;
	
	private List<ResultDateDTO> tracks;
	

	public GoalTrackingPeriodType getPeriodType() {
		return periodType;
	}

	public void setPeriodType(GoalTrackingPeriodType periodType) {
		this.periodType = periodType;
	}

	public Integer getCountTracks() {
		return countTracks;
	}

	public void setCountTracks(Integer countTracks) {
		this.countTracks = countTracks;
	}

	public List<ResultDateDTO> getTracks() {
		return tracks;
	}

	public void setTracks(List<ResultDateDTO> tracks) {
		this.tracks = tracks;
	}

}
