package com.moguhu.zuul.component;

import com.moguhu.baize.client.constants.BooleanEnum;
import com.moguhu.baize.client.constants.ParamMapTypeEnum;
import com.moguhu.baize.client.constants.PositionEnum;
import com.moguhu.baize.client.model.ApiDto;
import com.moguhu.baize.client.model.ApiParamDto;
import com.moguhu.baize.client.model.ApiParamMapDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API 参数转换器
 * <p>
 * Created by xuefeihu on 18/9/21.
 */
public class ApiParamParser {

    public static final String MAPPING_METHOD_KEY = "mappingMethod";
    public static final String MAPPING_VALUE_KEY = "mappingValue";

    /**
     * 后端参数转换
     *
     * @param request
     * @param api
     * @return
     */
    public static Map<String, Map<String, String>> parseBackendParam(HttpServletRequest request, ApiDto api) {
        Map<String, String> paramMap = new HashMap<>();
        Map<String, Map<String, String>> mappingMap = new HashMap<>();

        List<ApiParamDto> params = api.getParams();
        if (CollectionUtils.isEmpty(params)) {
            return mappingMap;
        }
        params.forEach(paramDto -> {
            String paramName = paramDto.getName();
            String paramValue = "";
            // 暂不支持 PATH 和 BODY 类型
            if (PositionEnum.GET.name().equals(paramDto.getPosition()) || PositionEnum.POST.name().equals(paramDto.getPosition())) {
                paramValue = request.getParameter(paramName);
            } else if (PositionEnum.HEAD.name().equals(paramDto.getPosition())) {
                paramValue = request.getHeader(paramName);
            }

            if (BooleanEnum.YES.name().equals(paramDto.getNeed()) && StringUtils.isEmpty(paramValue)) {
                throw new RuntimeException("parameter [" + paramValue + "] is needed");
            }
            // TODO 类型校验

            paramMap.put(paramName, paramValue);
        });

        // 生成后端参数
        List<ApiParamMapDto> mappings = api.getMappings();
        if (CollectionUtils.isEmpty(mappings)) {
            return mappingMap;
        }
        mappings.forEach(mappingDto -> {
            Map<String, String> mappingInfo = new HashMap<>();
            String mappingName = mappingDto.getName();
            String mappingValue = "";
            // 暂不支持 PATH 和 BODY 类型
            if (ParamMapTypeEnum.MAP.name().equals(mappingDto.getMapType())) {// 映射类型
                mappingValue = paramMap.get(mappingName);
                if (StringUtils.isEmpty(mappingValue)) {
                    mappingValue = mappingDto.getDefaultValue();
                }
            } else if (ParamMapTypeEnum.CONSTANT.name().equals(mappingDto.getMapType())) { // 常量类型
                mappingValue = mappingDto.getDefaultValue();
            }
            // 是否必须
            if (BooleanEnum.YES.name().equals(mappingDto.getNeed()) && StringUtils.isEmpty(mappingValue)) {
                throw new RuntimeException("parameter mapping [" + mappingValue + "] is needed");
            }

            mappingInfo.put(MAPPING_METHOD_KEY, mappingDto.getPosition());
            mappingInfo.put(MAPPING_VALUE_KEY, mappingValue);
            mappingMap.put(mappingName, mappingInfo);
        });

        return mappingMap;
    }
}
