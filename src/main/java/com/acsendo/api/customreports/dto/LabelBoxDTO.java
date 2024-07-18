package com.acsendo.api.customreports.dto;

import java.math.BigDecimal;
import java.util.List;


public class LabelBoxDTO {
	
	private String titleAxisX;
	private String titleAxisY;
	
	private String titleAxisX0X1;
	private String titleAxisX1X2;
	private String titleAxisX2X3;
	private String titleAxisX3X4;
	
	private String titleAxisY0Y1;
	private String titleAxisY1Y2;
	private String titleAxisY2Y3;
	private String titleAxisY3Y4;
	
	private BigDecimal axisx1;
	private BigDecimal axisx2;
	private BigDecimal axisx3;
	private BigDecimal axisx4;
	
	private BigDecimal axisy1;
	private BigDecimal axisy2;
	private BigDecimal axisy3;
	private BigDecimal axisy4;
	
	private Integer nineboxType;
	private Boolean nineboxDescription;
	
	private String nineboxTitle;
	private String nineboxInitialInfoText;
	private String nineboxInfoTextnoEmployee;
	
	
	//LISTA DE INFORMACION DE LOS CUADRANTES DE EL NINEBOX
	private List<QuadrantNineBoxInfoDTO> infoQuadrant;
	private String infoQuadrantStringJson;
	
	/** Variable que almacenará el componente que está configurado en el eje X*/
	private String typeAxisX;
	/** Variable que almacenará el componente que está configurado en el eje Y*/
	private String typeAxisY;
	
	/**
	 * Variable que almacena el label de la descripción en el eje X 
	 **/
	private String labelX;
	
	/**
	 * Variable que almacena el label de la descripción en el eje Y 
	 **/
	private String labelY;

	public String getTitleAxisX() {
		return titleAxisX;
	}

	public void setTitleAxisX(String titleAxisX) {
		this.titleAxisX = titleAxisX;
	}


	public String getTitleAxisY() {
		return titleAxisY;
	}

	public void setTitleAxisY(String titleAxisY) {
		this.titleAxisY = titleAxisY;
	}


	public String getTitleAxisX0X1() {
		return titleAxisX0X1;
	}

	public void setTitleAxisX0X1(String titleAxisX0X1) {
		this.titleAxisX0X1 = titleAxisX0X1;
	}

	public String getTitleAxisX1X2() {
		return titleAxisX1X2;
	}

	public void setTitleAxisX1X2(String titleAxisX1X2) {
		this.titleAxisX1X2 = titleAxisX1X2;
	}

	public String getTitleAxisX2X3() {
		return titleAxisX2X3;
	}

	public void setTitleAxisX2X3(String titleAxisX2X3) {
		this.titleAxisX2X3 = titleAxisX2X3;
	}

	public String getTitleAxisX3X4() {
		return titleAxisX3X4;
	}

	public void setTitleAxisX3X4(String titleAxisX3X4) {
		this.titleAxisX3X4 = titleAxisX3X4;
	}

	public String getTitleAxisY0Y1() {
		return titleAxisY0Y1;
	}

	public void setTitleAxisY0Y1(String titleAxisY0Y1) {
		this.titleAxisY0Y1 = titleAxisY0Y1;
	}

	public String getTitleAxisY1Y2() {
		return titleAxisY1Y2;
	}

	public void setTitleAxisY1Y2(String titleAxisY1Y2) {
		this.titleAxisY1Y2 = titleAxisY1Y2;
	}

	public String getTitleAxisY2Y3() {
		return titleAxisY2Y3;
	}

	public void setTitleAxisY2Y3(String titleAxisY2Y3) {
		this.titleAxisY2Y3 = titleAxisY2Y3;
	}

	public String getTitleAxisY3Y4() {
		return titleAxisY3Y4;
	}

	public void setTitleAxisY3Y4(String titleAxisY3Y4) {
		this.titleAxisY3Y4 = titleAxisY3Y4;
	}

	public BigDecimal getAxisx1() {
		return axisx1;
	}

	public void setAxisx1(BigDecimal axisx1) {
		this.axisx1 = axisx1;
	}

	public BigDecimal getAxisx2() {
		return axisx2;
	}

	public void setAxisx2(BigDecimal axisx2) {
		this.axisx2 = axisx2;
	}

	public BigDecimal getAxisx3() {
		return axisx3;
	}

	public void setAxisx3(BigDecimal axisx3) {
		this.axisx3 = axisx3;
	}

	public BigDecimal getAxisx4() {
		return axisx4;
	}

	public void setAxisx4(BigDecimal axisx4) {
		this.axisx4 = axisx4;
	}

	public BigDecimal getAxisy1() {
		return axisy1;
	}

	public void setAxisy1(BigDecimal axisy1) {
		this.axisy1 = axisy1;
	}

	public BigDecimal getAxisy2() {
		return axisy2;
	}

	public void setAxisy2(BigDecimal axisy2) {
		this.axisy2 = axisy2;
	}

	public BigDecimal getAxisy3() {
		return axisy3;
	}

	public void setAxisy3(BigDecimal axisy3) {
		this.axisy3 = axisy3;
	}

	public BigDecimal getAxisy4() {
		return axisy4;
	}

	public void setAxisy4(BigDecimal axisy4) {
		this.axisy4 = axisy4;
	}

	public Integer getNineboxType() {
		return nineboxType;
	}

	public void setNineboxType(Integer nineboxType) {
		this.nineboxType = nineboxType;
	}

	public Boolean getNineboxDescription() {
		return nineboxDescription;
	}

	public void setNineboxDescription(Boolean nineboxDescription) {
		this.nineboxDescription = nineboxDescription;
	}

	public String getNineboxTitle() {
		return nineboxTitle;
	}

	public void setNineboxTitle(String nineboxTitle) {
		this.nineboxTitle = nineboxTitle;
	}

	public String getNineboxInitialInfoText() {
		return nineboxInitialInfoText;
	}

	public void setNineboxInitialInfoText(String nineboxInitialInfoText) {
		this.nineboxInitialInfoText = nineboxInitialInfoText;
	}

	public String getNineboxInfoTextnoEmployee() {
		return nineboxInfoTextnoEmployee;
	}

	public void setNineboxInfoTextnoEmployee(String nineboxInfoTextnoEmployee) {
		this.nineboxInfoTextnoEmployee = nineboxInfoTextnoEmployee;
	}

	public List<QuadrantNineBoxInfoDTO> getInfoQuadrant() {
		return infoQuadrant;
	}

	public void setInfoQuadrant(List<QuadrantNineBoxInfoDTO> infoQuadrant) {
		this.infoQuadrant = infoQuadrant;
	}

	public String getInfoQuadrantStringJson() {
		return infoQuadrantStringJson;
	}

	public void setInfoQuadrantStringJson(String infoQuadrantStringJson) {
		this.infoQuadrantStringJson = infoQuadrantStringJson;
	}

	public String getTypeAxisX() {
		return typeAxisX;
	}

	public void setTypeAxisX(String typeAxisX) {
		this.typeAxisX = typeAxisX;
	}

	public String getTypeAxisY() {
		return typeAxisY;
	}

	public void setTypeAxisY(String typeAxisY) {
		this.typeAxisY = typeAxisY;
	}

	public String getLabelX() {
		return labelX;
	}

	public void setLabelX(String labelX) {
		this.labelX = labelX;
	}

	public String getLabelY() {
		return labelY;
	}

	public void setLabelY(String labelY) {
		this.labelY = labelY;
	}

}
