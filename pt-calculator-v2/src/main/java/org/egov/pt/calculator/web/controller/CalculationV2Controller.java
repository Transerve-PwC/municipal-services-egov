package org.egov.pt.calculator.web.controller;

import org.egov.pt.calculator.service.EstimationService;
import org.egov.pt.calculator.service.TranslationService;
import org.egov.pt.calculator.web.models.Calculation;
import org.egov.pt.calculator.web.models.CalculationReq;
import org.egov.pt.calculator.web.models.CalculationRes;
import org.egov.pt.calculator.web.models.propertyV2.AssessmentRequestV2;
import org.egov.pt.calculator.web.models.propertyV2.PropertyRequestV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import javax.validation.Valid;

@Controller
@RequestMapping("/propertytax/v2")
@Slf4j
public class CalculationV2Controller {


    private TranslationService translationService;

    private EstimationService estimationService;
    
    


    @Autowired
    public CalculationV2Controller(TranslationService translationService, EstimationService estimationService) {
        this.translationService = translationService;
        this.estimationService = estimationService;
    }

//    @PostMapping("/_estimate")
//    public ResponseEntity<CalculationRes> getTaxEstimation(@RequestBody @Valid AssessmentRequestV2 assessmentRequestV2) {
//
//    	log.info("===================== M[getTaxEstimation] assessmentRequestV2  {}",assessmentRequestV2.getAssessment().toString());
//        CalculationReq calculationReq = translationService.translate(assessmentRequestV2);
//        log.info("===================== M[getTaxEstimation] calculationReq  {}",calculationReq.getCalculationCriteria().get(0).toString());
//        
//        return new ResponseEntity<>(estimationService.getTaxCalculation(calculationReq), HttpStatus.OK);
//
//    }
    
    @PostMapping("/_estimate")
    public ResponseEntity<CalculationRes> getTaxEstimation(@RequestBody @Valid AssessmentRequestV2 assessmentRequestV2) {

    	log.info("===================== M[getTaxEstimation] assessmentRequestV2  {}",assessmentRequestV2.getAssessment().toString());
      CalculationReq calculationReq = translationService.translate(assessmentRequestV2);
      log.info("===================== M[getTaxEstimation] calculationReq  {}",calculationReq.getCalculationCriteria().get(0).toString());
      
      return new ResponseEntity<>(estimationService.fetchTaxCalculation(calculationReq), HttpStatus.OK);

  }

//    @PostMapping("/_calculate")
//	public ResponseEntity<Map<String, Calculation>> generateDemands(@RequestBody @Valid CalculationReq calculationReq) {
//		return new ResponseEntity<>(estimationService.CreateDemandWithoutCalculation(calculationReq), HttpStatus.OK);
//	}

}
