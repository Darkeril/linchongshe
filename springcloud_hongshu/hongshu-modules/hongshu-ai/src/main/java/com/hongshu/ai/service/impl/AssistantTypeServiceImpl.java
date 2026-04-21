package com.hongshu.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.exception.ErrorException;
import com.hongshu.ai.domain.command.AssistantTypeCommand;
import com.hongshu.ai.domain.entity.AssistantType;
import com.hongshu.ai.domain.vo.AssistantTypeVO;
import com.hongshu.ai.mapper.AssistantTypeMapper;
import com.hongshu.ai.service.IAssistantTypeService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 助手分类 服务实现类
 *
 * @author: myj
 * @date: 2023-11-22
 * @version: 1.0.0
 */
@Service
public class AssistantTypeServiceImpl extends ServiceImpl<AssistantTypeMapper, AssistantType> implements IAssistantTypeService {

    @Autowired
    private AssistantTypeMapper assistantTypeMapper;


    /**
     * 根据id获取助手分类信息
     *
     * @param id 助手分类id
     * @return
     */
    private AssistantType getAssistantType(Long id) {
        AssistantType assistantType = assistantTypeMapper.selectById(id);
        if (ValidatorUtil.isNull(assistantType)) {
            throw new ErrorException("助手分类信息不存在，无法操作");
        }
        return assistantType;
    }

    @Override
    public ResponseInfo<IPageInfo<AssistantTypeVO>> pageAssistantType(Query query) {
        IPage<AssistantTypeVO> iPage = assistantTypeMapper.pageAssistantType(new Page<>(query.getCurrent(), query.getSize()), query);
        return ResponseInfo.success(new IPageInfo(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getRecords()));
    }

    @Override
    public ResponseInfo<List<AssistantTypeVO>> listAssistantType(Query query) {
        return ResponseInfo.success(assistantTypeMapper.listAssistantType(query));
    }

    @Override
    public ResponseInfo<AssistantTypeVO> getAssistantTypeById(Long id) {
        return ResponseInfo.success(DozerUtil.convertor(getAssistantType(id), AssistantTypeVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo saveAssistantType(AssistantTypeCommand command) {
        AssistantType assistantType = DozerUtil.convertor(command, AssistantType.class);
        assistantType.setCreateUser(command.getOperater());
        assistantTypeMapper.insert(assistantType);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo updateAssistantType(AssistantTypeCommand command) {
        AssistantType assistantType = getAssistantType(command.getId());
        DozerUtil.convertor(command, assistantType);
        assistantType.setUpdateUser(command.getOperater());
        assistantType.setUpdateTime(LocalDateTime.now());
        assistantTypeMapper.updateById(assistantType);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeAssistantTypeByIds(List<Long> ids) {
        assistantTypeMapper.deleteBatchIds(ids);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeAssistantTypeById(Long id) {
        assistantTypeMapper.deleteById(id);
        return ResponseInfo.success();
    }

}
