package com.acsendo.api.customreports.util;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.acsendo.api.util.DataObjectUtil.getDouble;
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

import com.acsendo.api.customreports.dto.CategoryDTO;
import com.acsendo.api.customreports.dto.CompareEvaluationsExcelDTO;
import com.acsendo.api.customreports.dto.CompetencesByDivisionsDTO;
import com.acsendo.api.customreports.dto.CompetencesExcelReportDTO;
import com.acsendo.api.customreports.dto.EmployeeResultDTO;
import com.acsendo.api.customreports.dto.EmployeeResultExcelDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.enumerations.ELanguageCodes;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.model.LabelFlex;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.hcm.service.CompanyService;

import net.bytebuddy.dynamic.loading.PackageDefinitionStrategy.Definition.Undefined;



@Component
public class CustomReportsOKRSExcelHandler {
	
	private final String COMPETENCES_REPORT_TITLE  = "Competences report";
	
	private String language;
	
	private Long companyId;
	
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private LabelRepository labelRepository;
	
	@Autowired
	private CustomReportsCompetencesExcelHandler competencesExcel;
	
	public static final String GENERAL_SHEET = "general_result_title";
	public static final String DIVISIONS_SHEET = "results_by_division";
	public static final String COMPANY_SHEET = "company_results";
	public static final String EMPLOYEE_SHEET = "results_by_employee";
	public static final String INFO_TOTAL_SHEET = "info_total_title";

	public static final String CATEGORY_TITLE = "category_title";
	public static final String YEAR_TITLE = "year";
	public static final String GENERAL_RESULT_TITLE = "general_result_title";
	public static final String PERIOD_TITLE = "period";
	public static final String RESULT_TITLE = "result";
	public static final String OBJECTIVE_TITLE = "objective_title";
	public static final String DIVISION_TITLE = "division_title";
	public static final String EMPLOYEE_TITLE = "collaborator_title";
	public static final String TYPE_TITLE = "type_title";
	public static final String OKR_TITLE = "type_title";
	public static final String KR_TITLE = "KR";
	public static final String INITIATIVES_TITLE = "initiatives";
	public static final String JOB_TITLE = "job_title";
	public static final String JOB_LEVEL_TITLE = "category_label";
	public static final String SUBSIDIARY_TITLE = "subsidiary_title";
	
	DecimalFormat df = new DecimalFormat("0.00");
	
	
	/**
	 * Método principal de la creación del reporte excel de OKRS
	 * @param data
	 * @param companyId
	 * @return
	 */
	public byte[] getOKRSExcelReport(Long companyId, boolean isFiltered, boolean isByCompany, List<CategoryDTO> generalData, 
			List<CategoryDTO> companyData, List<Object[]> divisionsData, List<Object[]> employeeData, List<Object[]> extraFieldNames, List<Object[]> extraFields) {
		
		
		byte[] result = null;
		
		this.companyId = companyId;
		setLanguage(companyId);
		
		// Crea el libro de excel con las respectivas hojas
		Workbook workBook = new XSSFWorkbook();
		if(isFiltered) {
			createSheetEmployees(employeeData , workBook, getLabel(ECustomReportsLabelsExcelTemplate.REPORTS,INFO_TOTAL_SHEET), extraFieldNames, extraFields);
			createSheetGeneral(generalData, workBook, getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, GENERAL_SHEET));
			if(isByCompany) {
				createSheetCompany(companyData, workBook,getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, COMPANY_SHEET));
			}else {
				createSheetDivisions(divisionsData, workBook, getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, DIVISIONS_SHEET));
			}
		
			
		}else {
			createSheetEmployees(employeeData , workBook, getLabel(ECustomReportsLabelsExcelTemplate.REPORTS,INFO_TOTAL_SHEET), extraFieldNames, extraFields);
			createSheetGeneral(generalData, workBook, getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, GENERAL_SHEET));
			createSheetCompany(companyData, workBook,getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, COMPANY_SHEET));
			createSheetDivisions(divisionsData, workBook, getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, DIVISIONS_SHEET));

		}
		
		//crea el flujo del reporte
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
	 * Crea la hoja de Resultados de todos los años
	 * @param rawData
	 * @param workBook
	 */
	private void createSheetGeneral(List<CategoryDTO> rawData , Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(YEAR_TITLE);
		headers.add(GENERAL_RESULT_TITLE);
		headers.add(PERIOD_TITLE);
		headers.add(RESULT_TITLE);
		
		String[] headersNames = this.getHeaderLabels(headers);
		
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataGeneral(rawData, rowNumber, sheetOne);
		
	}
	
	/**
	 * Crea la hoja de resultados con objetivos de la empresa
	 * @param rawData
	 * @param workBook
	 */
	private void createSheetCompany(List<CategoryDTO> rawData , Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(competencesExcel.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.OKRS));
		headers.add(GENERAL_RESULT_TITLE);
		headers.add(DIVISION_TITLE);
		headers.add(RESULT_TITLE);
		
		String[] headersNames = this.getHeaderLabels(headers);
		
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataCompany(rawData, rowNumber, sheetOne);
		
	}
	
	/**
	 * Crea la hoja de resultados de objetivos por departamento
	 * @param rawData
	 * @param workBook
	 */
	private void createSheetDivisions(List<Object[]> rawData , Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(DIVISION_TITLE);
		headers.add(competencesExcel.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.OKRS));
		headers.add(RESULT_TITLE);
		
		String[] headersNames = this.getHeaderLabels(headers);
		
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataDivisions(rawData, rowNumber, sheetOne);
		
	}
	
	/**
	 * Crea la hoja de resultados por empleado
	 * @param rawData
	 * @param workBook
	 */
	private void createSheetEmployees(List<Object[]> rawData , Workbook workBook, String title,List<Object[]> extraFieldNames, List<Object[]> extraFields) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 8;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		
		Row rowHeader2=sheetOne.createRow(6);
		Cell cellInfo = rowHeader2.createCell(0);
		

		sheetOne.addMergedRegion(CellRangeAddress.valueOf("A7:D7"));
		cellInfo.setCellValue("Recuerde que las iniciativas no se incluyen en el promedio general");
		cellInfo.setCellStyle(getInfoTitleStyle(workBook, sheetOne));
		sheetOne.setColumnWidth(6, 32 * 500);
	

		headers.add(EMPLOYEE_TITLE);
		headers.add(TYPE_TITLE);
		headers.add(competencesExcel.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.OKRS));
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
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataEmployees(rawData, rowNumber, sheetOne,extraFieldNames, extraFields);
		
	}
	
	
	
	/**
	 * Método que inserta los datos en la hoja de Objetivos por año
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataGeneral(List<CategoryDTO> data,int row,Sheet sheetOne) {
		for(CategoryDTO temp : data) {
			for(ResultDTO res  : temp.getResults()) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				Cell cellYearName = newRow.createCell(celIdex);
				Cell cellresult = newRow.createCell(celIdex+1);
				Cell cellProcessName = newRow.createCell(celIdex+2);
				Cell cellProcessValue = newRow.createCell(celIdex+3);
				cellYearName.setCellValue(temp.getName());
				cellresult.setCellValue(df.format(temp.getValue()));
				cellProcessName.setCellValue(res.getName());
				cellProcessValue.setCellValue(df.format(res.getValue()));
				celIdex++; 
			}
		}
	}

	
	/**
	 * Método que inserta los datos en la hoja de objetivos de la empresa
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataCompany(List<CategoryDTO> data,int row,Sheet sheetOne) {
		for(CategoryDTO temp : data) {
			for(ResultDTO res  : temp.getResults()) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				Cell cellYearName = newRow.createCell(celIdex);
				Cell cellresult = newRow.createCell(celIdex+1);
				Cell cellProcessName = newRow.createCell(celIdex+2);
				Cell cellProcessValue = newRow.createCell(celIdex+3);
				cellYearName.setCellValue(temp.getName());
				cellresult.setCellValue(df.format(temp.getValue()));
				cellProcessName.setCellValue(res.getName());
				cellProcessValue.setCellValue(df.format(res.getValue()));
				celIdex++; 
			}
		}
	}
	
	/**
	 * Método que inserta los datos en la hoja resultados por departamento
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataDivisions(List<Object[]> data,int row,Sheet sheetOne) {
			for(Object[] res : data) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				Cell cellDivisionName = newRow.createCell(celIdex);
				Cell cellObjectiveName = newRow.createCell(celIdex+1);
				Cell cellValue = newRow.createCell(celIdex+2);
				cellDivisionName.setCellValue(getString(res[2]));
				cellObjectiveName.setCellValue(getString(res[1]));
				cellValue.setCellValue(df.format(getDouble(res[3])));
				celIdex++; 
			}
	}
	
	/**
	 * Método que inserta los datos en la hoja resultados por empleado
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataEmployees(List<Object []> data,int row,Sheet sheetOne, List<Object[]> extraFieldNames, List<Object[]> extraFields) {
		
		Map<Long,Object[]> mapEmp = extraFields.stream().collect(Collectors.toMap(item -> getLong(item[0]), item -> item));
		
		for(Object[] res  : data) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellPersonName = newRow.createCell(celIdex);
			Cell cellType = newRow.createCell(celIdex+1);
			Cell cellOkrName = newRow.createCell(celIdex+2);
			Cell cellResult = newRow.createCell(celIdex+3);
			Cell cellDivisionName = newRow.createCell(celIdex+4);
			Cell cellSubsidiaryName = newRow.createCell(celIdex+5);
			Cell cellJobLevelName = newRow.createCell(celIdex+6);
			Cell cellJobName = newRow.createCell(celIdex+7);
			
			
			cellPersonName.setCellValue(getString(res[1]));
			if(getString(res[2]).equals("KEY_RESULT")) {
				cellType.setCellValue(KR_TITLE);
			}else {
				cellType.setCellValue(getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, INITIATIVES_TITLE));
			}
			cellOkrName.setCellValue(getString(res[4]));
			cellResult.setCellValue(df.format(getDouble(res[5])));
			cellDivisionName.setCellValue(getString(res[6]));
			cellSubsidiaryName.setCellValue(getString(res[7]));
			cellJobLevelName.setCellValue(getString(res[8]));
			cellJobName.setCellValue(getString(res[9]));
			
			List<Cell> cellFields = new ArrayList<>();
			if (extraFieldNames.size() > 0) {
				for (int i = 0; i < extraFieldNames.size(); i++) {
					Cell cellField = newRow.createCell(celIdex+8 + i);
					cellFields.add(cellField);
				}				
			}
			
			
			if (cellFields.size() > 0 && extraFields.size()>0) {

				Object[] empExtra=mapEmp.get(((BigInteger)res[0]).longValue());

				for (int i = 0; i < extraFieldNames.size(); i++) {
					String field=empExtra!=null ?(String)(empExtra[i+1]): "";
					cellFields.get(i).setCellValue(field);
				}				
			}
			
			celIdex++; 
		}
	}
		
	/**
	 * Obtiene un label según el idioma de la empresa
	 * @param module
	 * @param labelCode
	 * @return
	 */
	
	public String getLabel(ECustomReportsLabelsExcelTemplate module, String labelCode) {
		Label labelTemp = null;		
		labelTemp = labelRepository.findByModuleCode(module.toString().toLowerCase(), labelCode.toString().toLowerCase());
		if(labelTemp != null) {
			if(this.language.equals(ELanguageCodes.es.toString())) {
				return labelTemp.getSpanish();
			}else if(this.language.equals(ELanguageCodes.en.toString())) {
				return labelTemp.getEnglish();
			}else {
				return labelTemp.getPortuguese();
			}
		}
		return labelCode;
	}
	
	/**
	 * Establece el idioma con el que se genera el reporte
	 * @param companyId
	 */
	public void setLanguage(Long companyId) {		
		
		this.language = ELanguageCodes.es.toString(); 
		
		Company company = this.companyService.getCompanyById(companyId);
		
		if( company.getLanguageCode() != null ) {
			this.language = company.getLanguageCode(); 			
		}
		
	}
	
	/**
	 * Método que setea estilos base y nombre de una hoja
	 * @param workBook
	 * @param nameSheet
	 * @return
	 */
	private Sheet createBaseSheet(Workbook workBook, String nameSheet) {
		// Creamos la Hoja de Excel
		Sheet sheetOne = workBook.createSheet(nameSheet);
		// Anadimos el titulo de la hoja
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
	
	
	private CellStyle getInfoTitleStyle(Workbook workBook, Sheet sheet) {

		Font headerFont = workBook.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		headerFont.setFontHeightInPoints((short) 11);
		headerFont.setFontName("Arial");

		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
		cellStyle.setFont(headerFont);

		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		cellStyle.setAlignment(CellStyle.ALIGN_LEFT);

		return cellStyle;
	}
	
	/**
	 * Obtiene Labels de los headers de la tabla según el idioma de la empresa
	 * @param headers 
	 * @return
	 */
	private String[] getHeaderLabels(List<String> headers) {

		List<String> headerLabels = new ArrayList<String>();
		
		for(String temp : headers) {
			headerLabels.add(getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, temp));
		}
		return headerLabels.toArray(new String[0]);

	}
	
	/**
	 * Aplica los estillos a los headers del reporte
	 * @param workBook
	 * @param sheet
	 * @return
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
}
