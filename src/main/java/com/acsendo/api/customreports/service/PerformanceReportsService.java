package com.acsendo.api.customreports.service;

import static com.acsendo.api.util.DataObjectUtil.getDouble;
import static com.acsendo.api.util.DataObjectUtil.getLong;
import static com.acsendo.api.util.DataObjectUtil.getString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acsendo.api.company.repository.CompanyRepository;
import com.acsendo.api.customReports.dao.PerformanceDynamicResultsDAO;
import com.acsendo.api.customReports.dao.ResultsRedshiftDAO;
import com.acsendo.api.customReports.dto.EmployeePerformanceDTO;
import com.acsendo.api.customReports.dto.PerformanceProcessEmployeeDTO;
import com.acsendo.api.customReports.enumerations.PerformanceModule;
import com.acsendo.api.customReports.model.PerformancePeriod;
import com.acsendo.api.customReports.model.PerformancePeriodDetail;
import com.acsendo.api.customReports.repository.PerformancePeriodDetailRepository;
import com.acsendo.api.customReports.repository.PerformancePeriodRepository;
import com.acsendo.api.customReports.util.PerformanceFilter;
import com.acsendo.api.customreports.dto.FiltersResultsDTO;
import com.acsendo.api.customreports.dto.LabelBoxDTO;
import com.acsendo.api.customreports.dto.ModulePerformanceDTO;
import com.acsendo.api.customreports.dto.NineBoxResultDTO;
import com.acsendo.api.customreports.dto.PerformanceCategoryDTO;
import com.acsendo.api.customreports.dto.PerformanceDTO;
import com.acsendo.api.customreports.dto.PerformanceDetailEmployeeDTO;
import com.acsendo.api.customreports.dto.PerformanceFiltersDTO;
import com.acsendo.api.customreports.dto.PeriodDTO;
import com.acsendo.api.customreports.dto.PeriodDetailDTO;
import com.acsendo.api.customreports.dto.ProcessDTO;
import com.acsendo.api.customreports.dto.QuadrantNineBoxInfoDTO;
import com.acsendo.api.customreports.dto.SemaphoreDTO;
import com.acsendo.api.customreports.util.CustomReportsPerformanceExcelHandler;
import com.acsendo.api.extrafield.repository.ExtraFieldRepository;
import com.acsendo.api.goals.model.GoalPeriod;
import com.acsendo.api.goals.repository.GoalPeriodRepository;
import com.acsendo.api.hcm.dto.PageableResponse;
import com.acsendo.api.hcm.enumerations.EntityState;
import com.acsendo.api.hcm.enumerations.PerformanceComponentType;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.ConfigurationNinebox;
import com.acsendo.api.hcm.model.Division;
import com.acsendo.api.hcm.model.Employee;
import com.acsendo.api.hcm.model.Evaluation;
import com.acsendo.api.hcm.model.JobRole;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.model.LabelFlex;
import com.acsendo.api.hcm.repository.ConfigurationNineboxRepository;
import com.acsendo.api.hcm.repository.DivisionRepository;
import com.acsendo.api.hcm.repository.EmployeeRepository;
import com.acsendo.api.hcm.repository.EvaluationRepository;
import com.acsendo.api.hcm.repository.JobRoleRepository;
import com.acsendo.api.hcm.repository.LabelFlexRepository;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.hcm.service.CompanyService;
import com.acsendo.api.okrs.dao.OKRProcessDAO;
import com.acsendo.api.okrs.model.OKRPeriod;
import com.acsendo.api.okrs.repository.OKRPeriodRepository;
import com.acsendo.api.okrs.repository.ObjectiveKeyResultRepository;
import com.acsendo.api.pid.model.DevelopIndividualPlan;
import com.acsendo.api.pid.repository.DevelopIndividualPlanRepository;
import com.acsendo.api.pid.repository.DipEmployeeRepository;
import com.acsendo.api.results.dao.PerformanceResultsDAO;

@Service
public class PerformanceReportsService {

	@Autowired
	private GoalReportsService goalService;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private LabelFlexRepository labelRepository;

	@Autowired
	private ConfigurationNineboxRepository configurationNineboxRepository;

	@Autowired
	private DivisionRepository divisionrepository;

	@Autowired
	private EvaluationRepository evaluationRepository;

	@Autowired
	private ResultsRedshiftDAO redshiftDAO;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private GoalPeriodRepository goalRepository;

	@Autowired
	private ObjectiveKeyResultRepository okrRepository;

	@Autowired
	private PerformancePeriodRepository performancePeriodRepository;

	@Autowired
	private PerformancePeriodDetailRepository performancePeriodDetailRepository;

	@Autowired
	private PerformanceResultsDAO performanceResultDAO;

	@Autowired
	private JobRoleRepository jobRoleRepository;

	@Autowired
	private OKRPeriodRepository okrPeriodRepository;

	@Autowired
	private OKRProcessDAO okrProcessDAO;

	@Autowired
	private ClimateReportsService climateService;

	@Autowired
	private LabelRepository labelAngularRepository;

	@Autowired
	private DevelopIndividualPlanRepository developIndividualPlanRepository;

	@Autowired
	private DipEmployeeRepository dipEmployeeRepository;

	@Autowired
	private PerformanceDynamicResultsDAO performanceDynamicResultsDAO;
	
	@Autowired
	private ClimateReportsService climateReportsService;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private ExtraFieldRepository extraFieldRepository;
	
	@Autowired
	private CustomReportsPerformanceExcelHandler excelHandler;
	
	
	@Autowired
	private ExtraFieldsEmployeeService extraFieldService;

	/*
	 * Contiene promedio de una categoría de cargo en sus diferentes procesos
	 */
	Double categoryAvg = 0.0;

	/**
	 * Ignora los label de metas y competencias
	 */
	public static final Long IGNORE_LABELS_COMPETENCE_GOAL_NINEBOX = 205L;
	
	/**
	 * Listado de ids empleados a cargo del líder
	 */
	List<Long> collaboratorIds = null;
	
	

	/**
	 * Método que contiene información del semáforo de resultados y el porcentaje de
	 * resultados de metas y de competencias
	 * 
	 * @param companyId Identificador de la empresa
	 * @return PerfomanceDTO
	 */
	public PerformanceDTO getPerformanceConfiguration(Long companyId) {

		Optional<Company> optcompany = companyRepository.findById(companyId);

		PerformanceDTO dto = new PerformanceDTO();
		if (optcompany.isPresent()) {
			
			redshiftDAO.createCompetenceCompanyTable(companyId);
			redshiftDAO.createGoalCompanyTable(companyId);
			
			Company company = optcompany.get();
			dto.setWeightCompetences(company.getCompetencespercentage());
			dto.setWeightGoalsOkrs(company.getGoalspercentage());
			dto.setSemaphore(getSemaphore(company));
			dto.setCompetencesFormat(company.getCompetencesResultFormat());
			Optional<Boolean> hasConfig = companyRepository.hasConfigurationCompany(companyId, 251L);
			if (hasConfig.isPresent()) {
				dto.setGoalsFormat("SCALE");
			} else {
				dto.setGoalsFormat("PERCENTAGE");
			}

			if (company.getLimit5() == null) {
				if (company.getCompetencesResultFormat() != "PERCENTAGE") {
					dto.setLimit(5D);
				} else {
					dto.setLimit(100D);
				}
			} else {
				dto.setLimit(company.getLimit5().doubleValue());
			}
			dto.setEvaluationSelectedId(company.getCompetencesevaluation());
			dto.setGoalPeriodId(company.getGoalsperiod());
		}
		return dto;

	}

	/**
	 * Obtiene listado de los intervalos que componene el semaforo de resultados de
	 * la empresa
	 * 
	 * @param compan Entidad con la información de la empresa
	 * @return Listado de los intervalos
	 */
	public List<SemaphoreDTO> getSemaphore(Company company) {

		// cambiar nombre de DTO en caso de tener lo mismo
		List<SemaphoreDTO> semaphore = new ArrayList<SemaphoreDTO>();
		String languageCode = company.getLanguageCode();

		// se verifica que el limite 1 tenga valor
		if (company.getLimit1() != null) {
			goalService.addSemaphore(semaphore, company.getLimit1(), company.getLimit1_color(), languageCode,
					company.getId(), "_RED", "#e46053", true);
		}

		// al no tener un orden especifico se realiza esta operacion anterior con cada
		// uno de los 5 limites
		if (company.getLimit2() != null) {
			goalService.addSemaphore(semaphore, company.getLimit2(), company.getLimit2_color(), languageCode,
					company.getId(), "_ORANGE", "#f2972a", true);
		}
		if (company.getLimit3() != null) {
			goalService.addSemaphore(semaphore, company.getLimit3(), company.getLimit3_color(), languageCode,
					company.getId(), "_YELLOW", "#ffd93b", true);
		}
		if (company.getLimit4() != null) {
			goalService.addSemaphore(semaphore, company.getLimit4(), company.getLimit4_color(), languageCode,
					company.getId(), "_GREEN", "#bdd262", true);
		}

		BigDecimal limit = company.getLimit5() != null ? company.getLimit5() : new BigDecimal(100);
		goalService.addSemaphore(semaphore, limit, company.getLimit5_color(), languageCode, company.getId(), "_BLUE",
				"#36bcc2", true);

		// Ordena semáforo de límite menor a mayor
		List<SemaphoreDTO> semaphoreSorted = new ArrayList<SemaphoreDTO>();
		semaphoreSorted = semaphore.stream().sorted((d1, d2) -> d1.getCompLimit().compareTo(d2.getCompLimit()))
				.collect(Collectors.toList());

		return semaphoreSorted;
	}

	@SuppressWarnings("unused")
	public LabelBoxDTO getLabelsAndConfigurationsBoxNinebox(Long companyId, Boolean isOkrs) {

		LabelBoxDTO boxDto = new LabelBoxDTO();

		Company company = companyRepository.getOne(companyId);

		String lang = company.getLanguageCode();

		Optional<Boolean> hasConfig = companyRepository.hasConfigurationCompany(companyId,
				IGNORE_LABELS_COMPETENCE_GOAL_NINEBOX);

		String competencesLabelName = getLabel(("COMPETENCE_KEY_LABEL_" + company.getId()), lang);
		if (competencesLabelName.equals("COMPETENCE_KEY_LABEL_" + company.getId()) || hasConfig.isPresent()) {
			competencesLabelName = getLabel("competences", lang);
		}

		String goalsOkrsLabelName = "";
		String performanceTypeGoalOKRS = "";
		String labelGoalOkrs = "";
		if ((isOkrs!=null && !isOkrs) || (isOkrs==null && company.getGoalsperiod()!=null)) {
			goalsOkrsLabelName = getLabel(("GOAL_KEY_LABEL_" + company.getId()), lang);
			if (goalsOkrsLabelName.equals("GOAL_KEY_LABEL_" + company.getId()) || hasConfig.isPresent()) {
				goalsOkrsLabelName = getLabel("goals", lang);
			}
			performanceTypeGoalOKRS = PerformanceComponentType.GOALS.toString();
			labelGoalOkrs = "goals";
		} else {
			goalsOkrsLabelName = getLabel("okrs", lang);
			performanceTypeGoalOKRS = PerformanceComponentType.OKRS.toString();
			labelGoalOkrs = "okrs";
		}

		ConfigurationNinebox config = configurationNineboxRepository.findTop1ByCompanyIdOrderByIdDesc(companyId);
		boolean xCompetence = config.getX().equals(PerformanceComponentType.COMPETENCES);
		boolean yCompetence = config.getY().equals(PerformanceComponentType.COMPETENCES);

		// Para saber cual modula está en el X y en eje Y
		boxDto.setTypeAxisX(xCompetence ? PerformanceComponentType.COMPETENCES.toString() : performanceTypeGoalOKRS);
		boxDto.setTypeAxisY(yCompetence ? PerformanceComponentType.COMPETENCES.toString() : performanceTypeGoalOKRS);

		// SET titleAxisX
		boxDto.setTitleAxisX(getLabel(companyId + "_ninebox_title_axis_x", lang));
		if (boxDto.getTitleAxisX().equals(companyId + "_ninebox_title_axis_x") || boxDto.getTitleAxisX().equals("")) {
			boxDto.setTitleAxisX(xCompetence ? competencesLabelName : goalsOkrsLabelName);
		}
		boxDto.setLabelX(xCompetence ? competencesLabelName : goalsOkrsLabelName);

		// SET titleAxisY
		boxDto.setTitleAxisY(getLabel(companyId + "_ninebox_title_axis_y", lang));
		if (boxDto.getTitleAxisY().equals(companyId + "_ninebox_title_axis_y") || boxDto.getTitleAxisY().equals("")) {
			boxDto.setTitleAxisY(yCompetence ? competencesLabelName : goalsOkrsLabelName);
		}
		boxDto.setLabelY(yCompetence ? competencesLabelName : goalsOkrsLabelName);

		// SET titleAxisX0X1
		String titleAxisX0X1 = companyId + "_ninebox_title_axis_x0_x1";
		boxDto.setTitleAxisX0X1(getLabel(titleAxisX0X1, lang));
		if (boxDto.getTitleAxisX0X1().equals(titleAxisX0X1) || boxDto.getTitleAxisX0X1().equals("")) {
			boxDto.setTitleAxisX0X1(getLabel("ninebox_low_level", lang));
		}
		// SET titleAxisX1X2
		String titleAxisX1X2 = companyId + "_ninebox_title_axis_x1_x2";
		boxDto.setTitleAxisX1X2(getLabel(titleAxisX1X2, lang));
		if (boxDto.getTitleAxisX1X2().equals(titleAxisX1X2) || boxDto.getTitleAxisX1X2().equals("")) {
			boxDto.setTitleAxisX1X2(getLabel("ninebox_medium_level", lang));
		}
		// SET titleAxisX2X3
		String titleAxisX2X3 = companyId + "_ninebox_title_axis_x2_x3";
		boxDto.setTitleAxisX2X3(getLabel(titleAxisX2X3, lang));
		if (boxDto.getTitleAxisX2X3().equals(titleAxisX2X3) || boxDto.getTitleAxisX2X3().equals("")) {
			boxDto.setTitleAxisX2X3(getLabel("ninebox_high_level", lang));
		}
		// SET titleAxisX3X4
		String titleAxisX3X4 = companyId + "_ninebox_title_axis_x3_x4";
		boxDto.setTitleAxisX3X4(getLabel(titleAxisX3X4, lang));
		if (boxDto.getTitleAxisX3X4().equals(titleAxisX3X4) || boxDto.getTitleAxisX3X4().equals("")) {
			boxDto.setTitleAxisX3X4(getLabel("ninebox_very_high_level", lang));
		}

		// SET titleAxisY0Y1
		String titleAxisY0Y1 = companyId + "_ninebox_title_axis_y0_y1";
		boxDto.setTitleAxisY0Y1(getLabel(titleAxisY0Y1, lang));
		if (boxDto.getTitleAxisY0Y1().equals(titleAxisY0Y1) || boxDto.getTitleAxisY0Y1().equals("")) {
			boxDto.setTitleAxisY0Y1(getLabel("ninebox_low_level", lang));
		}
		// SET titleAxisY1Y2
		String titleAxisY1Y2 = companyId + "_ninebox_title_axis_y1_y2";
		boxDto.setTitleAxisY1Y2(getLabel(titleAxisY1Y2, lang));
		if (boxDto.getTitleAxisY1Y2().equals(titleAxisY1Y2) || boxDto.getTitleAxisY1Y2().equals("")) {
			boxDto.setTitleAxisY1Y2(getLabel("ninebox_medium_level", lang));
		}
		// SET titleAxisY2Y3
		String titleAxisY2Y3 = companyId + "_ninebox_title_axis_y2_y3";
		boxDto.setTitleAxisY2Y3(getLabel(titleAxisY2Y3, lang));
		if (boxDto.getTitleAxisY2Y3().equals(titleAxisY2Y3) || boxDto.getTitleAxisY2Y3().equals("")) {
			boxDto.setTitleAxisY2Y3(getLabel("ninebox_high_level", lang));
		}
		// SET titleAxisY3Y4
		String titleAxisY3Y4 = companyId + "_ninebox_title_axis_y3_y4";
		boxDto.setTitleAxisY3Y4(getLabel(titleAxisY3Y4, lang));
		if (boxDto.getTitleAxisY3Y4().equals(titleAxisY3Y4) || boxDto.getTitleAxisY3Y4().equals("")) {
			boxDto.setTitleAxisY3Y4(getLabel("ninebox_very_high_level", lang));
		}

		// Inicializar la información del cuadrante
		initInfoQuadrant(company, boxDto);

		boxDto.setNineboxType(company.getNine_box_type());
		boxDto.setNineboxDescription(company.getNine_box_description());
		boxDto.setAxisx1(company.getAxisx1());
		boxDto.setAxisx2(company.getAxisx2());
		boxDto.setAxisx3(company.getAxisx3());
		boxDto.setAxisx4(company.getAxisx4());
		boxDto.setAxisy1(company.getAxisy1());
		boxDto.setAxisy2(company.getAxisy2());
		boxDto.setAxisy3(company.getAxisy3());
		boxDto.setAxisy4(company.getAxisy4());
		boxDto.setNineboxTitle(getLabel("ninebox_title", lang));
		boxDto.setNineboxInfoTextnoEmployee(getLabel("divisions_without_results_9box", lang));
		boxDto.setNineboxInitialInfoText(getLabel("nine_box_initial_info_text", lang));

		// LLena la información del cuadrante como Json
		getStringJsonInfoQuadrant(boxDto);

		return boxDto;

	}

	private void initInfoQuadrant(Company company, LabelBoxDTO boxDto) {
		String nameLabelAxis = "";
		String descLabelAxis = "";
		QuadrantNineBoxInfoDTO objectQuand = null;

		boxDto.setInfoQuadrant(new ArrayList<QuadrantNineBoxInfoDTO>((int) Math.pow(company.getNine_box_type(), 2)));

		for (int i = 0; i < (int) Math.pow(company.getNine_box_type(), 2); i++) {

			objectQuand = new QuadrantNineBoxInfoDTO();

			// SET NOMBRE
			nameLabelAxis = getLabel(company.getId() + "_QUADRANT_" + (i + 1), company.getLanguageCode());
			if (nameLabelAxis.equals(company.getId() + "_QUADRANT_" + (i + 1))) {
				nameLabelAxis = getLabel("QUADRANT_" + infoTextDefaultQuadrant(company.getNine_box_type(), (i + 1)),
						company.getLanguageCode());
			}
			objectQuand.setQuadrantName(nameLabelAxis);

			// SET DESC
			descLabelAxis = getLabel(company.getId() + "_DESC_QUADRANT_" + (i + 1), company.getLanguageCode());
			if (descLabelAxis.equals(company.getId() + "_DESC_QUADRANT_" + (i + 1))) {
				descLabelAxis = getLabel(
						"DESC_QUADRANT_" + infoTextDefaultQuadrant(company.getNine_box_type(), (i + 1)),
						company.getLanguageCode());
			}
			objectQuand.setQuadrantDescription(descLabelAxis);

			// SET COLOR
			objectQuand.setQuadrantColor(infoColorDefaultQuadrant(company.getNine_box_type(), (i + 1)));
			boxDto.getInfoQuadrant().add(objectQuand);
		}
	}

	private String getLabel(String code, String lang) {

		LabelFlex label = labelRepository.findByLanguageCodeAndCode(lang, code);

		if (label != null) {
			return label.getLabel();
		}
		return "";
	}

	private String infoTextDefaultQuadrant(int nineBoxType, int quadrant) {
		String result = "";

		if (nineBoxType == 3) {
			if (quadrant == 1) {
				result = "CONFIABLE";
			} else if (quadrant == 2) {
				result = "IMPACTO";
			} else if (quadrant == 3) {
				result = "ESTRELLA";
			} else if (quadrant == 4) {
				result = "EFECTIVO";
			} else if (quadrant == 5) {
				result = "CLAVE";
			} else if (quadrant == 6) {
				result = "DESARROLLAR";
			} else if (quadrant == 7) {
				result = "RIESGO";
			} else if (quadrant == 8) {
				result = "DILEMA";
			} else if (quadrant == 9) {
				result = "ENIGMA";
			}
		} else if (nineBoxType == 4) {
			if (quadrant == 1) {
				result = "CONFIABLE";
			} else if (quadrant == 2) {
				result = "CONFIABLE";
			} else if (quadrant == 3) {
				result = "IMPACTO";
			} else if (quadrant == 4) {
				result = "ESTRELLA";
			} else if (quadrant == 5) {
				result = "CONFIABLE";
			} else if (quadrant == 6) {
				result = "CLAVE";
			} else if (quadrant == 7) {
				result = "CLAVE";
			} else if (quadrant == 8) {
				result = "DESARROLLAR";
			} else if (quadrant == 9) {
				result = "EFECTIVO";
			} else if (quadrant == 10) {
				result = "CLAVE";
			} else if (quadrant == 11) {
				result = "CLAVE";
			} else if (quadrant == 12) {
				result = "ENIGMA";
			} else if (quadrant == 13) {
				result = "RIESGO";
			} else if (quadrant == 14) {
				result = "DILEMA";
			} else if (quadrant == 15) {
				result = "ENIGMA";
			} else if (quadrant == 16) {
				result = "ENIGMA";
			}
		}

		return result;
	}

	private String infoColorDefaultQuadrant(int nineBoxType, int quadrant) {
		String result = "";

		if (nineBoxType == 3) {
			if (quadrant == 1) {
				result = "color3";
			} else if (quadrant == 2) {
				result = "color5";
			} else if (quadrant == 3) {
				result = "color6";
			} else if (quadrant == 4) {
				result = "color2";
			} else if (quadrant == 5) {
				result = "color4";
			} else if (quadrant == 6) {
				result = "color5";
			} else if (quadrant == 7) {
				result = "color1";
			} else if (quadrant == 8) {
				result = "color2";
			} else if (quadrant == 9) {
				result = "color3";
			}
		} else if (nineBoxType == 4) {
			if (quadrant == 1) {
				result = "color3";
			} else if (quadrant == 2) {
				result = "color3";
			} else if (quadrant == 3) {
				result = "color5";
			} else if (quadrant == 4) {
				result = "color6";
			} else if (quadrant == 5) {
				result = "color3";
			} else if (quadrant == 6) {
				result = "color4";
			} else if (quadrant == 7) {
				result = "color4";
			} else if (quadrant == 8) {
				result = "color5";
			} else if (quadrant == 9) {
				result = "color2";
			} else if (quadrant == 10) {
				result = "color4";
			} else if (quadrant == 11) {
				result = "color4";
			} else if (quadrant == 12) {
				result = "color3";
			} else if (quadrant == 13) {
				result = "color1";
			} else if (quadrant == 14) {
				result = "color2";
			} else if (quadrant == 15) {
				result = "color3";
			} else if (quadrant == 16) {
				result = "color3";
			}
		}

		return result;
	}

	/**
	 * Método que obtiene el Json con la información de los cuadrantes
	 * 
	 * @param boxDto
	 */
	private void getStringJsonInfoQuadrant(LabelBoxDTO boxDto) {

		String result = "[";

		for (int i = 0; i < boxDto.getInfoQuadrant().size(); i++) {

			result = result + "{";
			result = result + "name: \"" + boxDto.getInfoQuadrant().get(i).getQuadrantName() + "\", ";
			result = result + "desc: \"" + boxDto.getInfoQuadrant().get(i).getQuadrantDescription() + "\", ";
			result = result + "backgroundColor: \"" + boxDto.getInfoQuadrant().get(i).getQuadrantColor() + "\"";

			if (i == boxDto.getInfoQuadrant().size() - 1) {
				result = result + "}";
			} else {
				result = result + "},";
			}
		}
		result = result + "]";

		boxDto.setInfoQuadrantStringJson(result.replaceAll("(\n|\r)", " "));

	}

	/**
	 * Método que obtiene los Resultados de metas y competencias por departamento
	 * para Ninebox
	 * 
	 * @param companyId    Identificador de la empresa
	 * @param evaluationId Identificador de una evaluación de competencias
	 * @param goalPeriodId Identificador de un período de metas
	 * @return List<NineBoxResultDTO> Listado de resultados de ninebox
	 **/
	public List<NineBoxResultDTO> getNineBoxResultGroupByDivision(Long companyId, FiltersResultsDTO filters) {

		Optional<Company> companyOpt = companyRepository.findById(companyId);
		List<NineBoxResultDTO> nineboxList = new ArrayList<NineBoxResultDTO>();
		ConfigurationNinebox config = configurationNineboxRepository.findTop1ByCompanyIdOrderByIdDesc(companyId);
		Map<String, String> labels = companyService.getCustomLabels(companyId);

		if (companyOpt.isPresent()) {
			Company company = companyOpt.get();
			Optional<Evaluation> evaluationOpt = evaluationRepository.findById(filters.getEvaluationId());
			Evaluation evaluation = evaluationOpt.isPresent() ? evaluationOpt.get() : null;

			List<Division> divisions = divisionrepository.findByCompanyOrderByNameAsc(company);
			
			extraFieldService.validateLevelAndJobFilters(filters);
			//VAlida los empleados que coinciden con los filtros extras
			Map <Long, Long> employeesIdsWithExtraFields= extraFieldService.validateExtraFieldsEmployeesFilter(filters, companyId);
			String employeesIds=null;
			// Obtenemos el listado de resultados de competencias
			List<Object[]> listCompetences = new ArrayList<Object[]>();
			if(employeesIdsWithExtraFields!=null && !employeesIdsWithExtraFields.isEmpty()) {
				employeesIds=employeesIdsWithExtraFields.entrySet().stream().map(m->m.getKey().toString()).collect(Collectors.joining(","));
				// Obtenemos el listado de resultados de competencias
				listCompetences = redshiftDAO.getResultsGroupByDivisionRedshift(evaluation, filters.getSubsidiariesAdmin(), filters.getCalibrated(), 
						filters.getLevelId(), filters.getJobName(), employeesIds);
			}else if(filters.getExtraField()==null || (filters.getLevelId()!= null || filters.getJobName()!=null)) {
				// Obtenemos el listado de resultados de competencias
				listCompetences = redshiftDAO.getResultsGroupByDivisionRedshift(evaluation, filters.getSubsidiariesAdmin(), filters.getCalibrated(), 
						filters.getLevelId(), filters.getJobName(), employeesIds);
			}

			

			GoalPeriod period = null;
			List<Object[]> listGoals = new ArrayList<Object[]>();

			List<Object[]> listOkrs = new ArrayList<Object[]>();
			if (!filters.getIsOkrs() && filters.getPeriodId()!=null) {
				Optional<GoalPeriod> periodOpt = goalRepository.findById(filters.getPeriodId());
					if(periodOpt.isPresent()) {
					   period = periodOpt.get();
					  // Obtenemos la lista de resultados de metas
					  listGoals = redshiftDAO.getAvgGoalGroupByDivision(period, filters.getSubsidiariesAdmin(), filters.getLevelId(), filters.getJobName(), employeesIds);
					}
				} else {
				listOkrs = okrProcessDAO.getAvgDivisions(filters.getPeriodId(), filters.getSubsidiariesAdmin(), filters.getLevelId(), filters.getJobName(), employeesIds);
			}

			
			// Configuración de escala en metas
			Optional<Boolean> hasConfig = companyRepository.hasConfigurationCompany(companyId, 251L);

			for (Division division : divisions) {
				NineBoxResultDTO ninebox = new NineBoxResultDTO();
				ninebox.setId(division.getId());
				ninebox.setName(division.getName());
				ninebox.setPhoto("");

				Double avgCompetences = getDivisionsCompetencesResult(evaluation, division.getId(), listCompetences,
						filters.getIsOkrs(), hasConfig.isPresent());
				Double avgGoalsOkrs = 0.0;

				if (!filters.getIsOkrs()) {
					avgGoalsOkrs = getGoalsResult( division.getId(), listGoals);
					// Validaciones por si competencias está en porcentaje, metas también debe ir en
					// porcentaje
					if ((!evaluation.getCompany().getCompetencesResultFormat().equals("SCALE") && hasConfig.isPresent())
							|| (!hasConfig.isPresent() && period!=null && period.getGoalLimit().longValue() < 100L)) {
						avgGoalsOkrs = (avgGoalsOkrs * 100) / period.getGoalLimit().longValue();
					}
				} else {
					avgGoalsOkrs = getOkrsResult(division.getId(), listOkrs);
				}
				ninebox.setDataArray(getResultArray(config, avgCompetences, avgGoalsOkrs));
				ninebox.setDataDesc(getDataDetails(company, labels, avgCompetences, avgGoalsOkrs, filters.getIsOkrs()));
				nineboxList.add(ninebox);

			}
		}

		return nineboxList;
	}

	private List<Double> getResultArray(ConfigurationNinebox config, Double avgCompetences, Double avgGoalsOkrs) {

		List<Double> resultArray = new ArrayList<Double>();
		// En la primera posición del array siempre va el eje Y
		if (config.getY() == PerformanceComponentType.COMPETENCES) {
			resultArray.add(avgCompetences);
			// metas
			resultArray.add(avgGoalsOkrs);
		} else {
			resultArray.add(avgGoalsOkrs);
			resultArray.add(avgCompetences);
		}

		return resultArray;
	}

	private List<String> getDataDetails(Company company, Map<String, String> labels, Double avgCompetences,
			Double avgGoalsOkrs, Boolean isOkrs) {

		List<String> dataDetails = new ArrayList<String>();
		StringBuilder detailCompetence = new StringBuilder(labels.get("labelCompetences")).append(": ")
				.append(avgCompetences);
		if (company.getCompetencesResultFormat().equals("PERCENTAGE") || isOkrs) {
			detailCompetence.append("%");
		}

		dataDetails.add(detailCompetence.toString());

		String label = isOkrs ? labels.get("labelOkrs") : labels.get("labelGoals");
		StringBuilder detailOther = new StringBuilder(label).append(": ").append(avgGoalsOkrs);

		if (company.getCompetencesResultFormat().equals("PERCENTAGE") || isOkrs) {
			detailOther.append("%");
		}
		dataDetails.add(detailOther.toString());

		return dataDetails;

	}

	/**
	 * Método que busca el promedio de competencias de un departamento en específico
	 * dentro de una lista de departamentos.
	 * 
	 */
	private Double getDivisionsCompetencesResult(Evaluation evaluation, Long divisionId, List<Object[]> list,
			Boolean isOkrs, Boolean scaleGoal) {

		Double value = 0.0;
		if (evaluation != null && list != null) {

			Object[] divisionResult = list.stream().filter(element -> divisionId.equals(getLong(element[0]))).findAny()
					.orElse(null);
			if (divisionResult != null) {
				if (evaluation.getCompany().getCompetencesResultFormat().equals("SCALE") && !isOkrs && scaleGoal) {
					value = Math.round((Double) (divisionResult[2]) * 100D) / 100D;
				} else {
					value = Math.round((Double) (divisionResult[3]) * 100D) / 100D;
				}
			}
		}

		return value;
	}

	/**
	 * Método que busca el promedio de metas de un departamento/empleado en
	 * específico dentro de una lista de departamentos.
	 * 
	 */
	private Double getGoalsResult( Long entityId, List<Object[]> list) {

		Double value = 0.0;
		if (list != null) {

			Object[] result = list.stream().filter(element -> entityId.equals(getLong(element[0]))).findAny()
					.orElse(null);
			if (result != null) {
				value = Math.round((Double) (result[2]) * 100D) / 100D;
				list.remove(result);
			}
		}

		return value;
	}

	/**
	 * Método que busca el promedio de Okrs de un departamento/empleado en
	 * específico dentro de una lista de departamentos.
	 * 
	 */
	private Double getOkrsResult(Long entityId, List<Object[]> list) {

		Double value = 0.0;

		Object[] result = list.stream().filter(element -> entityId.equals(getLong(element[0]))).findAny().orElse(null);
		if (result != null) {
			value = Math.round(getDouble(result[1]) * 100D) / 100D;
			list.remove(result);
		}

		return value;
	}

	/**
	 * Método que obtiene los periodos de desempeno dinámico
	 * 
	 * @param companyId Identificador de la empresa
	 * @return List<PeriodDTO> Listado de los períodos existentes para la empresa
	 **/
	public List<PeriodDTO> getPerformancePeriodsByCompany(Long companyId, Long employeeId) {

		List<PeriodDTO> list = new ArrayList<PeriodDTO>();

		if (employeeId == null) {
			List<PerformancePeriod> periods = performancePeriodRepository
					.findByCompanyIdAndStateOrderByStartDateDesc(companyId, EntityState.ACTIVE);

			if (periods.size() > 0) {
				list = periods.stream()
						.map(p -> new PeriodDTO(p.getId(), p.getName(), p.getStartDate(), p.getEndDate()))
						.collect(Collectors.toList());
			}
		} else {
			list = getPerformancePeriodByEmployeeId(companyId, employeeId);
		}

		return list;
	}

	/**
	 * Método para eliminar un periodo dinamico de desempenio
	 * 
	 * @param periodId
	 */
	public void deletePerformancePeriod(Long periodId) {

		Optional<PerformancePeriod> periodOpt = performancePeriodRepository.findById(periodId);

		if (periodOpt.isPresent()) {
			PerformancePeriod period = periodOpt.get();
			period.setState(EntityState.DELETED);
			performancePeriodRepository.save(period);
		}
	}

	/**
	 * Método que crea un nuevo periodo dinámico de desempenio
	 * 
	 * @param periodDto Periodo de desempenio
	 * 
	 */
	@SuppressWarnings("unused")
	private void createPerformancePeriod(PeriodDTO periodDto) {

		PerformancePeriod period = null;
		if (periodDto.getId() != null) {
			Optional<PerformancePeriod> periodOpt = performancePeriodRepository.findById(periodDto.getId());
			if (periodOpt.isPresent()) {
				period = periodOpt.get();
			}
		} else {
			period = new PerformancePeriod();
		}

		if (period != null) {
			period.setName(periodDto.getName());
			period.setStartDate(period.getStartDate());
			period.setEndDate(period.getEndDate());
			performancePeriodRepository.save(period);
		}

	}

	/**
	 * Método que obtiene los Resultados de metas y competencias por empleado para
	 * Ninebox
	 * Si recibe el id del departamento, devuelve los resultados de los empleados de ese departamento
	 * Si recibe el id del empleado, solo la información del empleado
	 * Si no recibe evaluationId, periodId.. se toma el configurado en resultados para la empresa
	 * 
	 * @param companyId    Identificador de la empresa
	 * @param evaluationId Identificador de una evaluación de competencias
	 * @param goalPeriodId Identificador de un período de metas y/o Okrs
	 * @param isOkrs       para saber si es por Okrs o metas
	 * @param divisionId   Identificador del empleado
	 * @param isLeader    Identifica si el usuario logueado es líder y devolver el resultado de sus colaboradores
	 * @return List<NineBoxResultDTO> Listado de resultados de ninebox
	 **/

	public List<NineBoxResultDTO> getNineBoxResultGroupByEmployee(Long companyId, FiltersResultsDTO filters) {


		Optional<Company> companyOpt = companyRepository.findById(companyId);
		List<NineBoxResultDTO> nineboxList = new ArrayList<NineBoxResultDTO>();
		ConfigurationNinebox config = configurationNineboxRepository.findTop1ByCompanyIdOrderByIdDesc(companyId);
		Map<String, String> labels = companyService.getCustomLabels(companyId);

		if (companyOpt.isPresent()) {
			Company company = companyOpt.get();
			
			List<Object[]> listCompetences = new ArrayList<Object[]>();
			Evaluation evaluation = null;
			Long evaluationId = null;
			Boolean isCalibrated = null;
			if (filters.getEvaluationId() == null) {
				evaluationId = company.getCompetencesevaluation();
				if (filters.getEmployeeId() != null) {
					Optional<Evaluation> evaluationCompeOpt = evaluationRepository.findById(evaluationId);
					isCalibrated = evaluationCompeOpt.get().getViewCalibration();					
				} else {
					isCalibrated = filters.getCalibrated();
				}
			} else {
				isCalibrated = filters.getCalibrated();
				evaluationId = filters.getEvaluationId();
			}
			
			extraFieldService.validateLevelAndJobFilters(filters);
			//VAlida los empleados que coinciden con los filtros extras
			Map <Long, Long> employeesIdsWithExtraFields= extraFieldService.validateExtraFieldsEmployeesFilter(filters, companyId);
		
			
			if (evaluationId != null) {
			  Optional<Evaluation> evaluationOpt = evaluationRepository.findById(evaluationId);
			  evaluation = evaluationOpt.isPresent() ? evaluationOpt.get() : null;
			  String employeesIds=null;
			  if(employeesIdsWithExtraFields!=null && !employeesIdsWithExtraFields.isEmpty()) {
					employeesIds=employeesIdsWithExtraFields.entrySet().stream().map(m->m.getKey().toString()).collect(Collectors.joining(","));
					 // Obtenemos el listado de resultados de competencias
					 listCompetences = redshiftDAO.getResultsGroupByEmployeesWithExtraFields(evaluationOpt.get().getCompany().getId(), 
								evaluationOpt.get(), filters.getDivisionId(),filters.getSubsidiariesAdmin(), employeesIds, isCalibrated, filters.getLevelId(), filters.getJobName());
						
			  }else if (filters.getExtraField()==null || (filters.getLevelId()!= null || filters.getJobName()!=null)) {
				  
				  if(filters.getIsLeader()!=null && filters.getIsLeader()) {
						  Optional<List<Long>> collOpt=employeeRepository.findCollaboratorsIdByBoss(filters.getEmployeeId());

						  if(collOpt.isPresent()) {
							  List<Long>  collaboratorIds =  collOpt.get();
							employeesIds=StringUtils.join(collaboratorIds, ',');
						  }
				}else if(!filters.getIsLeader() && filters.getEmployeeId()!=null) {
					employeesIds=filters.getEmployeeId().toString();
				}
				 
				  // Obtenemos el listado de resultados de competencias
					 listCompetences = redshiftDAO.getResultsGroupByEmployeesWithExtraFields(evaluationOpt.get().getCompany().getId(), 
								evaluationOpt.get(), filters.getDivisionId(),filters.getSubsidiariesAdmin(), employeesIds, isCalibrated, filters.getLevelId(), filters.getJobName());
						
			  }
			  
			
			}
			
			GoalPeriod period=null;
            Long periodId=filters.getPeriodId()!=null? filters.getPeriodId() : company.getGoalsperiod();
            if(periodId!=null) {
			  Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
		      period= periodOpt.isPresent()? periodOpt.get():null;
		    }
		
			List<Object[]> listGoalsOkrs = getGoalsOrOkrs(filters.getIsOkrs(), company, filters.getPeriodId(), filters.getDivisionId(), filters.getSubsidiariesAdmin(),
					filters.getEmployeeId(), filters.getIsLeader(), filters);
			
		

			// Configuración de escala en metas
			Optional<Boolean> hasConfig = companyRepository.hasConfigurationCompany(companyId, 251L);
			Boolean hasPercentageGoal= ((evaluation!=null && !evaluation.getCompany().getCompetencesResultFormat().equals("SCALE") && hasConfig.isPresent())
			|| (!hasConfig.isPresent() && period!=null && period.getGoalLimit().longValue() < 100L));
			Long goalLimit=period!=null && period.getGoalLimit()!=null? period.getGoalLimit().longValue() :0L;
			Boolean labelOkrs= filters.getIsOkrs()!=null? filters.getIsOkrs() :  (filters.getIsOkrs()==null && company.getGoalsperiod()!=null? false :true );

			for (Object[] object : listCompetences) {
				
					NineBoxResultDTO ninebox = new NineBoxResultDTO();
					ninebox.setId(getLong(object[0]));
					ninebox.setName(getString(object[1]));
					ninebox.setPhoto("/fotoemp?id=" + ninebox.getId());
					Double avgCompetences = 0.0;
					
					//Valida tipo de escala para obtener el valor en escala o porcentaje
					if (evaluation.getCompany().getCompetencesResultFormat().equals("SCALE") && (!labelOkrs && !hasPercentageGoal)) {
						avgCompetences =   Math.round((Double) (object[6]) * 100D) / 100D;
					}else {
						avgCompetences =   Math.round((Double) (object[7]) * 100D) / 100D;
					}
					
					Double avgGoalsOkrs = 0.0;
	
					if ((filters.getIsOkrs()!=null && !filters.getIsOkrs()) || (filters.getIsOkrs()==null && company.getGoalsperiod()!=null)) {
						
							avgGoalsOkrs = getGoalsResult(getLong(object[0]), listGoalsOkrs);
							// Validaciones por si competencias está en porcentaje, metas también debe ir en
							// porcentaje
							if (hasPercentageGoal) {
								avgGoalsOkrs = (avgGoalsOkrs * 100) / period.getGoalLimit().longValue();
							}
						
					} else {
						avgGoalsOkrs = getOkrsResult(getLong(object[0]), listGoalsOkrs);
					}
					ninebox.setDataArray(getResultArray(config, avgCompetences, avgGoalsOkrs));
					ninebox.setDataDesc(getDataDetails(company, labels, avgCompetences, avgGoalsOkrs, labelOkrs));
					nineboxList.add(ninebox);
			}

			

            //Procesa los que tienen Okrs o metas y no tienen competencias
			if(listGoalsOkrs!=null && listGoalsOkrs.size()>0) {
			   processAllGoalsOrOkrs(listGoalsOkrs, filters.getIsOkrs(), nineboxList, config, labels, company, hasPercentageGoal, goalLimit, employeesIdsWithExtraFields);
			}
		}

		return nineboxList;
	}
	

	
	/**
	 *  Método que obtiene la información de resultados de metas o okrs (De acuerdo a los parametros)
	 * */
	private List<Object[]> getGoalsOrOkrs(Boolean isOkrs, Company company, Long periodId, Long divisionId, String subsidiariesAdmin, 
			Long employeeId, Boolean isLeader, FiltersResultsDTO filters){
		
		List<Object[]> list=new ArrayList<Object[]>();
		
		// Si viene el parametro para saber si consultar la información de metas (isOkrs=false), se hace la validación 
		//o si no viene el parámetro se consulta el periodo de metas configurado
		if ((isOkrs!=null && !isOkrs) || (isOkrs==null && company.getGoalsperiod()!=null) || (isOkrs != null && !isOkrs && periodId!=null)) {
			periodId=periodId!=null? periodId : company.getGoalsperiod();
			
			Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
			GoalPeriod period = periodOpt.isPresent() ? periodOpt.get() : null;
			// Obtenemos la lista de resultados de metas
			try {
				List<Long> collaborators=new ArrayList<Long>();
				if(!isLeader) {
				  collaborators.add(employeeId);
				}else {
					Optional<List<Long>> opt=employeeRepository.findCollaboratorsIdByBoss(employeeId);
					if(opt.isPresent()) {
						collaborators=opt.get();
					}
				}

				list=  performanceResultDAO.getGoalsData(company, period, divisionId, filters.getLevelId(), filters.getJobName(), employeeId, collaborators, subsidiariesAdmin);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if ( (isOkrs != null && isOkrs) || (isOkrs == null && company.getOkrsPeriod() != null) || ( isOkrs!=null && isOkrs && periodId != null)) {
			periodId=periodId!=null? periodId: company.getOkrsPeriod();
			if(employeeId!=null) {
			     list = okrProcessDAO.getOkrsResultsGroupedByEmployee(periodId, employeeId, isLeader);
			}else {
				if (divisionId != null) {
					list = okrRepository.getOkrsEmployeeResults(periodId, divisionId);					
				} else {
					list = this.okrProcessDAO.getOkrsResultsByEmployeeExcel(periodId);
				}
			}
		}
		
		return list;
		
	}
	
	

	private void processAllGoalsOrOkrs(List<Object[]> listGoalsOkrs, Boolean isOkrs,
			List<NineBoxResultDTO> nineboxList, ConfigurationNinebox config, Map<String, String> labels,
			Company company, Boolean hasPercentageGoal, Long goalLimit, Map <Long, Long> employeesIdsWithExtraFields ) {
		

		if ((isOkrs!=null && !isOkrs) || (isOkrs==null && company.getGoalsperiod()!=null)) {

			listGoalsOkrs.stream().forEach(object -> {
				Long idWithExtraField= employeesIdsWithExtraFields!=null? employeesIdsWithExtraFields.get(getLong(object[0])): null;
				if(idWithExtraField!=null || employeesIdsWithExtraFields==null) {
					NineBoxResultDTO ninebox = new NineBoxResultDTO();
					ninebox.setId(getLong(object[0]));
					ninebox.setName(getString(object[1]));
					ninebox.setPhoto("/fotoemp?id=" + ninebox.getId());
					Double value = Math.round((Double) (object[2]) * 100D) / 100D;
					
					if(hasPercentageGoal) {
						value = (value * 100) / goalLimit;
					}
					ninebox.setDataArray(getResultArray(config, 0.0, value));
					ninebox.setDataDesc(getDataDetails(company, labels, 0.0D, value, false));
					nineboxList.add(ninebox);
				}
			});

		} else {

			listGoalsOkrs.stream().forEach(object -> {
				Long idWithExtraField=employeesIdsWithExtraFields!=null? employeesIdsWithExtraFields.get(getLong(object[0])):null;
				if(idWithExtraField!=null || employeesIdsWithExtraFields==null) {
					NineBoxResultDTO ninebox = new NineBoxResultDTO();
					ninebox.setId(((BigInteger)(object[0])).longValue());
					ninebox.setName(getString(object[2]));
					ninebox.setPhoto("/fotoemp?id=" + ninebox.getId());
					Double value = Math.round(getDouble(object[1]) * 100D) / 100D;
					ninebox.setDataArray(getResultArray(config, 0.0, value));
					ninebox.setDataDesc(getDataDetails(company, labels, 0.0D, value, true));
					nineboxList.add(ninebox);
				}

			});

		}

	}

	/**
	 * Guarda o actualiza información del período de desempeño de una empresa y sus
	 * procesos
	 * 
	 * @param companyId Identificador de la compañía
	 * @param dtoPeriod Contiene información del período y sus procesos
	 */
	@Transactional
	public boolean savePerformancePeriod(Long companyId, PeriodDetailDTO dtoPeriod) {

		Company company = companyRepository.getOne(companyId);
		if (company != null) {
			String lang = company.getLanguageCode();
			if (dtoPeriod.getPeriod().getId() == null) {

				// Se guarda información del período
				if (dtoPeriod.getPeriod() != null) {
					PerformancePeriod period = new PerformancePeriod();
					period.setName(dtoPeriod.getPeriod().getName());
					period.setStartDate(dtoPeriod.getPeriod().getStartDate());
					period.setEndDate(dtoPeriod.getPeriod().getEndDate());
					period.setCompany(company);
					performancePeriodRepository.save(period);

					// Se guardan los procesos del período
					if (dtoPeriod.getModules() != null) {
						dtoPeriod.getModules().stream().forEach(module -> {
							if (module.getName() != null && !module.getName().equals("")) {
								// Actualiza etiqueta del módulo
								updateModuleLabel(module.getModule(), lang, module.getName(), company.getId());
							}
							module.getProcesses().stream().forEach(process -> {
								process.getCategories().stream().forEach(category -> {
									PerformancePeriodDetail detail = new PerformancePeriodDetail();
									detail.setModule(module.getModule());
									detail.setProcessId(process.getProcessId());
									detail.setCompanyLevelId(category.getCompanyLevelId());
									detail.setWeight(category.getWeight());
									detail.setPerformancePeriod(period);
									performancePeriodDetailRepository.save(detail);
								});
							});
						});
					}
				}

			} else {

				// Se consulta información del período
				Optional<PerformancePeriod> optPeriod = performancePeriodRepository
						.findById(dtoPeriod.getPeriod().getId());
				if (optPeriod.isPresent()) {
					// Se actualiza información del período
					optPeriod.get().setName(dtoPeriod.getPeriod().getName());
					optPeriod.get().setStartDate(dtoPeriod.getPeriod().getStartDate());
					optPeriod.get().setEndDate(dtoPeriod.getPeriod().getEndDate());
					performancePeriodRepository.save(optPeriod.get());

					updatePerformancePeriodDetail(optPeriod.get(), dtoPeriod);
				}
			}
		}

		return true;
	}

	private void updatePerformancePeriodDetail(PerformancePeriod generalPeriod, PeriodDetailDTO dtoPeriod) {
		List<Long> periodDetailIds = new ArrayList<Long>();
		// Se actualizan los procesos del período
		dtoPeriod.getModules().stream().forEach(module -> {
			if (module.getName() != null && !module.getName().equals("")) {
				// Actualiza etiqueta del módulo
				updateModuleLabel(module.getModule(), generalPeriod.getCompany().getLanguageCode(), module.getName(),
						generalPeriod.getCompany().getId());
			}
			module.getProcesses().stream().forEach(process -> {
				process.getCategories().stream().forEach(category -> {
					// Si no existe el proceso, se crea uno nuevo
					if (category.getId() == null) {
						PerformancePeriodDetail detail = new PerformancePeriodDetail();
						detail.setModule(module.getModule());
						detail.setProcessId(process.getProcessId());
						detail.setCompanyLevelId(category.getCompanyLevelId());
						detail.setWeight(category.getWeight());
						detail.setPerformancePeriod(generalPeriod);
						detail = performancePeriodDetailRepository.save(detail);
						periodDetailIds.add(detail.getId());
					} else {
						// Si ya existe, se actualizan sus datos
						Optional<PerformancePeriodDetail> periodDetail = performancePeriodDetailRepository
								.findById(category.getId());
						if (periodDetail.isPresent()) {
							periodDetail.get().setCompanyLevelId(category.getCompanyLevelId());
							periodDetail.get().setWeight(category.getWeight());
							performancePeriodDetailRepository.save(periodDetail.get());
							periodDetailIds.add(periodDetail.get().getId());
						}
					}
				});
			});
		});

		// Se eliminan procesos que ya no existen en la actualización
		if (periodDetailIds.size() > 0) {
			List<PerformancePeriodDetail> listProceses = performancePeriodDetailRepository
					.findByPeriodDetailIds(periodDetailIds, generalPeriod.getId());
			listProceses.stream().forEach(process -> {
				Optional<PerformancePeriodDetail> periodDetail = performancePeriodDetailRepository
						.findById(process.getId());
				if (periodDetail.isPresent()) {
					periodDetail.get().setState(EntityState.DELETED);
					performancePeriodDetailRepository.save(periodDetail.get());
				}
			});
		}

	}

	/***
	 * Método que actualiza etiqueta general del módulo
	 */
	@Transactional
	private void updateModuleLabel(PerformanceModule module, String lang, String name, Long companyId) {
		String strModule = module.toString();

		if (module.equals(PerformanceModule.COMPETENCES) || module.equals(PerformanceModule.GOALS)
				|| module.equals(PerformanceModule.PID)) {
			LabelFlex label = null;
			String nameModule = strModule.equals("PID") ? strModule : strModule.substring(0, strModule.length() - 1);
			label = labelRepository.findByLanguageCodeAndCode(lang, nameModule + "_KEY_LABEL_" + companyId);
			// Se actualiza etiqueta con nombre ingresado
			if (label != null) {
				label.setLabel(name);
				labelRepository.save(label);
			}
		}

		if (module.equals(PerformanceModule.CLIMATE) || module.equals(PerformanceModule.OKRS)) {
			Label labelNewApp = null;
			labelNewApp = labelAngularRepository.findByCompanyIdAndModuleAndCode(companyId, "common",
					"module_" + strModule.toLowerCase() + "_title");

			// Si no existe la etiqueta, se crea para la empresa
			if (labelNewApp == null) {
				labelNewApp = new Label();
				labelNewApp.setCode("module_" + strModule.toLowerCase() + "_title");
				labelNewApp.setCompany_id(companyId);
				labelNewApp.setModule("common");
			}
			// Se actualiza etiqueta con nombre ingresado
			if (lang.equals("es")) {
				labelNewApp.setSpanish(name);
			} else if (lang.equals("en")) {
				labelNewApp.setSpanish(name);
				labelNewApp.setEnglish(name);
			} else if (lang.equals("pt")) {
				labelNewApp.setSpanish(name);
				labelNewApp.setPortuguese(name);
			}
			labelAngularRepository.save(labelNewApp);

			// Se actualiza etiqueta clima nuevo
			if (module.equals(PerformanceModule.CLIMATE)) {
				Label labelNewClimateApp = null;
				labelNewClimateApp = labelAngularRepository.findByCompanyIdAndModuleAndCode(companyId, "common",
						"module_climate_survey_title");
				// Si no existe la etiqueta, se crea para la empresa
				if (labelNewClimateApp == null) {
					labelNewClimateApp = new Label();
					labelNewClimateApp.setCode("module_climate_survey_title");
					labelNewClimateApp.setCompany_id(companyId);
					labelNewClimateApp.setModule("common");
				}
				// Se actualiza etiqueta con nombre ingresado
				if (lang.equals("es")) {
					labelNewClimateApp.setSpanish(name);
				} else if (lang.equals("en")) {
					labelNewClimateApp.setSpanish(name);
					labelNewClimateApp.setEnglish(name);
				} else if (lang.equals("pt")) {
					labelNewClimateApp.setSpanish(name);
					labelNewClimateApp.setPortuguese(name);
				}
				labelAngularRepository.save(labelNewClimateApp);
			}
		}
	}

	/**
	 * Método que obtiene el detalle del periodo dinamico
	 * 
	 * @param periodId Identificador del periodo dinámico
	 * @return PeriodDetailDTO Detalle del período
	 */
	public PeriodDetailDTO getPeriodDetailById(Long periodId) {

		PeriodDetailDTO dto = new PeriodDetailDTO();

		Optional<PerformancePeriod> period = performancePeriodRepository.findById(periodId);

		List<ModulePerformanceDTO> modules = new ArrayList<ModulePerformanceDTO>();

		if (period.isPresent()) {
			List<PerformancePeriodDetail> list = performancePeriodDetailRepository
					.findAllByPerformancePeriod(period.get());

			PeriodDTO periodDTO = new PeriodDTO(period.get().getId(), period.get().getName(),
					period.get().getStartDate(), period.get().getEndDate());
			dto.setPeriod(periodDTO);

			list.stream().forEach(module -> {

				ModulePerformanceDTO moduleDTO = new ModulePerformanceDTO();

				Optional<ModulePerformanceDTO> moduleOpt = modules.stream()
						.filter(m -> m.getModule().toString().equals(module.getModule().toString())).findFirst();

				if (moduleOpt.isPresent()) {
					moduleDTO = moduleOpt.get();
				} else {
					moduleDTO.setModule(module.getModule());
					String strModule = module.getModule().toString();

					if (module.getModule().equals(PerformanceModule.COMPETENCES)
							|| module.getModule().equals(PerformanceModule.GOALS)
							|| module.getModule().equals(PerformanceModule.PID)) {
						String nameModule = strModule.equals("PID") ? strModule
								: strModule.substring(0, strModule.length() - 1);
						moduleDTO.setName(getLabel(nameModule + "_KEY_LABEL_" + period.get().getCompany().getId(),
								period.get().getCompany().getLanguageCode()));

					} else {
						Label labelNewApp = labelAngularRepository.findByCompanyIdAndModuleAndCode(
								period.get().getCompany().getId(), "common",
								"module_" + strModule.toLowerCase() + "_title");
						if (labelNewApp != null) {
							if (period.get().getCompany().getLanguageCode().equals("es")) {
								moduleDTO.setName(labelNewApp.getSpanish());
							} else if (period.get().getCompany().getLanguageCode().equals("en")) {
								moduleDTO.setName(labelNewApp.getEnglish());
							} else if (period.get().getCompany().getLanguageCode().equals("pt")) {
								moduleDTO.setName(labelNewApp.getPortuguese());
							}
						}
					}

					List<ProcessDTO> processList = new ArrayList<ProcessDTO>();
					moduleDTO.setProcesses(processList);
					modules.add(moduleDTO);
				}

				createProcessesDTO(module, moduleDTO);

			});

		}

		dto.setModules(modules);

		return dto;
	}

	/**
	 * Método que crea procesos de un módulo de un periodo dinámico
	 */
	private ProcessDTO createProcessesDTO(PerformancePeriodDetail module, ModulePerformanceDTO moduleDTO) {

		ProcessDTO processDTO = new ProcessDTO();

		Optional<ProcessDTO> processOpt = moduleDTO.getProcesses().stream()
				.filter(p -> p.getProcessId().equals(module.getProcessId())).findFirst();

		if (processOpt.isPresent()) {
			processDTO = processOpt.get();
		} else {
			processDTO.setProcessId(module.getProcessId());
			moduleDTO.getProcesses().add(processDTO);
			List<PerformanceCategoryDTO> categoriesList = new ArrayList<PerformanceCategoryDTO>();
			processDTO.setCategories(categoriesList);

		}

		PerformanceCategoryDTO categoryDTO = new PerformanceCategoryDTO(module.getId(), module.getCompanyLevelId(),
				module.getWeight());
		processDTO.getCategories().add(categoryDTO);

		return processDTO;
	}

	/**
	 * Obtiene los resultados dinámicos creados para el nivel de cargo del
	 * colaborador y donde el colaborador esté asignado a los procesos configurados
	 * 
	 * @param companyId  Identificador de la empresa
	 * @param employeeId Identificador del empleado
	 * 
	 */
	public List<PeriodDTO> getPerformancePeriodByEmployeeId(Long companyId, Long employeeId) {

		JobRole jobrole = jobRoleRepository.getJobroleByEmployeeId(employeeId);

		List<PeriodDTO> dtos = new ArrayList<PeriodDTO>();
		Optional<Boolean> hasConfigDinamic = companyRepository.hasConfigurationCompany(companyId, 264L);
		
		if (jobrole != null && hasConfigDinamic.isPresent()) {
			List<PerformancePeriod> periods = performancePeriodRepository.findByCompanyAndLevelAndEmployee(companyId,
					jobrole.getLevel().getId(), employeeId);

			if (periods.size() > 0) {
				dtos = periods.stream()
						.map(p -> new PeriodDTO(p.getId(), p.getName(), p.getStartDate(), p.getEndDate()))
						.collect(Collectors.toList());
			}
		}

		return dtos;

	}

	/**
	 * Obtiene promedio de desempeño de un empleado, puede ser el configurado en la
	 * empresa (antiguo) o según lo configurado en un período de desempeño dinámico
	 * (nuevo)
	 * 
	 * @param companyId  Identificador de compañía
	 * @param employeeId Identificador del empleado
	 * @param periodId   Identificador de período de desempeño
	 */
	public PerformanceDetailEmployeeDTO getAverageAndDetailPerformanceEmployee(Long companyId, Long employeeId, Long periodId, Boolean isLeader, Long goalPeriodId, Long evaluationId) {

		PerformanceDetailEmployeeDTO moduleDetail = new PerformanceDetailEmployeeDTO();

		Map<String, String> labels = companyService.getCustomLabels(companyId);

		
		Optional<Boolean> hasConfigDinamic = companyRepository.hasConfigurationCompany(companyId, 264L);
		
		// Es configuración de performance dinámico
		if(periodId !=null && hasConfigDinamic.isPresent()) {
			
			Double avg = 0.0;
			List<PerformanceProcessEmployeeDTO> processes = new ArrayList<PerformanceProcessEmployeeDTO>();
			
			JobRole jobrole = jobRoleRepository.getJobroleByEmployeeId(employeeId);
			List<PerformancePeriodDetail> list = performancePeriodDetailRepository.findByPeriodIdAndLevelId(periodId, jobrole.getLevel().getId());

			
			// Configuración de escala en metas
			Optional<Boolean> hasConfigOpt = companyRepository.hasConfigurationCompany(companyId, 251L);
			Boolean hasConfigScaleGoal=hasConfigOpt.isPresent()? hasConfigOpt.get():false;
			Optional<Company> company=companyRepository.findById(companyId);
			String formatType=validateScaleOrPercentageDinamic(list, company.get(), hasConfigScaleGoal);
			moduleDetail.setType(formatType);
     	   
			for(PerformancePeriodDetail detail: list) {
               PerformanceProcessEmployeeDTO dto=null;
               Double totalValidated=0.0D;
               
               if (detail.getModule().equals(PerformanceModule.COMPETENCES)) {
            	   
            	   Optional<Evaluation> evaluation=evaluationRepository.findById(detail.getProcessId());
            	   if (evaluation.isPresent()) {
            		   dto=createNewProcessDetail(dto, detail,evaluation.get().getName(), labels.get("labelCompetences"));
	            	   dto.setRedirect(evaluation.get().getShowinplatform());
	            	   dto.setFormatType(evaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")? "" : "%");
	            	   Double total=redshiftDAO.getAvgCompetencesByEvaluationRedshift(evaluation.get(), null, employeeId, null,null, null, evaluation.get().getViewCalibration(), false);
	            	   dto.setTotal(total);
	            	   totalValidated=validateScaleOrPercentageCompetences(evaluation.get(), formatType.equals("PERCENTAGE") , hasConfigScaleGoal , total);

            	   }
            	   
               } else if(detail.getModule().equals(PerformanceModule.GOALS)) {
            	   
            	   Optional<GoalPeriod> periodOpt = goalRepository.findById(detail.getProcessId());
            	   if(periodOpt.isPresent()) {
            		   dto=createNewProcessDetail(dto, detail,periodOpt.get().getName(), labels.get("labelGoals"));
	             	   dto.setRedirect(periodOpt.get().getShowGoalResults());
	             	   dto.setFormatType(hasConfigScaleGoal? "":"%");
	             	   Double total=redshiftDAO.getAvgGoalsByPeriodRedshift(periodOpt.get(), null,null, null, null, employeeId, null, false);
	            	   dto.setTotal(total);
	            	   totalValidated=validateScaleOrPercentageGoals(periodOpt.get(), hasConfigScaleGoal, total,formatType.equals("PERCENTAGE"));

            	   }
               } else if(detail.getModule().equals(PerformanceModule.OKRS)) {
            	   
            	   Optional<OKRPeriod> okrPeriodOpt = okrPeriodRepository.findById(detail.getProcessId());
            	   if (okrPeriodOpt.isPresent()) {
            		   dto=createNewProcessDetail(dto, detail,okrPeriodOpt.get().getName(), labels.get("labelOkrs"));
            		   dto.setRedirect(true);
            		   dto.setFormatType("%");
            		   List<Object[]> result= okrProcessDAO.getOkrsResultsGroupedByEmployee(okrPeriodOpt.get().getId(), employeeId, isLeader);
            		   if (result!=null && result.size()>0) {
            			   dto.setTotal(getDouble(result.get(0)[1]));
            			   totalValidated=dto.getTotal();
            		   } else {
            			   dto.setTotal(0.0);
            		   }            		   
            	   }
        
               } else if(detail.getModule().equals(PerformanceModule.CLIMATE)) {
            	   
            	   Optional<Evaluation> evaluation=evaluationRepository.findById(detail.getProcessId());
            	   
            	   if(evaluation.isPresent()) {
	            	   dto=createNewProcessDetail(dto, detail,evaluation.get().getName(), labels.get("labelClimate"));
	            	   dto.setRedirect(true);
	            	   dto.setFormatType("%");
	            	   String type = climateReportsService.getTypesEvaluationClimate(companyId, detail.getProcessId());
					   // Valida tipo de evaluación clima o enps
					   Boolean isEnps = type.equals("ENPS") ? true : false;
	            	   dto.setTotal(this.climateService.getAvgClimate(evaluation.get().getId(), employeeId, isEnps, null, companyId,null));
	            	   totalValidated=dto.getTotal();
            	   }
            	   
               } else if(detail.getModule().equals(PerformanceModule.PID)) {
            	   
            	 
            	   Optional <DevelopIndividualPlan> dip =  developIndividualPlanRepository.getOnePid(detail.getProcessId());
            	   if(dip.isPresent()) {
	            	   dto=createNewProcessDetail(dto, detail,dip.get().getName(), labels.get("labelPid"));
	            	   dto.setRedirect(false);
	            	   dto.setFormatType("%");
	            	   dto.setTotal((double) dipEmployeeRepository.avgProgressEmployee(detail.getProcessId(), employeeId));
	            	   totalValidated=dto.getTotal();
            	   }
               }
				
				
           	   avg = totalValidated != null ? avg + ((totalValidated*detail.getWeight())/100) : avg;
           	   if(dto!=null) {
                 processes.add(dto);	
           	   }

			}

			moduleDetail.setAverage(avg);
			moduleDetail.setProcesses(processes);

		} else {
			// Es configuración antigua
			moduleDetail = getAveragePerformanceEmployeeOldConfiguration(companyId, employeeId, labels, isLeader, goalPeriodId, evaluationId);

		}

		return moduleDetail;

	}
	
	
	private PerformanceProcessEmployeeDTO createNewProcessDetail(PerformanceProcessEmployeeDTO dto, PerformancePeriodDetail detail, String name, String moduleName) {
		
		dto=new PerformanceProcessEmployeeDTO();
        dto.setProcessId(detail.getProcessId());
        dto.setType(detail.getModule());
        dto.setWeight(detail.getWeight());
        dto.setName(name);
        dto.setModuleName(moduleName);
        
        return dto;
	}
	
	
	/** 
	 * Valida si tiene los módulos de OKrs, metas y/o clima, si tiene alguno, todo se debe mostrar en porcentaje
	 * 
	 * */
	private String validateScaleOrPercentageDinamic(List<PerformancePeriodDetail> list, Company company, Boolean hasScaleGoal) {
		
		String type="PERCENTAGE";
		Optional<PerformancePeriodDetail> opt=list.stream().filter(l->l.getModule().equals(PerformanceModule.OKRS) || (l.getModule().equals(PerformanceModule.PID)) || 
				(l.getModule().equals(PerformanceModule.CLIMATE))).findFirst();
		
		Optional<PerformancePeriodDetail> optCompetences=list.stream().filter(l->l.getModule().equals(PerformanceModule.COMPETENCES)).findFirst();
		
		Optional<PerformancePeriodDetail> optGoals=list.stream().filter(l->l.getModule().equals(PerformanceModule.GOALS)).findFirst();
		
		if(!opt.isPresent() && (optCompetences.isPresent() && company.getCompetencesResultFormat().equals("SCALE")) && 
				(optGoals.isPresent() && hasScaleGoal)) {
			
			type="SCALE";
		}
	
		return type;
	
	}

	/**
	 * Obtiene promedio de desempeño de un empleado, con los procesos configurados
	 * por la empresa (antiguo)
	 * 
	 * @param companyId  Identificador de compañía
	 * @param employeeId Identificador del empleado
	 * @param labels     Etiquetas con los nombres de los módulos
	 */
	private PerformanceDetailEmployeeDTO getAveragePerformanceEmployeeOldConfiguration(Long companyId, Long employeeId,
			Map<String, String> labels, Boolean isLeader, Long goalPeriodId, Long evaluationId) {

		PerformanceDetailEmployeeDTO moduleDetail = new PerformanceDetailEmployeeDTO();
		Company company = companyRepository.getOne(companyId);
		Double avg = null;
		Double totalValidatedComp = null, totalValidatedGoal = null;
		List<PerformanceProcessEmployeeDTO> processes = new ArrayList<PerformanceProcessEmployeeDTO>();
		
		// Configuración de escala en metas
		Optional<Boolean> hasConfigOpt = companyRepository.hasConfigurationCompany(companyId, 251L);
		Boolean hasConfigScaleGoal=hasConfigOpt.isPresent()? hasConfigOpt.get():false;
		
		moduleDetail.setType(company.getCompetencesResultFormat().equals("SCALE") && hasConfigScaleGoal? "SCALE" : "PERCENTAGE");

		// Validamos competencias
		// Si evaluationId trae información es porque la empresa tiene activa la configuración 79
		if ((company != null && company.getCompetencesevaluation() != null) || company!=null && evaluationId!=null ) {

			 Long evaluationCompId = evaluationId != null ? evaluationId : company.getCompetencesevaluation();
			 Optional<Evaluation> evaluation=evaluationRepository.findById(evaluationCompId);
      	     if(evaluation.isPresent()) {
				PerformanceProcessEmployeeDTO dto = new PerformanceProcessEmployeeDTO();
				dto.setProcessId(evaluation.get().getId());
				dto.setType(PerformanceModule.COMPETENCES);
				dto.setName(evaluation.get().getName());
				dto.setModuleName(labels.get("labelCompetences"));
				dto.setRedirect(evaluation.get().getShowinplatform());
				dto.setFormatType(evaluation.get().getCompany().getCompetencesResultFormat().equals("SCALE")? "" : "%");
				Double total = redshiftDAO.getAvgCompetencesByEvaluationRedshift(evaluation.get(), null, employeeId, null,null, null, evaluation.get().getViewCalibration(), true);
				dto.setTotal(total);
				if (dto.getTotal()!=null) {
					dto.setWeight(company.getCompetencespercentage().doubleValue());
					totalValidatedComp = validateScaleOrPercentageCompetences(evaluation.get(), company.getOkrsPeriod() != null , hasConfigScaleGoal , total);
					processes.add(dto);
				}
      	   }

		}

		if ((company != null && company.getGoalsperiod() != null) || company != null && goalPeriodId != null) {

			Long periodId = goalPeriodId != null ? goalPeriodId : company.getGoalsperiod();
			Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
			if(periodOpt.isPresent() && periodOpt.get().getShowGoalResults()) {
				PerformanceProcessEmployeeDTO dto = new PerformanceProcessEmployeeDTO();
				dto.setProcessId(periodOpt.get().getId());
				dto.setType(PerformanceModule.GOALS);
				dto.setName(periodOpt.get().getName());
				dto.setModuleName(labels.get("labelGoals"));
				dto.setFormatType(hasConfigScaleGoal? "":"%");
				dto.setRedirect(periodOpt.get().getShowGoalResults());
				Double total=redshiftDAO.getAvgGoalsByPeriodRedshift(periodOpt.get(), null, null, null, null, employeeId, null, true);
				dto.setTotal(total);
				if (dto.getTotal() != null) {
					dto.setWeight(company.getGoalspercentage().doubleValue());
					totalValidatedGoal = validateScaleOrPercentageGoals(periodOpt.get(), hasConfigScaleGoal, total, false);
					processes.add(dto);
				}
			}

		} else if (company != null && company.getOkrsPeriod() != null) {


			Optional<OKRPeriod> periodOpt = okrPeriodRepository.findById(company.getOkrsPeriod());
			if (periodOpt.isPresent()) {
				PerformanceProcessEmployeeDTO dto = new PerformanceProcessEmployeeDTO();
				dto.setProcessId(periodOpt.get().getId());
				dto.setType(PerformanceModule.OKRS);
				dto.setWeight(company.getGoalspercentage().doubleValue());
				dto.setName(periodOpt.get().getName());
				dto.setModuleName(labels.get("labelOkrs"));
				dto.setRedirect(true);
				dto.setFormatType("%");
				List<Object[]> result= okrProcessDAO.getOkrsResultsGroupedByEmployee(periodOpt.get().getId(), employeeId, isLeader);
				if (result != null && result.size() > 0) {
	        	     dto.setTotal(getDouble(result.get(0)[1]));
	        	     totalValidatedGoal = dto.getTotal();
	        	     processes.add(dto);
	        	} 
			}	
		}
		
		//Configuración para aplicar castigo a evaluaciones con cero
		Optional<Boolean> configCastigateOpt = companyRepository.hasConfigurationCompany(companyId, 254L);
		Boolean configCastigate=configCastigateOpt.isPresent()? configCastigateOpt.get() : false;
		
		/**
		 * Realiza la validación de que hayan resultados en competencias y metas(u okrs) a la vez para multiplicar por sus respectivos pesos
		 * en el desempeño, si no hay valores en algún módulo se toma el valor completo del otro
		 */
		Boolean totalCompetencesZeroCast= totalValidatedComp!=null && totalValidatedComp==0 && (configCastigate);
		Boolean totalGoalsZeroCast=totalValidatedGoal!=null && totalValidatedGoal==0 && ( configCastigate);
		
	
		
		if ((totalValidatedComp != null &&  (totalValidatedComp!=0 || totalCompetencesZeroCast)) && 
				(totalValidatedGoal != null && (totalValidatedGoal!=0 || totalGoalsZeroCast))) {
			avg = (totalValidatedComp * company.getCompetencespercentage().doubleValue() / 100) + (totalValidatedGoal * company.getGoalspercentage().doubleValue() / 100);			
		} else if ((totalValidatedComp == null || totalValidatedComp==0)  && (totalValidatedGoal != null)) {
			avg = totalValidatedGoal;	
			if(totalValidatedComp==null) {
			   processes.get(0).setWeight(100.0);
			}
		} else if (totalValidatedComp != null && (totalValidatedGoal == null || totalValidatedGoal==0)) {
			avg = totalValidatedComp;
			if(totalValidatedGoal==null) {
			  processes.get(0).setWeight(100.0);
			}
		}		

		moduleDetail.setAverage(avg);
		moduleDetail.setProcesses(processes);

		return moduleDetail;
	}

	/**
	 * Obtiene el listado de resultados de performance para los empleados de una
	 * compañía dependiendo de su configuración
	 * 
	 * @param companyId Identificador de la compañía
	 * @param periodId  Identificador de un período de desempeño
	 * @param pageable  Paginador
	 * @param filters   Filtros para la consulta
	 */
	@SuppressWarnings("unchecked")
	public <T> PageableResponse<T> getEmployeeResultsPerformance(Long companyId, Long periodId, Pageable pageable,
			PerformanceFiltersDTO filters, Long employeeId, Long goalPeriodId, Long evaluationId) {

		collaboratorIds = null;
		// Validación si es líder, obtiene colaboradores a cargo
		if (employeeId != null) {
			collaboratorIds =  employeeRepository.findCollaboratorsIdByBoss(employeeId).get();
		}
		
		PageableResponse<T> pages = null;
		Integer maxResults = null;
		Integer startIndex = null;
		if (pageable != null) {
			maxResults = pageable.getPageSize();
			startIndex = pageable.getPageSize() * pageable.getPageNumber();
		}
		PerformanceFilter performanceFilter = PerformanceFilter.newFilter(filters.getName(), filters.getTypeName(),
				filters.getJobCategories(), filters.getDivisions(), collaboratorIds);

		// Consulta listado de empleados con sus respectivos filtros
		List<EmployeePerformanceDTO> collaborators = performanceDynamicResultsDAO
				.getEmployeesWithPerformanceFilters(companyId, performanceFilter, startIndex, maxResults);

		// Obtiene resultados y procesos
		collaborators.stream().forEach(collab -> {
			PerformanceDetailEmployeeDTO resultEmployee = getAverageAndDetailPerformanceEmployee(companyId,
					collab.getEmployeeId(), periodId, false, goalPeriodId, evaluationId);
			collab.setResult(resultEmployee.getAverage() != null ? resultEmployee.getAverage() : 0.0);
			collab.setProcesses(resultEmployee.getProcesses());
			collab.setFormatType(resultEmployee.getType());
			// Cuando es líder, trae la cantidad de colaboradores a cargo en su equipo
			if (employeeId != null) {
				Optional<Employee> employeeOpt = employeeRepository.findById(collab.getEmployeeId());
				if (employeeOpt.isPresent()) {
					collab.setCountCollaborators(employeeRepository.countCollaboratorsByBoss(employeeOpt.get()));				
				}				
			}
		});

		pages = new PageableResponse<T>();
		pages.setElements((List<T>) collaborators);
		pages.setTotal(performanceDynamicResultsDAO.getCountEmployeesWithPerformanceFilters(companyId, performanceFilter));

		return pages;
	}

	/**
	 * Obtiene promedio general de desempeño de una compañía, puede ser por los
	 * procesos configurados en la empresa (antiguo) o según lo configurado en un
	 * período de desempeño dinámico (nuevo)
	 * 
	 * @param companyId Identificador de la compañía
	 * @param periodId  Identificador de un período de desempeño
	 */

	public Map<String,String> getGeneralAvgPerformance(Long companyId, Long periodId, Long employeeId, Long goalPeriodId, Long evaluationId) {
		
		collaboratorIds = null;

		// Calcula promedio según procesos dinámicos configurados en el período
		if (periodId != null) {
			return getGeneralAvgDynamicPerformance(companyId, periodId, employeeId);
		} else {
			// Calcula promedio con configuración de empresa
			return getGeneralAvgOldPerformance(companyId, employeeId, goalPeriodId, evaluationId);
		}
	}

	/**
	 * Obtiene promedio de desempeño según procesos dinámicos configurados en el
	 * período
	 * 
	 * @param companyId Identificador de la compañía
	 * @param periodId  Identificador de un período de desempeño
	 */
	private Map<String,String> getGeneralAvgDynamicPerformance(Long companyId, Long periodId, Long employeeId) {
		
		Map<String,String> map = new HashMap<String, String>();
		
		Double finalAvg = 0.0;
		
		// Configuración de escala en metas
		Optional<Boolean> hasConfigOpt = companyRepository.hasConfigurationCompany(companyId, 251L);
		Boolean hasConfigScaleGoal=hasConfigOpt.isPresent()? hasConfigOpt.get():false;
		Optional<Company> company=companyRepository.findById(companyId);
		List<PerformancePeriodDetail> list=performancePeriodDetailRepository.findByPeriodId(periodId);
		String formatType=validateScaleOrPercentageDinamic(list, company.get(), hasConfigScaleGoal);

		// Si el promedio es para un líder, busca sus colaboradores a cargo y por cada
		// empleado verifica que procesos están asociados a esa categoría de cargo para
		// así hallar su promedio y después hacer un promedio de todos los colaboradores
		// del equipo	
		if (employeeId != null) {
			List<Long> collaboratorIds = null;	
			collaboratorIds =  employeeRepository.findCollaboratorsIdByBoss(employeeId).get();
			if (collaboratorIds != null && !collaboratorIds.isEmpty()) {
				List<Double> averageCollaborators = new ArrayList<>();	
				collaboratorIds.forEach(collab -> {
					PerformanceDetailEmployeeDTO resultEmployee = getAverageAndDetailPerformanceEmployee(companyId,
							collab, periodId, false, null, null);
					averageCollaborators.add(resultEmployee.getAverage());				
				});
				
				finalAvg = averageCollaborators.stream()
		                .filter(average -> average != null) 
		                .mapToDouble(Double::doubleValue)
		                .average().orElse(0.0);
			}
		} else {
			// Realiza promedio de compañía según las categorias de cargo configuradas en el periodo de desempeño
			List<Long> categoriesList = performancePeriodDetailRepository.getJobCategoriesByPerformancePeriod(periodId);
			if (categoriesList.size() > 0) {			
				
				List<Double> averagesList = new ArrayList<Double>();
				categoriesList.stream().forEach(job -> {
					
					// Consulta procesos asociados a esa categoría de cargo
					List<PerformancePeriodDetail> processList = null;
					if (job == null) {
						processList = performancePeriodDetailRepository.getPeriodDetailByPeriodIdAndLevelIdNull(periodId);
					} else {
						processList = performancePeriodDetailRepository.getPeriodDetailByPeriodIdAndLevelId(periodId, job);
					}				
					
					categoryAvg = 0.0;
					processList.stream().forEach(pr -> {
						if (pr.getModule().equals(PerformanceModule.COMPETENCES)) {
							Optional<Evaluation> optEvaluation = evaluationRepository.findById(pr.getProcessId());
							if (optEvaluation.isPresent()) {
								Double result = redshiftDAO.getAvgCompetencesByEvaluationRedshift(optEvaluation.get(), null,
										null, job,collaboratorIds, null, optEvaluation.get().getViewCalibration(), false);
								Double total=validateScaleOrPercentageCompetences(optEvaluation.get(), formatType.equals("PERCENTAGE"), hasConfigScaleGoal, result);
								categoryAvg = categoryAvg + (total * pr.getWeight());

							}
						} else if (pr.getModule().equals(PerformanceModule.GOALS)) {
							Optional<GoalPeriod> periodOpt = goalRepository.findById(pr.getProcessId());
							if (periodOpt.isPresent()) {
								Double result = redshiftDAO.getAvgGoalsByPeriodRedshift(periodOpt.get(), null, null, job,
										null, null, collaboratorIds, false);
								Double total=validateScaleOrPercentageGoals(periodOpt.get(), hasConfigScaleGoal, result,formatType.equals("PERCENTAGE"));
								categoryAvg = categoryAvg + (total * pr.getWeight());

							}
						} else if (pr.getModule().equals(PerformanceModule.OKRS)) {
							Optional<OKRPeriod> periodOpt = okrPeriodRepository.findById(pr.getProcessId());
							if (periodOpt.isPresent()) {
								Double result = okrProcessDAO.getAvgOkrsByPeriodAndJobLevel(periodOpt.get().getId(), job, collaboratorIds);
								categoryAvg = categoryAvg + (result * pr.getWeight());
							}
						} else if (pr.getModule().equals(PerformanceModule.CLIMATE)) {
							Optional<Evaluation> evaluationOpt = evaluationRepository.findById(pr.getProcessId());
							if (evaluationOpt.isPresent()) {
								String type = climateReportsService.getTypesEvaluationClimate(companyId, pr.getProcessId());
								Boolean isEnps = type.equals("ENPS") ? true : false;
								// Valida tipo de evaluación clima o enps
								Double result = this.climateService.getAvgClimate(evaluationOpt.get().getId(), null, isEnps,
										null, companyId, collaboratorIds);
								categoryAvg = categoryAvg + (result * pr.getWeight());
							}
						} else if (pr.getModule().equals(PerformanceModule.PID)) {
							Optional<DevelopIndividualPlan> dipOpt = developIndividualPlanRepository
									.getOnePid(pr.getProcessId());
							if (dipOpt.isPresent()) {
								Double result = performanceDynamicResultsDAO.getAvgPidCompanyAndJobLevel(pr.getProcessId(), job, collaboratorIds);
								categoryAvg = categoryAvg + (result * pr.getWeight());
							}
						}
					});
					categoryAvg = categoryAvg / 100;
					if (categoryAvg != 0.0)	averagesList.add(categoryAvg);					
					
				});
	
				// Promedio resultados de las categorías
				finalAvg = averagesList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
			}
		}
		
		map.put("avg", finalAvg.toString());
		map.put("formatType", formatType);
		
		return map;

	}

	/**
	 * Obtiene promedio de desempeño con configuración de empresa
	 * 
	 * @param companyId  Identificador de la compañía
	 * @param employeeId Identificador de empleado (líder)
	 */
	@SuppressWarnings("unused")
	private Map<String,String> getGeneralAvgOldPerformance(Long companyId, Long employeeId, Long goalPeriodId, Long evaluationId) {
		
		Double avg = 0.0;
		Company company = companyRepository.getOne(companyId);
		
		Map<String,String> map = new HashMap<String, String>();
		
		// Configuración de escala en metas
		Optional<Boolean> hasConfigOpt = companyRepository.hasConfigurationCompany(companyId, 251L);
		Boolean hasConfigScaleGoal = hasConfigOpt.isPresent()? hasConfigOpt.get():false;
		Double totalValidatedComp = null, totalValidatedGoal = null;
		List<Long> collaboratorIds = null;	
		
		if (employeeId != null) {
			collaboratorIds =  employeeRepository.findCollaboratorsIdByBoss(employeeId).get();
			if (collaboratorIds != null && !collaboratorIds.isEmpty()) {
				List<Double> averageCollaborators = new ArrayList<>();	
				collaboratorIds.forEach(collab -> {
					PerformanceDetailEmployeeDTO resultEmployee = getAverageAndDetailPerformanceEmployee(companyId,
							collab, null, false, goalPeriodId, evaluationId);
					averageCollaborators.add(resultEmployee.getAverage());				
				});
				
				avg = averageCollaborators.stream()
		                .filter(average -> average != null) 
		                .mapToDouble(Double::doubleValue)
		                .average().orElse(0.0);
			}

		} else {
			// Se valida evaluación de competencias configurada
			if ((company != null && company.getCompetencesevaluation() != null && company.getCompetencesevaluation()!=-1) || (company!=null && evaluationId!=null)) {
				Long evaluationIdComp=evaluationId!=null? evaluationId : company.getCompetencesevaluation();
				Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationIdComp);
				if (optEvaluation.isPresent()) {
					Double result = redshiftDAO.getAvgCompetencesByEvaluationRedshift(optEvaluation.get(), null, null, null, null, null, optEvaluation.get().getViewCalibration(), false);
					totalValidatedComp=validateScaleOrPercentageCompetences(optEvaluation.get(), company.getOkrsPeriod() != null, hasConfigScaleGoal, result);
					
				}
			}
			
			// Se valida período que tiene configurado, puede ser de metas u OKRs
			if ((company != null && company.getGoalsperiod() != null && company.getGoalsperiod()!=-1) || (company!=null && goalPeriodId!=null)) {
				Long periodId=goalPeriodId!=null? goalPeriodId : company.getGoalsperiod();
				Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
				if (periodOpt.isPresent() && periodOpt.get().getShowGoalResults()) {
					Double result = redshiftDAO.getAvgGoalsByPeriodRedshift(periodOpt.get(), null, null, null, null, null, null, false);
					totalValidatedGoal=validateScaleOrPercentageGoals(periodOpt.get(), hasConfigScaleGoal, result,false);
				}
				
			} else if (company != null && company.getOkrsPeriod() != null) {
				Optional<OKRPeriod> periodOpt = okrPeriodRepository.findById(company.getOkrsPeriod());
				if (periodOpt.isPresent()) {
					if (employeeId == null) {
						totalValidatedGoal = okrProcessDAO.getAvgOkrsCompany(periodOpt.get(), null);					
					} else {
						totalValidatedGoal = okrProcessDAO.getAvgOkrsByPeriodAndJobLevel(periodOpt.get().getId(), null, null);	
					}
				}
			}
			
			
			/**
			 * Realiza la validación de que hayan resultados en competencias y metas(u okrs) a la vez para multiplicar por sus respectivos pesos
			 * en el desempeño, si no hay valores en algún módulo se toma el valor completo del otro
			 */
			//Configuración para aplicar castigo a evaluaciones con cero
			Optional<Boolean> configCastigate = companyRepository.hasConfigurationCompany(companyId, 254L);
			Boolean totalCompetencesZeroCast=totalValidatedComp!=null && totalValidatedComp==0 && configCastigate.isPresent();
			Boolean totalGoalsZeroCast=totalValidatedGoal!=null && totalValidatedGoal==0 && configCastigate.isPresent();
			
			if ((totalValidatedComp != null &&  (totalValidatedComp!=0 || totalCompetencesZeroCast)) && 
					(totalValidatedGoal != null && (totalValidatedGoal!=0 || totalGoalsZeroCast))) {
				avg = (totalValidatedComp * company.getCompetencespercentage().doubleValue() / 100) + (totalValidatedGoal * company.getGoalspercentage().doubleValue() / 100);			
			} else if ((totalValidatedComp == null || totalValidatedComp==0)  && totalValidatedGoal != null) {
				avg = totalValidatedGoal;	
				
			} else if (totalValidatedComp != null && (totalValidatedGoal == null || totalValidatedGoal==0)) {
				avg = totalValidatedComp;
			}	
			
			
		}
		
	
		
		
		map.put("avg", avg.toString());
		map.put("formatType", company.getCompetencesResultFormat().equals("SCALE") && hasConfigScaleGoal? "SCALE" : "PERCENTAGE");
		
		return map;
	}
	
	
	/**
	 * Valida si competencias está en escala y Tiene resultados de Okrs o tiene metas en porcentaje,
	 *  se cambia los resultados de competencias a porcentaje 
	 **/
	private Double validateScaleOrPercentageCompetences(Evaluation evaluation, boolean isOkrs, boolean hasScaleGoal, Double total) {
		
		Double avgCompetences=total;
		if (evaluation.getCompany().getCompetencesResultFormat().equals("SCALE") && (isOkrs || !hasScaleGoal)) {
			avgCompetences = (total * 100) / evaluation.getComplimit();
		} 
		return avgCompetences;
	}
	
	/**
	 * Valida si Metas está en escala y competencias en porcentaje para convertir resultados de metas a porcentaje
	 * O valida si está en porcentaje que su valor límite no sea en escala 
	 * Se validan las variables porque prima el porcentaje
	 **/
	private Double validateScaleOrPercentageGoals( GoalPeriod period,  Boolean hasScaleGoal, Double total, Boolean hasOtherModuleWithPercentage) {
		
		Double avgGoal=total;
		Boolean hasPercentageGoal= ((!period.getCompany().getCompetencesResultFormat().equals("SCALE") && hasScaleGoal)
				|| (!hasScaleGoal && period!=null && period.getGoalLimit().longValue() < 100L) || (hasOtherModuleWithPercentage && hasScaleGoal) );
		
		if(hasPercentageGoal) {
			avgGoal = (total * 100) / period.getGoalLimit().longValue();
		}
		
		return avgGoal;
	}

	/**
	 * Obtiene id de la plantilla de reporte de desempeño
	 * 
	 * @param companyId Identificador de la compañia
	 * @param evaluationId Identificador de evaluación de competencias
	 * @param goalPeriodId Identificador de periodo de metas
	 */
	public Long getPdfReportTemplate(Long companyId, Long evaluationId, Long goalPeriodId) {
		
		Long reportId = 0L;
		Company company = companyRepository.getOne(companyId);
		
		// Se valida evaluación de competencias configurada
		if ((company != null && company.getCompetencesevaluation() != null && company.getCompetencesevaluation()!=-1) || (company != null && evaluationId != null)) {
			Long evaluationIdComp = evaluationId != null ? evaluationId : company.getCompetencesevaluation();
			Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationIdComp);
			if (optEvaluation.isPresent()) {				
				reportId = performanceDynamicResultsDAO.getPdfPerformanceReportTemplate(companyId, evaluationIdComp);
			}
		}
		
		return reportId;
	}

	/**
	 * Indica si tiene un reporte de desempeño configurado para mostrar botón de descarga
	 * Además que tenga activa la configuración 50: Descargar reporte de resultados
	 * 
	 * @param companyId Identificador de la compañia
	 * @param evaluationId Identificador de evaluación de competencias
	 */
	public boolean showDownloadButton(Long companyId, Long evaluationId) {
		Company company = companyRepository.getOne(companyId);
		if (company != null) {
			if (companyRepository.hasConfigurationCompany(companyId, 50L).isPresent()) {
				if (evaluationId != null) {
					return company.getReporttemplate() != null || evaluationRepository.findById(evaluationId).get().getReportTemplate() != null ? true : false;	
				} else {
					return company.getReporttemplate() != null ? true : false;	
				}
				
			}
		}
		return false;
	}

	/**
	 * Obtiene reporte excel de desempeño
	 * 
	 * @param companyId    Identificador de compañía
	 * @param evaluationId Identificador de evaluación de competencias
	 * @param goalPeriodId Identificador de período de metas
	 * @param isOkrs       Indica si son resultados por Okrs o metas
	 */
	public byte[] getPerformanceExcelReport(Long companyId, Long evaluationId, Long goalPeriodId, Boolean isOkrs, boolean calibrated) {

		List<Object[]> headersName = redshiftDAO.getExtraFieldsNamesByCompany(companyId);
		List<Object[]> extraFields = null;
		if (headersName.size() > 0) {
			extraFields = redshiftDAO.getExtraFieldsGroupByEmployee(companyId, headersName.size());			
		}

		Company company = companyRepository.getOne(companyId);
		List<Object[]> infoTotal = getInfoTotalExcel(company, evaluationId, goalPeriodId, isOkrs, calibrated);
		String headerName = isOkrs ? getLabel("okrs", company.getLanguageCode())
				: getLabel("goals", company.getLanguageCode());
		return this.excelHandler.getPerformanceExcelReport(infoTotal, headersName, extraFields, company.getId(),
				headerName);
	}

	/**
	 * Obtiene listado de resultados generales de desempeño con información de
	 * campos extras
	 * 
	 * @param company      Información de la compañia
	 * @param evaluationId Identificador evaluación de competencias
	 * @param goalPeriodId Idenfificador de período de metas u okrs
	 * @param isOkrs       Indica si se visualiza resultados de metas u okrs
	 */
	public List<Object[]> getInfoTotalExcel(Company company, Long evaluationId, Long goalPeriodId, Boolean isOkrs, boolean calibrated) {

		List<Object[]> resultsPerformance = new ArrayList<>();

		// Valida procesos a tener en cuenta
		Long evaluationIdComp = evaluationId != null ? evaluationId : company.getCompetencesevaluation();
		Optional<Evaluation> optEvaluation = evaluationRepository.findById(evaluationIdComp);
		Long periodId = goalPeriodId != null ? goalPeriodId : company.getGoalsperiod();

		// Consulta resultados de competencias
		List<Object[]> listCompetences = redshiftDAO.getResultsGroupByEmployee(company.getId(), optEvaluation.get(),
				null, null, null, null, null, null, null, calibrated, null, null);

		// Consulta resultados de metas u okrs
		FiltersResultsDTO filters = new FiltersResultsDTO();
		List<Object[]> listGoalsOkrs = getGoalsOrOkrs(isOkrs, company, periodId, null, null, null, Boolean.FALSE,
				filters);
		Map<Long, Object[]> mapGoalsOkrs = listGoalsOkrs != null
				? listGoalsOkrs.stream().collect(Collectors.toMap(item -> getLong(item[0]), item -> item)) : null;
		
		// Configuración de escala en metas
		Optional<Boolean> hasConfigOpt = companyRepository.hasConfigurationCompany(company.getId(), 251L);
		Boolean hasConfigScaleGoal = isOkrs ? false : hasConfigOpt.isPresent() ? hasConfigOpt.get() : false;
		
		if (listCompetences != null && !listCompetences.isEmpty()) {
			for (Object[] objectComp : listCompetences) {
				Object[] performance = new Object[9];
				// Llena información del empleado
				performance[0] = (String) objectComp[1];
				performance[4] = (String) objectComp[3];
				performance[5] = (String) objectComp[9];
				performance[6] = (String) objectComp[5];
				performance[7] = (String) objectComp[8];
				performance[8] = getLong(objectComp[0]);
				// Resultado de competencias
				if (company.getCompetencesResultFormat().equals("SCALE")) {
					Double result = Math.round((Double) (objectComp[6]) * 100D) / 100D;
					Double totalValidated = validateScaleOrPercentageCompetences(optEvaluation.get(), isOkrs, hasConfigScaleGoal, result);
					performance[2] = totalValidated;
				} else {
					Double result = Math.round((Double) (objectComp[7]) * 100D) / 100D;
					Double totalValidated = validateScaleOrPercentageCompetences(optEvaluation.get(), isOkrs, hasConfigScaleGoal, result);
					performance[2] = totalValidated;
				}
				// Resultados de metas/okrs y desempeño
				Object[] objGoalsOkrs = mapGoalsOkrs != null ? mapGoalsOkrs.get(getLong(objectComp[0])) : null;
				if (objGoalsOkrs != null) {
					mapGoalsOkrs.remove(getLong(objectComp[0]));
					Double totalModule = 0.0;
					if (!isOkrs) {
						Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
						if (periodOpt.isPresent()) {
							Double result = Math.round((Double) (objGoalsOkrs[2]) * 100D) / 100D;
							Double totalValidated = validateScaleOrPercentageGoals(periodOpt.get(), hasConfigScaleGoal, result, false);
							totalModule = totalValidated;							
						}
					} else {
						totalModule = ((BigDecimal) objGoalsOkrs[1]).doubleValue();						
					}
					performance[3] = totalModule;
					//Configuración para aplicar castigo a evaluaciones con cero
					Optional<Boolean> configCastigate = companyRepository.hasConfigurationCompany(company.getId(), 254L);
					Boolean totalCompetencesZeroCast= (Double) performance[2]==0 && configCastigate.isPresent();
					Boolean totalGoalsZeroCast=(Double)performance[3]==0 && configCastigate.isPresent();
					
					
					if(totalCompetencesZeroCast  || totalGoalsZeroCast  || ( (Double)performance[2]!=0 && (Double)performance[3]!=0)){
						
					  performance[1] = ((Double) performance[2] * company.getCompetencespercentage().doubleValue() / 100)
							+ ((Double) performance[3] * company.getGoalspercentage().doubleValue() / 100);
					} else if((Double)performance[3]==0 && !configCastigate.isPresent())  {
						performance[1] = Math.round((Double) (performance[2]) * 100D) / 100D;
					}else if((Double)performance[2]==0 && !configCastigate.isPresent()) {
						 performance[1] = Math.round((Double) (performance[3]) * 100D) / 100D;
					}
					
					
				} else {
					performance[1] = Math.round((Double) (performance[2]) * 100D) / 100D;
				}
				resultsPerformance.add(performance);
			}
		}

		// Resultados de metas u okrs sin competencias
		if (mapGoalsOkrs != null && !mapGoalsOkrs.isEmpty()) {
			for (Map.Entry<Long, Object[]> entry : mapGoalsOkrs.entrySet()) {
				Object[] performance = new Object[9];
				Object[] value = entry.getValue();

				// Llena información del empleado
				performance[8] = getLong(value[0]);
				Object name= isOkrs? value[2] :value[1];
				performance[0] = (String) name;
				performance[4] = (String) value[3];
				performance[5] = (String) value[4];
				performance[6] = (String) value[5];
				performance[7] = (String) value[6];
				// Resultado del módulo y desempeño
				performance[1] = isOkrs ? ((BigDecimal) value[1]).doubleValue() : Math.round((Double) (value[2]) * 100D) / 100D;
				performance[3] = isOkrs ? ((BigDecimal) value[1]).doubleValue() : Math.round((Double) (value[2]) * 100D) / 100D;
				resultsPerformance.add(performance);
			}
		}

		return resultsPerformance;

	}

}
