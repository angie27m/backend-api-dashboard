package com.acsendo.api.customreports.util;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.acsendo.api.util.DataObjectUtil.getString;
import static com.acsendo.api.util.DataObjectUtil.getLong;
import java.math.BigInteger;
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
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.enumerations.ELanguageCodes;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.hcm.service.CompanyService;



@Component
public class CustomReportsCompetencesExcelHandler {
		
	private String language;
	
	private Long companyId;
	
	@Autowired
	private CompanyService companyService;
	
	@Autowired
	private LabelRepository labelRepository;
	
	public static final String EVALUATIONS_SHEET = "process_title";
	public static final String DIVISIONS_SHEET = "divisions";
	public static final String HEATMAP_SHEET = "heatmap";
	public static final String COMPETENCES_SHEET = "competences";
	public static final String COLLABORATORS_SHEET = "collaborators";
	public static final String SUBSIDIARIES_SHEET = "subsidiaries";
	public static final String JOBS_SHEET = "jobs";
	public static final String INFO_TOTAL_SHEET = "info_total_title";

	

	
	public static final String EVALUATION_TITLE = "evaluation_title";
	public static final String COMPETENCE_TITLE = "title_competence";
	public static final String RESULT_BY_COMPETENCE_TITLE = "result";
	public static final String DIVISION_TITLE = "division_title";
	public static final String CATEGORY_TITLE = "category_title";
	public static final String LEADER_TITLE = "leader_title";
	public static final String MACROCOMPETENCE_TITLE = "macrocompetence_title";
	public static final String EMPLOYEE_TITLE = "collaborator_title";
	public static final String JOB_TITLE = "job_title";
	public static final String JOB_LEVEL_TITLE = "category_label";
	public static final String SUBSIDIARY_TITLE = "subsidiary_title";
	public static final String GENERAL_RESULT_TITLE = "general_result_title";
	public static final String BEHAVIOR_TITLE = "behavior";
	
	DecimalFormat df = new DecimalFormat("0.00");
	Map<String, String> customLabels;
	
	
	/**
	 * Método principal de la creación del reporte excel de competencias
	 * @param data
	 * @param companyId
	 * @return
	 */
	public byte[] getCompetencesExcelReport(
			List<CompareEvaluationsExcelDTO> compareEvaluations, 
			List<CompetencesByDivisionsDTO> compareDivisions,
			List<CompetencesByDivisionsDTO> compareCategoryJobs,
			List<CategoryDTO> macrocompetences,	
			List<Object[]> employees,
			List<CategoryDTO> subsidiaries,
			List<Object[]> jobs,
			Long companyId,
			boolean isByCategory,
			List<Object[]> namesExtraFields,
			List<Object[]> extraFields) {
		
		
		byte[] result = null;
		
		this.companyId = companyId;
		setLanguage(companyId);
		
		// Crea el libro de excel con las respectivas hojas
		Workbook workBook = new XSSFWorkbook();	
		createSheetEmployees(employees, workBook, getLabel(ECustomReportsLabelsExcelTemplate.COMMON, INFO_TOTAL_SHEET), namesExtraFields, extraFields);
		createSheetCompareEvaluations(compareEvaluations, workBook, getLabel(ECustomReportsLabelsExcelTemplate.COMMON, EVALUATIONS_SHEET));
		createSheetCompareDivisions(compareDivisions, workBook,getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, DIVISIONS_SHEET));
		createSheetCompareDivisionsCategoryJob(compareCategoryJobs, workBook, getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, HEATMAP_SHEET), isByCategory);
		createSheetMacrocompetences(macrocompetences, workBook, getLabel(ECustomReportsLabelsExcelTemplate.REPORTS,this.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.COMPETENCES)));
		createSheetSubsidiaries(subsidiaries, workBook,getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, SUBSIDIARIES_SHEET));
		createSheetJobs(jobs, workBook, getLabel(ECustomReportsLabelsExcelTemplate.REPORTS, JOBS_SHEET));
			
		
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
	 * Crea la hoja de comparativo de departamentos
	 * @param rawData
	 * @param workBook
	 */
	private void createSheetCompareEvaluations(List<CompareEvaluationsExcelDTO> rawData , Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		
		List<String> headers = new ArrayList<String>();
		headers.add(EVALUATION_TITLE);
		headers.add(this.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.COMPETENCES));
		headers.add(RESULT_BY_COMPETENCE_TITLE);
		
		String[] headersNames = this.getHeaderLabels(headers);
		
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataCompareEvaluations(rawData, rowNumber, sheetOne);
		
	}
	
	/**
	 * Crea la hoja de comparativo de divisiones
	 * @param rawData
	 * @param workBook
	 * @param title
	 */
	private void createSheetCompareDivisions(List<CompetencesByDivisionsDTO> rawData , Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		List<String> headers = new ArrayList<String>();
		headers.add(DIVISION_TITLE);
		headers.add(this.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.COMPETENCES));
		headers.add(RESULT_BY_COMPETENCE_TITLE);
		
		String[] headersNames = this.getHeaderLabels(headers);
		
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataCompareDivisions(rawData, rowNumber, sheetOne);
		
	}
	
	/**
	 * Crea la hoja del mapa de calor
	 * @param rawData
	 * @param workBook
	 * @param title
	 */
	private void createSheetCompareDivisionsCategoryJob(List<CompetencesByDivisionsDTO> rawData , Workbook workBook, String title, boolean isByCategory) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		List<String> headers = new ArrayList<String>();
		headers.add(DIVISION_TITLE);
		if(isByCategory) {
			headers.add(CATEGORY_TITLE);
		}else {
			headers.add(LEADER_TITLE);
		}
		headers.add(this.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.COMPETENCES));
		headers.add(RESULT_BY_COMPETENCE_TITLE);
		
		String[] headersNames = this.getHeaderLabels(headers);
		
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataCompareDivisionsCategoryJob(rawData, rowNumber, sheetOne);
		
	}
	
	/**
	 * Crea la hoja de competencias con macrocompetencias
	 * @param rawData
	 * @param workBook
	 * @param title
	 */
	private void createSheetMacrocompetences(List<CategoryDTO> rawData , Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		List<String> headers = new ArrayList<String>();
	
		
		if(rawData.get(0).getName() == null) {	
			headers.add(this.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.COMPETENCES));
			headers.add(RESULT_BY_COMPETENCE_TITLE);
		}else {
			headers.add(MACROCOMPETENCE_TITLE);
			headers.add(RESULT_BY_COMPETENCE_TITLE);
			headers.add(this.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.COMPETENCES));
			headers.add(RESULT_BY_COMPETENCE_TITLE);
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
		
		setDataMacrcompetences(rawData, rowNumber, sheetOne);
		
	}
	
	/**
	 * Crea la hoja de competencias por empleado
	 * @param rawData
	 * @param workBook
	 * @param title
	 */
	private void createSheetEmployees(List<Object[]> rawData , Workbook workBook, String title, List<Object[]> extraFieldNames, List<Object[]> extraFields) {
		
		
	
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 8;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		List<String> headers = new ArrayList<String>();
		headers.add(EMPLOYEE_TITLE);
		headers.add(this.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.COMPETENCES));
		headers.add(BEHAVIOR_TITLE);
		headers.add(RESULT_BY_COMPETENCE_TITLE);
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
		
		setDataEmployees(rawData, rowNumber, sheetOne,  extraFieldNames, extraFields);
		
	}
	
	/**
	 * Crea la hoja de competencias por sede
	 * @param rawData
	 * @param workBook
	 * @param title
	 */
	private void createSheetSubsidiaries(List<CategoryDTO> rawData , Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		List<String> headers = new ArrayList<String>();
		headers.add(SUBSIDIARY_TITLE);
		headers.add(GENERAL_RESULT_TITLE);
		headers.add(this.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.COMPETENCES));
		headers.add(RESULT_BY_COMPETENCE_TITLE);
		String[] headersNames = this.getHeaderLabels(headers);
		
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataSubsidiaries(rawData, rowNumber, sheetOne);
		
	}
	
	/**
	 * Crea la hoja de Competencias por cargo
	 * @param rawData
	 * @param workBook
	 * @param title
	 */
	private void createSheetJobs(List<Object[]> rawData , Workbook workBook, String title) {
		Sheet sheetOne = createBaseSheet(workBook, title);
		int rowNumber = 6;		
		Row rowHeader = sheetOne.createRow(rowNumber);
		List<String> headers = new ArrayList<String>();
		headers.add(JOB_TITLE);
		headers.add(this.getCustomLabel(companyId, ECustomReportsLabelsExcelTemplate.COMPETENCES));
		headers.add(RESULT_BY_COMPETENCE_TITLE);
		
		String[] headersNames = this.getHeaderLabels(headers);
		
		// Establece los headers
		for (int i = 0; i < headersNames.length; i++) {
			Cell cellHeader = rowHeader.createCell(i);
			cellHeader.setCellValue(headersNames[i]);
			cellHeader.setCellStyle(getStyleHeaders(workBook, sheetOne));
			// ancho para la columna actual fijo a 32 puntos
			sheetOne.setColumnWidth(i, 32 * 256);
		}
		
		setDataJobs(rawData, rowNumber, sheetOne);
		
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
	
	/**
	 * Método que inserta los datos en la hoja de comparación de evaluaciones
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataCompareEvaluations(List<CompareEvaluationsExcelDTO> data,int row,Sheet sheetOne) {
		
		
		if(data.get(0) != null) {
			for(ResultDTO res:data.get(0).getResults()) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				Cell cellNameEvaluationStringEva = newRow.createCell(celIdex);
				Cell cellCompetenceString = newRow.createCell(celIdex+1);
				Cell cellResultString = newRow.createCell(celIdex+2);
				cellNameEvaluationStringEva.setCellValue(data.get(0).getEvaluationName());
				cellCompetenceString.setCellValue(res.getName());
				cellResultString.setCellValue(df.format(res.getValue()));
				celIdex++;
			}
			
		}
		
		if(data.size() == 2) {
			for(ResultDTO res:data.get(1).getResults()) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				Cell cellNameEvaluationString = newRow.createCell(celIdex);
				Cell cellCompetenceString = newRow.createCell(celIdex+1);
				Cell cellResultString = newRow.createCell(celIdex+2);
				cellNameEvaluationString.setCellValue(data.get(1).getEvaluationName());
				cellCompetenceString.setCellValue(res.getName());
				cellResultString.setCellValue(df.format(res.getValue()));
				celIdex++;
			}
			
		}
		
	}
	
	/**
	 * Método que inserta los datos en la hoja de comparación de departamentos
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataCompareDivisions(List<CompetencesByDivisionsDTO> data,int row,Sheet sheetOne) {
		for(CompetencesByDivisionsDTO res:data) {
			for(ResultDTO result : res.getCompetences()) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				Cell cellNameDivisionString = newRow.createCell(celIdex);
				Cell cellCompetenceString = newRow.createCell(celIdex+1);
				Cell cellResultString = newRow.createCell(celIdex+2);
				cellNameDivisionString.setCellValue(res.getDivisionName());
				cellCompetenceString.setCellValue(result.getName());
				cellResultString.setCellValue(df.format(result.getValue()));
				celIdex++;
			}
		}
	}
	
	/**
	 * Método que inserta los datos en la hoja de mapa de calor
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataCompareDivisionsCategoryJob(List<CompetencesByDivisionsDTO> data,int row,Sheet sheetOne) {						
			for(CompetencesByDivisionsDTO res:data) {
				for(CategoryDTO category : res.getResults()) {
					for(ResultDTO result : category.getResults()) {
						row++;
						int celIdex = 0;
						Row newRow = sheetOne.createRow(row);
						Cell cellNameDivisionString = newRow.createCell(celIdex);
						Cell cellCategoryString = newRow.createCell(celIdex+1);
						Cell cellCompetenceString = newRow.createCell(celIdex+2);
						Cell cellResultString = newRow.createCell(celIdex+3);
						cellNameDivisionString.setCellValue(res.getDivisionName());
						cellCategoryString.setCellValue(category.getName());
						cellCompetenceString.setCellValue(result.getName());
						cellResultString.setCellValue(df.format(result.getValue()));
						celIdex++;
					}
				}
			}
		}

	/**
	 * Método que inserta los datos en la hoja de macrocompetencias
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataMacrcompetences(List<CategoryDTO> data,int row,Sheet sheetOne) {
		
		for(CategoryDTO res:data) {
			
			for(ResultDTO result : res.getResults()) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				if(res.getName() != null) {
					Cell cellNameMacrocompetence= newRow.createCell(celIdex);
					Cell cellResultMacroString = newRow.createCell(celIdex+1);
					Cell cellNameCompetenceString = newRow.createCell(celIdex+2);
					Cell cellResultCompetenceString = newRow.createCell(celIdex+3);
					cellNameMacrocompetence.setCellValue(res.getName());
					cellResultMacroString.setCellValue(df.format(res.getValue()));
					cellNameCompetenceString.setCellValue(result.getName());
					cellResultCompetenceString.setCellValue(df.format(result.getValue()));
					celIdex++;
				}else {
					Cell cellNameCompetenceString = newRow.createCell(celIdex);
					Cell cellResultCompetenceString = newRow.createCell(celIdex+1);
					cellNameCompetenceString.setCellValue(result.getName());
					cellResultCompetenceString.setCellValue(df.format(result.getValue()));
					celIdex++;
				}
				
			}
		}
	}
	
	/**
	 * Método que inserta los datos en la hoja de competencias por empleado
	 * @param list
	 * @param row
	 * @param sheetOne
	 */
	private void setDataEmployees(List<Object[]> list,int row,Sheet sheetOne, List<Object[]> extraFieldNames, List<Object[]> extraFields) {
		Company company = this.companyService.getCompanyById(this.companyId);
		
		Map<Long,Object[]> mapEmp = extraFields.stream().collect(Collectors.toMap(item -> getLong(item[0]), item -> item));
		
		for(Object[] data : list) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellPersonName= newRow.createCell(celIdex);
			Cell cellCompetenceName = newRow.createCell(celIdex+1);
			Cell cellBehaviorName = newRow.createCell(celIdex+2);
			Cell cellCompetenceValue = newRow.createCell(celIdex+3);
			Cell cellDivisionName = newRow.createCell(celIdex+4);
			Cell cellSubsidiaryName = newRow.createCell(celIdex+5);
			Cell cellJobLevelName = newRow.createCell(celIdex+6);
			Cell cellJobName = newRow.createCell(celIdex+7);

			cellPersonName.setCellValue(getString(data[1]));
			cellCompetenceName.setCellValue(getString(data[7]));
			cellBehaviorName.setCellValue(getString(data[9]));
			
			List<Cell> cellFields = new ArrayList<>();
			if (extraFieldNames.size() > 0) {
				for (int i = 0; i < extraFieldNames.size(); i++) {
					Cell cellField = newRow.createCell(celIdex+8 + i);
					cellFields.add(cellField);
				}				
			}

			if (company.getCompetencesResultFormat().equals("SCALE")) {
				cellCompetenceValue.setCellValue(df.format((Double) data[10]));
			} else {
				cellCompetenceValue.setCellValue(df.format((Double) data[11]));
			}
			

			cellDivisionName.setCellValue(getString(data[2]));
			cellSubsidiaryName.setCellValue(getString(data[4]));
			cellJobLevelName.setCellValue(getString(data[5]));
			cellJobName.setCellValue(getString(data[3]));
			if (cellFields.size() > 0 && extraFields.size()>0) {

				Object[] empExtra=mapEmp.get(((BigInteger)data[0]).longValue());

				for (int i = 0; i < extraFieldNames.size(); i++) {
					String field=empExtra!=null ?(String)(empExtra[i+1]): "";
					cellFields.get(i).setCellValue(field);
				}				
			}
			
			
			celIdex++;
		}
		

	}
	
	/**
	 * Método que inserta los datos en la hoja de competencias por sede
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataSubsidiaries(List<CategoryDTO> data,int row,Sheet sheetOne) {
		
		for(CategoryDTO result : data) {
			for(ResultDTO competence: result.getResults()) {
				row++;
				int celIdex = 0;
				Row newRow = sheetOne.createRow(row);
				Cell cellSubsidiaryName= newRow.createCell(celIdex);
				Cell cellAvg = newRow.createCell(celIdex+1);
				Cell cellCompetenceName = newRow.createCell(celIdex+2);
				Cell cellCompetenceValue = newRow.createCell(celIdex+3);
				cellSubsidiaryName.setCellValue(result.getName());
				cellAvg.setCellValue(result.getValue());
				cellCompetenceName.setCellValue(competence.getName());
				cellCompetenceValue.setCellValue(df.format(competence.getValue()));
				celIdex++;
			}
		}
	}
	
	/**
	 * Método que inserta los datos en la hoja de competencias por cargo
	 * @param data
	 * @param row
	 * @param sheetOne
	 */
	private void setDataJobs(List<Object[]> data,int row,Sheet sheetOne) {
		Company company = this.companyService.getCompanyById(this.companyId);
		for(Object[] result : data) {
			row++;
			int celIdex = 0;
			Row newRow = sheetOne.createRow(row);
			Cell cellJobName= newRow.createCell(celIdex);
			Cell cellCompetenceName = newRow.createCell(celIdex+1);
			Cell cellCompetenceValue = newRow.createCell(celIdex+2);
			cellJobName.setCellValue(getString(result[1]));
			cellCompetenceName.setCellValue(getString(result[3]));
			if (company.getCompetencesResultFormat().equals("SCALE")) {
				cellCompetenceValue.setCellValue(df.format((Double) result[4]));
			} else {
				cellCompetenceValue.setCellValue(df.format((Double) result[5]));
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
	
	public String getCustomLabel(Long companyId, ECustomReportsLabelsExcelTemplate module) {

		customLabels = companyService.getCustomLabels(companyId);
		if(module.equals(ECustomReportsLabelsExcelTemplate.COMPETENCES)) {
			return customLabels.get("labelCompetences");
		}
		
		if(module.equals(ECustomReportsLabelsExcelTemplate.GOALS)) {
			return customLabels.get("labelGoals");
		}
		
		if(module.equals(ECustomReportsLabelsExcelTemplate.OKRS)) {
			return customLabels.get("labelOkrs");
		}
		
		return "";
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
}
