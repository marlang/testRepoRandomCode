package com.thundermoose.plugins;

import com.atlassian.stash.auth.HttpAuthenticationContext;
import com.atlassian.stash.auth.HttpAuthenticationHandler;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.thundermoose.plugins.admin.AdminConfigDao;
import com.thundermoose.plugins.user.UserConfig;
import com.thundermoose.plugins.user.UserConfigDao;
import com.thundermoose.plugins.utils.Encrypter;
import com.thundermoose.plugins.utils.ServletUtils;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class TokenAuthenticationHandler implements HttpAuthenticationHandler {
  private static final Logger log = LoggerFactory.getLogger(TokenAuthenticationHandler.class);
  public static final String KEY_CONTAINER_AUTH_NAME = "auth.container.remote-user";
  public static final String USER_HEADER = "X-Auth-User";
  public static final String TOKEN_HEADER = "X-Auth-Token";

  private final UserService userService;
  private final ServletUtils servletUtils;
  private final UserConfigDao userDao;
  private final AdminConfigDao adminDao;

  public TokenAuthenticationHandler(UserService userService, ServletUtils servletUtils, UserConfigDao userDao, AdminConfigDao adminDao) {
    this.userService = userService;
    this.servletUtils = servletUtils;
    this.userDao = userDao;
    this.adminDao = adminDao;
  }

  @Override
  public void validateAuthentication(HttpAuthenticationContext httpAuthenticationContext) {

  }

  @Nullable
  @Override
  public StashUser authenticate(HttpAuthenticationContext httpAuthenticationContext) {
    HttpServletRequest request = httpAuthenticationContext.getRequest();
    String username = request.getHeader(USER_HEADER);
    String token = request.getHeader(TOKEN_HEADER);

    if (isNotEmpty(username) && isNotEmpty(token)) {
      log.warn("Token auth for user [" + username + "] allowed for [" + request.getRequestURI() + "]");
      if (isTokenValid(username, token)) {
        return userService.getUserByName(username);
      }
    }

    return null;
  }

  boolean isTokenValid(String username, String token) {
    Encrypter encrypter = new Encrypter(Base64.decodeBase64(adminDao.getAdminConfig().getKey()));
    String unencrypted = encrypter.decrypt(token);
    String[] split = unencrypted.split(":");

    if (split.length != 4) {
      //not a valid token
      return false;
    } else if (Objects.equals(split[0], username) && DateTime.now().isBefore(Long.parseLong(split[2]))) {
      //token is valid
      return true;
    } else if (Objects.equals(split[0], username) && DateTime.now().isAfter(Long.parseLong(split[2]))) {
      //token is expired, generate a new one
      userDao.setUserConfig(username, new UserConfig(encrypter.encrypt(servletUtils.generateTokenForUser(username, adminDao.getAdminConfig().getTtl()))));
    }
    return false;
  }

}