package com.acsendo.api.customreports.service;

import static com.acsendo.api.util.DataObjectUtil.getLong;
import static com.acsendo.api.util.DataObjectUtil.getString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.acsendo.api.customReports.model.GraphicCustomReport;
import com.acsendo.api.customReports.repository.CustomReportsGraphicsConfiguration;
import com.acsendo.api.customreports.dto.ResultDTO;

@Service
public class ExtraGraphicsService {
	
	@Autowired
	private CustomReportsGraphicsConfiguration graphicRepository;

	public List<ResultDTO> getGraphicsConfiguration(Long companyId, Long userId, String moduleName) {
		List<ResultDTO> selectedGraphics = new ArrayList<ResultDTO>();
		List<Object[]> list = graphicRepository.findGraphicsConfiguration(companyId, userId, moduleName);
		
		Function<Object[], ResultDTO> mapper = data -> {
			ResultDTO config = new ResultDTO();
			config.setId(getLong(data[0]));
			config.setName(getString(data[1]));
			return config;

		};
		
		selectedGraphics = list.stream().map(mapper).collect(Collectors.toList());
		return selectedGraphics;
	}

	public boolean saveGraphicsConfiguration(List<String> graphicConfigutarion, Long companyId, Long userId, String moduleName) {
		
		
			// se elimina todo lo relacionado con el modulo, user id y company id
			graphicRepository.deleteOptions(companyId, userId, moduleName);
			// se inserta la informacion nueva con el DTO que nos llego
			if(!graphicConfigutarion.isEmpty()) {				
				for (String permission : graphicConfigutarion) {
					GraphicCustomReport configuration = new GraphicCustomReport();
					configuration.setGraphicOption(permission);
					configuration.setCompanyId(companyId);
					configuration.setUserId(userId);
					configuration.setModuleName(moduleName);
					graphicRepository.save(configuration);
				}
			}
		
		return true;
	}
	
}
