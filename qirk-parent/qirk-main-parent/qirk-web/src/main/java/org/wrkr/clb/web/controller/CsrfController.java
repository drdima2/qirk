package org.wrkr.clb.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wrkr.clb.services.http.CookieService;
import org.wrkr.clb.services.util.http.Cookies;
import org.wrkr.clb.services.util.http.SessionAttribute;
import org.wrkr.clb.web.http.Header;
import org.wrkr.clb.web.json.JsonContainer;

@RestController
@RequestMapping(path = "csrf", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CsrfController extends BaseExceptionHandlerController {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(CsrfController.class);

    @Autowired
    private HttpSessionCsrfTokenRepository httpSessionCsrfTokenRepository;

    @Autowired
    private CookieService cookieService;

    @GetMapping(value = "refresh")
    public JsonContainer<Void, Void> refresh(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) {
        String headerCsrfToken = request.getHeader(Header.X_CSRF_TOKEN);
        if (headerCsrfToken == null || !headerCsrfToken.equals(session.getAttribute(SessionAttribute.CSRF))) {
            String newToken = httpSessionCsrfTokenRepository.generateToken(request).getToken();
            session.setAttribute(SessionAttribute.CSRF, newToken);
            response = cookieService.addCookie(response, Cookies.CSRF, newToken, null, false);
        }
        return new JsonContainer<Void, Void>();
    }
}
