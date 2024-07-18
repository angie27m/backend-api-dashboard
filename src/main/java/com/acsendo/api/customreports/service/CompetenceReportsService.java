package com.acsendo.api.customreports.service;

import static com.acsendo.api.util.DataObjectUtil.getDouble;
import static com.acsendo.api.util.DataObjectUtil.getLong;
import static com.acsendo.api.util.DataObjectUtil.getString;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.acsendo.api.company.repository.CompanyRepository;
import com.acsendo.api.competences.model.CommentCompetence;
import com.acsendo.api.competences.model.Questionnaire;
import com.acsendo.api.competences.repository.CommentCompetenceRepository;
import com.acsendo.api.competences.repository.CompetenceRepository;
import com.acsendo.api.competences.repository.Question2Repository;
import com.acsendo.api.competences.repository.QuestionnaireRepository;
import com.acsendo.api.customReports.dao.ResultsRedshiftDAO;
import com.acsendo.api.customreports.dto.BehaviorDetailDTO;
import com.acsendo.api.customreports.dto.CategoryDTO;
import com.acsendo.api.customreports.dto.CompareEvaluationsExcelDTO;
import com.acsendo.api.customreports.dto.CompetencesByDivisionsDTO;
import com.acsendo.api.customreports.dto.CompetencesEvaluationDTO;
import com.acsendo.api.customreports.dto.CompetencesExcelFiltersDTO;
import com.acsendo.api.customreports.dto.EmployeeResultDTO;
import com.acsendo.api.customreports.dto.EmployeeResultExcelDTO;
import com.acsendo.api.customreports.dto.FilterDTO;
import com.acsendo.api.customreports.dto.FiltersResultsDTO;
import com.acsendo.api.customreports.dto.QuestionCommentDTO;
import com.acsendo.api.customreports.dto.ResponseDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.dto.SemaphoreDTO;
import com.acsendo.api.customreports.util.CustomReportsCompetencesExcelHandler;
import com.acsendo.api.extrafield.dto.ExtraFieldValueFilterDTO;
import com.acsendo.api.extrafield.repository.ExtraFieldRepository;
import com.acsendo.api.hcm.dto.ConfigurationDTO;
import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.hcm.enumerations.EvaluationState2;
import com.acsendo.api.hcm.enumerations.EvaluationType;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Employee;
import com.acsendo.api.hcm.model.Evaluation;
import com.acsendo.api.hcm.model.LabelFlex;
import com.acsendo.api.hcm.repository.EmployeeRepository;
import com.acsendo.api.hcm.repository.EvaluationRepository;
import com.acsendo.api.hcm.repository.LabelFlexRepository;
import com.acsendo.api.hcm.service.CompanyService;
import com.acsendo.api.results.dao.PerformanceResultsDAO;
import com.acsendo.api.results.enumerations.CompetencesResultType;

@Service
public class CompetenceReportsService {

	@Autowired
	private ResultsRedshiftDAO redshiftDAO;
	
	@Autowired
	private PerformanceResultsDAO performanceResultDAO;

	@Autowired
	private EvaluationRepository evaluationRepository;
	
	@Autowired
	private CompetenceRepository competenceRepository;

	@Autowired
	private LabelFlexRepository labelFlexRepository;
	
	@Autowired
	private CompanyService companyService;

	@Autowired
	private CustomReportsCompetencesExcelHandler excelHandler;
	
	@Autowired
	private CommentCompetenceRepository commentCompetenceRepository;
	
	@Autowired
	private Question2Repository question2Repository;
	
	@Autowired
	private QuestionnaireRepository questionnaireRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private CompanyRepository companyRepository;
	
	@Autowired
	private ExtraFieldsEmployeeService extraFieldService;
	

	public static final String DIVISION_ID = "division_id";

	public static final String DIVISION_NAME = "division_name";
	
	public static final String COMPETENCE_ID = "competence_id";

	public static final String COMPETENCE_NAME = "competence_name_";
	
	public static final String SUBSIDIARY_ID = "subsidiary_id";

	public static final String SUBSIDIARY_NAME = "subsidiary_name";
	
	public static final String JOB_ID = "job_id";
	
	public static final String JOB_NAME = "job_name";
	
	DecimalFormat df = new DecimalFormat("0.00");

	/**
	 * Obtiene listado de evaluaciones de competencias de una empresa
	 * 
	 * @param companyId Identificador de la compañía
	 * @param subsidiariesAdmin Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 * @param employeeId   Identificador del empleado
	 * @param isLeader     Indica si el empleado es líder (true) o no (false)
	 * @return Listado de evaluaciones
	 */
	public List<CompetencesEvaluationDTO> getCompetencesEvaluationsByCompany(Long companyId, String subsidiariesAdmin, Long employeeId, Boolean isLeader) {
			
		List<CompetencesEvaluationDTO> evaluationsCompany = new ArrayList<CompetencesEvaluationDTO>();
		List<Object[]> resultList = redshiftDAO.getCompetencesEvaluationsByCompany(companyId, subsidiariesAdmin, employeeId, isLeader);		
		
		if (resultList != null && !resultList.isEmpty()) {			
			Function<Object[], CompetencesEvaluationDTO> mapper = res -> {
				CompetencesEvaluationDTO competenceDto = new CompetencesEvaluationDTO();
				competenceDto.setId(getLong(res[0]));				
				competenceDto.setName((String) res[1]);
				competenceDto.setStartDate((Date) res[2]);
				competenceDto.setFinalDate((Date) res[3]);
				competenceDto.setState(EvaluationState2.valueOf((String) res[4]));
				competenceDto.setResponseRate(getDouble(res[5]));
				competenceDto.setCompetenceModelId(getLong(res[6]));
				competenceDto.setSemaphore(getSemaphore(competenceDto.getId()));
				competenceDto.setCompLimit(getDouble(res[7]));
				competenceDto.setTypeCalibration((String) res[8]);
				competenceDto.setStateCalibrationEvaluation((String) res[9]);
				competenceDto.setViewCalibration((Boolean) res[10]);
				competenceDto.setShowSwitchResultsCalibration(competenceDto.getTypeCalibration() != null && !competenceDto.getTypeCalibration().equals("") && competenceDto.getStateCalibrationEvaluation() != null && competenceDto.getStateCalibrationEvaluation().equals("FINISHED") ? true : false);
				// Por defecto para reporteador se muestran todas las relaciones
				Boolean hideRelations=((Boolean) res[11]);
				competenceDto.setHideRelations(hideRelations!=null? hideRelations: false);
				return competenceDto;
			};
			evaluationsCompany = resultList.stream().map(mapper).collect(Collectors.toList());
		}
		
		redshiftDAO.createCompetenceCompanyTable(companyId);
		
		return evaluationsCompany;
	}

	/**
	 * Metodo que permite obtener los limites, colores y las etiquetas del semaforo
	 * de la evaluacion
	 * 
	 * @param companyId Identificador de la compañía
	 * @return Listado de DTO del semaforo
	 */
	public List<SemaphoreDTO> getSemaphore(Long evaluationId) {

		List<SemaphoreDTO> semaphoreEvaluation = new ArrayList<SemaphoreDTO>();
		Evaluation evaluation = evaluationRepository.getOne(evaluationId);
		String languageCode = evaluation.getCompany().getLanguageCode();

		// se verifica que el limite 1 tenga valor
		if (evaluation.getCompLimit1() != null) {
			addSemaphore(semaphoreEvaluation, evaluation.getCompLimit1(), evaluation.getCompLimit1Color(), languageCode,
					evaluationId, "_RED", "#e46053");
		}
		// al no tener un orden especifico se realiza esta operacion anterior con cada
		// uno de los 5 limites

		if (evaluation.getCompLimit2() != null) {
			addSemaphore(semaphoreEvaluation, evaluation.getCompLimit2(), evaluation.getCompLimit2Color(), languageCode,
					evaluationId, "_ORANGE", "#f2972a");
		}
		if (evaluation.getCompLimit3() != null) {
			addSemaphore(semaphoreEvaluation, evaluation.getCompLimit3(), evaluation.getCompLimit3Color(), languageCode,
					evaluationId, "_YELLOW", "#ffd93b");
		}
		if (evaluation.getCompLimit4() != null) {
			addSemaphore(semaphoreEvaluation, evaluation.getCompLimit4(), evaluation.getCompLimit4Color(), languageCode,
					evaluationId, "_GREEN", "#bdd262");
		}

		
		Boolean semaphoroInScale= evaluation.getCompany().getCompetencesResultFormat()!=null && evaluation.getCompany().getCompetencesResultFormat().equals("SCALE") &&
				semaphoreEvaluation.get(semaphoreEvaluation.size()-1).getCompLimit()<10;
			
		Double hasMaxLimit5= evaluation.getCompLimit5() != null ? evaluation.getCompLimit5(): 100D;
		Double limit =semaphoroInScale? evaluation.getComplimit() :  hasMaxLimit5;
		

		addSemaphore(semaphoreEvaluation, limit, evaluation.getCompLimit5Color(), languageCode, evaluationId, "_BLUE",
				"#36bcc2");
	
		// Ordena semáforo de límite menor a mayor
		List<SemaphoreDTO> semaphoreSorted = new ArrayList<SemaphoreDTO>();
		semaphoreSorted = semaphoreEvaluation.stream()
				.sorted((d1, d2) -> d1.getCompLimit().compareTo(d2.getCompLimit())).collect(Collectors.toList());

		return semaphoreSorted;

	}
	
	/**
	 * Agrega un intervalo con información del semáforo a la lista de todo el semaforo completo
	 */
	public void addSemaphore(List<SemaphoreDTO> list, Double limit, String limitColor, String language,
			Long evaluationID, String colorLetter, String colorCode) {

		SemaphoreDTO dataCompOne = new SemaphoreDTO();
		// se setea el valor del limite al intervalo
		dataCompOne.setCompLimit(limit);
		// se setea el label del intervalo
		LabelFlex intervalLabel = labelFlexRepository.findByLanguageCodeAndCode(language, evaluationID + colorLetter);
		if (intervalLabel != null && intervalLabel.getLabel() != null && !intervalLabel.getLabel().equals("")) {
			dataCompOne.setLabel(intervalLabel.getLabel());
		} else {
			dataCompOne.setLabel(labelFlexRepository.findByLanguageCodeAndCode(language, "Default" + colorLetter).getLabel());
		}
		
		// se verifica si hay color personalizado para este limite y lo setea al
		if (limitColor != null) {
			dataCompOne.setColor(limitColor);
		} else {
			dataCompOne.setColor(colorCode);
		}
		// se agrega a la lista de todos los limites
		list.add(dataCompOne);

	}

	/**
	 * Método que obtiene el promedio general de una evaluación de competencias
	 * 
	 * @param evaluationId Identificador de la evaluación de competencias
	 * @param subsidiariesAdmin Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 */
	public Double getAvgCompetences(Long evaluationId, String subsidiariesAdmin, Long employeeId, String relation, Boolean calibrated) {

		Double avg = 0.0;
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);

		if (optEvaluation.isPresent()) {
			avg = redshiftDAO.getAvgCompetencesByEvaluationRedshift(optEvaluation.get(), subsidiariesAdmin, employeeId, null, null, relation, calibrated, false);
		}
		return avg;
	}

	/**
	 * Obtiene las competencias de una evaluación con su respectivo promedio
	 * 
	 * @param evaluationId Identificador de la evaluación de competencias
	 * @param divisionId Identificador del departamento
	 * @param subsidiaryId Identificador de la sede
	 */
	public List<ResultDTO> getResultsGroupByCompetences(Long evaluationId, Long divisionId, Long subsidiaryId, Long jobId, Long competenceId, 
			String subsidiariesAdmin, Long employeeId, Boolean isLeader, Boolean calibrated) {

		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		List<ResultDTO> competences = new ArrayList<ResultDTO>();
		String language = optEvaluation.get().getCompany().getLanguageCode();

		if (optEvaluation.isPresent()) {
			List<Object[]> list = redshiftDAO.getResultsGroupByEntityRedshift(optEvaluation.get(), COMPETENCE_ID, COMPETENCE_NAME + language ,  divisionId, 
					subsidiaryId, Boolean.FALSE, competenceId, null, null, jobId, true, subsidiariesAdmin, employeeId, isLeader, null, calibrated);
			Function<Object[], ResultDTO> mapper = data -> {
				ResultDTO comp = new ResultDTO();
				comp.setId(getLong(data[0]));
				comp.setName(getString(data[1]));
				if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
					comp.setValue((Double)(data[2]));
				} else {
					comp.setValue((Double) (data[3]));
				}
				return comp;

			};
			competences = list.stream().map(mapper).collect(Collectors.toList());
			;
		}
		
		competences = competences.stream().sorted((c1, c2) -> c1.getId().compareTo(c2.getId())).collect(Collectors.toList());	
		
		return competences;
	}

	/**
	 * Obtiene el promedio de competencias de los líderes o niveles de cargode la compañía agrupado por departamentos, 
	 * tiene en cuenta los 3 posibles tipos de cálculo y devuelve el valor según tenga configurado la empresa por escala o porcentaje
	 * 
	 * @param evaluationId Identificador de la evaluación de competencias
	 * @param companyId Identificador de la compañia
	 * @param showLeaders Identifica si muestra resultados por líder (true) o por colaborador (false)
	 * @param subsidiariesAdmin Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 * @return Devuelve listado de departamentos con sus resultados
	 */
	public List<CompetencesByDivisionsDTO> getResultsCompetencesByDivisions(Long evaluationId, Long companyId, boolean showLeaders, String subsidiariesAdmin, Boolean calibrated) {
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		boolean isScale = optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE");
		List<CompetencesByDivisionsDTO> competencesResults = new ArrayList<CompetencesByDivisionsDTO>();
		List<Object[]> results = null;
		if(showLeaders) {
			results = redshiftDAO.getResultsCompetencesByLeaderRedshift(optEvaluation.get(), companyId, subsidiariesAdmin, calibrated);
		} else {
			results = redshiftDAO.getResultsCompetencesByJobLevelRedshift(optEvaluation.get(), companyId, subsidiariesAdmin, calibrated);
		}
		
		List<Object[]> divisionsCompetences=redshiftDAO.getResultsGroupByDivisionCompetenceRedshift(optEvaluation.get(), subsidiariesAdmin, calibrated);
		
		if (results != null && !results.isEmpty()) {
			List<Long> divisions = new ArrayList<Long>();
			results.stream().forEach(data -> {
				// Si no existe el departamento, se crea y guarda de una vez el elemento y competencia
				if (!divisions.contains(getLong(data[0]))) {
					CompetencesByDivisionsDTO objDivision = new CompetencesByDivisionsDTO();
					objDivision.setDivisionId(((BigInteger) data[0]).longValue());
					objDivision.setDivisionName((String) data[1]);
					List<CategoryDTO> listResults = new ArrayList<CategoryDTO>();
					CategoryDTO objEmp = new CategoryDTO();
					objEmp.setId(getLong(data[4]));
					objEmp.setName((String) data[5]);
					List<ResultDTO> listCompetences = new ArrayList<ResultDTO>();
					listCompetences.add(createNewCompetence(isScale, data));
					objEmp.setResults(listCompetences);
					listResults.add(objEmp);
					objDivision.setResults(listResults);
					
					
					List<Object[]> competences=divisionsCompetences.stream().filter(c -> getLong(c[0]).longValue() == objDivision.getDivisionId()).collect(Collectors.toList()); 
					List<ResultDTO> competCateg=competences.stream().map(c->newResult(getLong(c[2]), getString(c[3]), (Double)(c[4]), (Double)(c[5]),optEvaluation.get().getCompany().getCompetencesResultFormat()))
							.collect(Collectors.toList());
					objDivision.setCompetences(competCateg);
					competencesResults.add(objDivision);
					divisions.add(getLong(data[0]));
				} else {
					// Si ya existe el departamento, verifica que no exista el elemento para crearlo y agregar sus competencias
					Optional<CompetencesByDivisionsDTO> optDivision = competencesResults.stream()
							.filter(c -> c.getDivisionId() == ((BigInteger) data[0]).longValue()).findFirst();
					
					if (optDivision.isPresent()) {
						Optional<CategoryDTO> optElement = optDivision.get().getResults().stream()
						.filter(c -> c.getId() == ((BigInteger) data[4]).longValue()).findFirst();
						
						if (!optElement.isPresent()) {
							List<CategoryDTO> listResults = new ArrayList<CategoryDTO>();
							listResults.addAll(optDivision.get().getResults());
							CategoryDTO objEmp = new CategoryDTO();
							objEmp.setId(getLong(data[4]));
							objEmp.setName((String) data[5]);
							List<ResultDTO> listCompetences = new ArrayList<ResultDTO>();
							listCompetences.add(createNewCompetence(isScale, data));
							objEmp.setResults(listCompetences);
							listResults.add(objEmp);
							optDivision.get().setResults(listResults);
						} else {
							// Si ya existia el elemento, se le agrega su nueva competencia
							Optional<CategoryDTO> optResult = optDivision.get().getResults().stream()
									.filter(e -> e.getId() == ((BigInteger) data[4]).longValue()).findFirst();
							if(optResult.isPresent()) {
								List<ResultDTO> listCompetences = new ArrayList<ResultDTO>();
								listCompetences.addAll(optResult.get().getResults());								
								listCompetences.add(createNewCompetence(isScale, data));
								optResult.get().setResults(listCompetences);
							}							
						}

					}
				}
			});
		}
		return competencesResults;
	}	

	
	/**
	 * Crea objeto competencia con su respectiva información
	 * 
	 * @param isScale Indica si devuelve resultado por escala (true) o porcentaje (false)
	 * @param data Arreglo con información de la competencia
	 */
	private ResultDTO createNewCompetence(boolean isScale, Object[] data) {
		ResultDTO objComp = new ResultDTO();
		objComp.setId(getLong(data[2]));
		objComp.setName((String) data[3]);
		if (isScale) {
			objComp.setValue((Double) data[6]);
		} else {
			objComp.setValue((Double) data[7]);
		}
		return objComp;
	}
	
	
	/**
	 * Método que devuelve los departamentos con su respectivo promedio de resultados
	 * 
	 * @param evaluationId Identificador de la evaluación de competencias
	 */
	public List<ResultDTO> getResultsGroupByDivision(Long evaluationId, String subsidiariesAdmin, Boolean calibrated) {

		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		List<ResultDTO> competences = new ArrayList<ResultDTO>();

		if (optEvaluation.isPresent()) {
			List<Object[]> list = redshiftDAO.getResultsGroupByEntityRedshift(optEvaluation.get(), DIVISION_ID, DIVISION_NAME, null, null, Boolean.TRUE,null,null,null, null, false, 
					subsidiariesAdmin, null, null, null, calibrated);
			Function<Object[], ResultDTO> mapper = data -> {
				ResultDTO div = new ResultDTO();
				div.setId(getLong(data[0]));
				div.setName(getString(data[1]));
				if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
					div.setValue((Double)(data[2]));
				} else {
					div.setValue((Double) (data[3]));
				}
				return div;

			};
			competences = list.stream().map(mapper).collect(Collectors.toList());
			
		}

		return competences;
	}

	
	/**
	 * Obtiene las competencias de una evaluación con su respectivo promedio, agrupadas por macrocompetencia
	 * 
	 * @param evaluationId Identificador de la evaluación de competencias
	 * @param employeeId Identificador del empleado
	 * @param subsidiariesAdmin Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 */
	public List<CategoryDTO> getCompetencesResultByMacrocompetence(Long evaluationId, Long employeeId, Long divisionId, String subsidiariesAdmin, Boolean calibrated) {
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		List<CategoryDTO> macrocompetences = new ArrayList<CategoryDTO>();

		if (optEvaluation.isPresent()) {
			List<Object[]> list = redshiftDAO.getCompetencesResultByMacrocompetence(optEvaluation.get(), employeeId, divisionId, subsidiariesAdmin, calibrated);
			List<String> mcomp = new ArrayList<String>();			
			list.stream().forEach(data -> {
				if (!mcomp.contains(getString(data[4]))) {
					CategoryDTO comp = new CategoryDTO();
					comp.setName(getString(data[4]));
					ResultDTO objComp = new ResultDTO();
					List<ResultDTO> listCompetences = new ArrayList<ResultDTO>();
					objComp.setId(getLong(data[0]));
					objComp.setName(getString(data[1]));
					if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
						objComp.setValue((Double)(data[2]));
					} else {
						objComp.setValue((Double) (data[3]));
					}
					listCompetences.add(objComp);
					comp.setResults(listCompetences);
					macrocompetences.add(comp);
					mcomp.add(getString(data[4]));
				} else {
					Optional<CategoryDTO> optMacrocomp;
					if (data[4] == null) {
						optMacrocomp = macrocompetences.stream().findFirst();
					} else {
						optMacrocomp = macrocompetences.stream()
								.filter(c -> c.getName().equals((String) data[4])).findFirst();
					}
					
					List<ResultDTO> listCompetences = new ArrayList<ResultDTO>();
					if (optMacrocomp.get().getResults() != null) {
						listCompetences.addAll(optMacrocomp.get().getResults());
					}						
					ResultDTO objComp = new ResultDTO();
					objComp.setId(getLong(data[0]));
					objComp.setName(getString(data[1]));
					if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
						objComp.setValue((Double)(data[2]));
					} else {
						objComp.setValue((Double) (data[3]));
					}
					listCompetences.add(objComp);
					optMacrocomp.get().setResults(listCompetences);
				}	
			});
		}
		
		//Obtiene promedio de las macrocompetencias
		macrocompetences.stream().forEach(mc -> {
			Double avg = mc.getResults().stream().mapToDouble(data -> data.getValue()).average().orElse(0);
			mc.setValue(avg);
		});		 

		return macrocompetences;
	}
	
	
	/**
	 * Método que devuelve las sedes con su respectivo promedio de resultados
	 * 
	 * @param evaluationId Identificador de la evaluación de competencias
	 */
	public List<ResultDTO> getResultsGroupBySubsidiaries(Long evaluationId, String subsidiariesAdmin, Boolean calibrated) {
		//Consulta evaluación de competencias
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		List<ResultDTO> competences = new ArrayList<ResultDTO>();

		if (optEvaluation.isPresent()) {
			List<Object[]> list = redshiftDAO.getResultsGroupByEntityRedshift(optEvaluation.get(), SUBSIDIARY_ID, SUBSIDIARY_NAME, null, null, Boolean.TRUE,null,null,null, null, false, 
					subsidiariesAdmin, null, null, null, calibrated);
			Function<Object[], ResultDTO> mapper = data -> {
				ResultDTO div = new ResultDTO();
				div.setId(getLong(data[0]));
				div.setName(getString(data[1]));
				if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
					div.setValue((Double)(data[2]));
				} else {
					div.setValue((Double) (data[3]));
				}
				return div;

			};
			competences = list.stream().map(mapper).collect(Collectors.toList());
			
		}

		return competences;
	}
	
	/**
	 * Método que devuelve la información del componente de comparación de evaluaciones
	 * @param companyId Identificador de la compañia
	 * @param selectedEvaluationId Identificador de la evaluación principal
	 * @param comparedEvaluationId Identificador de la evaluación a comparar
	 * @return
	 */
	public List<CompareEvaluationsExcelDTO> getCompareEvaluationsData(Long companyId, Long selectedEvaluationId, Long comparedEvaluationId, String subsidiariesAdmin, Boolean calibrated) {
		
		Optional<Evaluation> selectedEvaluation = evaluationRepository.findById(selectedEvaluationId);
		Optional<Evaluation> comparedEvaluation = evaluationRepository.findById(comparedEvaluationId);
		
		CompareEvaluationsExcelDTO resultSelected = new CompareEvaluationsExcelDTO();
		CompareEvaluationsExcelDTO resultCompared = new CompareEvaluationsExcelDTO();
		
		List<CompareEvaluationsExcelDTO> result = new ArrayList<CompareEvaluationsExcelDTO>();
		
		if(selectedEvaluation.isPresent()) {
			List<ResultDTO> selectedEvaluationCompetences = this.getResultsGroupByCompetences(selectedEvaluationId, null, null, null, null, 
					subsidiariesAdmin, null, null, calibrated);
			resultSelected.setEvaluationName(selectedEvaluation.get().getName());
			resultSelected.setResults(selectedEvaluationCompetences);
			result.add(resultSelected);
		}
		
	
		if(comparedEvaluation.isPresent()) {
			List<ResultDTO> comparedEvaluationCompetences = this.getResultsGroupByCompetences(comparedEvaluationId, null, null, null, null, 
					subsidiariesAdmin, null, null, calibrated);
			resultCompared.setEvaluationName(comparedEvaluation.get().getName());
			resultCompared.setResults(comparedEvaluationCompetences);
			result.add(resultCompared);
		}
		
		return result;
	}
	
	/**
	 * Método que devuelve la información del componente de comparación de departamentos
	 * @param companyId Identificador de la compañia
	 * @param evaluationId Identificador de la evalución seleccionada
	 * @param divi1 Identificador del primer departamento seleccionado
	 * @param divi2 Identificador del segundo departamento seleccionado
	 * @param divi3 Identificador del tercer departamento seleccionado
	 * @param isFiltered Indica si el reporte se genera a partir de los filtros
	 * @param subsidiariesAdmin Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 */
	public List<CompetencesByDivisionsDTO> getCompareDivisionsData(Long companyId, Long evaluationId, Long divi1, Long divi2, Long divi3, boolean isFiltered, String subsidiariesAdmin, Boolean calibrated) {
		
		List<CompetencesByDivisionsDTO> result = new ArrayList<CompetencesByDivisionsDTO>();
		
		List<CompetencesByDivisionsDTO> competences = this.getResultsCompetencesByDivisions(evaluationId, companyId, false, subsidiariesAdmin, calibrated);
		
		if(isFiltered) {
			for(CompetencesByDivisionsDTO temp : competences) {
				if(temp.getDivisionId().equals(divi1) || temp.getDivisionId().equals(divi2) || temp.getDivisionId().equals(divi3)) {
					result.add(temp);
				}
			}
			return result;
		}
		
		return competences;
		
	}
	
	/**
	 * Método que devuelve la información del componente del heatmap
	 * @param companyId Identificador de la compañia
	 * @param evaluationId Identificador de la evaluación seleccionada
	 * @param isByCategory Indica si se filtra por categoría de cargo o por líderes
	 */
	public List<CompetencesByDivisionsDTO> getCompareDivisionsDataHeatMap(Long companyId, Long evaluationId, boolean  isByCategory, String subsidiariesAdmin, Boolean calibrated) {
			
		List<CompetencesByDivisionsDTO> competences = new ArrayList<CompetencesByDivisionsDTO>();
		
		if(!isByCategory) {
			competences = this.getResultsCompetencesByDivisions(evaluationId, companyId, true, subsidiariesAdmin, calibrated);
		}
		
		if(isByCategory) {
			competences = this.getResultsCompetencesByDivisions(evaluationId, companyId, false, subsidiariesAdmin, calibrated);
		}
		
		return competences;
	}

	
	/**
	 * Método que obtiene todos los datos para generar la sabana de datos
	 * 
	 * @param companyId Identificador de la compañia
	 * @param evaluationId Identificador de la evaluación
	 * @param filters filtros con los parametros para generar los datos 
	 */
	public byte[] getCompetencesExcelReport(Long companyId, Long evaluationId, CompetencesExcelFiltersDTO filters, String subsidiariesAdmin, Boolean calibrated) {
		
	
		 List<CompareEvaluationsExcelDTO> compareEvaluationsData = this.getCompareEvaluationsData(companyId,evaluationId, filters.getComparedEvaluationId(), subsidiariesAdmin, calibrated);
		 List<CompetencesByDivisionsDTO> compareDivisionsData = this.getCompareDivisionsData(companyId, evaluationId, filters.getFirstDivisionId(), filters.getSecondDivisionId(), filters.getThirdDivisionId(), filters.isFiltered(), subsidiariesAdmin, calibrated);
		 List<CompetencesByDivisionsDTO> compareDivisionsCategoryJobsData = this.getCompareDivisionsDataHeatMap(companyId, evaluationId, filters.isByCategory(), subsidiariesAdmin, calibrated);
		 List<CategoryDTO> macrocompetencesData = this.getCompetencesResultByMacrocompetence(evaluationId, null, null, subsidiariesAdmin, calibrated);
	
		 List<Object[]> employeeData = this.getCompetencesResultByMacrocompetenceExcel(evaluationId, filters.getEmployeeCompetencesDivisionId(), subsidiariesAdmin, calibrated);
		
		 List<CategoryDTO> subsidiariesData = this.getResultsGroupBySubsidiariesExcelReport(evaluationId, filters.getFirstSubsidiaryId(), filters.getSecondSubsidiaryId(), filters.getThirdSubsidiaryId(),filters.isFiltered(), subsidiariesAdmin, calibrated);

		 Long divisionJobId=filters.isFiltered()?filters.getDivisionIdJobFilter():null;
		 Long competenceFilterId=filters.isFiltered()? filters.getCompetenceId():null;
		 List<Object[]> jobData = this.getCompetencesResultGroupByJobExcel(evaluationId, divisionJobId, competenceFilterId, subsidiariesAdmin, calibrated);
		
	
		 List<Object[]> headersName = redshiftDAO.getExtraFieldsNamesByCompany(companyId);
	
		 List<Object[]> extraFields =new ArrayList<Object[]>();
		 if(headersName.size()>0) {
		    extraFields = redshiftDAO.getExtraFieldsGroupByEmployee(companyId, headersName.size());

		 }
		return this.excelHandler.getCompetencesExcelReport(compareEvaluationsData, compareDivisionsData , compareDivisionsCategoryJobsData, macrocompetencesData, employeeData, subsidiariesData, jobData, companyId, filters.isByCategory(), headersName, extraFields);		
	}
	
	
	/**
	 * Devuelve el id del report template de la empresa
	 * 
	 * @param companyId Identificador de la compañía
	 */
	public Long getReportTemplateByCompnay(Long companyId) {
		return this.companyService.getCompanyById(companyId).getReporttemplate();
	}


	/**
	 * Obtiene promedio de resultados por empleados para una evaluación de competencias
	 * 
	 * @param companyId Identificador de la compañía
	 * @param evaluationId Identificador de la evaluación de competencias
	 * @param pageable Contiene información de paginación
	 * @param divisionId Identificador del departamento
	 * @param employeeName Nombre del empleado
	 * @param subsidiariesAdmin Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 */
	@SuppressWarnings("unchecked")
	public <T>PageableResponse<T>  getResultsGroupByEmployee(Long companyId, Long evaluationId, Pageable pageable, Long divisionId,
			String employeeName, String subsidiariesAdmin, Boolean calibrated) {
		
		PageableResponse<T> pages = null;
		Integer maxResults = null;
		Integer startIndex = null;		
		if (pageable != null) {
			maxResults = pageable.getPageSize();
			startIndex = pageable.getPageSize() * pageable.getPageNumber();
		}
		
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);	
		if (optEvaluation.isPresent()) {
			List<EmployeeResultDTO> employeeList = new ArrayList<EmployeeResultDTO>();
			List<Object[]> list = redshiftDAO.getResultsGroupByEmployee(companyId, optEvaluation.get(), divisionId, employeeName, startIndex, maxResults, subsidiariesAdmin, null, null, calibrated, null, null);
			
			
			Function<Object[], EmployeeResultDTO> mapper = data -> {
				EmployeeResultDTO result = new EmployeeResultDTO();
				result.setEmployeeId(getLong(data[0]));
				result.setEmployeeName(getString(data[1]));
				result.setDivisionId(getLong(data[2]));
				result.setDivisionName(getString(data[3]));
				result.setJobId(getLong(data[4]));
				result.setJobName(getString(data[5]));				
				if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
					result.setValue((Double) data[6]);
				} else {
					result.setValue((Double) data[7]);
				}
				return result;
			};
			employeeList = list.stream().map(mapper).collect(Collectors.toList());
			pages = new PageableResponse<T>();
			pages.setElements((List<T>) employeeList);
			pages.setTotal(redshiftDAO.getCountResultsGroupByEmployee(companyId, optEvaluation.get(), divisionId, employeeName, subsidiariesAdmin, calibrated));			
		}

		return pages;
	}
	
	/**
	 * Devuelve el un resultDTO que contiene el cargo y su porcentaje
	 * @param companyId Identificador de la compañía
	 * @param evaluationId Identificador de la evaluación de competencias
	 * @param pageable Contiene información de paginación
	 * @param divisionId Identificador del departamento
	 * @param competenceId Identificador de la competencia 
	 * @param subsidiariesAdmin Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 */
	@SuppressWarnings("unchecked")
	public <T> PageableResponse<T> getCompetencesByJob(Long companyId, Long evaluationId, Pageable pageable,
			Long divisionId, Long competenceId, String subsidiariesAdmin, Boolean calibrated) {
		
		PageableResponse<T> pages = null;
		Integer maxResults = null;
		Integer startIndex = null;		
		if (pageable != null) {
			maxResults = pageable.getPageSize();
			startIndex = pageable.getPageSize() * pageable.getPageNumber();
		}
		
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);	
		if (optEvaluation.isPresent()) {
			List<ResultDTO> jobsList = new ArrayList<ResultDTO>();
			List<Object[]> list = redshiftDAO.getResultsGroupByEntityRedshift(optEvaluation.get(),JOB_ID,JOB_NAME,divisionId,null,Boolean.FALSE,competenceId,startIndex,maxResults, null,false,
					subsidiariesAdmin, null, null, null, calibrated);
			Function<Object[], ResultDTO> mapper = data -> {
				ResultDTO result = new ResultDTO();
				result.setId(getLong(data[0]));
				result.setName(getString(data[1]));
				if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
					result.setValue((Double) data[2]);
				} else {
					result.setValue((Double) data[3]);
				}
				return result;
			};
			jobsList = list.stream().map(mapper).collect(Collectors.toList());
				
			pages = new PageableResponse<T>();
			pages.setElements((List<T>) jobsList);
			pages.setTotal(redshiftDAO.getResultsGroupByEntityRedshift(optEvaluation.get(),JOB_ID,JOB_NAME,divisionId,null,Boolean.FALSE,competenceId,null,null,null,false,
					subsidiariesAdmin, null, null, null, calibrated).size());			

		}

		return pages;
	
	}
	
	/**
	 * Obtiene información que se va a mostrar en excel de la evaluación de competencias
	 * @param companyId Identificador de la compañía
	 * @param evaluationId Identificador de la evaluación de competencias
	 * @param subsidiariesAdmin Lista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 */
	public List<EmployeeResultExcelDTO>  getResultsGroupByEmployeeForExcelReport(Long companyId, Long evaluationId, String subsidiariesAdmin, Boolean calibrated) {
		
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		List<EmployeeResultExcelDTO> employeeList = new ArrayList<EmployeeResultExcelDTO>();
		
		if (optEvaluation.isPresent()) {
			List<Object[]> list = redshiftDAO.getResultsGroupByEmployee(companyId, optEvaluation.get(), null, null, null, null, subsidiariesAdmin, null, null, calibrated, null, null);
			
			Function<Object[], EmployeeResultExcelDTO> mapper = data -> {
				EmployeeResultExcelDTO result = new EmployeeResultExcelDTO();
				result.setEmployeeId(getLong(data[0]));
				result.setEmployeeName(getString(data[1]));
				result.setDivisionId(getLong(data[2]));
				result.setDivisionName(getString(data[3]));
				result.setJobId(getLong(data[4]));
				result.setJobName(getString(data[5]));
				result.setResults(this.getCompetencesResultByMacrocompetence(evaluationId, result.getEmployeeId(), null, subsidiariesAdmin, calibrated));
				if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
					result.setValue((Double) data[6]);
				} else {
					result.setValue((Double) data[7]);
				}
				return result;
			};
			employeeList = list.stream().map(mapper).collect(Collectors.toList());
		}
		

		return employeeList;
	}
	
	/**
	 * Método que devuelve la información del componente de competencias por sede
	 * 
	 * @param evaluationId Identificador de la evaluación seleccionada
	 * @param firstSubsidiaryId Identificador de la primer sede seleccionada
	 * @param secondSubsidiaryId Identificador de la segunda sede seleccionada
	 * @param thirdSubsidiaryId Identificador de la tercer sede seleccionada
	 * @param isFiltered Indica si la información se va a devolver filtrada
	 * @param subsidiariesAdminLista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 */
	public List<CategoryDTO> getResultsGroupBySubsidiariesExcelReport(Long evaluationId, Long firstSubsidiaryId, Long secondSubsidiaryId, Long thirdSubsidiaryId, boolean isFiltered, String subsidiariesAdmin, Boolean calibrated) {
		//Consulta evaluación de competencias
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		List<CategoryDTO> competences = new ArrayList<CategoryDTO>();


		if (optEvaluation.isPresent()) {	
			List<Object[]> list = redshiftDAO.getResultsGroupByEntityRedshift(optEvaluation.get(), SUBSIDIARY_ID, SUBSIDIARY_NAME, null, null, Boolean.TRUE,null,null,null, null,false,
					subsidiariesAdmin, null, null, null, calibrated);
			
			if(!subsidiariesAdmin.equals("null") && subsidiariesAdmin.isEmpty() && isFiltered && firstSubsidiaryId!=null) {
				 subsidiariesAdmin=firstSubsidiaryId.toString();
				if(secondSubsidiaryId!=null) {
					subsidiariesAdmin=","+secondSubsidiaryId.toString();
				}
				
				if(thirdSubsidiaryId!=null) {
					subsidiariesAdmin=","+ thirdSubsidiaryId.toString();
				}
			}
			List<Object[]> competencesBySub=redshiftDAO.getResultsGroupBySubsidiariesExcel(optEvaluation.get(), subsidiariesAdmin, calibrated);
			Function<Object[], CategoryDTO> mapper = data -> {
				CategoryDTO div = new CategoryDTO();
				div.setId(getLong(data[0]));
				div.setName(getString(data[1]));
				if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
					div.setValue((Double)(data[2]));
				} else {
					div.setValue((Double) (data[3]));
				}
				
				List<Object[]> subFiltered=competencesBySub.stream().filter(s->getLong(s[0]).equals(div.getId())).collect(Collectors.toList());
				
				List<ResultDTO> result = new ArrayList<ResultDTO>();
				if(subFiltered!=null) {
					subFiltered.stream().forEach(compt->{
						Double res=0.0;
						if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
						  res=(Double)(compt[3]);
						}else {
							res=(Double)(compt[4]);
						}
						ResultDTO dto=new ResultDTO(getLong(compt[1]), getString(compt[2]), res);
						result.add(dto);
					});
				}
				div.setResults(result);
				return div;

			};
			competences = list.stream().map(mapper).collect(Collectors.toList());

		}
		return competences;
		
	}
	
	/**
	 * Devuelve los datos del componente de competencias por colaborador
	 * 
	 * @param evaluationId Identificador de la evaluación seleccionada
	 * @param divisionId Identificador del filtro de división
	 * @param subsidiariesAdminLista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 */
	public List<Object[]> getCompetencesResultByMacrocompetenceExcel(Long evaluationId, Long divisionId, String subsidiariesAdmin, Boolean calibrated) {
		
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);

		if (optEvaluation.isPresent()) {
			List<Object[]> list = redshiftDAO.getBehaviorsEmployeeResultByExcel(optEvaluation.get(), divisionId, subsidiariesAdmin, calibrated, null, null);
			return list;
		}
		return null;
	}
	
	/**
	 * Método que devuelve la información del componente de competencias por cargo 
	 * 
	 * @param evaluationId Identificador de la evaluación
	 * @param divisionId Identificador del departamento seleccionado
	 * @param competenceId Identificador de la competencia seleccionada
	 * @param subsidiariesAdminLista de sedes a la que hace parte un rol subadmin con filtro de sedes especifico
	 */
	public List<Object[]> getCompetencesResultGroupByJobExcel(Long evaluationId, Long divisionId, Long competenceId, String subsidiariesAdmin, Boolean calibrated) {
		
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);	
		if (optEvaluation.isPresent()) {
			List<Object[]> list = redshiftDAO.getResultsGroupByJobExcel(optEvaluation.get(), divisionId, competenceId, subsidiariesAdmin, calibrated);
			
			return list;
		}
		
		return null;
		
	}
	
	
	private ResultDTO newResult(Long id, String name, Double scale,Double percentage, String type) {
		
		ResultDTO dto=new ResultDTO();
		dto.setId(id);
		dto.setName(name);
		if(type.equals("SCALE")) {
			dto.setValue(scale);
		}else {
			dto.setValue(percentage);
		}
		
		return dto;
	}
	
	
	
	/**
	 * Método que ejecuta procedimiento almacenado para volver a calcular resultados de una evaluación de competencias
	 * 
	 * */
	public void executeStoredProcedureCompetences(Long companyId, Long evaluationId) {
		
		redshiftDAO.executeStoredProcedureCompetences(companyId, evaluationId);
	}
	
	
	/**
	 * Obtiene listado de módulos activos de una empresa
	 * 
	 * @param companyId Identificador de la compañía
	 */
	public Map<String, String> getActiveModulesByCompany(Long companyId) {

		Map<String, String> mapModules = new HashMap<String, String>();
		Object[] modulesPerformance = redshiftDAO.getPerformanceModulesByCompany(companyId);
		Object[] modulesEngagement = redshiftDAO.getEngagementModulesByCompany(companyId);
		// Consulta módulos de desempeño
		if (modulesPerformance.length > 0) {
			StringBuffer perf = new StringBuffer();
			for (int x = 0; x < modulesPerformance.length; x++) {
				if (modulesPerformance[x] != null) {		
					// Valida que tenga activo el módulo de OKR's para listarlo
					if (modulesPerformance[x].equals("OKRS")) {
						if (companyRepository.hasConfigurationCompany(companyId, 211L).isPresent()) {
							perf = perf.append(modulesPerformance[x]);
						}							
					} else {
						perf = perf.append(modulesPerformance[x]);
					}
				}
				if (!perf.toString().isEmpty() && x < modulesPerformance.length - 1) {
					perf = perf.append(",");
				}
			}
			if (perf.length() > 0) {
				if (perf.lastIndexOf(",") == perf.length() - 1)
					perf.deleteCharAt(perf.length() - 1);
				mapModules.put("performance", perf.toString());
			}
		}
		// Consulta módulos de engagement
		if (modulesEngagement.length > 0) {
			StringBuffer engmnt = new StringBuffer();
			for (int x = 0; x < modulesEngagement.length; x++) {
				if (modulesEngagement[x] != null) {
					engmnt = engmnt.append(modulesEngagement[x]);
				}
				if (!engmnt.toString().isEmpty() && x < modulesEngagement.length - 1) {
					engmnt = engmnt.append(",");
				}
			}
			if (engmnt.length() > 0) {
				if (engmnt.lastIndexOf(",") == engmnt.length() - 1)
					engmnt.deleteCharAt(engmnt.length() - 1);
				mapModules.put("engagement", engmnt.toString());
			}
		}
		return mapModules;
	}

	/**
	 * Método que devuelve el detalle de las competencias y relaciones de una
	 * evaluación de un colaborador
	 * 
	 * @param companyId    Identificador de la compañía
	 * @param evaluationId Identificador de la evaluación
	 * @param employeeId   Identificador del empleado
	 */
	public Map<String, String> getDetailCollaborator(Long companyId, Long evaluationId, Long employeeId) {

		Map<String, String> map = new HashMap<String, String>();

		List<Object[]> relations = redshiftDAO.getDetailCollaborator(companyId, evaluationId, employeeId);

		if (relations.size() > 0) {
			map.put("competences", ((BigInteger) (relations.get(0)[1])).toString());
			String listString = relations.stream().map(obj -> getString(obj[0])).collect(Collectors.joining(", "));

			map.put("relations", listString);
		}

		return map;

	}

	/**
	 * Obtiene las competencias del colaborador con su respectivo promedio paginadas
	 * 
	 * @param companyId    Identificador de la compañía
	 * @param evaluationId Identificador de la evaluación
	 * @param employeeId   Identificador del empleado
	 * @param pageable     Paginador
	 * @param isLeader     Indica si el empleado es líder (true) o no (false)
	 * @param orderType    Ordenamiento ASC o DESC
	 * @param weight 
	 */
	@SuppressWarnings("unchecked")
	public <T> PageableResponse<T> getCompetencesCollaborator(Long companyId, Long evaluationId, Long employeeId,
			Pageable pageable, Boolean isLeader, String orderType, String relation,Boolean calibrated, Boolean weight) {

		PageableResponse<T> pages = null;
		Integer maxResults = null;
		Integer startIndex = null;
		if (pageable != null) {
			maxResults = pageable.getPageSize();
			startIndex = pageable.getPageSize() * pageable.getPageNumber();
		}
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		List<CategoryDTO> competences = new ArrayList<CategoryDTO>();
		String language = optEvaluation.get().getCompany().getLanguageCode();

		if (optEvaluation.isPresent()) {
			List<Object[]> list = redshiftDAO.getResultsGroupByEntityRedshift(optEvaluation.get(), COMPETENCE_ID,
					COMPETENCE_NAME + language, null, null, Boolean.FALSE, null, startIndex, maxResults, null, true,
					"null", employeeId, isLeader, orderType, relation,calibrated);
			
			Map<Long, Double> map = getWeightCompetences(optEvaluation, language);
			
			if(list!=null && list.size()>0) {
			
				//Buscamos los comentarios de las competencias consultadas
				List<Long> competencesId=list.stream().map(obj-> (getLong(obj[0]))).collect(Collectors.toList());
				Optional<List<CommentCompetence>> comments = findcomment(evaluationId, employeeId, relation,
						competencesId); 
						
				Function<Object[], CategoryDTO> mapper = data -> {
					CategoryDTO comp = new CategoryDTO();
					comp.setId(getLong(data[0]));
					comp.setName(getString(data[1]));
					if (optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")) {
						comp.setValue((Double) (data[2]));
					} else {
						comp.setValue((Double) (data[3]));
					}
					if(map != null) {
						comp.setWeight(map.get(comp.getId()));
					}
					if(comments.isPresent()) {
						List<CommentCompetence> commentCompetence=comments.get().stream().filter(c-> c.getCompetence().getId()==comp.getId()).collect(Collectors.toList());
						List<ResultDTO> commentRes=commentCompetence.stream().map(c->new ResultDTO(c.getId(), c.getComments())).collect(Collectors.toList());
						comp.setResults(commentRes);
					}
					return comp;
	
				};
				
				competences = list.stream().map(mapper).collect(Collectors.toList());
			}	
		}

		pages = new PageableResponse<T>();
		pages.setElements((List<T>) competences);
		pages.setTotal(getResultsGroupByCompetences(evaluationId, null, null, null, null, "null", employeeId, isLeader, calibrated)
				.size());
		return pages;
	}

	private Map<Long, Double> getWeightCompetences(Optional<Evaluation> optEvaluation, String language) {
		Map<Long,Double> map = new HashMap<Long, Double>();
		CompetencesResultType type = performanceResultDAO.getCompetencesResultType(optEvaluation.get().getCompany(),
				optEvaluation.get());
		if(!type.equals(CompetencesResultType.COMPETENCE_WEIGHT) && !type.equals(CompetencesResultType.AVERAGE_COMPETENCE_RELATION_WEIGHT)) {
			return map;
		}

		List<String[]> weigtsByCompetence = competenceRepository.getServiceModelFullLight(optEvaluation.get().getCompetence_model().getId(), language).get();
		
		for (String[] weigtByCompetence : weigtsByCompetence) {
			map.put(Long.valueOf(weigtByCompetence[4]) , Double.valueOf(weigtByCompetence[3]) );
		}
		return map;
	}

	private Optional<List<CommentCompetence>> findcomment(Long evaluationId, Long employeeId, String relation,
			List<Long> competencesId) {
		if(relation != null && !relation.equals("") && !relation.equals("null")) {
			return commentCompetenceRepository.findByRelation(competencesId, evaluationId, employeeId,relation);
		}
		return commentCompetenceRepository.findByCompetenceId(competencesId, evaluationId, employeeId);
	}

	/**
	 * Obtiene los comportamientos de una competencia con sus respectivas relaciones
	 * y resultado
	 * 
	 * @param companyId    Identificador de la compañía
	 * @param evaluationId Identificador de la evaluación
	 * @param employeeId   Identificador del empleado
	 * @param competenceId Identificador de la competencia 
	 * @param calibrated 
	 * @param relation 
	 */
	public List<BehaviorDetailDTO> getBehaviorsDetailByCompetence(Long companyId, Long evaluationId, Long employeeId,
			Long competenceId, Boolean calibrated) {

		List<BehaviorDetailDTO> behaviors = new ArrayList<BehaviorDetailDTO>();
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		Boolean isScale = optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE");

		if (optEvaluation.isPresent()) {
			// consulta relaciones de la evaluación
			List<Object[]> detail = redshiftDAO.getDetailCollaborator(companyId, evaluationId, employeeId);
			List<String> relations = detail.stream().map(r -> (String) r[0]).collect(Collectors.toList());

			List<Object[]> list = redshiftDAO.getBehaviorsDetailByCompetence(optEvaluation.get(), employeeId,
					competenceId,calibrated);
			
			List<Object[]> totalBehaviors=redshiftDAO.getBehaviorsEmployeeResultByExcel(optEvaluation.get(), null, null, calibrated, employeeId, competenceId);
			
			if(list!=null && list.size()>0) {
				
				List<Long> behaviorsId=list.stream().map(obj-> (getLong(obj[0]))).collect(Collectors.toList());
				
				List<Object[]> comments=question2Repository.findCommentsQuestion(behaviorsId, evaluationId, employeeId);
				
				totalBehaviors.stream().forEach(behavior->{
					
					BehaviorDetailDTO dto = new BehaviorDetailDTO();
					BigInteger id = (BigInteger) (behavior[8]);
					dto.setId(id.intValue());
					dto.setBehavior(getString(behavior[9]));
					if (isScale) {
						Double total=(Double) (behavior[10]);
						dto.setTotal(total!=null? total:0.0);
					} else {
						Double total=(Double) (behavior[11]);
						dto.setTotal(total!=null? total:0.0);
					}
					
					// se agregan relaciones
					Map<String, String> map = new HashMap<String, String>();
					for (String rel : relations) {
						Optional<Object[]> foundRelation = list.stream()
								.filter(bh -> bh[2].equals(rel) && getLong(bh[0]).equals(getLong(behavior[8]))).findFirst();
						if (foundRelation.isPresent()) {
							if (isScale) {
								map.put(rel, foundRelation.get()[3] == null ? "N/A" : df.format((Double) foundRelation.get()[3]));
							} else {
								map.put(rel, foundRelation.get()[4] == null ? "N/A" : df.format((Double) foundRelation.get()[4]));
							}
						} else {
							map.put(rel, "N/A");
						}
					}
					dto.setRelations(map);
					
					if(comments != null) {
						List<Object[]> commentsBehavior=comments.stream().filter(b->(((Long)b[0]).longValue()==((BigInteger) behavior[0]).longValue())).collect(Collectors.toList());
						List<FilterDTO> commentRes=commentsBehavior.stream().map(b->new FilterDTO((Long)(b[0]), getString(b[2]))).collect(Collectors.toList());
						dto.setComments(commentRes);
					}					
					behaviors.add(dto);
					
				});
			

			}
		}
		return behaviors;
	}
	
	
	
	/**
	 * Método que valida las configuraciones para mostrar resultados a un colaborador
	 * 
	 * */
	public Map<String, Boolean> getCompetenceResultsConfiguration(Long companyId){
		
		Map<String, Boolean> config=new HashMap<String, Boolean>();
		
		Company company=companyService.getCompanyById(companyId);
			
		Date today=new Date();
		Boolean dateFrom=false;
		if(company.getDatefrom()!=null) {
			dateFrom=today.after(company.getDatefrom());
		}
		config.put("dateFrom", dateFrom);
		
		ConfigurationDTO dto=companyService.getCompanyConfiguration(companyId, 83L, company.getLanguageCode());
		config.put("showResults", dto==null);
		

		return config;
		
	}
	
	
	/**
	 *  Obtiene los labels de los comentarios generales con los respectivos comentarios realizados al colaborador
	 *  
	 * */
	public List<QuestionCommentDTO> getGeneralSuggestionsByEvaluationAndEmployee(Long companyId, Long evaluationId, Long employeeId, String relation){
		
		
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationId);
		List<QuestionCommentDTO> generalComments=new ArrayList<QuestionCommentDTO>();
		
		if(optEvaluation.isPresent()) {
			Evaluation evaluation=optEvaluation.get();
			List<String> relations=new ArrayList<String>();
			if(relation==null) {
					// consulta relaciones de la evaluación
				List<Object[]> detail = redshiftDAO.getDetailCollaborator(companyId, evaluationId, employeeId);
				relations = detail.stream().map(r -> (String) r[0]).collect(Collectors.toList());
				}else {
					relations.add(relation);
				}
				
				if(relations.size()>0) {
				List<Questionnaire> questionnaires=questionnaireRepository.getQuestionnairesByEvaluationAndEvaluated(evaluationId, employeeId, relations);
				
				String language=evaluation.getCompany().getLanguageCode();
				Employee employee=employeeRepository.getOne(employeeId);
				
				ConfigurationDTO cdto=companyService.getCompanyConfiguration(companyId, 77L,language);
				//Preguntamos si la evaluación tiene la configuración multiidiomas, si es así debe tener en cuenta el idioma del empleado, sino el de la empresa
				if(cdto!=null && employee.getPerson().getLanguage() != null ) {
					language=employee.getPerson().getLanguage();
				}
				
				List<String> suggestions=new ArrayList<String>();
				List<String> suggestions2=new ArrayList<String>();
				List<String> suggestions3=new ArrayList<String>();
				
				questionnaires.stream().forEach(questionnaire->{
					
					if(questionnaire.getSuggestions()!=null && !questionnaire.getSuggestions().trim().isEmpty()) {
						suggestions.add(questionnaire.getSuggestions());
					}
					if(questionnaire.getSuggestions2()!=null && !questionnaire.getSuggestions2().trim().isEmpty()) {
						suggestions2.add(questionnaire.getSuggestions2());
					}
					if(questionnaire.getSuggestions3()!=null && !questionnaire.getSuggestions3().trim().isEmpty()) {
						suggestions3.add(questionnaire.getSuggestions3());
					}
					
				});
				
				if(evaluation.getGeneralSuggestion()!=null && evaluation.getGeneralSuggestion()) {
					String labelSuggestion=findLabelComments(evaluation,"GENERAL_SUGGESTION",language);
					//Angular
					QuestionCommentDTO dto=new QuestionCommentDTO(labelSuggestion, suggestions);
					generalComments.add(dto);
	
				}
				
				if(evaluation.getGeneralSuggestion2()!=null && evaluation.getGeneralSuggestion2()) {
					String labelSuggestion2=findLabelComments(evaluation,"ADITIONAL_SUGGESTION",language);
					QuestionCommentDTO dto=new QuestionCommentDTO(labelSuggestion2, suggestions2);
					generalComments.add(dto);	
				}
				
				if(evaluation.getGeneralSuggestion3()!=null && evaluation.getGeneralSuggestion3()) {
					String labelSuggestion3=findLabelComments(evaluation,"ADITIONAL_SUGGESTION_THREE_BOX",language);
					QuestionCommentDTO dto=new QuestionCommentDTO(labelSuggestion3, suggestions3);
	
					generalComments.add(dto);
				}
			}
			
		}
		
		return generalComments;
		
	}
	
	
	/**
	 * Método que busca los labels para un tipo de comentario especifico (General o los adicionales) u otro label
	 **/
	private String findLabelComments(Evaluation evaluation, String commentType, String languageCode) {
			
			String evaluationType = EvaluationType.THREESIXTY.toString();

			String code = evaluationType.toString().concat("_").concat(evaluation.getId()+"_").concat(commentType);
			LabelFlex label=labelFlexRepository.findByLanguageCodeAndCode(languageCode, code);
			
			if(label==null) {
				code =  evaluationType.concat("_DEFAULT_").concat(commentType);
				label=labelFlexRepository.findByLanguageCodeAndCode(languageCode, code);
			}
			
		 return label.getLabel();
	}

	/**
	 * Obtiene id de la plantilla para reporte pdf de competencias
	 * 
	 * @param companyId    Identificador de la compañía
	 * @param evaluationId Identificador de evaluación de competencias
	 */
	public Long getPdfReportTemplate(Long companyId, Long evaluationId) {
		Long templateId = 0L;
		templateId = redshiftDAO.getPdfCompetencesReportTemplate(evaluationId);
		return templateId;
	}
	
	
	
	
	/**
	 * Método que devuelve el semaforo con el total de personas que tienen el puntaje de cada item
	 * 
	 * @param companyId Identificador de la empresa
	 * @param filters Filtros 
	 * */
	public List<ResponseDTO> getEmployeesBySempahore(Long companyId, FiltersResultsDTO filters){
		
		List<ResponseDTO> responses= new ArrayList<ResponseDTO>();
		
		
		Optional<Evaluation> optEvaluation=evaluationRepository.findById(filters.getEvaluationId());
		
		if(optEvaluation.isPresent()) {
			List<SemaphoreDTO> semaphore=getSemaphore(filters.getEvaluationId());
			
			extraFieldService.validateLevelAndJobFilters(filters);
			//VAlida los empleados que coinciden con los filtros extras
			Map <Long, Long> employeesIdsWithExtraFields= extraFieldService.validateExtraFieldsEmployeesFilter(filters, companyId);
			String employeesIds=null;

			List<Object[]> list=new ArrayList<Object[]>();
			if(employeesIdsWithExtraFields!=null && !employeesIdsWithExtraFields.isEmpty()) {
				employeesIds=employeesIdsWithExtraFields.entrySet().stream().map(m->m.getKey().toString()).collect(Collectors.joining(","));
				 list  = redshiftDAO.getResultsGroupByEmployeesWithExtraFields(optEvaluation.get().getCompany().getId(), 
							optEvaluation.get(), filters.getDivisionId(),filters.getSubsidiariesAdmin(), employeesIds, filters.getCalibrated(), filters.getLevelId(), filters.getJobName());
			}else if (filters.getExtraField()==null || (filters.getLevelId()!= null || filters.getJobName()!=null)) {
				 list  = redshiftDAO.getResultsGroupByEmployeesWithExtraFields(optEvaluation.get().getCompany().getId(), 
							optEvaluation.get(), filters.getDivisionId(),filters.getSubsidiariesAdmin(), employeesIds, filters.getCalibrated(), filters.getLevelId(), filters.getJobName());
			}
			
		    //Validamos si la evaluacion es por escala se valida que el semaforo tambien esté en escala, si no tomamos el porcentaje
			int position=optEvaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE") && semaphore.get(semaphore.size()-1).getCompLimit()< 100 ? 6:7;
			
		    //Variable para tomar la posición del anterior semáforo para saber el limite inferior
		    Integer i=0;
			for(SemaphoreDTO s: semaphore) {
				
				Double lastComplimit= i==0? -1: semaphore.get(i-1).getCompLimit();
				List<Object[]> employees=list.stream().filter(l-> (Double)l[position]<= s.getCompLimit() 
						&& ( (Double) l[position]> lastComplimit)).collect(Collectors.toList());
				
				i=i+1;
				ResponseDTO res=new ResponseDTO();
				res.setLabel(s.getLabel());
				res.setCountResponses(employees!=null ? employees.size() : 0);
				Double percentage=employees!=null && list!=null && list.size()>0 ? (employees.size() *100.00)/ list.size() : 0.0;
				res.setPercentage(percentage);
				responses.add(res);
			}
		
		}
		
		return responses;

	}
	

	
	
}
