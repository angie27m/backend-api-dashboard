package com.acsendo.api.customreports.service;

import static com.acsendo.api.util.DataObjectUtil.getDouble;
import static com.acsendo.api.util.DataObjectUtil.getLong;
import static com.acsendo.api.util.DataObjectUtil.getString;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acsendo.api.company.repository.CompanyRepository;
import com.acsendo.api.customReports.dao.ResultsRedshiftDAO;
import com.acsendo.api.customreports.dto.CategoryDTO;
import com.acsendo.api.customreports.dto.FiltersOkrsExcel;
import com.acsendo.api.customreports.dto.PeriodDTO;
import com.acsendo.api.customreports.dto.ResultDTO;
import com.acsendo.api.customreports.dto.SemaphoreDTO;
import com.acsendo.api.customreports.util.CustomReportsOKRSExcelHandler;
import com.acsendo.api.hcm.model.Company;
import com.acsendo.api.hcm.model.Label;
import com.acsendo.api.hcm.repository.DivisionRepository;
import com.acsendo.api.hcm.repository.LabelRepository;
import com.acsendo.api.okrs.dao.OKRProcessDAO;
import com.acsendo.api.okrs.model.KeyResult;
import com.acsendo.api.okrs.model.OKRPeriod;
import com.acsendo.api.okrs.model.OKRProcess;
import com.acsendo.api.okrs.model.OKRSemaphore;
import com.acsendo.api.okrs.repository.OKRPeriodRepository;
import com.acsendo.api.okrs.repository.OKRProcessRepository;
import com.acsendo.api.okrs.repository.OKRSemaphoreRepository;
import com.acsendo.api.okrs.repository.ObjectiveKeyResultRepository;

@Service
public class OkrReportsService {

	@Autowired
	private OKRPeriodRepository okrPeriodRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private OKRProcessRepository okrProcessRepository;

	@Autowired
	private OKRSemaphoreRepository okrSemaphoreRepository;

	@Autowired
	private LabelRepository labelRepository;

	@Autowired
	private OKRProcessDAO okrProcessDAO;
	
	@Autowired
	private CustomReportsOKRSExcelHandler excelHandler;
	
	@Autowired
	private DivisionRepository divisionrepo;
	
	@Autowired
	private ObjectiveKeyResultRepository okrRepository;
	
	@Autowired
	private ResultsRedshiftDAO redshiftDAO;
	

	/**
	 * Obtiene listado de los periodos de okrs de una empresa
	 * 
	 * @param companyId Identificador de la compañía
	 * @param employeeId Identificador del empleado
	 * @param isLeader   Indica si es colaborador o líder
	 * @return Listado de los períodos de okrs
	 */
	public List<PeriodDTO> getOkrPeriodsByCompany(Long companyId, Long employeeId, Boolean isLeader) {

		Company company = companyRepository.getOne(companyId);

		List<PeriodDTO> periods = new ArrayList<PeriodDTO>();

		Optional<List<OKRProcess>> optOkrProcess = okrProcessRepository.getOKRProcessByCompany(company);
		if (optOkrProcess.isPresent()) {
			Optional<List<OKRPeriod>> optListPeriods = null;
			
			if (employeeId == null) {				
				// Obtiene períodos generales de la empresa
				optListPeriods = okrPeriodRepository.getOKRPeriodsByOkrProcess(optOkrProcess.get().get(0));			
			} else if (employeeId != null && !isLeader) {
				// Se traen los períodos donde el empleado tiene algun KR asignado
				optListPeriods = okrPeriodRepository.getOKRPeriodsEmployeeByOkrProcess(optOkrProcess.get().get(0).getId(), employeeId);
			}
			
			if (optListPeriods.isPresent()) {
				periods = optListPeriods.get().stream().map(okr -> createNewOkrPeriodDTO(okr))
						.collect(Collectors.toList());
			}
			// Ordenamiento descendente según el id de creación
			periods = periods.stream().sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).collect(Collectors.toList());
		}

		return periods;

	}

	private PeriodDTO createNewOkrPeriodDTO(OKRPeriod period) {
		ZoneId defaultZoneId = ZoneId.systemDefault();
		Date startDate = Date.from(period.getStartDate().atStartOfDay(defaultZoneId).toInstant());
		Date endDate = Date.from(period.getEndDate().atStartOfDay(defaultZoneId).toInstant());
		PeriodDTO dto = new PeriodDTO(period.getId(), period.getName(), startDate, endDate);
		dto.setSemaphore(getSemaphore(period.getOkrProcess()));
		return dto;
	}

	/**
	 * Obtiene listado de los intervalos que componene el semaforo de un proceso de
	 * okrs
	 * 
	 * @param okrProcess Proceso de okrs
	 * @return Listado de los intervalos
	 */
	public List<SemaphoreDTO> getSemaphore(OKRProcess okrProcess) {

		List<SemaphoreDTO> semaphoreOkrs = new ArrayList<SemaphoreDTO>();
		OKRSemaphore okrSemaphore = okrSemaphoreRepository.findByoKRProcess(okrProcess);
		String languageCode = "es";

		// se verifica que el limite 1 tenga valor
		if (okrSemaphore.getRedLimit() != null) {
			addSemaphore(semaphoreOkrs, okrSemaphore.getRedLimit(), "#E46053", languageCode, "progress_state_AT_RISK");
		}

		if (okrSemaphore.getYellowLimit() != null) {
			addSemaphore(semaphoreOkrs, okrSemaphore.getYellowLimit(), "#FFC554", languageCode,
					"progress_state_IN_PROCESS");
		}

		if (okrSemaphore.getGreenLimit() != null) {
			addSemaphore(semaphoreOkrs, okrSemaphore.getGreenLimit(), "#BDD262", languageCode,
					"progress_state_ON_TIME");
		}

		if (okrSemaphore.getBlueLimit() != null) {
			addSemaphore(semaphoreOkrs, okrSemaphore.getBlueLimit(), "#08b0ba", languageCode,
					"progress_state_EXCEEDED");
		}

		return semaphoreOkrs;
	}

	/**
	 * Agrega información del semáforo a la lista
	 */
	public void addSemaphore(List<SemaphoreDTO> list, Integer limit, String limitColor, String language,
			String labelCode) {

		SemaphoreDTO dataCompOne = new SemaphoreDTO();
		// se setea el valor del limite al intervalo
		dataCompOne.setCompLimit(limit.doubleValue());
		// se setea el label del intervalo
		dataCompOne.setLabel(getLabelSemaphore(null, labelCode, language));
		// se setea el color al intervalo
		dataCompOne.setColor(limitColor);
		// se agrega a la lista de todos los limites
		list.add(dataCompOne);

	}

	/**
	 * Obtiene la información de los labels del semáforo
	 */
	private String getLabelSemaphore(Long companyId, String code, String language) {
		Label labelCompany = this.labelRepository.findByCompanyIdAndModuleAndCode(companyId, "okrs", code);
		Label labelDefault = this.labelRepository.findByModuleCode("okrs", code);

		String label = "";
		if (labelCompany != null) {
			label = getLabelLanguageDefault(labelCompany, language);
		} else if (labelDefault != null) {
			label = getLabelLanguageDefault(labelDefault, language);
		}

		return label;

	}

	private String getLabelLanguageDefault(Label label, String language) {

		String labelText = "";

		if (label != null) {
			switch (language) {
			case "es":
				labelText = label.getSpanish();
				break;
			case "en":
				labelText = label.getEnglish();
				break;
			case "pt":
				labelText = label.getPortuguese();
				break;
			case "fr":
				labelText = label.getFrench();
				break;
			}
		}
		return labelText;

	}

	/**
	 * Obtiene promedio de objetivos de compañia o departamento de la empresa en un
	 * periodo de okrs
	 * 
	 * @param periodId   Identificador del periodo de okrs
	 * @param divisionId Identificador de departamento(s)
	 */
	public Double getAvgOkrsCompany(Long periodId, String divisionId) {
		Optional<OKRPeriod> periodOpt = okrPeriodRepository.findById(periodId);
		Double avg = 0.0D;
		if (periodOpt.isPresent()) {
			avg = okrProcessDAO.getAvgOkrsCompany(periodOpt.get(), divisionId);
		}

		return avg;
	}

	public List<CategoryDTO> getAvgOkrsGroupByObjective(Long periodId, String divisionId) {
		List<CategoryDTO> listCategories = new ArrayList<CategoryDTO>();
		Optional<OKRPeriod> periodOpt = okrPeriodRepository.findById(periodId);

		if (periodOpt.isPresent()) {
			List<Object[]> list = okrProcessDAO.getAvgOkrsGroupByObjective(periodOpt.get(), divisionId);
			Function<Object[], CategoryDTO> mapper = data -> {
				CategoryDTO result = new CategoryDTO();
				result.setId(getLong(data[0]));
				result.setName(getString(data[1]));
				result.setValue(getDouble(data[2]));
				if (divisionId == null) {
					result.setResults(getAvgOkrIdGroupByDivision(result.getId()));
				}				
				return result;
			};
			listCategories = list.stream().map(mapper).collect(Collectors.toList());
		}

		return listCategories;
	}

	/**
	 * Obtiene promedio de objetivos por departamentos en un periodo de okrs
	 * 
	 * @param periodId Identificador del periodo de okrs
	 */
	public List<ResultDTO> getAvgOkrsGroupByDivision(Long periodId) {
		List<ResultDTO> results = new ArrayList<ResultDTO>();
		Optional<OKRPeriod> periodOpt = okrPeriodRepository.findById(periodId);

		if (periodOpt.isPresent()) {
			List<Object[]> list = okrProcessDAO.getAvgOkrsGroupByDivision(periodOpt.get());
			Function<Object[], ResultDTO> mapper = data -> {
				ResultDTO result = new ResultDTO();
				result.setId(getLong(data[0]));
				result.setName(getString(data[1]));
				result.setValue(getDouble(data[2]));
				return result;
			};
			results = list.stream().map(mapper).collect(Collectors.toList());
		}

		return results;
	}

	/**
	 * Obtiene promedio de objetivos por departamentos según un objetivo
	 * 
	 * @param okrId Identificador del objetivo
	 */
	public List<ResultDTO> getAvgOkrIdGroupByDivision(Long okrId) {
		List<ResultDTO> results = new ArrayList<ResultDTO>();

		List<Object[]> list = okrProcessDAO.getAvgOkrIdGroupByDivision(okrId);
		Function<Object[], ResultDTO> mapper = data -> {
			ResultDTO result = new ResultDTO();
			result.setId(getLong(data[0]));
			result.setName(getString(data[1]));
			result.setValue(getDouble(data[2]));
			return result;
		};
		results = list.stream().map(mapper).collect(Collectors.toList());

		return results;
	}
	
	/**
	 * Obtiene el promedio de los objetivos de la empresa en un año
	 * 
	 * @param companyId Identificador de la compañia
	 */
	
	public List<ResultDTO> getAvgOkrsByYears(Long companyId) {
		
		List<ResultDTO> results = new ArrayList<ResultDTO>();
		List<Object[]> list = okrProcessDAO.getAvgOkrByYears(companyId, null); 
		Function<Object[], ResultDTO> mapper = data -> {
			ResultDTO result = new ResultDTO();
			result.setId(getLong(data[0]));
			result.setName(getString( data[1]));
			result.setValue(getDouble(data[2]));
		
			return result;
		};
		results = list.stream().map(mapper).collect(Collectors.toList());
			
		return results;
	}
	/**
	 * Obtiene el promedio de cada periodo del año
	 * 
	 * @param companyId Identificador de la compañia
	 * @param year Año a consultar
	 */
	public List<ResultDTO> getPeriodsOkrsByYear(Long companyId, Long year) {
		
		List<ResultDTO> results = new ArrayList<ResultDTO>();
		List<Object[]> list = okrProcessDAO.getPeriodsOkrsByYear(companyId, year);
		
		Function<Object[], ResultDTO> mapper = data -> {
			ResultDTO result = new ResultDTO();
			result.setId(getLong(data[0]));
			result.setName(getString(data[1]));
			result.setValue(getDouble(data[2]));
			return result;
		};
		results = list.stream().map(mapper).collect(Collectors.toList());
		
		return results;
	}

	/**
	 * Obtiene promedio de key results o iniciativas de un empleado según un período de okrs
	 * 
	 * @param periodId Identificador del periodo de okrs
	 * @param employeeId Identificador del empleado
	 * @param showKR Indica si se muestran key results o iniciativas
	 */
	public List<ResultDTO> getOkrResultsByEmployee(Long periodId, Long employeeId, boolean showKR) {
		List<ResultDTO> results = new ArrayList<ResultDTO>();
		List<Object[]> list = new ArrayList<Object[]>();
		
		// Si es true se muestran KR, false para iniciativas
		if (showKR) {
			list = okrProcessDAO.getkrResultsByEmployee(periodId, employeeId);
		} else {
			list = okrProcessDAO.getInitiativesByEmployee(periodId, employeeId);
		}		
		
		Function<Object[], ResultDTO> mapper = data -> {
			ResultDTO result = new ResultDTO();
			result.setId(getLong(data[0]));
			result.setName(getString(data[1]));
			result.setValue(getDouble(data[2]));
			return result;
		};
		results = list.stream().map(mapper).collect(Collectors.toList());

		return results;
	}
	
	/**
	 * Obtiene el promedio de los objetivos de la empresa en un año para el excel
	 * 
	 * @param companyId Identificador de la compañia
	 */
	
	public List<CategoryDTO> getAvgOkrsByYearsExcel(Long companyId,String year) {
		
		List<CategoryDTO> results = new ArrayList<CategoryDTO>();
		List<Object[]> list = okrProcessDAO.getAvgOkrByYears(companyId, year); 
		
		Function<Object[], CategoryDTO> mapper = data -> {
			CategoryDTO result = new CategoryDTO();
			result.setId(getLong(data[0]));
			result.setName(getString( data[1]));
			result.setValue(getDouble(data[2]));
			result.setResults(this.getPeriodsOkrsByYear(companyId, Long.parseLong(result.getName())));
		
			return result;
		};
		results = list.stream().map(mapper).collect(Collectors.toList());
			
		return results;
	}
	
	public String concatDivisions(List<String> divisions) {
		String result = "";
		for(int i = 0; i < divisions.size(); i++) {
			if(i != divisions.size()-1) {
				result = result.concat(divisions.get(i)).concat(",");
			}else {
				result = result.concat(divisions.get(i));
			}
		}
		return result;
	}
	
	/**
	 * Obtiene los objetivos de uno o todos los departamentos para el reporte excel
	 * @param periodId
	 * @param divisionId
	 * @param companyId
	 * @param isFiltered
	 * @return
	 */
	public List<Object[]> getAvgOkrsGroupByObjectiveExcel(Long periodId, Long divisionId, Long companyId, boolean isFiltered, boolean isByCompany) {
		Optional<OKRPeriod> periodOpt = okrPeriodRepository.findById(periodId);
		
		List<Object[]> list = new ArrayList<Object[]>();
		
		if (periodOpt.isPresent()) {
			if(isFiltered && !isByCompany) {	
				list = okrProcessDAO.getAvgOkrsGroupByObjectiveExcel(periodOpt.get(), divisionId.toString());
			}else {
				Optional<List<String>> divisions=this.divisionrepo.getDivisionsIdByCompany(companyId, periodId);
				
				if(divisions.isPresent()) {
					String allDivisions = this.concatDivisions(divisions.get());
					list = okrProcessDAO.getAvgOkrsGroupByObjectiveExcel(periodOpt.get(), allDivisions);
				}
			}
		}
		
		return list;

	}
	
	/**
	 * Método que obtiene todos los datos para generar la sabana de datos de OKRS
	 * 
	 * @param companyId Identificador de la compañia
	 * @param periodId Identificador del periodo
	 * @param filters Filtros para generar excel
	 */
	public byte[] getOKRSExcelReport(Long companyId, Long periodId, FiltersOkrsExcel filters) {
		List<CategoryDTO> generalData = this.getAvgOkrsByYearsExcel(companyId, filters.getYear());
		List<CategoryDTO> companyData = this.getAvgOkrsGroupByObjective(periodId, null);
		List<Object[]> divisionsData = this.getAvgOkrsGroupByObjectiveExcel(periodId, filters.getDivisionId(), companyId, filters.getIsFiltered(), filters.getIsByCompany());
		List<Object[]> employeeData = this.okrProcessDAO.getInitiativesByEmployeeExcel(periodId, filters.getEmployeeId(), filters.getByKr());
		
		 List<Object[]> headersName = redshiftDAO.getExtraFieldsNamesByCompany(companyId);
		 List<Object[]> extraFields =new ArrayList<Object[]>();
		 if(headersName.size()>0) {
		    extraFields= redshiftDAO.getExtraFieldsGroupByEmployee(companyId, headersName.size());
		 }
		 
		
		return this.excelHandler.getOKRSExcelReport(companyId, filters.getIsFiltered(), filters.getIsByCompany(), generalData, companyData, 
				divisionsData, employeeData, headersName, extraFields);	
	}
	

   /**
    *  Devuelve los Objectivos de departamento junto con los resultados claves por empleado
    *  @param periodId Identificador del período
    *  @param employeeId Identificador del empleado
    * */	
   public List<CategoryDTO> getKeyResultsByEmployee(Long periodId, Long employeeId){
	   
	   List<CategoryDTO> objectives=new ArrayList<CategoryDTO>();
	   List<Long> idsEmployee=new ArrayList<Long>();
	   idsEmployee.add(employeeId);
	   
	   //Consultamos los resultados claves del empleado
	   Optional<List<KeyResult>> keyResults=okrRepository.getKeyResultsByCollaborators(idsEmployee, periodId);
	   
	   if(keyResults.isPresent()) {
		   
		   keyResults.get().forEach(keyR->{
			   
			   //Buscamos si el padre del Key result (Objetivo) ya se encuentra agregado al arreglo general
				Optional<CategoryDTO> optResult = objectives.stream()
						.filter(objective->objective.getId()==keyR.getParent().getId()).findFirst();
				
				if(optResult.isPresent()) {
					
					   ResultDTO kr=new ResultDTO();
					   kr.setId(keyR.getId());
					   kr.setName(keyR.getName());
					   kr.setValue(keyR.getProgress());
					   
					   Integer index = objectives.indexOf(optResult.get());
					   objectives.get(index).getResults().add(kr);
					
				}else {
					
				    CategoryDTO obj=new CategoryDTO();
					obj.setId(keyR.getParent().getId());
					obj.setName(keyR.getParent().getName());
					
					ResultDTO kr=new ResultDTO();
					kr.setId(keyR.getId());
					kr.setName(keyR.getName());
				    kr.setValue(keyR.getProgress());
					
					List<ResultDTO> results=new ArrayList<ResultDTO>();
					results.add(kr);
					obj.setResults(results);
					objectives.add(obj);
					
				}
			   
		   });
		   
	   }
	   
	   return objectives;
   } 

}
