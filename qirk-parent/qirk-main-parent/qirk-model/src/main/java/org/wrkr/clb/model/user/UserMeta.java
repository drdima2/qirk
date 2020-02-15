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
package org.wrkr.clb.model.user;

import org.wrkr.clb.model.BaseIdEntityMeta;

public class UserMeta extends BaseIdEntityMeta {

    public static final String TABLE_NAME = "user_profile";

    public static final String username = "username";
    public static final String emailAddress = "email_address";
    public static final String passwordHash = "password_hash";
    public static final String createdAt = "created_at";
    public static final String manager = "manager";
    public static final String fullName = "full_name";

    public static final UserMeta DEFAULT = new UserMeta(TABLE_NAME);

    public UserMeta(String tableAlias) {
        super(tableAlias);
    };
}
