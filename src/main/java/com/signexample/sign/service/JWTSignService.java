package com.signexample.sign.service;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.api.TemplatesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.EnvelopeTemplateResult;
import com.docusign.esign.model.EnvelopeTemplateResults;
import com.docusign.esign.model.RecipientViewRequest;
import com.docusign.esign.model.TemplateRole;
import com.docusign.esign.model.ViewUrl;
import com.signexample.sign.config.DSConfig;
import com.signexample.sign.model.RequestDocuSign;
import com.signexample.sign.model.ResponseDocuSign;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
public class JWTSignService extends DSBase {

  private final String TEMPLATE_NOT_FOUND = "Template not found";

  public JWTSignService(ApiClient apiClient) {
    super(apiClient);
  }

  public ResponseDocuSign signDocument(RequestDocuSign requestDocuSign) throws ApiException, IOException {
    this.checkToken();

    String fullName = String.format("%s %s", requestDocuSign.getFirstname(), requestDocuSign.getLastname());
    EnvelopeDefinition envelopeDefinition = createEnvelopeDefinition(requestDocuSign.getEmail(), fullName);

    EnvelopesApi envelopeApi = new EnvelopesApi(this.apiClient);
    EnvelopeSummary results = envelopeApi.createEnvelope(getAccountId(), envelopeDefinition);

    RecipientViewRequest viewRequest = createViewRequest(requestDocuSign.getEmail(), fullName);

    // call the CreateRecipientView API
    ViewUrl recipientView = envelopeApi.createRecipientView(getAccountId(), results.getEnvelopeId(), viewRequest);

    return new ResponseDocuSign(recipientView.getUrl());
  }

  //region Private creation methods

  private String getTemplateId() throws ApiException {
    TemplatesApi templatesApi = new TemplatesApi(apiClient);
    TemplatesApi.ListTemplatesOptions options = templatesApi.new ListTemplatesOptions();
    options.setSearchText("Test template");

    // get the results
    EnvelopeTemplateResults envelopeTemplateResults = templatesApi.listTemplates(getAccountId(), options);

    if (Integer.parseInt(envelopeTemplateResults.getResultSetSize()) > 0) {
      // Yes. Save the template id and name
      EnvelopeTemplateResult template = envelopeTemplateResults.getEnvelopeTemplates().get(0);
      return template.getTemplateId();
    }

    throw new ApiException(TEMPLATE_NOT_FOUND);
  }

  private EnvelopeDefinition createEnvelopeDefinition(String email, String name) throws ApiException {

    TemplateRole templateRole = new TemplateRole()
      .email(email)
      .name(name)
      .roleName(DSConfig.ROLE_NAME)
      .clientUserId(DSConfig.CLIENT_USER_ID);

    EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition()
      .emailSubject(DSConfig.DOCUMENT_SUBJECT)
      .templateId(getTemplateId())
      .templateRoles(Arrays.asList(templateRole))
      .status(DSConfig.ENVELOPE_STATUS);

    return envelopeDefinition;
  }

  private RecipientViewRequest createViewRequest(String signerEmail, String signerName) {

    RecipientViewRequest viewRequest = new RecipientViewRequest();
    viewRequest.setReturnUrl(DSConfig.OAUTH_REDIRECT_URI);
    viewRequest.setAuthenticationMethod(DSConfig.AUTHENTICATION_METHOD);
    viewRequest.setEmail(signerEmail);
    viewRequest.setUserName(signerName);
    viewRequest.setClientUserId(DSConfig.CLIENT_USER_ID);

    return viewRequest;
  }

  //endregion
}
