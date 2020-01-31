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
package org.wrkr.clb.chat.services.mariadb;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.wrkr.clb.chat.model.mariadb.BaseAttachedChatMessage;
import org.wrkr.clb.chat.model.mariadb.IssueMessage;
import org.wrkr.clb.chat.repo.mariadb.BaseAttachedChatMessageRepo;
import org.wrkr.clb.chat.repo.mariadb.MariaDBIssueMessageRepo;
import org.wrkr.clb.common.crypto.token.chat.ChatTokenData;


@Service
public class MariaDBIssueChatService extends MariaDBAttachedChatService {

    @Autowired
    private MariaDBIssueMessageRepo messageRepo;

    @Override
    protected BaseAttachedChatMessageRepo getRepo() {
        return messageRepo;
    }

    @Override
    protected BaseAttachedChatMessage createMessageInstance(ChatTokenData tokenData, String message, UUID uuid) {
        BaseAttachedChatMessage issueMessage = new IssueMessage();
        issueMessage.setUuid(uuid.toString());
        issueMessage.setChatId(tokenData.chatId);
        issueMessage.setMessage(message);
        if (tokenData.senderId != null) {
            issueMessage.setSenderId(tokenData.senderId);
        } else {
            issueMessage.setSenderId(BaseAttachedChatMessage.SENDER_TYPE_EXAMPLE);
        }
        return issueMessage;
    }
}
