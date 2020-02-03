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
package org.wrkr.clb.common.mail;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.wrkr.clb.common.crypto.TokenGenerator;
import org.wrkr.clb.common.crypto.dto.TokenAndIvDTO;
import org.wrkr.clb.common.crypto.token.notification.NotificationSettingsTokenData;
import org.wrkr.clb.common.jms.notification.BaseTaskNotificationMessage;
import org.wrkr.clb.common.jms.notification.TaskCommentMessage;
import org.wrkr.clb.common.jms.notification.TaskUpdateNotificationMessage;
import org.wrkr.clb.common.util.strings.CharSet;

public class UserMailService extends BaseMailService {

    private static final Logger LOG = LoggerFactory.getLogger(UserMailService.class);

    // html templates
    private static final String DEFAULT_HTML_TEMPLATE = "message-templates/default-html-template-en.vm";
    private static final String NON_REGISTERED_HTML_TEMPLATE = "message-templates/non-registered-html-template-en.vm";
    @SuppressWarnings("unused")
    private static final String SUBSCRIPTION_FOOTER_HTML_TEMPLATE = "message-templates/subscription-footer-html-template-en.vm";

    // email subjects
    private static class Subject {
        private static final String REGISTRATION_CONFIRMATION = "Registration confirmation instructions";
        private static final String REGISTRATION = "Registration instructions";
        private static final String PASSWORD_RESET = "Password reset instructions";

        private static final String PROJECT_INVITE = "[Qirk] %s has invited you to %s";

        private static final String TASK_NOTIFICATION = "Qirk Report: task #%d in %s";
    }

    // front url config values
    private String frontUrl;
    private String activateTokenUrl;
    private String activateEmailTokenUrl;
    private String resetPasswordUrl;
    private String acceptInviteUrl;
    private String declineInviteUrl;
    private String manageSubscriptionsUrl;
    private String frontTaskUrl;

    public String getFrontUrl() {
        return frontUrl;
    }

    public void setFrontUrl(String frontUrl) {
        this.frontUrl = frontUrl;
    }

    public String getActivateTokenUrl() {
        return activateTokenUrl;
    }

    public void setActivateTokenUrl(String activateTokenUrl) {
        this.activateTokenUrl = activateTokenUrl;
    }

    public String getActivateEmailTokenUrl() {
        return activateEmailTokenUrl;
    }

    public void setActivateEmailTokenUrl(String activateEmailTokenUrl) {
        this.activateEmailTokenUrl = activateEmailTokenUrl;
    }

    public String getResetPasswordUrl() {
        return resetPasswordUrl;
    }

    public void setResetPasswordUrl(String resetPasswordUrl) {
        this.resetPasswordUrl = resetPasswordUrl;
    }

    public String getAcceptInviteUrl() {
        return acceptInviteUrl;
    }

    public void setAcceptInviteUrl(String acceptInviteUrl) {
        this.acceptInviteUrl = acceptInviteUrl;
    }

    public String getDeclineInviteUrl() {
        return declineInviteUrl;
    }

    public void setDeclineInviteUrl(String declineInviteUrl) {
        this.declineInviteUrl = declineInviteUrl;
    }

    public String getManageSubscriptionsUrl() {
        return manageSubscriptionsUrl;
    }

    public void setManageSubscriptionsUrl(String manageSubscriptionsUrl) {
        this.manageSubscriptionsUrl = manageSubscriptionsUrl;
    }

    public String getFrontTaskUrl() {
        return frontTaskUrl;
    }

    public void setFrontTaskUrl(String frontTaskUrl) {
        this.frontTaskUrl = frontTaskUrl;
    }

    @Autowired
    private TokenGenerator tokenGenerator;

    private TokenAndIvDTO generateNotificationSettingsToken(String userEmail, String notificationSettingsType) throws Exception {
        NotificationSettingsTokenData tokenData = new NotificationSettingsTokenData(userEmail, notificationSettingsType);
        return tokenGenerator.encrypt(tokenData.toJson());
    }

    protected String renderSubscriptionFooterHTMLTemplate(String body, TokenAndIvDTO tokenDTO)
            throws UnsupportedEncodingException {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("body", body);
        velocityContext.put("url",
                frontUrl + manageSubscriptionsUrl +
                        "?token=" + URLEncoder.encode(tokenDTO.token, CharSet.UTF8_UPPER) +
                        "&iv=" + URLEncoder.encode(tokenDTO.IV, CharSet.UTF8_UPPER));
        return renderTemplate(DEFAULT_HTML_TEMPLATE, velocityContext);
        // return renderTemplate(SUBSCRIPTION_FOOTER_HTML_TEMPLATE, velocityContext); TODO uncomment when front is ready
    }

    @SuppressWarnings("unused")
    private void sendEmailsWithSubscriptionToken(
            Collection<String> recipients, String subject, String body, String notificationSettingsType) throws Exception {
        long startTime = System.currentTimeMillis();

        for (String to : recipients) {
            body = renderSubscriptionFooterHTMLTemplate(
                    body, generateNotificationSettingsToken(to, notificationSettingsType));
            _sendEmail(to, subject, body);
        }

        if (LOG.isInfoEnabled()) {
            long resultTime = System.currentTimeMillis() - startTime;
            LOG.info("processed sendEmailsWithSubscriptionToken for subject " + subject + " in " + resultTime + " ms");
        }
    }

    public void sendServiceNewsEmail(List<String> recipients, String subject, String body) throws Exception {
        sendEmail(recipients, subject, body);
    }

    public EmailSentDTO sendConfirmationEmail(String emailAddress, String token) {
        try {
            String subject = Subject.REGISTRATION_CONFIRMATION;

            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("url", frontUrl + activateTokenUrl + token);

            String body = renderHTMLTemplate(DEFAULT_HTML_TEMPLATE,
                    renderTemplate("message-templates/registration-confirmation-en.vm", velocityContext));

            sendEmail(emailAddress, subject, body);
            return new EmailSentDTO(true);
        } catch (Exception e) {
            LOG.error("Could not send registration confirmation email", e);
            return new EmailSentDTO(false);
        }
    }

    public EmailSentDTO sendRegistrationEmail(String emailAddress, String password, String token) {
        try {
            String subject = Subject.REGISTRATION;

            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("url", frontUrl + activateEmailTokenUrl + token);
            velocityContext.put("password", password);

            String body = renderHTMLTemplate(DEFAULT_HTML_TEMPLATE,
                    renderTemplate("message-templates/registration-en.vm", velocityContext));

            sendEmail(emailAddress, subject, body);
            return new EmailSentDTO(true);
        } catch (Exception e) {
            LOG.error("Could not send registration email", e);
            return new EmailSentDTO(false);
        }
    }

    public EmailSentDTO sendPasswordResetEmail(String emailAddress, String token) {
        try {
            String subject = Subject.PASSWORD_RESET;

            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("url", frontUrl + resetPasswordUrl + token);

            String body = renderHTMLTemplate(DEFAULT_HTML_TEMPLATE,
                    renderTemplate("message-templates/password-reset-en.vm", velocityContext));

            sendEmail(emailAddress, subject, body);
            return new EmailSentDTO(true);
        } catch (Exception e) {
            LOG.error("Could not send password reset email", e);
            return new EmailSentDTO(false);
        }
    }

    public EmailSentDTO sendProjectInviteEmail(
            String emailAddress, String senderName, String projectName, String password, String token) {
        try {
            String subject = String.format(Subject.PROJECT_INVITE, senderName, projectName);

            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("frontUrl", frontUrl);
            velocityContext.put("acceptUrl", frontUrl + acceptInviteUrl + token);
            velocityContext.put("senderName", senderName);
            velocityContext.put("projectName", projectName);
            velocityContext.put("password", password);

            String body = renderHTMLTemplate(NON_REGISTERED_HTML_TEMPLATE,
                    renderTemplate("message-templates/project-invite-en.vm", velocityContext));

            sendEmail(emailAddress, subject, body);
            return new EmailSentDTO(true);
        } catch (Exception e) {
            LOG.error("Could not send password reset email", e);
            return new EmailSentDTO(false);
        }
    }

    private String generateTaskUrl(Map<String, Object> messageBody, Long taskNumber) {
        return frontUrl + String.format(frontTaskUrl,
                (String) messageBody.get(BaseTaskNotificationMessage.ORGANIZATION_UI_ID),
                (String) messageBody.get(BaseTaskNotificationMessage.PROJECT_UI_ID),
                taskNumber);
    }

    public void sendTaskCreatedEmail(List<String> recipients, Map<String, Object> messageBody) {
        if (recipients.isEmpty()) {
            return;
        }

        try {
            Long taskNumber = (Long) messageBody.get(TaskUpdateNotificationMessage.TASK_NUMBER);
            String projectName = (String) messageBody.get(TaskUpdateNotificationMessage.PROJECT_NAME);
            String subject = String.format(Subject.TASK_NOTIFICATION, taskNumber, projectName);

            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("createdByName",
                    ((String) messageBody.get(TaskUpdateNotificationMessage.UPDATED_BY_FULL_NAME)) + " " +
                            "@" + ((String) messageBody.get(TaskUpdateNotificationMessage.UPDATED_BY_USERNAME)));
            velocityContext.put("taskNumber", taskNumber);
            velocityContext.put("projectName", projectName);
            velocityContext.put("taskUrl", generateTaskUrl(messageBody, taskNumber));

            String body = renderTemplate("message-templates/task-created-en.vm", velocityContext);

            sendEmail(recipients, subject, body);
        } catch (Exception e) {
            LOG.error("Could not send task created email", e);
        }
    }

    public void sendTaskUpdatedEmail(List<String> recipients, Map<String, Object> messageBody) {
        if (recipients.isEmpty()) {
            return;
        }

        try {
            Long taskNumber = (Long) messageBody.get(TaskUpdateNotificationMessage.TASK_NUMBER);
            String projectName = (String) messageBody.get(TaskUpdateNotificationMessage.PROJECT_NAME);
            String subject = String.format(Subject.TASK_NOTIFICATION, taskNumber, projectName);

            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("updatedByName",
                    ((String) messageBody.get(TaskUpdateNotificationMessage.UPDATED_BY_FULL_NAME)) + " " +
                            "@" + ((String) messageBody.get(TaskUpdateNotificationMessage.UPDATED_BY_USERNAME)));
            velocityContext.put("taskNumber", taskNumber);
            velocityContext.put("projectName", projectName);
            velocityContext.put("taskUrl", generateTaskUrl(messageBody, taskNumber));

            Long oldAssignee = (Long) messageBody.get(TaskUpdateNotificationMessage.OLD_ASSIGNEE);
            Long newAssignee = (Long) messageBody.get(TaskUpdateNotificationMessage.NEW_ASSIGNEE);
            velocityContext.put("assigneeChanged", !Objects.equals(oldAssignee, newAssignee));
            velocityContext.put("newAssignee",
                    ((String) messageBody.get(TaskUpdateNotificationMessage.NEW_ASSIGNEE_FULL_NAME)) + " " +
                            "@" + ((String) messageBody.get(TaskUpdateNotificationMessage.NEW_ASSIGNEE_USERNAME)));

            String oldType = (String) messageBody.get(TaskUpdateNotificationMessage.OLD_TYPE);
            String newType = (String) messageBody.get(TaskUpdateNotificationMessage.NEW_TYPE);
            velocityContext.put("typeChanged", !newType.equals(oldType));
            velocityContext.put("newType",
                    messageBody.getOrDefault(TaskUpdateNotificationMessage.NEW_TYPE_HUMAN_READABLE, newType));

            String oldPriority = (String) messageBody.get(TaskUpdateNotificationMessage.OLD_PRIORITY);
            String newPriority = (String) messageBody.get(TaskUpdateNotificationMessage.NEW_PRIORITY);
            velocityContext.put("priorityChanged", !newPriority.equals(oldPriority));
            velocityContext.put("newPriority",
                    messageBody.getOrDefault(TaskUpdateNotificationMessage.NEW_PRIORITY_HUMAN_READABLE, newPriority));

            String oldStatus = (String) messageBody.get(TaskUpdateNotificationMessage.OLD_STATUS);
            String newStatus = (String) messageBody.get(TaskUpdateNotificationMessage.NEW_STATUS);
            velocityContext.put("statusChanged", !newStatus.equals(oldStatus));
            velocityContext.put("newStatus",
                    messageBody.getOrDefault(TaskUpdateNotificationMessage.NEW_STATUS_HUMAN_READABLE, newStatus));

            String body = renderTemplate("message-templates/task-updated-en.vm", velocityContext);

            sendEmail(recipients, subject, body);
        } catch (Exception e) {
            LOG.error("Could not send task updated email", e);
        }
    }

    public void sendTaskCommentedEmail(Collection<String> recipients, Map<String, Object> messageBody) {
        if (recipients.isEmpty()) {
            return;
        }

        try {
            Long taskNumber = (Long) messageBody.get(TaskCommentMessage.TASK_NUMBER);
            String projectName = (String) messageBody.get(TaskCommentMessage.PROJECT_NAME);
            String subject = String.format(Subject.TASK_NOTIFICATION, taskNumber, projectName);

            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("commentedByName",
                    (String) messageBody.get(TaskCommentMessage.SENDER_FULL_NAME) + "@ " +
                            (String) messageBody.get(TaskCommentMessage.SENDER_USERNAME));
            velocityContext.put("taskNumber", taskNumber);
            velocityContext.put("projectName", projectName);
            velocityContext.put("taskUrl", generateTaskUrl(messageBody, taskNumber));
            velocityContext.put("message", (String) messageBody.get(TaskCommentMessage.MESSAGE));

            String body = renderTemplate("message-templates/task-commented-en.vm", velocityContext);

            sendEmail(recipients, subject, body);
        } catch (Exception e) {
            LOG.error("Could not send task commented email", e);
        }
    }
}