package org.wrkr.clb.model.project.imprt.jira;

import java.time.OffsetDateTime;

import org.wrkr.clb.common.util.datetime.DateTimeUtils;
import org.wrkr.clb.model.project.Project;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImportedJiraProject {

    @JsonIgnore
    private Long uploadTimestamp;

    @JsonIgnore
    private Project project;
    @JsonProperty(value = "id")
    private Long projectId;

    @JsonProperty(value = "jira_id")
    private Long jiraProjectId;

    @JsonProperty(value = "jira_key")
    private String jiraProjectKey;

    @JsonProperty(value = "jira_name")
    private String jiraProjectName;

    @JsonIgnore
    private OffsetDateTime updatedAt;

    @JsonProperty(value = "updated_at")
    public String getUpdatedAtAsIso8601() {
        return updatedAt.format(DateTimeUtils.WEB_DATETIME_FORMATTER);
    }

    @JsonProperty(value = "name")
    @JsonInclude(Include.NON_NULL)
    public String getProjectName() {
        return (project == null ? null : project.getName());
    }

    public Long getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(Long uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getJiraProjectId() {
        return jiraProjectId;
    }

    public void setJiraProjectId(Long jiraProjectId) {
        this.jiraProjectId = jiraProjectId;
    }

    public String getJiraProjectKey() {
        return jiraProjectKey;
    }

    public void setJiraProjectKey(String jiraProjectKey) {
        this.jiraProjectKey = jiraProjectKey;
    }

    public String getJiraProjectName() {
        return jiraProjectName;
    }

    public void setJiraProjectName(String jiraProjectName) {
        this.jiraProjectName = jiraProjectName;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
