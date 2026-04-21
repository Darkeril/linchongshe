package com.hongshu.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.constant.RedisConstants;
import com.hongshu.ai.common.enums.StatusEnum;
import com.hongshu.ai.common.exception.ErrorException;
import com.hongshu.ai.common.utils.CommonUtil;
import com.hongshu.ai.common.utils.RedisUtilss;
import com.hongshu.ai.domain.command.OpenkeyCommand;
import com.hongshu.ai.domain.entity.Openkey;
import com.hongshu.ai.domain.vo.OpenkeyVO;
import com.hongshu.ai.mapper.OpenkeyMapper;
import com.hongshu.ai.service.IOpenkeyService;
import com.hongshu.common.core.constant.StringPoolConstant;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * openai token 服务实现类
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Service
public class OpenkeyServiceImpl extends ServiceImpl<OpenkeyMapper, Openkey> implements IOpenkeyService {

    @Autowired
    private OpenkeyMapper openkeyMapper;
    @Autowired
    private RedisUtilss redisUtil;


    /**
     * 根据id获取openai token信息
     *
     * @param id openai tokenid
     * @return
     */
    private Openkey getOpenkey(Long id) {
        Openkey openkey = openkeyMapper.selectById(id);
        if (ValidatorUtil.isNull(openkey)) {
            throw new ErrorException("openai token信息不存在，无法操作");
        }
        return openkey;
    }

    @Override
    public ResponseInfo<IPageInfo<OpenkeyVO>> pageOpenkey(Query query) {
        IPage<OpenkeyVO> iPage = openkeyMapper.pageOpenkey(new Page<>(query.getCurrent(), query.getSize()), query);
        iPage.getRecords().stream().forEach(v -> {
            v.setAppKey(CommonUtil.passportEncrypt(v.getAppKey()));
            v.setAppSecret(CommonUtil.passportEncrypt(v.getAppSecret()));
        });
        return ResponseInfo.success(new IPageInfo(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getRecords()));
    }


    @Override
    public ResponseInfo<List<OpenkeyVO>> listOpenkey(Query query) {
        List<OpenkeyVO> openkeyVOS = openkeyMapper.listOpenkey(query);
        openkeyVOS.stream().forEach(v -> {
            v.setAppKey(CommonUtil.passportEncrypt(v.getAppKey()));
            v.setAppSecret(CommonUtil.passportEncrypt(v.getAppSecret()));
        });
        return ResponseInfo.success(openkeyVOS);
    }

    @Override
    public ResponseInfo<OpenkeyVO> getOpenkeyById(Long id) {
        OpenkeyVO openkeyVO = DozerUtil.convertor(getOpenkey(id), OpenkeyVO.class);
        openkeyVO.setAppKey(CommonUtil.passportEncrypt(openkeyVO.getAppKey()));
        openkeyVO.setAppSecret(CommonUtil.passportEncrypt(openkeyVO.getAppSecret()));
        return ResponseInfo.success(openkeyVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo saveOpenkey(OpenkeyCommand command) {
        Openkey openkey = DozerUtil.convertor(command, Openkey.class);
        openkey.setCreateUser(command.getOperater());
        if (openkey.getUsedTokens() == null) {
            openkey.setUsedTokens(0L);
        }
        if (openkey.getTotalTokens() == null) {
            openkey.setTotalTokens(0L);
        }
        long surplus = openkey.getTotalTokens() - openkey.getUsedTokens();
        openkey.setSurplusTokens(Math.max(surplus, 0L));
        if (openkey.getStatus() == null) {
            openkey.setStatus(StatusEnum.ENABLED.getValue());
        }
        openkeyMapper.insert(openkey);
        redisUtil.set(RedisConstants.CHAT_MODEL + StringPoolConstant.COLON + openkey.getModel(), openkey);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo updateOpenkey(OpenkeyCommand command) {
        Openkey openkey = getOpenkey(command.getId());
        openkey.setModel(command.getModel());
        openkey.setAppId(command.getAppId());
        openkey.setAppKey(ValidatorUtil.isNotNull(command.getAppKey()) && !command.getAppKey().contains(StringPoolConstant.STAR) ? command.getAppKey() : openkey.getAppKey());
        openkey.setAppSecret(ValidatorUtil.isNotNull(command.getAppSecret()) && !command.getAppSecret().contains(StringPoolConstant.STAR) ? command.getAppSecret() : openkey.getAppSecret());
        openkey.setTotalTokens(command.getTotalTokens());
        openkey.setRemark(command.getRemark());
        openkey.setStatus(command.getStatus());
        if (openkey.getUsedTokens() == null) {
            openkey.setUsedTokens(0L);
        }
        if (openkey.getTotalTokens() != null) {
            long surplus = openkey.getTotalTokens() - openkey.getUsedTokens();
            openkey.setSurplusTokens(Math.max(surplus, 0L));
        }
        openkey.setUpdateUser(command.getOperater());
        openkey.setUpdateTime(LocalDateTime.now());
        openkeyMapper.updateById(openkey);
        redisUtil.set(RedisConstants.CHAT_MODEL + StringPoolConstant.COLON + openkey.getModel(), openkey);
        if (StatusEnum.DISABLED.getValue().equals(openkey.getStatus())) {
            redisUtil.del(RedisConstants.CHAT_MODEL + StringPoolConstant.COLON + openkey.getModel());
        }
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeOpenkeyByIds(List<Long> ids) {
        openkeyMapper.deleteBatchIds(ids);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeOpenkeyById(Long id) {
        openkeyMapper.deleteById(id);
        return ResponseInfo.success();
    }

}
