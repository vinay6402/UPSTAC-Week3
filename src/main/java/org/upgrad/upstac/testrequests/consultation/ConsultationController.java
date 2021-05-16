package org.upgrad.upstac.testrequests.consultation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

  Logger log = LoggerFactory.getLogger(ConsultationController.class);

  @Autowired private TestRequestUpdateService testRequestUpdateService;

  @Autowired private TestRequestQueryService testRequestQueryService;

  @Autowired TestRequestFlowService testRequestFlowService;

  @Autowired private UserLoggedInService userLoggedInService;

  @GetMapping("/in-queue")
  @PreAuthorize("hasAnyRole('DOCTOR')")
  public List<TestRequest> getForConsultations() {
    /* fetched test requests having status as 'LAB_TEST_COMPLETED' by using findBy() from
    testRequestQueryService class */
    // return TestRequest object in list
    return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('DOCTOR')")
  public List<TestRequest> getForDoctor() {
    /* fetched all test requests assigned for current doctor by using findByDoctor() from
    testRequestQueryService class */
    // return TestRequest object in list
    return testRequestQueryService.findByDoctor(getLoggedInUser());
  }

  @PreAuthorize("hasAnyRole('DOCTOR')")
  @PutMapping("/assign/{id}")
  public TestRequest assignForConsultation(@PathVariable Long id) {
    try {
      /* assign particular test request to current doctor by using assignForConsultation() from
      testRequestUpdateService class */
      // updateConsultation() return TestRequest object
      return testRequestUpdateService.assignForConsultation(id, getLoggedInUser());

    } catch (AppException e) {
      throw asBadRequest(e.getMessage());
    }
  }

  @PreAuthorize("hasAnyRole('DOCTOR')")
  @PutMapping("/update/{id}")
  public TestRequest updateConsultation(
      @PathVariable Long id, @RequestBody CreateConsultationRequest testResult) {
    try {
      /* update the result of the current test request id with test doctor comments by using
      updateConsultation() from testRequestUpdateService class*/
      // updateConsultation() return TestRequest object
      return testRequestUpdateService.updateConsultation(id, testResult, getLoggedInUser());
    } catch (ConstraintViolationException e) {
      throw asConstraintViolation(e);
    } catch (AppException e) {
      throw asBadRequest(e.getMessage());
    }
  }

  // this function return logged in user, created this method to avoid code redundancy
  private User getLoggedInUser() {
    return userLoggedInService.getLoggedInUser();
  }
}
