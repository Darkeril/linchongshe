package com.hongshu.web.service.sys.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hongshu.common.core.constant.AuthConstant;
import com.hongshu.common.core.domain.R;
import com.hongshu.common.core.exception.ServiceException;
import com.hongshu.common.core.global.BaseSQLConf;
import com.hongshu.common.core.utils.RandomUtil;
import com.hongshu.common.core.utils.StringUtils;
import com.hongshu.common.core.utils.ip.AddressUtils;
import com.hongshu.common.core.utils.ip.IpUtils;
import com.hongshu.common.core.validator.ValidatorUtil;
import com.hongshu.system.api.RemoteFileService;
import com.hongshu.system.api.domain.SysFile;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.mapper.sys.SysMemberMapper;
import com.hongshu.web.service.sys.ISysMemberService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 会员信息 服务层处理
 *
 
 */
@Slf4j
@Service
public class SysMemberServiceImpl implements ISysMemberService {

    @Autowired
    private SysMemberMapper memberMapper;
    //    @Autowired
//    private IOssService ossService;
    @Autowired
    private RemoteFileService remoteFileService;
//    @Value("${oss.type}")
//    Integer type;


    /**
     * 查询会员信息集合
     *
     * @param query 会员信息
     */
    public List<WebUser> selectMemberList(Query query) {

        QueryWrapper<WebUser> qw = new QueryWrapper<>();
        // 只查询未删除的用户
        qw.lambda().eq(WebUser::getDeleted, 0);
        // 通过工具类或者方法封装空值检查和查询条件构造（只在有值且不为空字符串时添加条件）
        Optional.ofNullable(query.getHsId()).ifPresent(hsId -> qw.lambda().like(WebUser::getHsId, hsId));
        if (query.getUsername() != null && !query.getUsername().trim().isEmpty()) {
            qw.lambda().like(WebUser::getUsername, query.getUsername());
        }
        if (query.getPhone() != null && !query.getPhone().trim().isEmpty()) {
            qw.lambda().like(WebUser::getPhone, query.getPhone());
        }
        Optional.ofNullable(query.getStatus()).filter(status -> status != null && !status.toString().trim().isEmpty())
                .ifPresent(status -> qw.lambda().eq(WebUser::getStatus, status));
        Optional.ofNullable(query.get("fromType")).ifPresent(fromType -> qw.lambda().eq(WebUser::getFromType, fromType));

        // 处理 beginTime 和 endTime
        String beginTimeStr = (String) query.get("params[beginTime]");
        String endTimeStr = (String) query.get("params[endTime]");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (beginTimeStr != null) {
            try {
                // 将字符串转为 Date
                Date beginTime = sdf.parse(beginTimeStr);
                // 添加大于等于条件
                qw.lambda().ge(WebUser::getCreateTime, beginTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (endTimeStr != null) {
            try {
                Date endTime = sdf.parse(endTimeStr);
                // 确保 endTime 是当天的结束时间（23:59:59）
                endTime = new Date(endTime.getTime() + 1000 * 60 * 60 * 24 - 1);
                // 添加小于等于条件
                qw.lambda().le(WebUser::getCreateTime, endTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 排序
        qw.orderByDesc("create_time");

        // 执行查询并返回结果
        return memberMapper.selectList(qw);
    }

    /**
     * 查询所有会员
     */
    @Override
    public List<WebUser> selectMemberAll() {
        return memberMapper.selectList(new QueryWrapper<>());
    }

    /**
     * 通过会员ID查询会员信息
     *
     * @param id 会员ID
     */
    @Override
    public WebUser selectMemberById(Long id) {
        QueryWrapper<WebUser> qw = new QueryWrapper<>();
        qw.lambda().eq(WebUser::getId, id).eq(WebUser::getDeleted, 0);
        return memberMapper.selectOne(qw);
    }

    /**
     * 新增会员信息
     *
     * @param user 会员信息
     */
    @Override
    public int insertMember(WebUser user, MultipartFile file) {
        // 校验手机号是否已存在（只检查未删除的用户）
        if (ObjectUtils.isNotEmpty(user.getPhone())) {
            QueryWrapper<WebUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(WebUser::getPhone, user.getPhone()).eq(WebUser::getDeleted, 0);
            Long count = memberMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new ServiceException("手机号 " + user.getPhone() + " 已存在，请使用其他手机号");
            }
        }
        if (StringUtils.isEmpty(user.getPassword())) {
            throw new ServiceException("密码不能为空");
        }

        user.setAvatar(AuthConstant.DEFAULT_AVATAR);
        // 上传头像
        if (ObjectUtils.isNotEmpty(file)) {
//            String avatar = ossService.upload(file);
            R<SysFile> upload = remoteFileService.upload(file);
            String avatar = upload.getData().getUrl();
            user.setAvatar(avatar);
        }
        user.setHsId(Long.valueOf(RandomUtil.randomNumbers(10)));
        user.setPassword(SecureUtil.md5(user.getPassword()));
        user.setLoginIp(IpUtils.getIpAddr());
        user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
        user.setCreator("System");
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        // 设置删除标识为0（正常状态）
        if (user.getDeleted() == null) {
            user.setDeleted(0);
        }
        return memberMapper.insert(user);
    }

    /**
     * 修改保存会员信息
     *
     * @param user 会员信息
     */
    @Override
    public int updateMember(WebUser user, MultipartFile file) {
        QueryWrapper<WebUser> qw = new QueryWrapper<>();
        qw.lambda().eq(WebUser::getId, user.getId()).eq(WebUser::getDeleted, 0);
        WebUser webUser = memberMapper.selectOne(qw);
        if (ObjectUtils.isEmpty(webUser)) {
            throw new ServiceException("会员不存在或已删除");
        }

        // 校验手机号是否已被其他会员使用（排除当前会员）
        if (ObjectUtils.isNotEmpty(user.getPhone()) && !user.getPhone().equals(webUser.getPhone())) {
            QueryWrapper<WebUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(WebUser::getPhone, user.getPhone())
                    .ne(WebUser::getId, user.getId());
            Long count = memberMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new ServiceException("手机号 " + user.getPhone() + " 已被其他会员使用，请使用其他手机号");
            }
        }

        // 更新头像
        if (ObjectUtils.isNotEmpty(file)) {
//            String avatar = ossService.upload(file);
            R<SysFile> upload = remoteFileService.upload(file);
            String avatar = upload.getData().getUrl();
            user.setAvatar(avatar);
        }
        String newPassword = webUser.getPassword();
        String originPassword = SecureUtil.md5(user.getPassword());
        // 确认更换了新密码
        if (!originPassword.equals(newPassword)) {
            user.setPassword(SecureUtil.md5(user.getPassword()));
        }
        user.setPassword(webUser.getPassword());
        user.setLoginIp(IpUtils.getIpAddr());
        user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
        user.setUpdater("System");
        user.setUpdateTime(new Date());
        return memberMapper.updateById(user);
    }

    /**
     * 删除会员信息
     *
     * @param id 会员ID
     */
    @Override
    public int deleteMemberById(Long id) {
        // 逻辑删除：更新 deleted 字段为 1
        WebUser user = memberMapper.selectById(id);
        if (user != null) {
            user.setDeleted(1);
            user.setUpdateTime(new Date());
            return memberMapper.updateById(user);
        }
        return 0;
    }

    /**
     * 批量删除会员信息
     *
     * @param ids 需要删除的会员ID
     */
    @Override
    public int deleteMemberByIds(Long[] ids) {
        int count = 0;
        for (Long id : ids) {
            WebUser user = selectMemberById(id);
            if (ValidatorUtil.isNull(user)) {
                log.info("用户不存在:{}", id);
                continue;
            }
            // 逻辑删除：更新 deleted 字段为 1
            user.setDeleted(1);
            user.setUpdateTime(new Date());
            int result = memberMapper.updateById(user);
            if (result > 0) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Integer getMemberCount(int status) {
        QueryWrapper<WebUser> queryWrapper = new QueryWrapper<>();
        // 添加逻辑删除过滤条件：只统计未删除的会员
        queryWrapper.eq("deleted", 0);
        queryWrapper.eq(BaseSQLConf.STATUS, status);
        return Math.toIntExact(memberMapper.selectCount(queryWrapper));
    }

    /**
     * 批量禁用会员信息
     *
     * @param ids 需要禁用的会员ID
     */
    @Override
    public void disableMemberByIds(Long[] ids) {
        for (Long id : ids) {
            WebUser user = selectMemberById(id);
            if (ValidatorUtil.isNotNull(user)) {
                log.info("用户不存在:{}", id);
                user.setStatus("1");
                user.setUpdateTime(new Date());
                memberMapper.updateById(user);
            }
        }
    }

    /**
     * 更新会员状态
     */
    @Override
    public void updateStatus(Long id, String status) {
        WebUser user = memberMapper.selectById(id);
        if (ObjectUtils.isNotEmpty(user)) {
            user.setStatus(status);
            user.setUpdateTime(new Date());
            memberMapper.updateById(user);
        }
    }

    @Override
    public void approvePendingUser(Long id) {
        WebUser user = memberMapper.selectById(id);
        if (user == null || (user.getDeleted() != null && user.getDeleted() != 0)) {
            throw new ServiceException("用户不存在或已删除");
        }
        if (!"2".equals(user.getStatus())) {
            throw new ServiceException("该用户不是待审核状态");
        }
        user.setStatus("0");
        user.setUpdateTime(new Date());
        memberMapper.updateById(user);
    }

    @Override
    public void rejectPendingUser(Long id) {
        WebUser user = memberMapper.selectById(id);
        if (user == null || (user.getDeleted() != null && user.getDeleted() != 0)) {
            throw new ServiceException("用户不存在或已删除");
        }
        if (!"2".equals(user.getStatus())) {
            throw new ServiceException("该用户不是待审核状态");
        }
        user.setStatus("1");
        user.setUpdateTime(new Date());
        memberMapper.updateById(user);
    }
}
