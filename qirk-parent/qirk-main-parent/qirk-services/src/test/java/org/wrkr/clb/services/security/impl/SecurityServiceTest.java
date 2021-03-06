package org.wrkr.clb.services.security.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.wrkr.clb.model.user.User;
import org.wrkr.clb.repo.user.UserRepo;
import org.wrkr.clb.services.BaseServiceTest;
import org.wrkr.clb.services.security.SecurityService;

public class SecurityServiceTest extends BaseServiceTest {

    private static final String privateUserEmail = "private_user@test.com";
    private static final String privateUserUsername = privateUserEmail;

    private static final String publicUserEmail = "public_user@test.com";
    private static final String publicUserUsername = publicUserEmail;

    private static final String disabledUserEmail = "disabled_user@test.com";
    private static final String disabledUserUsername = disabledUserEmail;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SecurityService securityService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void beforeTest() throws Exception {
        saveUser(privateUserEmail, privateUserUsername, DEFAULT_USER_PASSWORD);
        saveUser(publicUserEmail, publicUserUsername, DEFAULT_USER_PASSWORD);
        saveUser(disabledUserEmail, disabledUserUsername);
    }

    @After
    public void afterTest() {
        testRepo.clearTable(User.class);
    }

    @Test
    public void test_enabledUserIsAuthenticated() {
        User enabledUser = userRepo.getByUsername(privateUserUsername);

        securityService.isAuthenticated(enabledUser);
    }

    @Test
    public void test_disabledUserIsNotAuthenticated() {
        expectedException.expect(AuthenticationCredentialsNotFoundException.class);

        User disabledUser = userRepo.getByUsername(disabledUserUsername);

        securityService.isAuthenticated(disabledUser);
    }

    @Test
    public void test_nullUserIsNotAuthenticated() {
        expectedException.expect(AuthenticationCredentialsNotFoundException.class);

        securityService.isAuthenticated(null);
    }

    @Test
    public void test_userCanReadPublicUserProfile() {
        User publicUser = userRepo.getByUsername(publicUserUsername);
        User otherUser = userRepo.getByUsername(privateUserUsername);

        securityService.authzCanReadUserProfile(otherUser, publicUser.getId());
    }

    @Test
    public void test_userCanReadPrivateUserProfile() {
        User privateUser = userRepo.getByUsername(privateUserUsername);
        User otherUser = userRepo.getByUsername(publicUserUsername);

        securityService.authzCanReadUserProfile(otherUser, privateUser.getId());
    }
}
