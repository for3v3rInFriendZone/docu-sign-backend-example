package com.signexample.sign.controller;

import com.docusign.esign.client.ApiException;
import com.signexample.sign.model.RequestDocuSign;
import com.signexample.sign.model.ResponseDocuSign;
import com.signexample.sign.service.JWTSignService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class SignController {

  private static final String SIGN_URL = "/sign";

  private final JWTSignService jwtSignService;

  public SignController(JWTSignService jwtSignService) {
    this.jwtSignService = jwtSignService;
  }

  @PostMapping(SIGN_URL)
  public ResponseDocuSign signDocument(@RequestBody RequestDocuSign requestDocuSign) {

    try {
      return jwtSignService.signDocument(requestDocuSign);
    } catch (ApiException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new ResponseDocuSign("");
  }
}
