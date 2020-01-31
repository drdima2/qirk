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
package org.wrkr.clb.services.dto;

import org.wrkr.clb.common.validation.IdAndUiIdObject;
import org.wrkr.clb.common.validation.constraints.NotNullIdOrUiId;

import com.fasterxml.jackson.annotation.JsonProperty;


@NotNullIdOrUiId(message = "Exactly one of fields 'id' and 'ui_id' in UiIdDTO must not be null")
public class IdOrUiIdDTO extends BaseEntityDTO implements IdAndUiIdObject {

    public Long id; // don't extend it from IdDTO, since validation usage is different

    @JsonProperty(value = "ui_id")
    public String uiId;

    public IdOrUiIdDTO() {
    }

    public IdOrUiIdDTO(Long id) {
        this.id = id;
    }

    public IdOrUiIdDTO(String uiId) {
        this.uiId = (uiId == null ? null : uiId.strip());
    }

    public IdOrUiIdDTO(Long id, String uiId) {
        this.id = id;
        this.uiId = (uiId == null ? null : uiId.strip());
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getUiId() {
        return uiId;
    }
}
