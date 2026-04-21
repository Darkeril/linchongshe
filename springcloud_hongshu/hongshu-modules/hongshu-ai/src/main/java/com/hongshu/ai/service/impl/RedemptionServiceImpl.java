package com.hongshu.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.exception.ErrorException;
import com.hongshu.ai.domain.command.RedemptionCommand;
import com.hongshu.ai.domain.entity.Redemption;
import com.hongshu.ai.domain.vo.RedemptionVO;
import com.hongshu.ai.mapper.RedemptionMapper;
import com.hongshu.ai.service.IRedemptionService;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 兑换码 服务实现类
 *
 * @author: Yang
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Service
public class RedemptionServiceImpl extends ServiceImpl<RedemptionMapper, Redemption> implements IRedemptionService {

    @Autowired
    private RedemptionMapper redemptionMapper;


    /**
     * 根据id获取兑换码信息
     *
     * @param id 兑换码id
     * @return
     */
    private Redemption getRedemption(Long id) {
        Redemption redemption = redemptionMapper.selectById(id);
        if (ValidatorUtil.isNull(redemption)) {
            throw new ErrorException("兑换码信息不存在，无法操作");
        }
        return redemption;
    }

    @Override
    public ResponseInfo<IPageInfo<RedemptionVO>> pageRedemption(Query query) {
        IPage<RedemptionVO> iPage = redemptionMapper.pageRedemption(new Page<>(query.getCurrent(), query.getSize()), query);
        return ResponseInfo.success(new IPageInfo(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getRecords()));
    }

    @Override
    public ResponseInfo<List<RedemptionVO>> listRedemption(Query query) {
        return ResponseInfo.success(redemptionMapper.listRedemption(query));
    }

    @Override
    public ResponseInfo<RedemptionVO> getRedemptionById(Long id) {
        return ResponseInfo.success(DozerUtil.convertor(getRedemption(id), RedemptionVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo saveRedemption(RedemptionCommand command) {
        Redemption redemption = DozerUtil.convertor(command, Redemption.class);
        redemption.setCreateUser(command.getOperater());
        redemptionMapper.insert(redemption);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo updateRedemption(RedemptionCommand command) {
        Redemption redemption = getRedemption(command.getId());
        DozerUtil.convertor(command, redemption);
        redemption.setUpdateUser(command.getOperater());
        redemption.setUpdateTime(LocalDateTime.now());
        redemptionMapper.updateById(redemption);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeRedemptionByIds(List<Long> ids) {
        redemptionMapper.deleteBatchIds(ids);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeRedemptionById(Long id) {
        redemptionMapper.deleteById(id);
        return ResponseInfo.success();
    }

}
