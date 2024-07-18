package com.acsendo.api.customreports.util;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

import com.acsendo.api.enumerations.ELanguageCodes;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.hcm.service.CompanyService;

@Component
public class CustomReportsPerformanceExcelHandler {

	private String language;
	private Long companyId;
	private String module;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private LabelRepository labelRepository;

	@Autowired
	private CustomReportsCompetencesExcelHandler competencesExcelHandler;

	// Códigos para etiquetas de las hojas del reporte
	public static final String INFO_TOTAL_SHEET = "info_total_title";
	// Códigos para etiquetas de los títulos de las tablas del reporte
	public static final String EMPLOYEE_TITLE = "subordinate";
	public static final String RESULT_PERFORMANCE = "result_performance";
	public static final String RESULT_TITLE = "result";
	public static final String DIVISION_TITLE = "division";
	public static final String SUBSIDIARY_TITLE = "subsidiary";
	public static final String JOB_LEVEL_TITLE = "job_category";
	public static final String JOB_TITLE = "job";

	DecimalFormat df = new DecimalFormat("0.00");
	String titleSheet;

	/**
	 * Método principal de la creación del reporte excel de desempeño
	 * 
	 * @param infoTotal Resultados por empleado con información total de desempeño
	 * @param companyId Identificador de la compañía
	 */
	public byte[] getPerformanceExcelReport(List<Object[]> infoTotal, List<Object[]> namesExtraFields,
			List<Object[]> extraFields, Long companyId1, String moduleName) {

		byte[] result = null;
		setLanguage(companyId1);
		companyId = companyId1;
		module = moduleName;

		titleSheet = competencesExcelHandler.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.PERFORMANCE);
		// Crea el libro de excel con las respectivas hojas
		Workbook workBook = new XSSFWorkbook();
		if (infoTotal.size() > 0) {
			createSheetInfoTotalResults(infoTotal, workBook,
					getLabel(ECustomReportsLabelsExcelTemplate.COMMON, INFO_TOTAL_SHEET), namesExtraFields,
					extraFields);
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
	 * Crea la hoja de resultados por empleado con toda su información y campos
	 * extras
	 * 
	 * @param rawData  Datos que se muestran en la hoja
	 * @param workBook Representación del libro de Excel
	 * @param title    Título de la hoja
	 */
	private void createSheetInfoTotalResults(List<Object[]> rawData, Workbook workBook, String title,
			List<Object[]> extraFieldNames, List<Object[]> extraFields) {

		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;
		Row rowHeader = sheetOne.createRow(rowNumber);

		List<String> headers = new ArrayList<String>();
		headers.add(EMPLOYEE_TITLE);
		headers.add(RESULT_PERFORMANCE);
		headers.add(RESULT_TITLE);
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
			if (i == 2) headersNames[i] = headersNames[i] + " " + this.competencesExcelHandler.getCustomLabel(companyId,
					ECustomReportsLabelsExcelTemplate.COMPETENCES);
			if (i == 3) headersNames[i] = headersNames[i] + " " + module;
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// Ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}

		setInfoTotalResults(rawData, rowNumber, sheetOne, extraFieldNames, extraFields);

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
	 * Método que inserta los datos en la hoja de información total de desempeño
	 * 
	 * @param results  Datos a mostrarse en la hoja
	 * @param row      Fila de la hoja de excel
	 * @param sheetOne Representación de una hoja de cálculo de Excel
	 */
	private void setInfoTotalResults(List<Object[]> results, int row, Sheet sheetOne, List<Object[]> extraFieldNames,
			List<Object[]> extraFields) {

		Map<Long, Object[]> mapEmp = extraFields != null ? extraFields.stream()
				.collect(Collectors.toMap(item -> getLong(item[0]), item -> item)) : null;

		for (Object[] data : results) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellEmployeeName = newRow.createCell(celIdex);
			Cell cellResultPerformance = newRow.createCell(celIdex + 1);
			Cell cellResultComp = newRow.createCell(celIdex + 2);
			Cell cellResultGoalOkrs = newRow.createCell(celIdex + 3);
			Cell cellDivisionName = newRow.createCell(celIdex + 4);
			Cell cellSubsidiarieName = newRow.createCell(celIdex + 5);
			Cell cellJobCategory = newRow.createCell(celIdex + 6);
			Cell cellJobName = newRow.createCell(celIdex + 7);
			List<Cell> cellFields = new ArrayList<>();
			if (extraFieldNames.size() > 0) {
				for (int i = 0; i < extraFieldNames.size(); i++) {
					Cell cellField = newRow.createCell(celIdex + 8 + i);
					cellFields.add(cellField);
				}
			}

			cellEmployeeName.setCellValue(getString(data[0]));
			cellResultPerformance.setCellValue(data[1] != null ? df.format((Double) data[1]) : "");
			cellResultComp.setCellValue(data[2] != null ? df.format((Double) data[2]) : "");
			cellResultGoalOkrs.setCellValue(data[3] != null ? df.format((Double) data[3]) : "");
			cellDivisionName.setCellValue(getString(data[4]));
			cellSubsidiarieName.setCellValue(getString(data[5]));
			cellJobCategory.setCellValue(getString(data[6]));
			cellJobName.setCellValue(getString(data[7]));
			if (cellFields.size() > 0 && extraFields.size() > 0) {
				Object[] empExtra = mapEmp.get(data[8]);

				for (int i = 0; i < extraFieldNames.size(); i++) {
					String field = empExtra != null ? (String) (empExtra[i + 1]) : "";
					cellFields.get(i).setCellValue(field);
				}
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
