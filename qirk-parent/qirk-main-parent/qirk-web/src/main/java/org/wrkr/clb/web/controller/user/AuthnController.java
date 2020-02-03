/*
 * This file is part of the Java API to Qirk.
 * Copyright (C) 2020 Memfis LLC, Russia
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.wrkr.clb.web.controller.user;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.wrkr.clb.model.user.User;
import org.wrkr.clb.services.dto.user.PublicUserWithEmailDTO;
import org.wrkr.clb.services.dto.user.LoginDTO;
import org.wrkr.clb.services.http.CookieService;
import org.wrkr.clb.services.user.AuthnService;
import org.wrkr.clb.services.util.exception.BadRequestException;
import org.wrkr.clb.services.util.http.Cookies;
import org.wrkr.clb.services.util.http.SessionAttribute;
import org.wrkr.clb.web.controller.BaseAuthenticationExceptionHandlerController;
import org.wrkr.clb.web.http.Header;
import org.wrkr.clb.web.json.JsonContainer;


@RestController
@RequestMapping(path = "authn", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class AuthnController extends BaseAuthenticationExceptionHandlerController {

    @Autowired
    private AuthnService authnService;

    @Autowired
    private CookieService cookieService;

    @GetMapping(value = "check")
    public JsonContainer<PublicUserWithEmailDTO, Void> check(HttpSession session) {
        User user = (User) session.getAttribute(SessionAttribute.AUTHN_USER);
        return new JsonContainer<PublicUserWithEmailDTO, Void>(PublicUserWithEmailDTO.fromEntity(user));
    }

    @PostMapping(value = "login")
    public JsonContainer<Void, Void> login(
            HttpServletRequest request, HttpServletResponse response, HttpSession session, @RequestBody LoginDTO loginDTO)
            throws AuthenticationException, BadRequestException {
        long startTime = System.currentTimeMillis();

        response = authnService.login(request, response, session, loginDTO, request.getHeader(Header.X_FORWARDED_FOR));

        logProcessingTimeFromStartTime(startTime, "login");
        return new JsonContainer<Void, Void>();
    }

    @PostMapping(value = "logout")
    public JsonContainer<Void, Void> logout(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) {
        session.removeAttribute(SessionAttribute.AUTHN_USER);

        Cookie rememberMeCookie = cookieService.getCookie(request, Cookies.REMEMBER_ME);
        if (rememberMeCookie != null) {
            response = cookieService.removeCookie(response, rememberMeCookie);
        }

        return new JsonContainer<Void, Void>();
    }
}