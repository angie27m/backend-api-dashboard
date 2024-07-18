package com.acsendo.api.customreports.util;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.acsendo.api.util.DataObjectUtil.getString;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.acsendo.api.enumerations.ELanguageCodes;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.hcm.service.CompanyService;

@Component
public class CustomReportsClimateExcelHandler {

	private String language;
	DecimalFormat df = new DecimalFormat("0.00");

	@Autowired
	private CompanyService companyService;

	@Autowired
	private LabelRepository labelRepository;
	

	// Códigos para etiquetas de las hojas del reporte
	public static final String SATISFACTION_SHEET = "satisfaction_sheet";
	public static final String DIMENSIONS_SHEET = "dimensions";
	public static final String FACTORS_SHEET = "factors";
	public static final String QUESTIONS_SHEET = "questions_sheet";
	public static final String OPEN_RESPONSES_SHEET = "open_questions_sheet";
	public static final String SOCIODEMOGRAPHIC_SHEET = "sociodemographic_results";
	// Códigos para etiquetas de los títulos de las tablas del reporte
	public static final String SCALE_TITLE = "scale";
	public static final String DIMENSION_TITLE = "spider_dimension_title";
	public static final String RESULT_TITLE = "result";
	public static final String DIVISION_TITLE = "placeholder_division";
	public static final String FACTOR_TITLE = "spider_factor_title";
	public static final String QUESTION_TITLE = "question_title";
	public static final String SENTIMENT_TITLE = "sentiment_title";
	public static final String OPTION_TITLE = "option_title";
	

	/**
	 * Método principal de la creación del reporte excel de clima
	 * 
	 * @param satisfactionResults Resultados de satisfacción
	 * @param resultsByDimension  Resultados de dimensiones por departamento
	 * @param resultsByFactor     Resultados de factores por departamento
	 * @param questionResults     Resultados de preguntas clima
	 * @param openResponses		  Respuestas preguntas abiertas
	 * @param companyId           Identificador de la compañía
	 */
	public byte[] getClimateExcelReport(List<Object[]> satisfactionResults, List<Object[]> resultsByDimension,
			List<Object[]> resultsByFactor, List<Object[]> questionResults, List<Object[]> openResponses,
			List<Object[]> sociodemographicResults, Long companyId) {

		byte[] result = null;
		setLanguage(companyId);
		
		// Crea el libro de excel con las respectivas hojas
		Workbook workBook = new XSSFWorkbook();
		if (satisfactionResults != null && satisfactionResults.size() > 0) createSatisfactionResults(satisfactionResults, workBook, 
				getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, SATISFACTION_SHEET, companyId), companyId);		
		if (resultsByDimension != null && resultsByDimension.size() > 0) createSheetDimensionOrFactorResults(resultsByDimension, workBook, 
				getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, DIMENSIONS_SHEET, companyId), true, companyId);
		if (resultsByFactor != null && resultsByFactor.size() > 0) createSheetDimensionOrFactorResults(resultsByFactor, workBook, 
				getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, FACTORS_SHEET, companyId), false, companyId);
		if (questionResults != null && questionResults.size() > 0) createSheetQuestionResults(questionResults, workBook, 
				getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, QUESTIONS_SHEET, companyId), companyId);
		if (openResponses != null && openResponses.size() > 0) createSheetOpenResponses(openResponses, workBook, 
				getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, OPEN_RESPONSES_SHEET,companyId), companyId);
		if (sociodemographicResults != null && sociodemographicResults.size() > 0) createSheetSociodemographicResults(sociodemographicResults, workBook, 
				getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, SOCIODEMOGRAPHIC_SHEET, companyId), companyId); 
	 

		// Crea el flujo del reporte
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workBook.write(out);
			out.close();
			result = out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Crea la hoja de resultados de satisfacción
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSatisfactionResults(List<Object[]> rawData, Workbook workBook, String title, Long companyId) {
		Sheet sheetOne = createBaseSheet(workBook, title, companyId);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(SCALE_TITLE);
		headers.add(RESULT_TITLE);

		String[] headersNames = this.getHeaderLabels(headers, companyId);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setSatisfactionResults(rawData, rowNumber, sheetOne);
	}
	
	/**
	 * Método que inserta los datos en la hoja de resultados de satisfacción
	 * 
	 * @param satisfaction Datos a mostrarse en la hoja
	 * @param row          Fila de la hoja de excel
	 * @param sheetOne     Representación de una hoja de cálculo de Excel
	 */
	@SuppressWarnings("removal")
	private void setSatisfactionResults(List<Object[]> satisfaction, int row, Sheet sheetOne) {
		
		for (Object[] data : satisfaction) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellScaleName = newRow.createCell(celIdex);
			Cell cellResult = newRow.createCell(celIdex + 1);
			cellScaleName.setCellValue(getString(data[1]));
			cellResult.setCellValue(df.format(new Double(data[2].toString())));
			celIdex++;
		}
	}

	/**
	 * Crea la hoja de resultados de dimensiones/factores por departamento
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetDimensionOrFactorResults(List<Object[]> rawData, Workbook workBook, String title, Boolean isByDimension,
			Long companyId) {
		Sheet sheetOne = createBaseSheet(workBook, title, companyId);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);

		List<String> headers = new ArrayList<String>();
		headers.add(DIVISION_TITLE);
		if (isByDimension) {
			headers.add(DIMENSION_TITLE);
		}else {
			headers.add(FACTOR_TITLE);
		}
		headers.add(RESULT_TITLE);

		String[] headersNames = this.getHeaderLabels(headers, companyId);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setDimensionOrFactorResults(rawData, rowNumber, sheetOne);
	}

	/**
	 * Método que inserta los datos de dimensiones/factores en una nueva hoja de excel
	 * 
	 * @param results  Datos a mostrarse en la hoja
	 * @param row      Fila de la hoja de excel
	 * @param sheetOne Representación de una hoja de cálculo de Excel
	 */
	@SuppressWarnings("removal")
	private void setDimensionOrFactorResults(List<Object[]> results, int row, Sheet sheetOne) {
		if (results != null) {
			for (Object[] data : results) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				Cell cellDivisionName = newRow.createCell(celIdex);
				Cell cellEntityName = newRow.createCell(celIdex + 1);
				Cell cellResult = newRow.createCell(celIdex + 2);
				cellDivisionName.setCellValue(getString(data[1]));
				cellEntityName.setCellValue(getString(data[3]));
				cellResult.setCellValue(df.format(new Double(data[5].toString())));
				celIdex++;
			}			
		}
	}

	/**
	 * Crea la hoja de resultados de las preguntas realizadas
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetQuestionResults(List<Object[]> rawData, Workbook workBook, String title, Long companyId) {
		Sheet sheetOne = createBaseSheet(workBook, title, companyId);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);

		List<String> headers = new ArrayList<String>();
		headers.add(QUESTION_TITLE);
		headers.add(RESULT_TITLE);
		headers.add(DIMENSION_TITLE); 
		if (rawData.get(0)[5] != null && !rawData.get(0)[5].toString().equals("")) {
			headers.add(FACTOR_TITLE);			
		}

		String[] headersNames = this.getHeaderLabels(headers, companyId);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setQuestionResults(rawData, rowNumber, sheetOne);
	}
	
	/**
	 * Método que inserta los datos de preguntas en una nueva hoja de excel
	 * 
	 * @param results  Datos a mostrarse en la hoja
	 * @param row      Fila de la hoja de excel
	 * @param sheetOne Representación de una hoja de cálculo de Excel
	 */
	@SuppressWarnings("removal")
	private void setQuestionResults(List<Object[]> results, int row, Sheet sheetOne) {
		if (results != null) {
			for (Object[] data : results) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				Cell cellQuestionName = newRow.createCell(celIdex);
				Cell cellResult = newRow.createCell(celIdex + 1);
				Cell cellDimensionName = newRow.createCell(celIdex + 2);
				Cell cellFactorName = newRow.createCell(celIdex + 3);
				cellQuestionName.setCellValue(getString(data[1]));
				cellResult.setCellValue(df.format(new Double(data[3].toString())));
				cellDimensionName.setCellValue(getString(data[4]));
				cellFactorName.setCellValue(getString(data[5]));
				celIdex++;
			}			
		}
	}
	
	/**
	 * Crea la hoja de resultados de preguntas abiertas
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetOpenResponses(List<Object[]> rawData, Workbook workBook, String title, Long companyId) {
		Sheet sheetOne = createBaseSheet(workBook, title, companyId);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(QUESTION_TITLE);
		headers.add(RESULT_TITLE);
		headers.add(SENTIMENT_TITLE);

		String[] headersNames = this.getHeaderLabels(headers, companyId);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			if (i == 2) {
				sheetOne.setColumnWidth(i, 30 * 256);
			} else {
				sheetOne.setColumnWidth(i, 64 * 256);				
			}
		}

		setOpenResponses(rawData, rowNumber, sheetOne, companyId);
	}
	
	/**
	 * Método que inserta los datos en la hoja de resultados de preguntas abiertas
	 * 
	 * @param openResponses Datos a mostrarse en la hoja
	 * @param row          Fila de la hoja de excel
	 * @param sheetOne     Representación de una hoja de cálculo de Excel
	 */
	private void setOpenResponses(List<Object[]> openResponses, int row, Sheet sheetOne, Long companyId) {
		
		for (Object[] data : openResponses) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellQuestion = newRow.createCell(celIdex);
			Cell cellResult = newRow.createCell(celIdex + 1);
			Cell cellSentiment = newRow.createCell(celIdex + 2);
			cellQuestion.setCellValue(getString(data[0]));
			cellResult.setCellValue(getString(data[1]));
			cellSentiment.setCellValue(getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, getString(data[2]), companyId));
			celIdex++;
		}
	}
	
	/**
	 * Crea la hoja de resultados de preguntas sociodemográficas
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetSociodemographicResults(List<Object[]> rawData, Workbook workBook, String title, Long companyId) {
		Sheet sheetOne = createBaseSheet(workBook, title, companyId);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(QUESTION_TITLE);
		headers.add(OPTION_TITLE);
		headers.add(RESULT_TITLE);

		String[] headersNames = this.getHeaderLabels(headers, companyId);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			if (i == 2) {
				sheetOne.setColumnWidth(i, 30 * 256);
			} else {
				sheetOne.setColumnWidth(i, 64 * 256);				
			}
		}

		setSociodemographicResults(rawData, rowNumber, sheetOne);
	}
	
	/**
	 * Método que inserta los datos en la hoja de resultados de preguntas abiertas
	 * 
	 * @param sociodemographic Datos a mostrarse en la hoja
	 * @param row              Fila de la hoja de excel
	 * @param sheetOne         Representación de una hoja de cálculo de Excel
	 */
	@SuppressWarnings("removal")
	private void setSociodemographicResults(List<Object[]> sociodemographic, int row, Sheet sheetOne) {
		
		for (Object[] data : sociodemographic) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellQuestion = newRow.createCell(celIdex);
			Cell cellResult = newRow.createCell(celIdex + 1);
			Cell cellSentiment = newRow.createCell(celIdex + 2);
			cellQuestion.setCellValue(getString(data[1]));
			cellResult.setCellValue(getString(data[2]));
			cellSentiment.setCellValue(df.format(new Double(data[3].toString())));
			celIdex++;
		}
	}
	

	/**
	 * Método que setea estilos base y nombre de una hoja
	 * 
	 * @param workBook  Representación del libro de Excel
	 * @param nameSheet Nombre de la hoja de cálculo
	 */
	private Sheet createBaseSheet(Workbook workBook, String nameSheet, Long companyId) {
		// Creamos la Hoja de Excel
		Sheet sheetOne = workBook.createSheet(nameSheet);
		// Añadimos el titulo de la hoja
		Row rowTitle = sheetOne.createRow(1);
		Cell cellTitle = rowTitle.createCell(0);
		cellTitle.setCellValue(getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, "title_climate_leader", companyId));
		cellTitle.setCellStyle(getMainTitleStyle(workBook, sheetOne));
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("A2:D6"));
		return sheetOne;
	}

	private CellStyle getMainTitleStyle(Workbook workBook, Sheet sheet) {

		Font headerFont = workBook.createFont();
		headerFont.setColor(IndexedColors.GREY_80_PERCENT.index);
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerFont.setFontHeightInPoints((short) 35);
		headerFont.setFontName("Poppins");

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFont(headerFont);

		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		return cellStyle;
	}

	/**
	 * Obtiene labels de los headers de la tabla según el idioma de la empresa
	 * 
	 * @param headers Títulos de las tablas
	 */
	private String[] getHeaderLabels(List<String> headers, Long companyId) {
		List<String> headerLabels = new ArrayList<String>();

		for (String code : headers) {
			headerLabels.add(getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, code, companyId));
		}
		return headerLabels.toArray(new String[0]);
	}

	/**
	 * Aplica los estillos a los headers del reporte
	 * 
	 * @param workBook Representación del libro de Excel
	 * @param sheet    Representación de una hoja de cálculo de Excel
	 */
	private CellStyle getStyleHeaders(Workbook workBook, Sheet sheet) {

		Font headerFont = workBook.createFont();
		headerFont.setColor(IndexedColors.WHITE.index);
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.index);
		cellStyle.setFillPattern((short) 1);
		cellStyle.setFont(headerFont);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

		return cellStyle;
	}
	
	/**
	 * Obtiene un label según el idioma de la empresa
	 * 
	 * @param module    Módulo al que pertenece la etiqueta
	 * @param labelCode Código de la etiqueta
	 */
	public String getLabel(ECustomReportsLabelsExcelTemplate module, String labelCode, Long companyId) {
		Label labelTemp = null;
		labelTemp = labelRepository.findByCompanyIdAndModuleAndCode(companyId, module.toString().toLowerCase(),
				labelCode.toString().toLowerCase());
		if (labelTemp == null) labelTemp = labelRepository.findByModuleCode(module.toString().toLowerCase(),
				labelCode.toString());
		
		if (labelTemp != null) {
			if (this.language.equals(ELanguageCodes.es.toString())) {
				return labelTemp.getSpanish();
			} else if (this.language.equals(ELanguageCodes.en.toString())) {
				return labelTemp.getEnglish();
			} else {
				return labelTemp.getPortuguese();
			}
		}
		return labelCode;
	}

	/**
	 * Establece el idioma con el que se genera el reporte
	 * 
	 * @param companyId Identificador de la compañía
	 */
	public void setLanguage(Long companyId) {
		this.language = ELanguageCodes.es.toString();
		Company company = this.companyService.getCompanyById(companyId);
		if (company.getLanguageCode() != null) {
			this.language = company.getLanguageCode();
		}

	}
}
