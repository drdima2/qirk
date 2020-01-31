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
package org.wrkr.clb.repo;

import java.util.List;

import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.wrkr.clb.model.BaseIdEntity;
import org.wrkr.clb.model.BaseIdEntityMeta;


@Repository
public abstract class JDBCBaseIdRepo extends JDBCBaseMainRepo {

    protected <E extends BaseIdEntity> Long[] buildIdsArray(List<E> entities) {
        Long[] result = new Long[entities.size()];
        for (int i = 0; i < entities.size(); i++) {
            result[i] = entities.get(i).getId();
        }
        return result;
    }

    protected <E extends BaseIdEntity> E setIdAfterSave(E entity, KeyHolder keyHolder) {
        entity.setId((Long) keyHolder.getKeys().get(BaseIdEntityMeta.id));
        return entity;
    }
}
