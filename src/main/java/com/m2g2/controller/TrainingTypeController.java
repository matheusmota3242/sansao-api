package com.m2g2.controller;

import com.m2g2.constants.EndpointConstant;
import com.m2g2.dto.response.TrainingTypeResponse;
import com.m2g2.model.TrainingType;
import com.m2g2.service.TrainingTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/training-type")
public class TrainingTypeController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeController.class);
    private final TrainingTypeService service;

    public TrainingTypeController(TrainingTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<TrainingTypeResponse> getAll() {
        logger.info(EndpointConstant.ENTRADA_SEM_ARGUMENTO, "getAll");
        List<TrainingTypeResponse> traniningTypesResponse = service.getAllNames();
        logger.info(EndpointConstant.SAIDA_COM_ARGUMENTO, "getAll", traniningTypesResponse);
        return traniningTypesResponse;
    }
}
