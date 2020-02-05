/*
 * This file is part of the Java API to Qirk.
 * Copyright (C) 2020 Memfis Inc.
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
package org.wrkr.clb.services.dto.project;

import java.util.ArrayList;
import java.util.List;

import org.wrkr.clb.model.project.Project;

public class ProjectNameAndUiIdDTO extends ProjectUiIdDTO {

    public String name;

    public static ProjectNameAndUiIdDTO fromEntity(Project project) {
        ProjectNameAndUiIdDTO dto = new ProjectNameAndUiIdDTO();

        dto.id = project.getId();
        dto.name = project.getName();
        dto.uiId = project.getUiId();

        return dto;
    }

    public static List<ProjectNameAndUiIdDTO> fromEntities(List<Project> projectList) {
        List<ProjectNameAndUiIdDTO> dtoList = new ArrayList<ProjectNameAndUiIdDTO>(projectList.size());
        for (Project project : projectList) {
            dtoList.add(fromEntity(project));
        }
        return dtoList;
    }
}
