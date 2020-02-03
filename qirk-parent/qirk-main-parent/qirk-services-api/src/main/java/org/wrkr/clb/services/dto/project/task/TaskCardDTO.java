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
package org.wrkr.clb.services.dto.project.task;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.wrkr.clb.common.validation.groups.OnCreate;
import org.wrkr.clb.common.validation.groups.OnUpdate;
import org.wrkr.clb.model.project.task.TaskCard;
import org.wrkr.clb.services.dto.IdDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TaskCardDTO extends IdDTO {

    @JsonProperty(value = "record_version")
    @NotNull(message = "record_version in RoadDTO must not be null", groups = OnUpdate.class)
    public Long recordVersion;

    @JsonProperty(value = "road")
    @NotNull(message = "road_id in TaskCardDTO must not be null", groups = OnCreate.class)
    public Long roadId;

    @NotBlank(message = "name in TaskCardDTO must not be blank")
    public String name;

    @JsonProperty(value = "status")
    @NotNull(message = "status in TaskCardDTO must not be null")
    public String statusNameCode = TaskCard.Status.STOPPED.toString();
}