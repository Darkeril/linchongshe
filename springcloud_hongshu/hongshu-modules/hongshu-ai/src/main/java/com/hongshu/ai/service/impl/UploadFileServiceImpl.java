package com.hongshu.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hongshu.ai.common.IPageInfo;
import com.hongshu.ai.common.Query;
import com.hongshu.ai.common.exception.ErrorException;
import com.hongshu.ai.domain.command.UploadFileCommand;
import com.hongshu.ai.domain.entity.UploadFile;
import com.hongshu.ai.domain.vo.UploadFileVO;
import com.hongshu.ai.mapper.UploadFileMapper;
import com.hongshu.ai.service.IUploadFileService;
import com.hongshu.common.core.enums.ResponseInfo;
import com.hongshu.common.core.utils.DozerUtil;
import com.hongshu.common.core.validator.ValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件管理 服务实现类
 *
 * @author: myj
 * @date: 2023-04-28
 * @version: 1.0.0
 */
@Service
public class UploadFileServiceImpl extends ServiceImpl<UploadFileMapper, UploadFile> implements IUploadFileService {

    @Autowired
    private UploadFileMapper uploadFileMapper;


    /**
     * 根据id获取文件管理信息
     *
     * @param id 文件管理id
     * @return
     */
    private UploadFile getFile(Long id) {
        UploadFile uploadFile = uploadFileMapper.selectById(id);
        if (ValidatorUtil.isNull(uploadFile)) {
            throw new ErrorException("文件管理信息不存在，无法操作");
        }
        return uploadFile;
    }

    @Override
    public ResponseInfo<IPageInfo<UploadFileVO>> pageFile(Query query) {
        IPage<UploadFileVO> iPage = uploadFileMapper.pageFile(new Page<>(query.getCurrent(), query.getSize()), query);
        return ResponseInfo.success(new IPageInfo(iPage.getCurrent(), iPage.getSize(), iPage.getTotal(), iPage.getRecords()));
    }

    @Override
    public ResponseInfo<List<UploadFileVO>> listFile(Query query) {
        return ResponseInfo.success(uploadFileMapper.listFile(query));
    }

    @Override
    public ResponseInfo<UploadFileVO> getFileById(Long id) {
        return ResponseInfo.success(DozerUtil.convertor(getFile(id), UploadFileVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo saveFile(UploadFileCommand command) {
        UploadFile uploadFile = DozerUtil.convertor(command, UploadFile.class);
        uploadFile.setCreateUser(command.getOperater());
        uploadFileMapper.insert(uploadFile);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo updateFile(UploadFileCommand command) {
        UploadFile uploadFile = getFile(command.getId());
        DozerUtil.convertor(command, uploadFile);
        uploadFile.setUpdateUser(command.getOperater());
        uploadFile.setUpdateTime(LocalDateTime.now());
        uploadFileMapper.updateById(uploadFile);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeFileByIds(List<Long> ids) {
        uploadFileMapper.deleteBatchIds(ids);
        return ResponseInfo.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "masterTransactionManager")
    public ResponseInfo removeFileById(Long id) {
        uploadFileMapper.deleteById(id);
        return ResponseInfo.success();
    }

}
