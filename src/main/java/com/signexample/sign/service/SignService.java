package com.signexample.sign.service;

import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.Document;
import com.docusign.esign.model.EnvelopeDefinition;
import com.docusign.esign.model.EnvelopeSummary;
import com.docusign.esign.model.RecipientViewRequest;
import com.docusign.esign.model.Recipients;
import com.docusign.esign.model.SignHere;
import com.docusign.esign.model.Signer;
import com.docusign.esign.model.Tabs;
import com.docusign.esign.model.ViewUrl;
import com.signexample.sign.controller.SignController;
import com.signexample.sign.model.RequestDocuSign;
import com.signexample.sign.model.ResponseDocuSign;
import com.sun.jersey.core.util.Base64;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Service
public class SignService {

  private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6IjY4MTg1ZmYxLTRlNTEtNGNlOS1hZjFjLTY4OTgxMjIwMzMxNyJ9.eyJUb2tlblR5cGUiOjUsIklzc3VlSW5zdGFudCI6MTU2ODg4Mzc4NywiZXhwIjoxNTY4OTEyNTg3LCJVc2VySWQiOiJlZjBjYzEzYi02Y2U4LTQzM2MtODAyMS0yZWE5Nzg4MjcyNjAiLCJzaXRlaWQiOjEsInNjcCI6WyJzaWduYXR1cmUiLCJjbGljay5tYW5hZ2UiLCJvcmdhbml6YXRpb25fcmVhZCIsImdyb3VwX3JlYWQiLCJwZXJtaXNzaW9uX3JlYWQiLCJ1c2VyX3JlYWQiLCJ1c2VyX3dyaXRlIiwiYWNjb3VudF9yZWFkIiwiZG9tYWluX3JlYWQiLCJpZGVudGl0eV9wcm92aWRlcl9yZWFkIiwiZHRyLnJvb21zLnJlYWQiLCJkdHIucm9vbXMud3JpdGUiLCJkdHIuZG9jdW1lbnRzLnJlYWQiLCJkdHIuZG9jdW1lbnRzLndyaXRlIiwiZHRyLnByb2ZpbGUucmVhZCIsImR0ci5wcm9maWxlLndyaXRlIiwiZHRyLmNvbXBhbnkucmVhZCIsImR0ci5jb21wYW55LndyaXRlIl0sImF1ZCI6ImYwZjI3ZjBlLTg1N2QtNGE3MS1hNGRhLTMyY2VjYWUzYTk3OCIsImlzcyI6Imh0dHBzOi8vYWNjb3VudC1kLmRvY3VzaWduLmNvbS8iLCJzdWIiOiJlZjBjYzEzYi02Y2U4LTQzM2MtODAyMS0yZWE5Nzg4MjcyNjAiLCJhbXIiOlsiaW50ZXJhY3RpdmUiXSwiYXV0aF90aW1lIjoxNTY4ODgzNzg1LCJwd2lkIjoiYzFhNTQ0NGItMjRmMy00ZmVjLTk3OGUtZDRjZWIyZTBkYjA2In0.BmLNTDhbj5itmMltVRH39QrLcW-zqpqNQD3R6LwJMm9BXLaY6vhyZInr2bPJIC4WmXuoBxoBRNfsKliRWuzrNdTl3zrKRYIL0SRZw9UdNcMl0sH3zAqxwVP2agjgStLfTpjXxFcjwuNy-a1lgmJQWr6gatMJ9sG-glYPmDfa2hMojoF5n8KIDxTKMSy5qL8hSuszQAo9NiueEiQuZX6pFN4Eiv_ShnT9ZpkYrYRHTWq0CGQq-8XsgxijkwLJcWSG7uZAb9hW6MSGKx2zjJihNeufLcVTXyj8xubdAHfEUdSXQpVZmvug-zwtEN_w3a47ll15a6H209aEIb2N6ogYKA";
  private static final String ACCOUNT_ID = "c04cf817-aef1-4316-883c-62952ec2c025";
  private static final String BASE_PATH = "https://demo.docusign.net/restapi";
  private static final String DOC_PDF = "World_Wide_Corp_lorem.pdf";
  private static final String CLIENT_URL_REDIRECT = "http://localhost:4200/signed";

  public ResponseDocuSign signDocument(RequestDocuSign requestDocuSign) throws IOException, ApiException {
    byte[] buffer = readFile(DOC_PDF);
    String docBase64 = new String(Base64.encode(buffer));

    // Create the DocuSign document object
    Document document = new Document();
    document.setDocumentBase64(docBase64);
    document.setName("Example document"); // can be different from actual file name
    document.setFileExtension("pdf"); // many different document types are accepted
    document.setDocumentId("1"); // a label used to reference the doc

    // The signer object
    // Create a signer recipient to sign the document, identified by name and email
    // We set the clientUserId to enable embedded signing for the recipient
    Signer signer = new Signer();
    signer.setEmail(requestDocuSign.getEmail());
    signer.setName(String.format("%s %s", requestDocuSign.getFirstname(), requestDocuSign.getLastname()));
    signer.clientUserId("123");
    signer.recipientId("1");

    // Create a signHere tabs (also known as a field) on the document,
    // We're using x/y positioning. Anchor string positioning can also be used
    SignHere signHere = new SignHere();
    signHere.setDocumentId("1");
    signHere.setPageNumber("1");
    signHere.setRecipientId("1");
    signHere.setTabLabel("Sign here");
    signHere.setXPosition("290");
    signHere.setYPosition("147");

    // Add the tabs to the signer object
    // The Tabs object wants arrays of the different field/tab types
    Tabs signerTabs = new Tabs();
    signerTabs.setSignHereTabs(Arrays.asList(signHere));
    signer.setTabs(signerTabs);

    // Next, create the top level envelope definition and populate it.
    EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
    envelopeDefinition.setEmailSubject("Please sign this document");
    envelopeDefinition.setDocuments(Arrays.asList(document));
    // Add the recipient to the envelope object
    Recipients recipients = new Recipients();
    recipients.setSigners(Arrays.asList(signer));
    envelopeDefinition.setRecipients(recipients);
    envelopeDefinition.setStatus("sent"); // requests that the envelope be created and sent.

    // Step 2. Call DocuSign to create and send the envelope
    ApiClient apiClient = new ApiClient(BASE_PATH);
    apiClient.addDefaultHeader("Authorization", "Bearer " + ACCESS_TOKEN);
    EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
    EnvelopeSummary results = envelopesApi.createEnvelope(ACCOUNT_ID, envelopeDefinition);
    String envelopeId = results.getEnvelopeId();

    // Step 3. The envelope has been created.
    //         Request a Recipient View URL (the Signing Ceremony URL)
    RecipientViewRequest viewRequest = new RecipientViewRequest();
    // Set the url where you want the recipient to go once they are done signing
    // should typically be a callback route somewhere in your app.
    viewRequest.setReturnUrl(CLIENT_URL_REDIRECT);
    viewRequest.setAuthenticationMethod("None");
    viewRequest.setEmail(requestDocuSign.getEmail());
    viewRequest.setUserName(String.format("%s %s", requestDocuSign.getFirstname(), requestDocuSign.getLastname()));
    viewRequest.setClientUserId("123");
    // call the CreateRecipientView API
    ViewUrl results1 = envelopesApi.createRecipientView(ACCOUNT_ID, envelopeId, viewRequest);

    // Step 4. The Recipient View URL (the Signing Ceremony URL) has been received.
    //         The user's browser will be redirected to it.

    return new ResponseDocuSign(results1.getUrl());
  }

  // Read a file
  private byte[] readFile(String path) throws IOException {
    InputStream is = SignController.class.getResourceAsStream("/" + path);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[1024];
    while ((nRead = is.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }
    buffer.flush();

    return buffer.toByteArray();
  }
}
