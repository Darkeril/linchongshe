package com.hongshu.ai.common.third;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.stereotype.Component;

/**
 * 获取微信服务文件处理类
 *
 * @author: myj
 * @date: 2023/2/1
 * @version: 1.0.0
 */
@Component
public class WxServiceHandler {

    /**
     * 获取微信小程序配置
     *
     * @param clientId 应用id
     * @return 配置信息
     */
    public WxMaService getMaService(String clientId) {
        WxMaService service = WxMaServiceKit.getMaService(clientId);
        if (ValidatorUtil.isNull(service) || ValidatorUtil.isNull(service.getWxMaConfig().getAppid())) {
            service = new WxMaServiceImpl();
            WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
            config.setAppid(WxAppConstant.APP_ID);
            config.setSecret(WxAppConstant.APP_SECRET);
            service.setWxMaConfig(config);
            WxMaServiceKit.setMaService(WxAppConstant.APP_ID, service);
        }
        return service;
    }

}
