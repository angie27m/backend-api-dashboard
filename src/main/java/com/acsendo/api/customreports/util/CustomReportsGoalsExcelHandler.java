package com.acsendo.api.customreports.util;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.acsendo.api.util.DataObjectUtil.getLong;
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

import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.enumerations.ELanguageCodes;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.hcm.service.CompanyService;

@Component
public class CustomReportsGoalsExcelHandler {

	private String language;
	private Long companyId;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private LabelRepository labelRepository;
	
	@Autowired
	private CustomReportsCompetencesExcelHandler competencesExcelHandler;

	// Códigos para etiquetas de las hojas del reporte
	public static final String INFO_TOTAL_SHEET = "info_total_title";
	public static final String GOALS_SHEET = "module_goals_title";
	public static final String DIVISIONS_SHEET = "divisions";
	public static final String STRATEGIC_GOALS_SHEET = "strategic_goals_title";
	public static final String TRACKINGS_SHEET = "trackings";
	// Códigos para etiquetas de los títulos de las tablas del reporte
	public static final String RESULT_BOSS = "result_boss";
	public static final String RESULT_EMPLOYEE = "result_employee";
	public static final String RESULT_TITLE = "result";
	public static final String DIVISION_TITLE = "division";
	public static final String STRATEGIC_GOAL_TITLE = "strategic_goal";
	public static final String JOB_LEVEL_TITLE = "job_category";
	public static final String SUBSIDIARY_TITLE = "subsidiary";
	public static final String TYPE_TITLE = "type";
	public static final String TRACKING_TITLE = "module_tracking_workremote_title";
	public static final String DATE_TRACKING_TITLE = "date_tracking_title";
	public static final String EMPLOYEE_TITLE = "subordinate";
	public static final String JOB_TITLE = "job";
	public static final String CATEGORY_TITLE = "category_title";

	DecimalFormat df = new DecimalFormat("0.00");
	Boolean showLeaderGoal = null;
	String titleSheet;

	/**
	 * Método principal de la creación del reporte excel de metas
	 * 
	 * @param goalsInfoTotal       Resultados por empleado con información total de metas
	 * @param generalGoalResults   Resultados generales de metas
	 * @param resultsByDivision    Resultados de metas por departamento
	 * @param resultsByStrategic   Resultados de metas por metas estratégicas
	 * @param trackingsGoalResults Resultados por seguimientos de metas
	 * @param companyId            Identificador de la compañía
	 */
	public byte[] getGoalsExcelReport(List<Object[]> goalsInfoTotal, List<Object[]> generalGoalResults, List<ResultDTO> resultsByDivision,
			List<ResultDTO> resultsByStrategic, List<Object[]> trackingsGoalResults, Boolean showLeader, List<Object[]> namesExtraFields,
			List<Object[]> extraFields,	Long companyId1) {

		byte[] result = null;
		setLanguage(companyId1);
		showLeaderGoal = showLeader;
		companyId = companyId1;
				
		titleSheet = competencesExcelHandler.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.GOALS);
		// Crea el libro de excel con las respectivas hojas
		Workbook workBook = new XSSFWorkbook();
		if (goalsInfoTotal.size() > 0) {
			createSheetInfoTotalGoalResults(goalsInfoTotal, workBook, getLabel(ECustomReportsLabelsExcelTemplate.COMMON, INFO_TOTAL_SHEET), namesExtraFields, extraFields);
		}
		createSheetGeneralGoalResults(generalGoalResults, workBook, titleSheet);
		createSheetDivisionResults(resultsByDivision, workBook,
				getLabel(ECustomReportsLabelsExcelTemplate.COMMON, DIVISIONS_SHEET));
		createSheetStrategicGoalResults(resultsByStrategic, workBook,
				getLabel(ECustomReportsLabelsExcelTemplate.COMMON, STRATEGIC_GOALS_SHEET));
		if (trackingsGoalResults.size() > 0) {
			createSheetSTrackingsGoalResults(trackingsGoalResults, workBook,
					getLabel(ECustomReportsLabelsExcelTemplate.COMMON, TRACKINGS_SHEET));
		}

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
	 * Crea la hoja de resultados por empleado con toda su información y campos extras
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetInfoTotalGoalResults(List<Object[]> rawData, Workbook workBook, String title, List<Object[]> extraFieldNames, List<Object[]> extraFields) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(EMPLOYEE_TITLE);
		headers.add(titleSheet);
		headers.add(RESULT_TITLE);
		headers.add(DIVISION_TITLE);
		headers.add(SUBSIDIARY_TITLE);
		headers.add(JOB_LEVEL_TITLE);
		headers.add(JOB_TITLE);		
		if (extraFieldNames.size() > 0) {
			for (Object[] objArray : extraFieldNames) {
	            Object columnValue = objArray[1];
	            headers.add(columnValue.toString());
		    }			
		}

		String[] headersNames = this.getHeaderLabels(headers);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setInfoTotalGoalResults(rawData, rowNumber, sheetOne, extraFieldNames, extraFields);

	}

	/**
	 * Crea la hoja de resultados generales de metas
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetGeneralGoalResults(List<Object[]> rawData, Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(titleSheet);
		headers.add(JOB_LEVEL_TITLE);
		headers.add(DIVISION_TITLE);
		headers.add(SUBSIDIARY_TITLE);
		headers.add(RESULT_TITLE);

		String[] headersNames = this.getHeaderLabels(headers);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setGeneralGoalResults(rawData, rowNumber, sheetOne);

	}

	/**
	 * Crea la hoja de resultados de metas por departamento
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetDivisionResults(List<ResultDTO> rawData, Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);

		List<String> headers = new ArrayList<String>();
		headers.add(DIVISION_TITLE);
		headers.add(RESULT_TITLE);

		String[] headersNames = this.getHeaderLabels(headers);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setDataResults(rawData, rowNumber, sheetOne);
	}

	/**
	 * Crea la hoja de resultados de metas por metas estratégicas
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetStrategicGoalResults(List<ResultDTO> rawData, Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);

		List<String> headers = new ArrayList<String>();
		headers.add(STRATEGIC_GOAL_TITLE);
		headers.add(RESULT_TITLE);

		String[] headersNames = this.getHeaderLabels(headers);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setDataResults(rawData, rowNumber, sheetOne);
	}

	/**
	 * Crea la hoja de resultados por seguimientos de metas
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetSTrackingsGoalResults(List<Object[]> rawData, Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);

		List<String> headers = new ArrayList<String>();
		headers.add(STRATEGIC_GOAL_TITLE);
		headers.add(DIVISION_TITLE);
		headers.add(TRACKING_TITLE);
		headers.add(DATE_TRACKING_TITLE);
		headers.add(EMPLOYEE_TITLE);
		if (showLeaderGoal == null) {
			headers.add(RESULT_BOSS);
			headers.add(RESULT_EMPLOYEE);
		} else if (showLeaderGoal) {
			headers.add(RESULT_BOSS);
		} else if (!showLeaderGoal) {
			headers.add(RESULT_EMPLOYEE);
		}

		String[] headersNames = this.getHeaderLabels(headers);

		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setTrackingsGoalResults(rawData, rowNumber, sheetOne);

	}

	/**
	 * Método que setea estilos base y nombre de una hoja
	 * 
	 * @param workBook  Representación del libro de Excel
	 * @param nameSheet Nombre de la hoja de cálculo
	 */
	private Sheet createBaseSheet(Workbook workBook, String nameSheet) {
		// Creamos la Hoja de Excel
		Sheet sheetOne = workBook.createSheet(nameSheet);
		// Añadimos el titulo de la hoja
		Row rowTitle = sheetOne.createRow(1);
		Cell cellTitle = rowTitle.createCell(0);
		cellTitle.setCellValue("Crehana");
		cellTitle.setCellStyle(getMainTitleStyle(workBook, sheetOne));
		sheetOne.addMergedRegion(CellRangeAddress.valueOf("A2:D6"));
		return sheetOne;
	}

	private CellStyle getMainTitleStyle(Workbook workBook, Sheet sheet) {

		Font headerFont = workBook.createFont();
		headerFont.setColor(IndexedColors.GREY_80_PERCENT.index);
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerFont.setFontHeightInPoints((short) 50);
		headerFont.setFontName("Arial Black");

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
	private String[] getHeaderLabels(List<String> headers) {
		List<String> headerLabels = new ArrayList<String>();

		for (String code : headers) {
			headerLabels.add(getLabel(ECustomReportsLabelsExcelTemplate.COMMON, code));
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
	 * Método que inserta los datos en la hoja de información total de metas
	 * 
	 * @param goals    Datos a mostrarse en la hoja
	 * @param row      Fila de la hoja de excel
	 * @param sheetOne Representación de una hoja de cálculo de Excel
	 */
	private void setInfoTotalGoalResults(List<Object[]> goals, int row, Sheet sheetOne, List<Object[]> extraFieldNames,
			List<Object[]> extraFields) {
		
		Map<Long, Object[]> mapEmp = extraFields != null ? extraFields.stream()
				.collect(Collectors.toMap(item -> getLong(item[0]), item -> item)) : null;

		for (Object[] data : goals) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellEmployeeName = newRow.createCell(celIdex);
			Cell cellGoalName = newRow.createCell(celIdex + 1);
			Cell cellResult = newRow.createCell(celIdex + 2);
			Cell cellDivisionName = newRow.createCell(celIdex + 3);
			Cell cellSubsidiarieName = newRow.createCell(celIdex + 4);
			Cell cellJobCategory = newRow.createCell(celIdex + 5);
			Cell cellJobName = newRow.createCell(celIdex + 6);
			List<Cell> cellFields = new ArrayList<>();
			if (extraFieldNames.size() > 0) {
				for (int i = 0; i < extraFieldNames.size(); i++) {
					Cell cellField = newRow.createCell(celIdex + 7 + i);
					cellFields.add(cellField);
				}
			}
			cellEmployeeName.setCellValue(getString(data[1]));
			cellGoalName.setCellValue(getString(data[2]));
			cellResult.setCellValue(data[3] != null ? df.format((Double) data[3]) : "");
			cellDivisionName.setCellValue(getString(data[4]));
			cellSubsidiarieName.setCellValue(getString(data[5]));
			cellJobCategory.setCellValue(getString(data[6]));
			cellJobName.setCellValue(getString(data[7]));
			if (cellFields.size() > 0 && extraFields.size() > 0) {
				Object[] empExtra = mapEmp.get(((BigInteger) data[0]).longValue());

				for (int i = 0; i < extraFieldNames.size(); i++) {
					String field = empExtra != null ? (String) (empExtra[i + 1]) : "";
					cellFields.get(i).setCellValue(field);
				}
			}
			celIdex++;
		}

	}

	/**
	 * Método que inserta los datos en la hoja de resultados de metas
	 * 
	 * @param goals    Datos a mostrarse en la hoja
	 * @param row      Fila de la hoja de excel
	 * @param sheetOne Representación de una hoja de cálculo de Excel
	 */
	private void setGeneralGoalResults(List<Object[]> goals, int row, Sheet sheetOne) {

		for (Object[] data : goals) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellGoalName = newRow.createCell(celIdex);
			Cell cellDivisionName = newRow.createCell(celIdex + 1);
			Cell cellSubsidiarieName = newRow.createCell(celIdex + 2);
			Cell cellJobCategory = newRow.createCell(celIdex + 3);
			Cell cellResult = newRow.createCell(celIdex + 4);
			cellGoalName.setCellValue(getString(data[0]));
			cellDivisionName.setCellValue(getString(data[1]));
			cellSubsidiarieName.setCellValue(getString(data[2]));
			cellJobCategory.setCellValue(getString(data[3]));
			cellResult.setCellValue(df.format((Double) data[4]));
			celIdex++;
		}

	}

	/**
	 * Método que inserta los datos en las hojas de resultados de metas estratégicas
	 * y departamentos
	 * 
	 * @param data     Datos a mostrarse en la hoja
	 * @param row      Fila de la hoja de excel
	 * @param sheetOne Representación de una hoja de cálculo de Excel
	 */
	private void setDataResults(List<ResultDTO> data, int row, Sheet sheetOne) {

		for (ResultDTO result : data) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellSubsidiaryName = newRow.createCell(celIdex);
			Cell cellAvg = newRow.createCell(celIdex + 1);
			cellSubsidiaryName.setCellValue(result.getName());
			cellAvg.setCellValue(df.format(result.getValue()));
			celIdex++;
		}
	}

	/**
	 * Método que inserta los datos en la hoja de resultados de seguimientos
	 * 
	 * @param goals    Datos a mostrarse en la hoja
	 * @param row      Fila de la hoja de excel
	 * @param sheetOne Representación de una hoja de cálculo de Excel
	 */
	private void setTrackingsGoalResults(List<Object[]> goals, int row, Sheet sheetOne) {

		for (Object[] data : goals) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellStrategicGoalName = newRow.createCell(celIdex);
			Cell cellDivisionName = newRow.createCell(celIdex + 1);
			Cell cellTrackingName = newRow.createCell(celIdex + 2);
			Cell cellTrackingDate = newRow.createCell(celIdex + 3);
			Cell cellEmployeeName = newRow.createCell(celIdex + 4);
			Cell cellResultBoss = newRow.createCell(celIdex + 5);
			Cell cellResultColab = newRow.createCell(celIdex + 6);
			cellStrategicGoalName.setCellValue(getString(data[0]));
			cellDivisionName.setCellValue(getString(data[1]));
			cellTrackingName.setCellValue(getString(data[2]));
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
			cellTrackingDate.setCellValue(simpleDateFormat.format((Date) data[3]));
			cellEmployeeName.setCellValue(getString(data[4]));
			if (showLeaderGoal == null) {
				cellResultBoss.setCellValue(df.format((Double) data[5]));
				cellResultColab.setCellValue(df.format((Double) data[6]));
			} else if (showLeaderGoal) {
				cellResultBoss.setCellValue(df.format((Double) data[5]));
			} else if (!showLeaderGoal) {
				cellResultBoss.setCellValue(df.format((Double) data[6]));
			}
			celIdex++;
		}

	}

	/**
	 * Obtiene un label según el idioma de la empresa
	 * 
	 * @param module    Módulo al que pertenece la etiqueta
	 * @param labelCode Código de la etiqueta
	 */
	public String getLabel(ECustomReportsLabelsExcelTemplate module, String labelCode) {
		Label labelTemp = null;
		labelTemp = labelRepository.findByModuleCode(module.toString().toLowerCase(),
				labelCode.toString().toLowerCase());
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
