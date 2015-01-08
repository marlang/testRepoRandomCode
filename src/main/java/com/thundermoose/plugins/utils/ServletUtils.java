package com.thundermoose.plugins.utils;

import com.atlassian.sal.api.auth.LoginUriProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class ServletUtils {
  private final LoginUriProvider loginUriProvider;

  public ServletUtils(LoginUriProvider loginUriProvider) {
    this.loginUriProvider = loginUriProvider;
  }

  public void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
  }

  public URI getUri(HttpServletRequest request) {
    StringBuffer builder = request.getRequestURL();
    if (request.getQueryString() != null) {
      builder.append("?");
      builder.append(request.getQueryString());
    }
    return URI.create(builder.toString());
  }
}
