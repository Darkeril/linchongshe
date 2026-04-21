package com.hongshu.ai.controller.app;

import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.constant.RedisConstants;
import com.hongshu.ai.common.enums.SmsEnum;
import com.hongshu.ai.common.enums.StatusEnum;
import com.hongshu.ai.common.utils.AliyunSMSUtil;
import com.hongshu.ai.common.utils.RedisUtilss;
import com.hongshu.ai.common.utils.TencentSMSUtil;
import com.hongshu.ai.common.constant.BaseConfigConstant;
import com.hongshu.ai.domain.command.ChatCommand;
import com.hongshu.ai.domain.dto.config.AppInfoDTO;
import com.hongshu.ai.domain.dto.config.BaseInfoDTO;
import com.hongshu.ai.domain.dto.config.ExtraInfoDTO;
import com.hongshu.ai.domain.vo.AgreementVO;
import com.hongshu.ai.domain.vo.AppConfigVO;
import com.hongshu.ai.service.*;
import com.hongshu.common.core.enums.IntEnum;
import com.hongshu.common.core.enums.ResponseEnum;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.utils.RandomUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.common.core.validator.base.BaseAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取小程序基础信息接口
 *
 * @author: myj
 * @date: 2023/5/4
 * @version: 1.0.0
 */
@RestController
@RequestMapping("/app/api")
public class AppApiController {

    @Autowired
    private IBaseConfigService baseConfigService;
    @Autowired
    private IAssistantTypeService assistantTypeService;
    @Autowired
    private IAssistantService assistantService;
    @Autowired
    private IAgreementService contentService;
    @Autowired
    private GptService gptService;
    @Autowired
    private RedisUtilss redisUtils;


    /**
     * 发送短信
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     *//**/
    @PostMapping("/sms/send")
    public ResponseInfo sendSmsCode(@RequestBody Query query) {
        ExtraInfoDTO extraInfo = baseConfigService.getBaseConfigByName("extraInfo", ExtraInfoDTO.class);
        if (ValidatorUtil.isNull(extraInfo) || ValidatorUtil.isNull(extraInfo.getSmsType()) || SmsEnum.NONE.getValue().equals(extraInfo.getSmsType())) {
            return ResponseInfo.validateFail("未开通短信功能");
        }
        query = new Query(query);
        String tel = query.getTel();
        if (IntEnum.ELEVEN.getValue() != tel.length()) {
            return ResponseInfo.businessFail("请输入正确手机号码");
        }
        String code = RandomUtil.randomNumbers(6);
        Map<String, Object> map = new HashMap<>(16);
        map.put("code", code);
        String key = RedisConstants.TEL_CODE + tel;
        if (ValidatorUtil.isNotNull(redisUtils.get(key))) {
            return ResponseInfo.customizeError(ResponseEnum.REPEAT_REQUEST_SMS);
        }
        if (SmsEnum.ALI.getValue().equals(extraInfo.getSmsType())) {
            AliyunSMSUtil.sendSms(extraInfo, tel, extraInfo.getRegisterTemplate(), map);
        } else if (SmsEnum.TECENT.getValue().equals(extraInfo.getSmsType())) {
            TencentSMSUtil.sendSms(extraInfo, tel, extraInfo.getRegisterTemplate(), new String[]{code});
        } else {
            return ResponseInfo.validateFail("未知的短信方式，发送失败");
        }
        redisUtils.set(key, code, RedisConstants.FIVE_MINUTES);
        return ResponseInfo.success();
    }

    /**
     * 获取app配置信息
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping("/home/config")
    public ResponseInfo getHomeConfig() {
        BaseInfoDTO baseInfo = baseConfigService.getBaseConfigByName(BaseConfigConstant.BASE_INFO, BaseInfoDTO.class);
        AppInfoDTO appInfo = baseConfigService.getBaseConfigByName(BaseConfigConstant.APP_INFO, AppInfoDTO.class);
        AppConfigVO appConfig = AppConfigVO.builder().baseInfo(baseInfo).appInfo(appInfo).build();
        return ResponseInfo.success(appConfig);
    }

    /**
     * 获取协议信息
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping("/content/agreement/{type}")
    public ResponseInfo getAgreement(@PathVariable("type") Integer type) {
        Query query = new Query();
        query.put("type", type);
        query.put("status", StatusEnum.ENABLED.getValue());
        List<AgreementVO> contents = contentService.listContent(query).getData();
        if (ValidatorUtil.isNullIncludeArray(contents)) {
            return ResponseInfo.success();
        }
        return ResponseInfo.success(contents.get(0));
    }

    /**
     * 获取系统内容信息
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping("/content/{type}")
    public ResponseInfo listContent(@PathVariable("type") Integer type) {
        Query query = new Query();
        query.put("type", type);
        query.put("status", StatusEnum.ENABLED.getValue());
        return contentService.listContent(query);
    }

    /**
     * 获取内容详情
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping("/content/detail/{id}")
    public ResponseInfo getContent(@PathVariable("id") Long id) {
        return contentService.getContentById(id);
    }

    /**
     * 获取Ai分类
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping("/assistant/type")
    public ResponseInfo listAssistantType() {
        Query query = new Query();
        query.put("status", StatusEnum.ENABLED.getValue());
        return assistantTypeService.listAssistantType(query);
    }

    /**
     * 获取Ai助手
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping("/assistant")
    public ResponseInfo listAssistantByType(@RequestParam Map map) {
        Query query = new Query(map, true);
        query.put("status", StatusEnum.ENABLED.getValue());
        return assistantService.listAssistantByApp(query);
    }

    /**
     * 获取Ai助手
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping("/assistant/{id}")
    public ResponseInfo getAssistantById(@PathVariable Long id) {
        return assistantService.getAssistantById(id);
    }

    /**
     * 随机获取Ai助手
     *
     * @author: myj
     * @date: 2023/1/9
     * @version: 1.0.0
     */
    @GetMapping("/assistant/random")
    public ResponseInfo listAssistant(@RequestParam(required = false) Map map) {
        Query query = new Query();
        query.put("status", StatusEnum.ENABLED.getValue());
        query.put("size", ValidatorUtil.isNotNull(map.get("size")) ? BaseAssert.getParamInt(map, "size") : 3);
        return assistantService.listAssistantByApp(new Query(query));
    }

    /**
     * 提问
     *
     * @author: myj
     * @date: 2023/5/5
     * @version: 1.0.0
     */
    @PostMapping("/chat")
    public ResponseInfo sendMessage(@RequestBody ChatCommand command) {
        command.setApi(true);
        command = gptService.validateGptCommand(command);
        return ResponseInfo.success(gptService.chatMessage(command));
    }

}
