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
package org.wrkr.clb.common.jms.message.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class NewCommentMessage extends BaseStatisticsMessage {

    private static final ObjectWriter MESSAGE_WRITER = new ObjectMapper().writerFor(NewCommentMessage.class);

    public static final String OWNER_TYPE = "owner_type";
    public static final String OWNER_ID = "owner_id";
    public static final String CREATED_AT = "created_at";

    @JsonProperty(value = OWNER_TYPE)
    public String ownerType;
    @JsonProperty(value = OWNER_ID)
    public long ownerId;
    @JsonProperty(value = CREATED_AT)
    public long createdAt;

    public NewCommentMessage(String ownerType, long ownerId, long createdAt) {
        super(Code.NEW_COMMENT);
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    @Override
    public String toJson() throws JsonProcessingException {
        return MESSAGE_WRITER.writeValueAsString(this);
    }
}