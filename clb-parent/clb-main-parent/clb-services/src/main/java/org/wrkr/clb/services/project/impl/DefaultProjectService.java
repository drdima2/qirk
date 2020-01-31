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
package org.wrkr.clb.services.project.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Tuple;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.wrkr.clb.common.crypto.TokenGenerator;
import org.wrkr.clb.common.crypto.dto.TokenAndIvDTO;
import org.wrkr.clb.common.crypto.token.chat.ChatTokenData;
import org.wrkr.clb.common.jms.statistics.ProjectDocUpdateMessage;
import org.wrkr.clb.common.jms.statistics.StatisticsSender;
import org.wrkr.clb.common.util.chat.ChatType;
import org.wrkr.clb.common.util.strings.MarkdownUtils;
import org.wrkr.clb.model.InviteStatus;
import org.wrkr.clb.model.Language;
import org.wrkr.clb.model.organization.Organization;
import org.wrkr.clb.model.organization.OrganizationMember;
import org.wrkr.clb.model.project.Project;
import org.wrkr.clb.model.project.ProjectApplication;
import org.wrkr.clb.model.project.ProjectInvite;
import org.wrkr.clb.model.project.task.ProjectTaskNumberSequence;
import org.wrkr.clb.model.user.User;
import org.wrkr.clb.repo.LanguageRepo;
import org.wrkr.clb.repo.TagRepo;
import org.wrkr.clb.repo.organization.JDBCOrganizationRepo;
import org.wrkr.clb.repo.organization.OrganizationMemberRepo;
import org.wrkr.clb.repo.project.JDBCProjectRepo;
import org.wrkr.clb.repo.project.ProjectApplicationRepo;
import org.wrkr.clb.repo.project.ProjectInviteRepo;
import org.wrkr.clb.repo.project.ProjectRepo;
import org.wrkr.clb.repo.project.task.ProjectTaskNumberSequenceRepo;
import org.wrkr.clb.repo.project.task.TaskSubscriberRepo;
import org.wrkr.clb.services.TagService;
import org.wrkr.clb.services.dto.ChatPermissionsDTO;
import org.wrkr.clb.services.dto.ExistsDTO;
import org.wrkr.clb.services.dto.NameAndUiIdDTO;
import org.wrkr.clb.services.dto.RecordVersionDTO;
import org.wrkr.clb.services.dto.project.ProjectApplicationStatusDTO;
import org.wrkr.clb.services.dto.project.ProjectDTO;
import org.wrkr.clb.services.dto.project.ProjectDocDTO;
import org.wrkr.clb.services.dto.project.ProjectInviteOptionDTO;
import org.wrkr.clb.services.dto.project.ProjectInviteStatusDTO;
import org.wrkr.clb.services.dto.project.ProjectMemberDTO;
import org.wrkr.clb.services.dto.project.ProjectReadDTO;
import org.wrkr.clb.services.dto.project.ProjectWithOrganizationDTO;
import org.wrkr.clb.services.impl.BaseVersionedEntityService;
import org.wrkr.clb.services.project.ProjectMemberService;
import org.wrkr.clb.services.project.ProjectService;
import org.wrkr.clb.services.security.ProjectSecurityService;
import org.wrkr.clb.services.security.SecurityService;
import org.wrkr.clb.services.user.UserFavoriteService;
import org.wrkr.clb.services.util.exception.ApplicationException;
import org.wrkr.clb.services.util.exception.NotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;


//@Service configured in clb-services-ctx.xml
@Validated
public class DefaultProjectService extends BaseVersionedEntityService implements ProjectService {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(DefaultProjectService.class);

    // chat token config values
    private Integer chatTokenNotBeforeToleranceSeconds;
    private Integer chatTokenLifetimeSeconds;

    public Integer getChatTokenNotBeforeToleranceSeconds() {
        return chatTokenNotBeforeToleranceSeconds;
    }

    public void setChatTokenNotBeforeToleranceSeconds(Integer chatTokenNotBeforeToleranceSeconds) {
        this.chatTokenNotBeforeToleranceSeconds = chatTokenNotBeforeToleranceSeconds;
    }

    public Integer getChatTokenLifetimeSeconds() {
        return chatTokenLifetimeSeconds;
    }

    public void setChatTokenLifetimeSeconds(Integer chatTokenLifetimeSeconds) {
        this.chatTokenLifetimeSeconds = chatTokenLifetimeSeconds;
    }

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private JDBCProjectRepo jdbcProjectRepo;

    @Autowired
    private ProjectTaskNumberSequenceRepo taskNumberSequenceRepo;

    @Autowired
    private ProjectInviteRepo projectInviteRepo;

    @Autowired
    private ProjectApplicationRepo projectApplicationRepo;

    @Autowired
    private JDBCOrganizationRepo organizationRepo;

    @Autowired
    private OrganizationMemberRepo organizationMemberRepo;

    @Autowired
    private TagRepo tagRepo;

    @Autowired
    private TagService tagService;

    @Autowired
    private LanguageRepo languageRepo;

    @Autowired
    private TaskSubscriberRepo taskSubscriberRepo;

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private UserFavoriteService userFavoriteService;

    @Autowired
    private ProjectSecurityService securityService;

    @Autowired
    private SecurityService authnSecurityService;

    @Autowired
    private StatisticsSender statisticsSender;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public ExistsDTO checkUiId(User currentUser, String uiId) {
        // security start
        authnSecurityService.isAuthenticated(currentUser);
        // security finish

        return new ExistsDTO(projectRepo.existsByUiId(uiId.toLowerCase()));
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, propagation = Propagation.MANDATORY)
    public Project create(Organization organization, ProjectDTO projectDTO, List<User> membersToCreate)
            throws Exception {
        Project project = new Project();
        project.setOrganization(organization);
        project.setPrivate(projectDTO.isPrivate || organization.isPrivate());

        ProjectTaskNumberSequence taskNumberSequence = new ProjectTaskNumberSequence();
        taskNumberSequence = taskNumberSequenceRepo.save(taskNumberSequence);
        project.setTaskNumberSequence(taskNumberSequence);

        String descriptionMd = projectDTO.description.strip();
        String descriptionHtml = MarkdownUtils.markdownToHtml(descriptionMd);
        project.setDescriptionMd(descriptionMd);
        project.setDescriptionHtml(descriptionHtml);

        project.setName(projectDTO.name);
        project.setKey(projectDTO.key);
        project.setDocumentationMd("");
        project.setDocumentationHtml("");

        String uiId = projectDTO.uiId.strip().toLowerCase();
        if (uiId.isEmpty()) {
            do {
                uiId = RandomStringUtils.randomAlphanumeric(Organization.UI_ID_LENGTH).toLowerCase();
            } while (projectRepo.existsByUiId(uiId));
        }
        project.setUiId(uiId);

        project.setTags(tagService.getOrCreate(projectDTO.tagNames));

        List<Language> languages = languageRepo.listByIds(projectDTO.languageIds);
        project.setLanguages(languages);

        projectRepo.persist(project);

        for (User user : membersToCreate) {
            OrganizationMember orgMember = organizationMemberRepo.getNotFiredByUserAndProjectId(user, project.getId());
            projectMemberService.create(project, orgMember, new ProjectMemberDTO(true, true));
        }

        return project;
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class)
    public ProjectReadDTO create(User currentUser, ProjectDTO projectDTO) throws Exception {
        // security start
        securityService.authzCanCreateProject(currentUser, projectDTO.organization);
        // security finish

        Organization organization = null;
        if (projectDTO.organization.id != null) {
            organization = organizationRepo.getById(projectDTO.organization.id);
        } else if (projectDTO.organization.uiId != null) {
            organization = organizationRepo.getByUiId(projectDTO.organization.uiId);
        }
        if (organization == null) {
            throw new NotFoundException("Organization");
        }

        List<User> membersToCreate = new ArrayList<User>();
        if (projectDTO.makeMeMember) {
            membersToCreate.add(currentUser);
        }

        Project project = create(organization, projectDTO, membersToCreate);
        ProjectReadDTO dto = ProjectReadDTO.fromEntityWithDescAndDocs(project);

        return dto;
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class)
    public ProjectReadDTO update(User currentUser, ProjectDTO projectDTO) throws ApplicationException {
        // security start
        securityService.authzCanUpdateProject(currentUser, projectDTO.id);
        // security finish

        Project project = projectRepo.getAndFetchOrganizationAndDropboxSettings(projectDTO.id);
        if (project == null) {
            throw new NotFoundException("Project");
        }
        project = checkRecordVersion(project, projectDTO.recordVersion);

        String descriptionMd = projectDTO.description.strip();
        String descriptionHtml = MarkdownUtils.markdownToHtml(descriptionMd);
        project.setDescriptionMd(descriptionMd);
        project.setDescriptionHtml(descriptionHtml);

        project.setName(projectDTO.name);
        if (!project.getKey().isEmpty()) {
            project.setKey(projectDTO.key);
        }
        boolean wasPrivate = project.isPrivate();
        project.setPrivate(projectDTO.isPrivate || project.getOrganization().isPrivate());

        if (!projectDTO.uiId.isBlank()) {
            project.setUiId(projectDTO.uiId.strip().toLowerCase());
        }

        project.setTags(tagService.getOrCreate(projectDTO.tagNames));

        List<Language> languages = languageRepo.listByIds(projectDTO.languageIds);
        project.setLanguages(languages);

        project = projectRepo.merge(project);

        if (!wasPrivate && project.isPrivate()) {
            taskSubscriberRepo.deleteNonMembersByProjectId(project.getId());
        }

        ProjectReadDTO dto = ProjectReadDTO.fromEntityWithDescAndDocsAndDropboxSettings(project);

        return dto;
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public ProjectDocDTO getDocumentation(User currentUser, Long id) throws ApplicationException {
        // security start
        securityService.authzCanReadProject(currentUser, id);
        // security finish

        Project project = jdbcProjectRepo.getByIdForDocumentation(id);
        if (project == null) {
            throw new NotFoundException("Project");
        }
        return ProjectDocDTO.fromEntity(project);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public ProjectDocDTO getDocumentationByUiId(User currentUser, String uiId) throws ApplicationException {
        // security start
        securityService.authzCanReadProject(currentUser, uiId);
        // security finish

        Project project = jdbcProjectRepo.getByUiIdForDocumentation(uiId);
        if (project == null) {
            throw new NotFoundException("Project");
        }
        return ProjectDocDTO.fromEntity(project);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class)
    public ProjectReadDTO updateDocumentation(User currentUser, ProjectDocDTO documentationDTO)
            throws ApplicationException {
        // security start
        securityService.authzCanUpdateProject(currentUser, documentationDTO.id);
        // security finish

        Project project = projectRepo.getAndFetchOrganization(documentationDTO.id);
        if (project == null) {
            throw new NotFoundException("Project");
        }
        project = checkRecordVersion(project, documentationDTO.recordVersion);

        String documentationMd = documentationDTO.documentation.strip();
        String documentationHtml = MarkdownUtils.markdownToHtml(documentationMd);
        project.setDocumentationMd(documentationMd);
        project.setDocumentationHtml(documentationHtml);

        project = projectRepo.merge(project);

        // statistics
        statisticsSender.send(new ProjectDocUpdateMessage(currentUser.getId()));
        // statistics

        return ProjectReadDTO.fromEntityWithDescAndDocs(project);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class)
    public void makePublic(User currentUser, RecordVersionDTO projectDTO) throws Exception {
        // security start
        securityService.authzCanUpdateProject(currentUser, projectDTO.id);
        // security finish

        Project project = projectRepo.get(projectDTO.id);
        if (project == null) {
            throw new NotFoundException("Project");
        }
        project = checkRecordVersion(project, projectDTO.recordVersion);

        project.setPrivate(false);
        projectRepo.merge(project);
    }

    /*@formatter:off
    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class)
    public ProjectReadDTO addDropbox(User currentUser, OAuthCodeDTO codeDTO) throws Exception {
        // security start
        securityService.authzCanUpdateProject(currentUser, codeDTO.id);
        // security finish

        Project project = projectRepo.get(codeDTO.id);
        if (project == null) {
            throw new NotFoundException("Project");
        }
        project = checkRecordVersion(project, codeDTO.recordVersion);

        String token = dropboxService.getToken(codeDTO.code, dropboxService.getRedirectURIForProject());
        DropboxSettings dropboxSettings = new DropboxSettings();
        dropboxSettings.setToken(token);
        dropboxSettingsRepo.persist(dropboxSettings);

        project.setDropboxSettings(dropboxSettings);

        project = projectRepo.merge(project);
        return ProjectReadDTO.fromEntityWithDescAndDocsAndDropboxSettings(project);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public ProjectDropboxDTO getDropbox(User currentUser, Long id) {
        // security start
        securityService.authzCanReadProject(currentUser, id);
        // security finish

        Project project = jdbcProjectRepo.getByIdAndFetchDropboxSettings(id);
        return ProjectDropboxDTO.fromEntity(project);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public ProjectDropboxDTO getDropboxByUiId(User currentUser, String uiId) {
        // security start
        securityService.authzCanReadProject(currentUser, uiId);
        // security finish

        Project project = jdbcProjectRepo.getByUiIdAndFetchDropboxSettings(uiId);
        return ProjectDropboxDTO.fromEntity(project);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class)
    public ProjectReadDTO removeDropbox(User currentUser, RecordVersionDTO projectDTO) throws ApplicationException {
        // security start
        securityService.authzCanUpdateProject(currentUser, projectDTO.id);
        // security finish

        Project project = projectRepo.get(projectDTO.id);
        if (project == null) {
            throw new NotFoundException("Project");
        }
        project = checkRecordVersion(project, projectDTO.recordVersion);

        project.setDropboxSettings(null);

        project = projectRepo.merge(project);
        return ProjectReadDTO.fromEntityWithDescAndDocsAndDropboxSettings(project);
    }
    @formatter:on*/

    private Project getProjectByIdWithEverythingForReadAndFetchMembershipForSecurity(User currentUser, Long projectId) {
        if (currentUser == null) {
            return jdbcProjectRepo.getByIdWithEverythingForRead(projectId);
        }
        return jdbcProjectRepo.getByIdWithEverythingForReadAndFetchMembershipForSecurity(projectId, currentUser.getId());
    }

    private ProjectReadDTO getDTOWithPermissions(User currentUser, Long projectId, boolean includeApplication)
            throws ApplicationException {
        if (projectId == null) {
            throw new NotFoundException("Project");
        }
        Project project = getProjectByIdWithEverythingForReadAndFetchMembershipForSecurity(currentUser, projectId);
        if (project == null) {
            throw new NotFoundException("Project");
        }

        project.setTags(tagRepo.listByProjectId(project.getId()));
        project.setLanguages(languageRepo.listByProjectId(project.getId()));

        ProjectReadDTO dto = ProjectReadDTO.fromEntityWithEverythingForRead(project);
        if (includeApplication) {
            ProjectApplication application = projectApplicationRepo.getLastByUserAndProject(currentUser, project);
            dto.application = ProjectApplicationStatusDTO.fromEntity(application);
        }
        return dto;
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public ProjectReadDTO get(User currentUser, Long id, boolean includeApplication) throws ApplicationException {
        boolean canRead = false;
        try {
            // security start
            securityService.authzCanReadProject(currentUser, id);
            // security finish
            canRead = true;
        } finally {
            if (!canRead) {
                userFavoriteService.deleteByUserAndProjectId(currentUser, id);
            }
        }

        return getDTOWithPermissions(currentUser, id, includeApplication);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public ProjectReadDTO getByUiId(User currentUser, String uiId, boolean includeApplication) throws ApplicationException {
        boolean canRead = false;
        Long id = null;
        try {
            // security start
            id = securityService.authzCanReadProject(currentUser, uiId);
            // security finish
            canRead = true;
        } finally {
            if (!canRead) {
                userFavoriteService.deleteByUserAndProjectUiId(currentUser, uiId);
            }
        }

        return getDTOWithPermissions(currentUser, id, includeApplication);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public List<ProjectWithOrganizationDTO> listManagedByUser(User currentUser) {
        List<Tuple> projectList = projectRepo
                .listByNotFiredManagerOrganizationOrProjectMemberUserAndFetchOrganization(currentUser);
        return ProjectWithOrganizationDTO.fromTuplesWithOrganizationName(projectList);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public List<ProjectWithOrganizationDTO> listByUser(User currentUser) {
        List<Project> projectList = projectRepo.listByNotFiredProjectMemberUserAndFetchOrganization(currentUser);
        return ProjectWithOrganizationDTO.fromEntitiesWithOrganizationName(projectList);
    }

    @Deprecated
    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public List<ProjectInviteOptionDTO> listInviteOptions(User currentUser, long userId) {
        List<Tuple> projectsWithOrganizations = projectRepo
                .listByNotFiredManagerOrganizationOrProjectMemberUserAndFetchOrganization(currentUser);

        List<Long> projectIds = new ArrayList<Long>();
        List<ProjectInviteOptionDTO> dtoList = new ArrayList<ProjectInviteOptionDTO>();
        for (Tuple tuple : projectsWithOrganizations) {
            ProjectInviteOptionDTO dto = ProjectInviteOptionDTO.fromTuple(tuple);
            projectIds.add(dto.id);
            dtoList.add(dto);
        }

        List<Tuple> projectIdsWithInviteStatuses = projectInviteRepo.listProjectIdAndInviteStatusByProjectIdsAndUserId(projectIds,
                userId);
        Map<Long, ProjectInvite> projectIdToInvite = new HashMap<Long, ProjectInvite>(projectIdsWithInviteStatuses.size());
        for (Tuple tuple : projectIdsWithInviteStatuses) {
            Long projectId = tuple.get(0, Long.class);
            ProjectInvite invite = tuple.get(1, ProjectInvite.class);
            invite.setStatus(tuple.get(2, InviteStatus.class));
            projectIdToInvite.put(projectId, invite); // invites are ordered ascending by time, so we get the last one
        }

        Set<Long> alreadyParticipatingProjectIds = new HashSet<Long>(projectRepo.listProjectIdsByNotFiredMemberUserId(userId));

        for (ProjectInviteOptionDTO dto : dtoList) {
            dto.isMember = alreadyParticipatingProjectIds.contains(dto.id);
            ProjectInvite invite = projectIdToInvite.get(dto.id);
            dto.invite = ProjectInviteStatusDTO.fromInvite(invite);
        }

        return dtoList;
    }

    @Deprecated
    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public List<ProjectReadDTO> listTop() {
        List<Project> projectList = projectRepo.listPublicAndFetchOrganizationAndOrderDescById();
        return ProjectReadDTO.fromEntitiesWithOrganization(projectList);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true, propagation = Propagation.MANDATORY)
    public List<NameAndUiIdDTO> listAvailableToMemberByOrganization(Organization organization,
            OrganizationMember organizationMember) {
        if (organizationMember == null) {
            if (!organization.isPrivate()) {
                List<Project> projectList = projectRepo.listPublicByOrganization(organization);
                return NameAndUiIdDTO.fromProjects(projectList);
            }
            return new ArrayList<NameAndUiIdDTO>();
        }

        if (organizationMember.isManager()) {
            List<Project> projectList = projectRepo.listByOrganization(organization);
            return NameAndUiIdDTO.fromProjects(projectList);

        }

        List<Project> projectList = projectRepo.listAvailableToOrganizationMemberByOrganization(organization,
                organizationMember, !organization.isPrivate());
        return NameAndUiIdDTO.fromProjects(projectList);
    }

    @Override
    @Transactional(value = "jpaTransactionManager", rollbackFor = Throwable.class, readOnly = true)
    public ChatPermissionsDTO getChatToken(User currentUser, Long projectId) throws JsonProcessingException, Exception {
        // security
        securityService.authzCanReadProject(currentUser, projectId);
        // security

        ChatTokenData tokenData = new ChatTokenData();
        tokenData.chatType = ChatType.PROJECT;
        tokenData.chatId = projectId;

        if (currentUser == null) {
            tokenData.senderId = null;
            tokenData.write = false;
        } else {
            tokenData.senderId = currentUser.getId();
            try {
                securityService.authzCanWriteToProjectChat(currentUser, projectId);
                tokenData.write = true;
            } catch (SecurityException e) {
                tokenData.write = false;
            }
        }

        long now = System.currentTimeMillis();
        tokenData.notBefore = now - chatTokenNotBeforeToleranceSeconds * 1000;
        tokenData.notOnOrAfter = now + chatTokenLifetimeSeconds * 1000;

        TokenAndIvDTO dto = tokenGenerator.encrypt(tokenData.toJson());
        return new ChatPermissionsDTO(dto, true, tokenData.write);
    }
}
