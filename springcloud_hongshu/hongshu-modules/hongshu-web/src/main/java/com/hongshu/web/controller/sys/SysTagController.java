package com.hongshu.web.controller.sys;

import com.hongshu.common.core.web.controller.BaseController;
import com.hongshu.common.core.web.domain.AjaxResult;
import com.hongshu.common.core.web.page.TableDataInfo;
import com.hongshu.common.core.annotation.Log;
import com.hongshu.common.core.enums.BusinessType;
import com.hongshu.web.domain.Query;
import com.hongshu.web.domain.entity.WebTag;
import com.hongshu.web.service.sys.ISysTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签操作处理
 *

 */
@RestController
@RequestMapping("/tag")
public class SysTagController extends BaseController {

    @Autowired
    private ISysTagService tagService;


    /**
     * 获取标签列表
     */
    @PreAuthorize("@ss.hasPermi('web:tag:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam Map<String, Object> map) {
        this.startPage();
        List<WebTag> tagList = tagService.selectTagList(new Query(map));
        return getDataTable(tagList);
    }

    /**
     * 根据标签ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('web:tag:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(tagService.selectTagById(id));
    }

    /**
     * 新增标签
     */
    @PreAuthorize("@ss.hasPermi('web:tag:add')")
    @Log(title = "标签管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WebTag tag) {
        return toAjax(tagService.insertTag(tag));
    }

    /**
     * 修改标签
     */
    @PreAuthorize("@ss.hasPermi('web:tag:edit')")
    @Log(title = "标签管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WebTag tag) {
        return toAjax(tagService.updateTag(tag));
    }

    /**
     * 删除标签
     */
    @PreAuthorize("@ss.hasPermi('web:tag:remove')")
    @Log(title = "标签管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String ids) {
        // 将逗号分隔的ID字符串转换为Long数组
        String[] idArray = ids.split(",");
        Long[] longIds = new Long[idArray.length];
        for (int i = 0; i < idArray.length; i++) {
            longIds[i] = Long.parseLong(idArray[i].trim());
        }
        return toAjax(tagService.deleteTagByIds(longIds));
    }
}
