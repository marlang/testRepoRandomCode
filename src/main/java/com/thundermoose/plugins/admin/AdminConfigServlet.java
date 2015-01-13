package com.thundermoose.plugins.admin;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.thundermoose.plugins.utils.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.atlassian.templaterenderer.TemplateRenderer;

public class AdminConfigServlet extends HttpServlet {
  private static final Logger log = LoggerFactory.getLogger(AdminConfigServlet.class);

  private final UserManager userManager;
  private final TemplateRenderer renderer;
  private final ServletUtils servletUtils;

  public AdminConfigServlet(UserManager userManager, TemplateRenderer renderer, ServletUtils servletUtils) {
    this.userManager = userManager;
    this.renderer = renderer;
    this.servletUtils = servletUtils;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    UserProfile user = userManager.getRemoteUser();
    if (user == null || !userManager.isSystemAdmin(user.getUserKey())) {
      servletUtils.redirectToLogin(request, response);
      return;
    }

    response.setContentType("text/html;charset=utf-8");
    renderer.render("admin.vm", response.getWriter());
  }

}