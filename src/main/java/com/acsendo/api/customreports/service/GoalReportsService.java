package com.acsendo.api.customreports.service;

import static com.acsendo.api.util.DataObjectUtil.getLong;
import static com.acsendo.api.util.DataObjectUtil.getString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acsendo.api.company.repository.CompanyRepository;
import com.acsendo.api.customReports.dao.ResultsRedshiftDAO;
import com.acsendo.api.customreports.dto.CategoryDTO;
import com.acsendo.api.customreports.dto.FilterDTO;
import com.acsendo.api.customreports.dto.FiltersResultsDTO;
import com.acsendo.api.customreports.dto.PeriodDTO;
import com.acsendo.api.customreports.dto.ResponseDTO;
import com.acsendo.api.customreports.dto.GoalTrackDTO;
import com.acsendo.api.customreports.dto.GoalsExcelFiltersDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.dto.ResultDateDTO;
import com.acsendo.api.customreports.dto.SemaphoreDTO;
import com.acsendo.api.customreports.dto.StrategicGoalDTO;
import com.acsendo.api.customreports.util.CustomReportsGoalsExcelHandler;
import com.acsendo.api.goals.enumerations.GoalTrackType;
import com.acsendo.api.goals.model.GoalPeriod;
import com.acsendo.api.goals.model.GoalTrackingPeriodType;
import com.acsendo.api.goals.repository.GoalPeriodRepository;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.LabelFlex;
import com.acsendo.api.hcm.repository.EmployeeRepository;
import com.acsendo.api.hcm.repository.LabelFlexRepository;

@Service
public class GoalReportsService {

	@Autowired
	private GoalPeriodRepository goalRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private LabelFlexRepository labelRepository;

	@Autowired
	private ResultsRedshiftDAO resultsRedshifDao;

	@Autowired
	private CustomReportsGoalsExcelHandler excelHandler;
	
	@Autowired
	private EmployeeRepository employeeRepository;

	/**
	 * Obtiene listado de los periodos de metas de una empresa
	 * 
	 * @param companyId  Identificador de la compañía
	 * @param employeeId Identificador del empleado
	 * @param isLeader   Indica si el empleado es líder (true) o no (false)
	 * @return Listado de los períodos de metas
	 */
	public List<PeriodDTO> getGoalsByCompany(Long companyId, Long employeeId, Boolean isLeader, Boolean resultsDashboardState) {

		Company company = companyRepository.getOne(companyId);

		List<PeriodDTO> periods = new ArrayList<PeriodDTO>();

		if (company != null) {
			Optional<List<GoalPeriod>> optGoals = null;
			if (resultsDashboardState != null && resultsDashboardState) {
				optGoals = goalRepository.findGoalPeriodsByCompanyAndStateDifferentDelete(company.getId());				
			} else {
				if (employeeId == null) {
					optGoals = goalRepository.findGoalPeriodsByCompanyAndStateDifferentDelete(company.getId());				
				} else {
					// Procesos para resultados de colaboradores
					if (!isLeader) {
						List<Long> employeeIds=new ArrayList<Long>();
						employeeIds.add(employeeId);
						optGoals = goalRepository.findGoalsPeriodsByEmployeeId(company.getId(), employeeIds);	
					}else{
						Optional<List<Long>> opt=employeeRepository.findCollaboratorsIdByBoss(employeeId);
						if(opt.isPresent()) {
								optGoals=goalRepository.findGoalsPeriodsByEmployeeId(company.getId(), opt.get());
							
						}
					}
				}				
			}

			if (optGoals !=null && optGoals.isPresent()) {
				periods = optGoals.get().stream().map(goal -> createNewGoalPeriodDTO(goal)).collect(Collectors.toList());
			}
		}
		// Ordenamiento descendente según el id de creación
		periods = periods.stream().sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).collect(Collectors.toList());
		resultsRedshifDao.createGoalCompanyTable(companyId);

		return periods;
	}

	/**
	 * Crea nuevo período de metas con su respectiva información
	 */
	private PeriodDTO createNewGoalPeriodDTO(GoalPeriod period) {
		Long goalsLimit = 100L;
		if (period.getGoalLimit() != null) {
			goalsLimit = period.getGoalLimit().longValue();
		}
		PeriodDTO dto = new PeriodDTO(period.getId(), period.getName(), period.getStartdate(), period.getEnddate(),
				goalsLimit);
		dto.setSemaphore(getSemaphore(period.getId()));
		return dto;
	}

	/**
	 * Método que consulta los departamentos, sedes y/o niveles de cargos que tienen
	 * resultados de metas en un período específico
	 * 
	 * @param companyId  Identificador de la compañía
	 * @param periodId   Identificador del período de metas
	 * @param typeFilter Tipo de filtro: 'DIVISION', 'SUBSIDIARY', 'LEVEL'
	 */
	public List<FilterDTO> getFiltersGoals(Long companyId, Long periodId, String typeFilter, String subsidiariesAdmin) {

		List<Object[]> list = resultsRedshifDao.divisionsWithResultsGoals(companyId, periodId, typeFilter, subsidiariesAdmin);
		List<FilterDTO> filters = new ArrayList<FilterDTO>();
		filters = list.stream().map(data -> new FilterDTO(getLong(data[0]), getString(data[1])))
				.collect(Collectors.toList());

		return filters;
	}

	/**
	 * Obtiene listado de los intervalos que componene el semaforo de una meta
	 * 
	 * @param periodId Identificador del período de metas
	 * @return Listado de los intervalos
	 */
	public List<SemaphoreDTO> getSemaphore(Long periodId) {

		// cambiar nombre de DTO en caso de tener lo mismo
		List<SemaphoreDTO> semaphoreGoals = new ArrayList<SemaphoreDTO>();
		Optional<GoalPeriod> goalPeriodOpt = goalRepository.findById(periodId);
		
		if(goalPeriodOpt.isPresent()) {
			
			GoalPeriod goalPeriod=goalPeriodOpt.get();
			String languageCode = goalPeriod.getCompany().getLanguageCode();

			// se verifica que el limite 1 tenga valor
			if (goalPeriod.getLimit1() != null) {
				addSemaphore(semaphoreGoals, goalPeriod.getLimit1(), goalPeriod.getLimit1_color(), languageCode, periodId,
						"_RED", "#e46053", false);
			}

			// al no tener un orden especifico se realiza esta operacion anterior con cada
			// uno de los 5 limites
			if (goalPeriod.getLimit2() != null) {
				addSemaphore(semaphoreGoals, goalPeriod.getLimit2(), goalPeriod.getLimit2_color(), languageCode, periodId,
						"_ORANGE", "#f2972a", false);
			}
			if (goalPeriod.getLimit3() != null) {
				addSemaphore(semaphoreGoals, goalPeriod.getLimit3(), goalPeriod.getLimit3_color(), languageCode, periodId,
						"_YELLOW", "#ffd93b", false);
			}
			if (goalPeriod.getLimit4() != null) {
				addSemaphore(semaphoreGoals, goalPeriod.getLimit4(), goalPeriod.getLimit4_color(), languageCode, periodId,
						"_GREEN", "#bdd262", false);
			}

			BigDecimal limit = goalPeriod.getGoalLimit() != null ? goalPeriod.getGoalLimit() : new BigDecimal(100);
			addSemaphore(semaphoreGoals, limit, goalPeriod.getGoalLimit_color(), languageCode, periodId, "_BLUE",
					"#36bcc2", false);
			
		}
		

		// Ordena semáforo de límite menor a mayor
		List<SemaphoreDTO> semaphoreSorted = new ArrayList<SemaphoreDTO>();
		semaphoreSorted = semaphoreGoals.stream().sorted((d1, d2) -> d1.getCompLimit().compareTo(d2.getCompLimit()))
				.collect(Collectors.toList());

		return semaphoreSorted;
	}

	/**
	 * Agrega información del semáforo a la lista
	 */
	public void addSemaphore(List<SemaphoreDTO> list, BigDecimal limit, String limitColor, String language, Long goalId,
			String colorLetter, String colorCode, boolean isFromPerformance) {

		SemaphoreDTO dataCompOne = new SemaphoreDTO();
		// se setea el valor del limite al intervalo 1
		dataCompOne.setCompLimit(limit.doubleValue());
		// se setea el label del intervalo 1
		String labelCompany = isFromPerformance ? "_COMPANY" : "";
		LabelFlex label = labelRepository.findByLanguageCodeAndCode(language, goalId + labelCompany + colorLetter);
		if (label != null && label.getLabel() != null && !label.getLabel().equals("")) {
			dataCompOne.setLabel(label.getLabel());
		} else {
			dataCompOne
					.setLabel(labelRepository.findByLanguageCodeAndCode(language, "Default" + colorLetter).getLabel());
		}
		// se verifica si hay color personalizado para este limite y lo setea al
		// intervalo 1
		if (limitColor != null) {
			dataCompOne.setColor(limitColor);
		} else {
			dataCompOne.setColor(colorCode);
		}
		// se agrega a la lista de todos los limites
		list.add(dataCompOne);

	}

	/**
	 * Método que obtiene el promedio de metas de la compañía
	 * 
	 * @param periodId   Identificador del período de metas
	 * @param filters    Filtros para gráfica de metas
	 * @param employeeId Identificador del empleado
	 * @param isLeader   Indica si el empleado es líder (true) o no (false)
	 */
	public Double getAvgGoalsCompany(Long periodId, FiltersResultsDTO filters, Long employeeId, Boolean isLeader) {

		Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);

		Double avg = 0.0D;
		if (periodOpt.isPresent()) {
			avg = resultsRedshifDao.getAvgGoalsByPeriodRedshift(periodOpt.get(), filters.getDivisionsId(),
					filters.getSubsidiaryId(), filters.getLevelId(), filters.getSubsidiaries(), employeeId, null, false);
		}

		return avg;
	}

	/**
	 * Metodo que obtiene el promedio de resultados por metas estratégicas de un
	 * período
	 * 
	 * @param periodId Identificador del período de metas
	 * @return Listado de resultados
	 */
	public List<ResultDTO> getAvgStrategicGoals(Long periodId, String subsidiariesAdmin) {
		List<ResultDTO> results = new ArrayList<ResultDTO>();
		Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);

		if (periodOpt.isPresent()) {
			List<Object[]> list = resultsRedshifDao.getAvgStrategicGoals(periodOpt.get(), subsidiariesAdmin);
			Function<Object[], ResultDTO> mapper = data -> {
				ResultDTO result = new ResultDTO();
				result.setId(getLong(data[0]));
				result.setName(getString(data[1]));
				result.setValue((Double) data[2]);
				return result;
			};
			results = list.stream().map(mapper).collect(Collectors.toList());
		}

		return results;
	}

	/**
	 * Obtiene el promedio de metas agrupado por nivel de cargo y departamentos.
	 * 
	 * @param periodId Identificador del período de metas
	 * @param filters  Filtros que pueden aplicarse a la consulta (Departamento,
	 *                 sede, nivel de cargo)
	 */
	public List<CategoryDTO> getAvgGoalGroupByLevelAndDivision(Long periodId, FiltersResultsDTO filters) {

		Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
		List<CategoryDTO> listLevels = new ArrayList<CategoryDTO>();

		if (periodOpt.isPresent()) {

			List<Object[]> results = resultsRedshifDao.getAvgGoalsGroupByLevelAndDivisionRedshift(periodOpt.get(),
					filters.getDivisionsId(), filters.getSubsidiaryId(), filters.getLevelId(), false, filters.getSubsidiaries());

			results.stream().forEach(result -> {
				Optional<CategoryDTO> optCategory = listLevels.stream()
						.filter(data -> data.getId() == ((BigInteger) result[0]).longValue()).findFirst();
				if (optCategory.isPresent()) {
					ResultDTO resultDTO = new ResultDTO();
					resultDTO.setId(getLong(result[2]));
					resultDTO.setName(getString(result[3]));
					resultDTO.setValue((Double) result[4]);
					Integer index = listLevels.indexOf(optCategory.get());
					listLevels.get(index).getResults().add(resultDTO);
				} else {
					CategoryDTO categoryDTO = new CategoryDTO();
					categoryDTO.setId(getLong(result[0]));
					categoryDTO.setName(getString(result[1]));

					ResultDTO resultDTO = new ResultDTO();
					resultDTO.setId(getLong(result[2]));
					resultDTO.setName(getString(result[3]));
					resultDTO.setValue((Double) result[4]);
					List<ResultDTO> dtos = new ArrayList<ResultDTO>();
					dtos.add(resultDTO);
					categoryDTO.setResults(dtos);
					listLevels.add(categoryDTO);

				}
			});

		}

		return listLevels;
	}

	/**
	 * Metodo que obtiene el promedio de resultados de metas agrupado por
	 * departamentos
	 * 
	 * @param periodId Identificador del período de metas
	 * @return Listado de resultados
	 */
	public List<ResultDTO> getAvgGoalsGroupByDivision(Long periodId, String subsidiariesAdmin) {
		List<ResultDTO> results = new ArrayList<ResultDTO>();
		Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);

		if (periodOpt.isPresent()) {
			List<Object[]> list = resultsRedshifDao.getAvgGoalGroupByDivision(periodOpt.get(), subsidiariesAdmin, null, null,null);
			Function<Object[], ResultDTO> mapper = data -> {
				ResultDTO result = new ResultDTO();
				result.setId(getLong(data[0]));
				result.setName(getString(data[1]));
				result.setValue((Double) data[2]);
				return result;
			};
			results = list.stream().map(mapper).collect(Collectors.toList());
		}

		return results;
	}

	/**
	 * Metodo que obtiene la cantidad de seguimientos realizados y totales de una
	 * meta estratégica
	 * 
	 * @param periodId    Identificador del período de metas
	 * @param strategicId Identificador de la meta estratégica
	 */
	public StrategicGoalDTO getTracksByStrategicGoal(Long companyId, Long periodId, Long strategicId, String subsidiariesAdmin) {
		StrategicGoalDTO strategicGoals = new StrategicGoalDTO();
		Object[] object = resultsRedshifDao.getTracksByStrategicGoal(companyId, periodId, strategicId, subsidiariesAdmin);
		if (object != null) {
			strategicGoals.setDoneTracks(getLong(object[0]));
			strategicGoals.setTotalTracks(getLong(object[1]));
		}

		return strategicGoals;
	}

	/**
	 * Obtiene cumplimiento de los seguimientos según un período especifico de
	 * seguimento y una meta estratégica
	 * 
	 * @param periodId    Identificador del período de metas
	 * @param strategicId Identificador de la meta estratégica
	 * @param divisionId  Identificador del departamento
	 */
	public List<GoalTrackDTO> getAverageTracksByStrategicGoal(Long periodId, Long strategicId, Long divisionId,
			boolean showLeader, String subsidiariesAdmin) {
		List<GoalTrackDTO> resultTracks = new ArrayList<GoalTrackDTO>();
		Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
		if (periodOpt.isPresent()) {
			// Si el tipo de período de seguimiento es personalizado, se obtiene
			// directamente la cantidad de seguimientos
			if (periodOpt.get().getTracktype().equals(GoalTrackType.CUSTOM)) {
				GoalTrackDTO goal = new GoalTrackDTO();
				goal.setPeriodType(GoalTrackingPeriodType.CUSTOM);
				List<ResultDateDTO> results = new ArrayList<ResultDateDTO>();
				results = avgStrategicGoalsByPeriodType(GoalTrackingPeriodType.CUSTOM, periodOpt.get(), strategicId,
						divisionId, showLeader, subsidiariesAdmin);
				goal.setCountTracks(results.size());
				goal.setTracks(results);
				resultTracks.add(goal);
			} else {
				// Se lista los periodos definidos y se obtiene su respectiva cantidad de
				// seguimientos
				periodOpt.get().getGoalTrackPeriods().stream().forEach((p) -> {
					GoalTrackDTO goal = new GoalTrackDTO();
					goal.setPeriodType(p.getType());
					List<ResultDateDTO> results = new ArrayList<ResultDateDTO>();
					results = avgStrategicGoalsByPeriodType(p.getType(), periodOpt.get(), strategicId, divisionId,
							showLeader, subsidiariesAdmin);
					goal.setCountTracks(results.size());
					goal.setTracks(results);
					resultTracks.add(goal);
				});
			}
		}

		return resultTracks;
	}

	/**
	 * Obtiene promedio de resultados para los seguimientos realizados en cada
	 * período
	 */
	private List<ResultDateDTO> avgStrategicGoalsByPeriodType(GoalTrackingPeriodType trackType, GoalPeriod period,
			Long strategicId, Long divisionId, boolean showLeader, String subsidiariesAdmin) {
		List<ResultDateDTO> results = new ArrayList<ResultDateDTO>();

		List<Object[]> list = resultsRedshifDao.avgStrategicGoalsByPeriodType(trackType, period, strategicId,
				divisionId, showLeader, subsidiariesAdmin);
		Function<Object[], ResultDateDTO> mapper = data -> {
			ResultDateDTO result = new ResultDateDTO();
			result.setDate((String) data[1] + "-" + (String) data[0]);
			result.setValue((Double) data[2]);
			return result;
		};
		results = list.stream().map(mapper).collect(Collectors.toList());

		return results;
	}

	/**
	 * Método que obtiene todos los datos para generar la sábana de datos
	 * 
	 * @param companyId Identificador de la compañia
	 * @param periodId  Identificador del período de metas
	 * @param filters   filtros con los parametros para generar los datos
	 */
	public byte[] getGoalsExcelReport(Long companyId, Long periodId, GoalsExcelFiltersDTO filters, String subsidiariesAdmin) {

		// Información de campos extras
		List<Object[]> headersName = resultsRedshifDao.getExtraFieldsNamesByCompany(companyId);
		List<Object[]> extraFields = null;
		if (headersName.size() > 0) {
			extraFields = resultsRedshifDao.getExtraFieldsGroupByEmployee(companyId, headersName.size());			
		}
		
		List<Object[]> goalsInfoTotal = getInfoTotalGoalsExcel(periodId, subsidiariesAdmin);
		List<Object[]> goalsGroupByDivision = getAvgGoalGroupByLevelAndDivisionExcel(periodId, filters.getDivisionsId(),
				filters.getSubsidiaryId(), filters.getLevelId(), subsidiariesAdmin);
		List<ResultDTO> resultsByDivision = getAvgGoalsGroupByDivision(periodId, subsidiariesAdmin);
		List<ResultDTO> resultsByStrategic = getAvgStrategicGoals(periodId, subsidiariesAdmin);
		if (filters.getStrategicGoalId() == null) {
			filters.setStrategicGoalId(-1L);
		}
		
		List<Object[]> trackingsGoalResults = getAverageTracksByStrategicGoalExcel(periodId,
				filters.getStrategicGoalId(), filters.getShowLeader(), filters.getDivisionIdGoal(), subsidiariesAdmin);

		return this.excelHandler.getGoalsExcelReport(goalsInfoTotal, goalsGroupByDivision, resultsByDivision, resultsByStrategic,
				trackingsGoalResults, filters.getShowLeader(), headersName, extraFields, companyId);
	}

	/**
	 * Obtiene listado de resultados generales de metas
	 * 
	 * @param periodId     Identificador del período de metas
	 * @param divisionsId  Identificadores de los departamentos
	 * @param levelId      Identicador del nivel de cargo
	 * @param subsidiaryId Identificador de la sede
	 */
	public List<Object[]> getAvgGoalGroupByLevelAndDivisionExcel(Long periodId, String divisionsId, Long subsidiaryId,
			Long levelId, String subsidiariesAdmin) {

		Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);

		if (periodOpt.isPresent()) {
			List<Object[]> results = resultsRedshifDao.getAvgGoalsGroupByLevelAndDivisionRedshift(periodOpt.get(),
					divisionsId, subsidiaryId, levelId, true, subsidiariesAdmin);
			return results;
		}
		return null;
	}

	/**
	 * Obtiene listado de resultados de seguimientos de metas
	 * 
	 * @param periodId        Identificador del período de metas
	 * @param strategicGoalId Identificador de la meta estratégica
	 * @param showLeader      Indica si se muestran progresos por líder (true) o
	 *                        colaborador (false)
	 * @param divisionId      Identificador del departamento
	 */
	public List<Object[]> getAverageTracksByStrategicGoalExcel(Long periodId, Long strategicGoalId, Boolean showLeader,
			Long divisionId, String subsidiariesAdmin) {

		Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
		if (periodOpt.isPresent()) {
			// Si el tipo de período de seguimiento es personalizado, se obtiene
			// directamente sus resultados
			if (periodOpt.get().getTracktype().equals(GoalTrackType.CUSTOM)) {
				return resultsRedshifDao.avgStrategicGoalsByPeriodTypeExcel(GoalTrackingPeriodType.CUSTOM,
						periodOpt.get(), strategicGoalId, divisionId, showLeader, subsidiariesAdmin);
			} else {
				// Se lista los periodos definidos para este período de metas y se obtiene sus
				// respectivos resultados
				List<Object[]> resultsPeriods = new ArrayList<Object[]>();
				periodOpt.get().getGoalTrackPeriods().stream().forEach((p) -> {
					resultsPeriods.addAll(resultsRedshifDao.avgStrategicGoalsByPeriodTypeExcel(p.getType(),
							periodOpt.get(), strategicGoalId, divisionId, showLeader, subsidiariesAdmin));
				});
				return resultsPeriods;
			}
		}
		return null;
	}
	
	
	/**
	 * Método que ejecuta procedimiento almacenado para volver a calcular resultados de un período de metas
	 * 
	 * */
	public void executeStoredProcedureGoals(Long companyId, Long goalPeriodId) {
		
		resultsRedshifDao.executeStoredProcedureGoals(companyId, goalPeriodId);
	}

	/**
	 * Metodo que obtiene el listado de metas de un empleado con su promedio y peso
	 * 
	 * @param periodId   Identificador del período de metas
	 * @param employeeId Identificador del empleado
	 * @param isLeader   Indica si es colaborador o líder
	 */
	public List<ResponseDTO> getGoalsListByEmployee(Long periodId, Long employeeId, Boolean isLeader) {

		List<ResponseDTO> goalsList = new ArrayList<ResponseDTO>();
		Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
		if (periodOpt.isPresent()) {

			List<Object[]> list = resultsRedshifDao.getGoalsListByEmployee(periodOpt.get(), employeeId, isLeader);

			if (!list.isEmpty()) {
				Function<Object[], ResponseDTO> mapper = data -> {
					ResponseDTO resultGoal = new ResponseDTO();
					resultGoal.setId(getLong(data[0]));
					resultGoal.setLabel(getString(data[1]));
					resultGoal.setPercentage((Double) data[3]);
					resultGoal.setCountResponses(((Double) data[2]).intValue());
					resultGoal.setWeight((Double) data[2]);
					return resultGoal;
				};
				goalsList = list.stream().map(mapper).collect(Collectors.toList());
			}
		}

		return goalsList;
	}

	/**
	 * Obtiene id de la plantilla para reporte pdf de metas
	 * 
	 * @param companyId   Identificador de la compañía
	 * @param periodId    Identificador del período de metas
	 */
	public Long getPdfReportTemplate(Long companyId, Long periodId) {
		
		Long templateId = 0L;
		templateId = resultsRedshifDao.getPdfGoalsReportTemplate(periodId);
		return templateId;
	}
	
	/**
	 * Obtiene listado de resultados generales de metas con información de campos
	 * extras
	 * 
	 * @param periodId     Identificador del período de metas
	 * @param subsidiaryId Identificador de la sede
	 */
	public List<Object[]> getInfoTotalGoalsExcel(Long periodId, String subsidiariesAdmin) {

		Optional<GoalPeriod> periodOpt = goalRepository.findById(periodId);
		List<Object[]> results = new ArrayList<>();

		if (periodOpt.isPresent()) {
			results = resultsRedshifDao.getInfoTotalGoalsExcel(periodOpt.get(), subsidiariesAdmin);
		}
		return results;
	}

}
