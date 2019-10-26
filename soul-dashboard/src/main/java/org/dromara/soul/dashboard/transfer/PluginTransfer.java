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

package org.dromara.soul.dashboard.transfer;


import org.dromara.soul.admin.entity.PluginDO;
import org.dromara.soul.admin.vo.PluginVO;
import org.dromara.soul.common.dto.PluginData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * The interface Plugin transfer.
 *
 * @author xiaoyu(Myth)
 */
@Mapper
public interface PluginTransfer {

    /**
     * The constant INSTANCE.
     */
    PluginTransfer INSTANCE = Mappers.getMapper(PluginTransfer.class);


    /**
     * Map to data plugin data.
     *
     * @param pluginDO the plugin do
     * @return the plugin data
     */
    PluginData mapToData(PluginDO pluginDO);

    /**
     * Map data tovo plugin data.
     *
     * @param pluginVO the plugin vo
     * @return the plugin data
     */
    PluginData mapDataTOVO(PluginVO pluginVO);


}
