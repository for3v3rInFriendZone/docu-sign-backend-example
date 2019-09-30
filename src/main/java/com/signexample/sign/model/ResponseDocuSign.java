package com.signexample.sign.model;

public class ResponseDocuSign {
  private String redirectUrl;

  public ResponseDocuSign(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }
}
