package com.hongshu.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.ResponseInfo;
import com.hongshu.ai.common.exception.ErrorException;
import com.hongshu.ai.domain.command.ModelCommand;
import com.hongshu.ai.domain.entity.Model;
import com.hongshu.ai.domain.vo.ModelVO;
import com.hongshu.ai.mapper.ModelMapper;
import com.hongshu.ai.service.IModelService;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 大模型信息 服务实现类
 *
 * @author: myj
 * @date: 2023-12-01
 * @version: 1.0.0
 */
@Service
public class ModelServiceImpl extends ServiceImpl<ModelMapper, Model> implements IModelService {

    @Autowired
    private ModelMapper modelMapper;

    /**
     * 根据id获取大模型信息信息
     *
     * @param id 大模型信息id
     * @return
     */
    private Model getModel(Long id) {
        Model model = modelMapper.selectById(id);
        if (ValidatorUtil.isNull(model)) {
            throw new ErrorException("大模型信息信息不存在，无法操作");
        }
        return model;
    }

    @Override
    public ResponseInfo<IPageInfo<ModelVO>> pageModel(Query query) {
        IPage<ModelVO> iPage = modelMapper.pageModel(new Page<>(query.getCurrent(), query.getSize()), query);
        return ResponseInfo.success(new IPageInfo(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getRecords()));
    }

    @Override
    public ResponseInfo<List<ModelVO>> listModel(Query query) {
        return ResponseInfo.success(modelMapper.listModel(query));
    }

    @Override
    public ResponseInfo<ModelVO> getModelById(Long id) {
        return ResponseInfo.success(DozerUtil.convertor(getModel(id), ModelVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo saveModel(ModelCommand command) {
        Model model = DozerUtil.convertor(command, Model.class);
        model.setCreateUser(command.getOperater());
        modelMapper.insert(model);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo updateModel(ModelCommand command) {
        Model model = getModel(command.getId());
        DozerUtil.convertor(command, model);
        model.setUpdateUser(command.getOperater());
        model.setUpdateTime(LocalDateTime.now());
        modelMapper.updateById(model);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeModelByIds(List<Long> ids) {
        modelMapper.deleteBatchIds(ids);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeModelById(Long id) {
        modelMapper.deleteById(id);
        return ResponseInfo.success();
    }

}
