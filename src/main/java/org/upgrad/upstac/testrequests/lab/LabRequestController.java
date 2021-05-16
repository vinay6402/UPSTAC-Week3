package org.upgrad.upstac.testrequests.lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
@RequestMapping("/api/labrequests")
public class LabRequestController {

  Logger log = LoggerFactory.getLogger(LabRequestController.class);

  @Autowired private TestRequestUpdateService testRequestUpdateService;

  @Autowired private TestRequestQueryService testRequestQueryService;

  @Autowired private TestRequestFlowService testRequestFlowService;

  @Autowired private UserLoggedInService userLoggedInService;

  @GetMapping("/to-be-tested")
  @PreAuthorize("hasAnyRole('TESTER')")
  public List<TestRequest> getForTests() {
    return testRequestQueryService.findBy(RequestStatus.INITIATED);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('TESTER')")
  public List<TestRequest> getForTester() {
    /* test requests assigned for current tester are fetched by using findByTester() from
    testRequestQueryService class*/
    // return TestRequest object in list
    return testRequestQueryService.findByTester(getLoggedInUser());
  }

  @PreAuthorize("hasAnyRole('TESTER')")
  @PutMapping("/assign/{id}")
  public TestRequest assignForLabTest(@PathVariable Long id) {
    return testRequestUpdateService.assignForLabTest(id, getLoggedInUser());
  }

  @PreAuthorize("hasAnyRole('TESTER')")
  @PutMapping("/update/{id}")
  public TestRequest updateLabTest(
      @PathVariable Long id, @RequestBody CreateLabResult createLabResult) {

    try {
      return testRequestUpdateService.updateLabTest(id, createLabResult, getLoggedInUser());
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
