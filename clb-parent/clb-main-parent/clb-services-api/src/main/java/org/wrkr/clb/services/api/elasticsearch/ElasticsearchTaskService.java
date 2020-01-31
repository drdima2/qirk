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
package org.wrkr.clb.services.api.elasticsearch;

import java.io.IOException;

import org.elasticsearch.search.SearchHits;
import org.wrkr.clb.model.project.task.Task;
import org.wrkr.clb.repo.context.TaskSearchContext;

public interface ElasticsearchTaskService extends ElasticsearchService<Task> {

    public void updateForJira(Task task) throws IOException;

    public void updateCardAndHidden(Task task) throws Exception;

    public SearchHits search(Long projectId, TaskSearchContext searchContext, int size) throws Exception;
}
