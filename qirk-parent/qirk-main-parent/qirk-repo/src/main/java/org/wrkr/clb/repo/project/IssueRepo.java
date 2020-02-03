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
package org.wrkr.clb.repo.project;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Repository;
import org.wrkr.clb.model.project.Issue;
import org.wrkr.clb.model.project.Issue_;
import org.wrkr.clb.model.project.Project;
import org.wrkr.clb.model.project.Project_;
import org.wrkr.clb.model.user.User;
import org.wrkr.clb.repo.JPABaseIdRepo;


@Repository
public class IssueRepo extends JPABaseIdRepo<Issue> {

    @Override
    public Issue get(Long id) {
        return get(Issue.class, id);
    }

    public Issue getAndFetchUser(Long id) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Issue> query = cb.createQuery(Issue.class);

        Root<Issue> root = query.from(Issue.class);
        root.fetch(Issue_.reporter, JoinType.LEFT);

        query.where(cb.equal(root.get(Issue_.id), id));
        return getSingleResultOrNull(query);
    }

    public List<Long> listReportedIdsByUser(User user) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> idsQuery = cb.createQuery(Long.class);

        Root<Issue> issueRoot = idsQuery.from(Issue.class);

        idsQuery.select(issueRoot.get(Issue_.ID));
        idsQuery.where(cb.equal(issueRoot.get(Issue_.reporter), user));
        return getResultList(idsQuery);
    }

    public List<Issue> listByProjectIdAndFetchReporter(Long projectId) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Issue> query = cb.createQuery(Issue.class);

        Root<Issue> issueRoot = query.from(Issue.class);
        issueRoot.fetch(Issue_.reporter, JoinType.LEFT);
        Join<Issue, Project> projectJoin = issueRoot.join(Issue_.project);

        query.where(cb.equal(projectJoin.get(Project_.id), projectId));
        return getResultList(query);
    }

    public List<Issue> listByProjectUiIdAndFetchReporter(String projectUiId) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Issue> query = cb.createQuery(Issue.class);

        Root<Issue> issueRoot = query.from(Issue.class);
        issueRoot.fetch(Issue_.reporter, JoinType.LEFT);
        Join<Issue, Project> projectJoin = issueRoot.join(Issue_.project);

        query.where(cb.equal(projectJoin.get(Project_.uiId), projectUiId));
        return getResultList(query);
    }
}