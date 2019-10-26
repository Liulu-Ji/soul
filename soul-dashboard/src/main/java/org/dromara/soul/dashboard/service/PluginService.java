/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * Contributor license agreements.See the NOTICE file distributed with
 * This work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * he License.You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.dromara.soul.dashboard.service;

import org.dromara.soul.dashboard.dto.PluginDTO;
import org.dromara.soul.dashboard.query.PluginQuery;
import org.dromara.soul.dashboard.result.CommonPageResponse;
import org.dromara.soul.dashboard.result.CommonResponse;
import org.dromara.soul.dashboard.vo.PluginVO;

import java.util.List;

/**
 * The interface Plugin service.
 *
 * @author xiaoyu
 */
public interface PluginService {

    /**
     * Save or update common result.
     *
     * @param pluginDTO the plugin dto
     * @return the common result
     */
    CommonResponse saveOrUpdate(PluginDTO pluginDTO);

    /**
     * Delete string.
     *
     * @param ids the ids
     * @return the string
     */
    CommonResponse batchDelete(List<String> ids);


    /**
     * Enabled string.
     *
     * @param ids     the ids
     * @param enabled the enable
     * @return the string
     */
    CommonResponse batchEnabled(List<String> ids, Boolean enabled);

    /**
     * find plugin by id.
     *
     * @param id pk.
     * @return {@linkplain PluginVO}
     */
    PluginVO findById(String id);

    /**
     * find page of plugin by query.
     *
     * @param pluginQuery {@linkplain PluginQuery}
     * @return {@linkplain CommonPageResponse}
     */
    CommonPageResponse<PluginVO> listPageByQuery(PluginQuery pluginQuery);


}
