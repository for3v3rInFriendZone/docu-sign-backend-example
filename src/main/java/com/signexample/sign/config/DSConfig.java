package com.signexample.sign.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public final class DSConfig {

  public static final String CLIENT_ID;
  public static final String IMPERSONATED_USER_GUID;
  public static final String TARGET_ACCOUNT_ID;
  public static final String OAUTH_REDIRECT_URI;
  public static final String PRIVATE_KEY;
  public static final String DS_AUTH_SERVER;
  public static final String ROLE_NAME;
  public static final String CLIENT_USER_ID;
  public static final String DOCUMENT_SUBJECT;
  public static final String ENVELOPE_STATUS;
  public static final String AUTHENTICATION_METHOD;

  static {
    // Try load from environment variables
    Map<String, String> config = loadFromEnv();

    if (config == null) {
      // Try load from properties file
      config = loadFromProperties();
    }

    CLIENT_ID = fetchValue(config, "DS_CLIENT_ID");
    IMPERSONATED_USER_GUID = fetchValue(config, "DS_IMPERSONATED_USER_GUID");
    TARGET_ACCOUNT_ID = fetchValue(config, "DS_TARGET_ACCOUNT_ID");
    PRIVATE_KEY = fetchValue(config, "DS_PRIVATE_KEY");
    DS_AUTH_SERVER = fetchValue(config, "DS_AUTH_SERVER"); // use account.docusign.com for production
    OAUTH_REDIRECT_URI = fetchValue(config, "OATH_REDIRECT_URI");
    ROLE_NAME = fetchValue(config, "ROLE_NAME");
    CLIENT_USER_ID = fetchValue(config, "CLIENT_USER_ID");
    DOCUMENT_SUBJECT = fetchValue(config, "DOCUMENT_SUBJECT");
    ENVELOPE_STATUS = fetchValue(config, "ENVELOPE_STATUS");
    AUTHENTICATION_METHOD = fetchValue(config, "AUTHENTICATION_METHOD");
  }

  /**
   * fetch configuration value by key.
   *
   * @param config preloaded configuration key/value map
   * @param name   key of value
   * @return value as string or default empty string
   */
  private static String fetchValue(Map<String, String> config, String name) {
    String val = config.get(name);

    if ("DS_TARGET_ACCOUNT_ID".equals(name) && "FALSE".equals(val)) {
      return null;
    }

    return ((val != null) ? val : "");
  }

  /**
   * This method check if environment variables exists and load it into Map
   *
   * @return Map of key/value of environment variables if exists otherwise, return null
   */
  private static Map<String, String> loadFromEnv() {
    String clientId = System.getenv("DS_CLIENT_ID");

    if (clientId != null && clientId.length() > 0) {
      return System.getenv();
    }

    return null;
  }

  /**
   * This method load properties located in config.properties file in the working directory.
   *
   * @return Map of key/value of properties
   */
  private static Map<String, String> loadFromProperties() {
    Properties properties = new Properties();
    InputStream input = null;

    try {
      input = DSConfig.class.getResourceAsStream("/config.properties");
      properties.load(input);
    } catch (IOException e) {
      throw new RuntimeException("can not load configuration file", e);
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          throw new RuntimeException("error occurs will closing input stream: ", e);
        }
      }
    }

    Set<Map.Entry<Object, Object>> set = properties.entrySet();
    Map<String, String> mapFromSet = new HashMap<String, String>();

    for (Map.Entry<Object, Object> entry : set) {
      mapFromSet.put((String) entry.getKey(), (String) entry.getValue());
    }

    return mapFromSet;
  }

  public static final String AUD() {
    if (DS_AUTH_SERVER != null && DS_AUTH_SERVER.startsWith("https://"))
      return DS_AUTH_SERVER.substring(8);
    else if (DS_AUTH_SERVER != null && DS_AUTH_SERVER.startsWith("http://"))
      return DS_AUTH_SERVER.substring(7);

    return DS_AUTH_SERVER;
  }

}
