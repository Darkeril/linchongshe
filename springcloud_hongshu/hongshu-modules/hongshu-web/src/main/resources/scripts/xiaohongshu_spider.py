#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
小红书爬虫脚本
通过命令行参数接收爬取请求，返回JSON格式结果
"""

import json
import sys
import argparse
import requests
from bs4 import BeautifulSoup
import time
import random
import math
import os
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed

# 为 true 时打印大量调试日志（默认关闭，减少 stderr I/O，略提速）
_SPIDER_DEBUG = os.environ.get("XHS_SPIDER_DEBUG", "").strip() in ("1", "true", "yes")
# 视频 feed 详情并发数（过大易触发限流）
_VIDEO_DETAIL_CONCURRENCY = max(1, min(8, int(os.environ.get("XHS_VIDEO_DETAIL_CONCURRENCY", "4") or 4)))

# 尝试导入execjs用于签名生成
try:
    import execjs
    EXECJS_AVAILABLE = True
except ImportError:
    EXECJS_AVAILABLE = False
    print("警告: PyExecJS未安装，无法生成签名。请运行: pip install PyExecJS", file=sys.stderr)

def parse_args():
    """解析命令行参数"""
    parser = argparse.ArgumentParser(description='小红书爬虫')
    parser.add_argument('--keyword', type=str, required=True, help='搜索关键词')
    parser.add_argument('--type', type=str, default='note', choices=['note', 'image', 'video'], help='爬取类型')
    parser.add_argument('--sortType', type=int, default=0, help='排序方式：0-综合排序, 1-最新, 2-最多点赞, 3-最多评论, 4-最多收藏')
    parser.add_argument('--noteTime', type=int, default=0, help='笔记时间：0-不限, 1-一天内, 2-一周内, 3-半年内')
    parser.add_argument('--page', type=int, default=1, help='页码')
    parser.add_argument('--pageSize', type=int, default=10, help='每页数量')
    parser.add_argument('--cookie', type=str, default='', help='Cookie值（必需，用于认证）')
    return parser.parse_args()

def trans_cookies(cookies_str):
    """解析Cookie字符串为字典"""
    if '; ' in cookies_str:
        ck = {i.split('=')[0]: '='.join(i.split('=')[1:]) for i in cookies_str.split('; ')}
    else:
        ck = {i.split('=')[0]: '='.join(i.split('=')[1:]) for i in cookies_str.split(';')}
    return ck

def generate_x_b3_traceid(length=16):
    """生成x-b3-traceid"""
    traceid = ""
    for _ in range(length):
        traceid += "abcdef0123456789"[math.floor(16 * random.random())]
    return traceid

def generate_xray_traceid():
    """生成x-xray-traceid（简化实现，参考Spider_XHS-master）"""
    # 简化实现：生成一个随机字符串
    # 如果需要完整实现，需要加载 xhs_xray.js 文件
    return ''.join(random.choices('abcdef0123456789', k=32))

_js_engine_cached = None
_js_engine_lock = threading.Lock()
_js_call_lock = threading.Lock()


def get_js_engine():
    """获取 JavaScript 执行引擎（进程内只 compile 一次，避免每条 feed 请求重复读盘/编译）"""
    global _js_engine_cached
    if not EXECJS_AVAILABLE:
        return None
    if _js_engine_cached is not None:
        return _js_engine_cached
    with _js_engine_lock:
        if _js_engine_cached is not None:
            return _js_engine_cached
        script_dir = os.path.dirname(os.path.abspath(__file__))
        js_file = os.path.join(script_dir, 'static', 'xhs_xs_xsc_56.js')
        if not os.path.exists(js_file):
            js_file = os.path.join(script_dir, '..', 'static', 'xhs_xs_xsc_56.js')
        if not os.path.exists(js_file):
            print(f"警告: 签名JS文件不存在: {js_file}", file=sys.stderr)
            return None
        try:
            with open(js_file, 'r', encoding='utf-8') as f:
                js_code = f.read()
            _js_engine_cached = execjs.compile(js_code)
            return _js_engine_cached
        except Exception as e:
            print(f"警告: 无法加载签名JS文件: {e}", file=sys.stderr)
            return None

def generate_xs_xs_common(a1, api, data='', method='POST'):
    """生成签名（x-s, x-s-common, x-t）
    完全按照Spider_XHS-master的xhs_util.py实现
    """
    if not EXECJS_AVAILABLE:
        return None, None, None
    
    try:
        js = get_js_engine()
        if not js:
            return None, None, None
        
        # PyExecJS 非线程安全：并行拉取视频详情时串行化 call
        with _js_call_lock:
            ret = js.call('get_request_headers_params', api, data, a1, method)
        # 直接访问字典键，与参考项目完全一致
        xs, xt, xs_common = ret['xs'], ret['xt'], ret['xs_common']
        return xs, xt, xs_common
    except Exception as e:
        print(f"警告: 生成签名失败: {e}", file=sys.stderr)
        return None, None, None

def get_request_headers_template():
    """获取请求头模板（参考Spider_XHS-master的xhs_util.py第53-76行）"""
    return {
        "authority": "edith.xiaohongshu.com",
        "accept": "application/json, text/plain, */*",
        "accept-language": "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6",
        "cache-control": "no-cache",
        "content-type": "application/json;charset=UTF-8",
        "origin": "https://www.xiaohongshu.com",
        "pragma": "no-cache",
        "referer": "https://www.xiaohongshu.com/",
        "sec-ch-ua": "\"Not A(Brand\";v=\"99\", \"Microsoft Edge\";v=\"121\", \"Chromium\";v=\"121\"",
        "sec-ch-ua-mobile": "?0",
        "sec-ch-ua-platform": "\"Windows\"",
        "sec-fetch-dest": "empty",
        "sec-fetch-mode": "cors",
        "sec-fetch-site": "same-site",
        "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0",
        "x-b3-traceid": "",
        "x-mns": "unload",
        "x-s": "",
        "x-s-common": "",
        "x-t": "",
        "x-xray-traceid": generate_xray_traceid()
    }

def generate_headers(a1, api, data='', method='POST'):
    """生成请求头（完全按照Spider_XHS-master的xhs_util.py第78-88行实现）"""
    # 先调用签名生成函数，传入原始的data（可能是字典）
    xs, xt, xs_common = generate_xs_xs_common(a1, api, data, method)
    x_b3_traceid = generate_x_b3_traceid()
    headers = get_request_headers_template()
    # 处理 None 值，避免后续 len() 报错
    headers['x-s'] = xs or ''
    headers['x-t'] = str(xt) if xt is not None else ''
    headers['x-s-common'] = xs_common or ''
    headers['x-b3-traceid'] = x_b3_traceid
    # 如果data存在，转换为JSON字符串（参考Spider_XHS-master第87行）
    if data:
        data = json.dumps(data, separators=(',', ':'), ensure_ascii=False)
    return headers, data

def generate_request_params(cookies_str, api, data='', method='POST'):
    """生成请求参数（完全按照Spider_XHS-master的xhs_util.py第90-94行实现）"""
    cookies = trans_cookies(cookies_str)
    # 完全按照参考项目，直接访问a1，如果不存在会抛出异常
    # 这样可以确保a1一定存在，否则签名生成会失败
    try:
        a1 = cookies['a1']
    except KeyError:
        print("错误: Cookie中未找到a1值，无法生成签名", file=sys.stderr)
        raise ValueError("Cookie中必须包含a1值")
    headers, data = generate_headers(a1, api, data, method)
    return headers, cookies, data

def get_note_detail(note_id, cookie_str, xsec_token=None, xsec_source='pc_search'):
    """
    获取笔记详情，用于提取视频URL（参考Spider_XHS-master项目）
    
    Args:
        note_id: 笔记ID
        cookie_str: Cookie字符串
        xsec_token: xsec_token（从搜索API返回的item中获取）
        xsec_source: xsec_source，默认为'pc_search'
    
    Returns:
        视频URL，如果获取失败返回None
    """
    try:
        # 详情API路径（参考Spider_XHS-master的xhs_pc_apis.py第368行）
        api_path = '/api/sns/web/v1/feed'
        
        # 请求参数（参考Spider_XHS-master的xhs_pc_apis.py第369-381行）
        params = {
            'source_note_id': note_id,
            'image_formats': ['jpg', 'webp', 'avif'],
            'extra': {
                'need_body_topic': '1'
            }
        }
        
        # 添加 xsec_token 和 xsec_source（参考Spider_XHS-master的xhs_pc_apis.py第379-380行）
        if xsec_token:
            params['xsec_token'] = xsec_token
        params['xsec_source'] = xsec_source
        
        # 使用generate_request_params函数，完全按照Spider_XHS-master的方式
        result = generate_request_params(cookie_str, api_path, params, 'POST')
        if result is None or result[0] is None:
            print(f"错误: 无法生成请求参数，可能是Cookie无效或缺少a1值", file=sys.stderr)
            return None
        
        headers, cookies, data_str = result
        
        url = 'https://edith.xiaohongshu.com' + api_path
        # 从 headers 中移除 Cookie，因为使用 cookies 参数
        headers_without_cookie = {k: v for k, v in headers.items() if k.lower() != 'cookie'}
        
        if _SPIDER_DEBUG:
            print(f"详情API请求URL: {url}", file=sys.stderr)
            print(f"详情API请求参数: {data_str[:200] if isinstance(data_str, str) else str(data_str)[:200]}...", file=sys.stderr)
        
        # 使用data.encode('utf-8')，与Spider_XHS-master保持一致
        response = requests.post(url, headers=headers_without_cookie, data=data_str.encode('utf-8'), cookies=cookies, timeout=15)
        
        if _SPIDER_DEBUG:
            print(f"详情API响应状态码: {response.status_code}", file=sys.stderr)
            if response.status_code != 200:
                print(f"详情API响应内容（前500字符）: {response.text[:500]}", file=sys.stderr)
        
        if response.status_code == 200:
            result = response.json()
            if _SPIDER_DEBUG:
                print(f"详情API响应: code={result.get('code')}, success={result.get('success')}, msg={result.get('msg')}", file=sys.stderr)
            if result.get('code') == 0 and result.get('success'):
                data = result.get('data', {})
                items = data.get('items', [])
                if items and len(items) > 0:
                    note_item = items[0]
                    note_card = note_item.get('note_card', {})
                    video_info = note_card.get('video', {})
                    if _SPIDER_DEBUG and video_info:
                        print(f"详情API找到video_info字段", file=sys.stderr)
                    if video_info:
                        consumer = video_info.get('consumer', {})
                        if consumer:
                            origin_video_key = consumer.get('origin_video_key', '')
                            if origin_video_key:
                                video_url = 'https://sns-video-bd.xhscdn.com/' + origin_video_key
                                if _SPIDER_DEBUG:
                                    print(f"成功从详情API提取视频URL（方式1）: {video_url[:100]}", file=sys.stderr)
                                return video_url
                        media = video_info.get('media', {})
                        if media:
                            stream = media.get('stream', {})
                            if stream:
                                h264_list = stream.get('h264', [])
                                if h264_list and len(h264_list) > 0:
                                    video_url = h264_list[0].get('master_url', '')
                                    if video_url:
                                        if _SPIDER_DEBUG:
                                            print(f"成功从详情API提取视频URL（方式2）: {video_url[:100]}", file=sys.stderr)
                                        return video_url
                        video_url = video_info.get('url', '')
                        if video_url:
                            if _SPIDER_DEBUG:
                                print(f"成功从详情API提取视频URL（方式3）: {video_url[:100]}", file=sys.stderr)
                            return video_url
                    elif _SPIDER_DEBUG:
                        print(f"详情API返回的note_card中没有video字段", file=sys.stderr)
                        note_card_str = json.dumps(note_card, ensure_ascii=False)
                        print(f"详情API返回的note_card结构（前500字符）: {note_card_str[:500]}", file=sys.stderr)
                else:
                    if _SPIDER_DEBUG:
                        print(f"详情API返回的items为空", file=sys.stderr)
            else:
                if _SPIDER_DEBUG:
                    print(f"详情API返回错误: code={result.get('code')}, msg={result.get('msg')}", file=sys.stderr)
        else:
            if _SPIDER_DEBUG:
                print(f"详情API请求失败，状态码: {response.status_code}", file=sys.stderr)
        
        return None
    except Exception as e:
        print(f"获取笔记详情失败 {note_id}: {e}", file=sys.stderr)
        return None

def spider_xiaohongshu(keyword, spider_type, page, page_size, cookie=None, sort_type_choice=0, note_time=0):
    """
    爬取小红书内容 - 使用官方API接口
    
    使用小红书搜索API: https://edith.xiaohongshu.com/api/sns/web/v1/search/notes
    
    分页说明（与业务层配合）：
    - 本服务/Java 端对「总条数、最大页码」没有额外硬编码限制；翻页即把 page 递增再调接口即可。
    - 请求体里的 page_size 必须与 PC 端一致固定为 20：传其它值（如 10）时接口可能只返回
      data={{has_more:false}} 而无 items，导致爬取为空。
    - 前端传入的 pageSize 仅用于返回列表截断（最多取前 pageSize 条）。
    - 小红书侧仍可能：Cookie 失效、账号异常、频率限流（见下方错误码处理）；深翻页时若结果重复或
      变空，多为平台策略或需保持同一次搜索的 search_id（当前每次请求会新生成 search_id，深页不保证
      与官方 PC 行为完全一致，必要时可扩展为传入 search_id）。

    Args:
        keyword: 搜索关键词
        spider_type: 爬取类型 (note/image/video)
        page: 页码
        page_size: 每页数量
        cookie: Cookie值（必需，用于认证）
        sort_type_choice: 排序方式 0-综合排序, 1-最新, 2-最多点赞, 3-最多评论, 4-最多收藏
        note_time: 笔记时间 0-不限, 1-一天内, 2-一周内, 3-半年内
    
    Returns:
        爬取结果列表
    """
    results = []
    
    try:
        # 检查Cookie
        if not cookie or not cookie.strip():
            print(f"警告: 未提供Cookie，无法调用API，返回空列表", file=sys.stderr)
            return []
        
        # 构建API请求URL
        api_url = "https://edith.xiaohongshu.com/api/sns/web/v1/search/notes"
        
        # 生成 search_id（21 位）；深翻页若与官方不一致，可后续改为首屏返回后复用同一 search_id
        search_id = ''.join(random.choices('abcdef0123456789', k=21))

        # 搜索接口 page_size 非 20 时实测会返回无 items 的空壳 data，必须与 Spider_XHS / PC 抓包一致
        API_NOTES_PAGE_SIZE = 20

        # 根据spider_type设置note_type和filter_note_type
        # 0-不限, 1-视频, 2-图文
        note_type_map = {
            'note': 0,    # 不限/全部
            'video': 1,   # 视频
            'image': 2    # 图文
        }
        note_type = note_type_map.get(spider_type, 0)
        
        # 设置filter_note_type（参考Spider_XHS项目）
        filter_note_type = "不限"
        if spider_type == 'video':
            filter_note_type = "视频笔记"
        elif spider_type == 'image':
            filter_note_type = "普通笔记"
        
        # 根据sort_type_choice设置sort_type（参考Spider_XHS项目）
        sort_type = "general"
        if sort_type_choice == 1:
            sort_type = "time_descending"
        elif sort_type_choice == 2:
            sort_type = "popularity_descending"
        elif sort_type_choice == 3:
            sort_type = "comment_descending"
        elif sort_type_choice == 4:
            sort_type = "collect_descending"
        
        # 根据note_time设置filter_note_time（参考Spider_XHS项目）
        filter_note_time = "不限"
        if note_time == 1:
            filter_note_time = "一天内"
        elif note_time == 2:
            filter_note_time = "一周内"
        elif note_time == 3:
            filter_note_time = "半年内"
        
        # 构建请求参数（完全按照Spider_XHS-master的xhs_pc_apis.py第466-512行）
        # 处理geo字段（参考第462-463行）
        geo_value = ""
        # 如果将来需要支持geo，可以在这里处理
        # if geo:
        #     geo_value = json.dumps(geo, separators=(',', ':'))
        
        params = {
            "keyword": keyword,
            "page": page,
            "page_size": API_NOTES_PAGE_SIZE,
            "search_id": search_id,
            "sort": "general",  # 固定为"general"，排序方式通过filters中的sort_type控制（参考第471行）
            "note_type": note_type,  # 参考第472行
            "ext_flags": [],  # 参考第473行
            "filters": [
                {
                    "tags": [sort_type],  # 参考第476-479行
                    "type": "sort_type"
                },
                {
                    "tags": [filter_note_type],  # 参考第481-485行
                    "type": "filter_note_type"
                },
                {
                    "tags": [filter_note_time],  # 参考第487-491行
                    "type": "filter_note_time"
                },
                {
                    "tags": ["不限"],  # 参考第493-497行，默认不限
                    "type": "filter_note_range"
                },
                {
                    "tags": ["不限"],  # 参考第499-503行，默认不限
                    "type": "filter_pos_distance"
                }
            ],
            "geo": geo_value,  # 参考第506行
            "image_formats": ["jpg", "webp", "avif"]  # 参考第507-510行
        }
        
        if _SPIDER_DEBUG:
            print(f"调试: 完整请求参数: {json.dumps(params, ensure_ascii=False, indent=2)}", file=sys.stderr)
        
        # 生成API路径（用于签名生成）
        api_path = "/api/sns/web/v1/search/notes"
        
        # 关键修复：完全按照Spider_XHS-master的方式处理
        # 使用generate_request_params函数，它会：
        # 1. 提取a1值（如果不存在会抛出异常）
        # 2. 调用generate_headers生成签名（传入字典params）
        # 3. 在generate_headers内部将params转换为JSON字符串
        # 4. 返回headers、cookies和转换后的data_str
        # 这样确保签名生成时使用的数据和实际发送的数据完全一致
        try:
            headers, cookies, data_str = generate_request_params(cookie, api_path, params, 'POST')
        except (ValueError, KeyError) as e:
            print(f"错误: 无法生成请求参数: {e}", file=sys.stderr)
            return []
        
        # 检查Cookie中是否有a1值
        a1_value = cookies.get('a1', '')
        if not a1_value:
            print(f"警告: Cookie中未找到a1值，可能影响签名生成", file=sys.stderr)
        else:
            if _SPIDER_DEBUG:
                print(f"调试: 找到a1值，长度={len(a1_value)}", file=sys.stderr)
        
        # 发送POST请求（完全按照Spider_XHS-master的xhs_pc_apis.py第514行）
        print(f"调用小红书搜索API: {api_url}", file=sys.stderr)
        print(
            f"请求参数: keyword={keyword}, page={page}, page_size(接口固定)={API_NOTES_PAGE_SIZE}, page_size(返回截断)={page_size}",
            file=sys.stderr,
        )
        if _SPIDER_DEBUG:
            print(
                f"完整请求参数（前500字符）: {data_str[:500] if isinstance(data_str, str) else str(data_str)[:500]}",
                file=sys.stderr,
            )
        
        # 打印关键请求头（不包含完整签名）
        # 安全处理 None 值
        x_s = headers.get('x-s') or ''
        x_t = headers.get('x-t') or ''
        x_s_common = headers.get('x-s-common') or ''
        if _SPIDER_DEBUG:
            print(
                f"调试: 请求头中的关键字段: x-s长度={len(x_s)}, x-t={x_t}, x-s-common长度={len(x_s_common)}",
                file=sys.stderr,
            )
        
        try:
            # 参考Spider_XHS-master项目第514行，直接使用requests.post
            # 从 headers 中移除 Cookie，因为使用 cookies 参数
            headers_without_cookie = {k: v for k, v in headers.items() if k.lower() != 'cookie'}
            # 关键修复：使用data.encode('utf-8')，与Spider_XHS-master保持一致
            # data_str已经是JSON字符串（由generate_headers转换）
            if _SPIDER_DEBUG:
                print(
                    f"调试: 发送请求，URL={api_url}, headers数量={len(headers_without_cookie)}, cookies数量={len(cookies)}",
                    file=sys.stderr,
                )
            response = requests.post(api_url, headers=headers_without_cookie, data=data_str.encode('utf-8'), cookies=cookies, timeout=15, verify=False)
            if _SPIDER_DEBUG:
                print(f"调试: API响应状态码: {response.status_code}", file=sys.stderr)
            response.raise_for_status()
            
            # 解析JSON响应
            response_data = response.json()
            
            # 检查响应状态
            api_code = response_data.get('code', 0)
            api_success = response_data.get('success', False)
            
            if api_code != 0 or not api_success:
                error_msg = response_data.get('msg', '未知错误')
                print(f"API返回错误: code={api_code}, msg={error_msg}", file=sys.stderr)
                
                # 特殊处理各种错误
                if api_code == -100 or '登录' in error_msg or '过期' in error_msg or 'expired' in error_msg.lower():
                    print(f"错误: Cookie已过期，请重新获取Cookie", file=sys.stderr)
                    print(f"提示: 请在浏览器中重新登录小红书，然后复制最新的Cookie值", file=sys.stderr)
                elif api_code == 300011 or '账号异常' in error_msg or '异常' in error_msg:
                    print(f"错误: 账号被检测为异常，可能是反爬虫机制", file=sys.stderr)
                    print(f"提示: 1) 请稍后再试 2) 尝试更换Cookie 3) 降低请求频率", file=sys.stderr)
                elif api_code == 300012 or '频率' in error_msg or '限流' in error_msg:
                    print(f"错误: 请求频率过高，被限流", file=sys.stderr)
                    print(f"提示: 请降低请求频率，稍后再试", file=sys.stderr)
                
                # API返回错误，返回空列表
                print(f"提示: API返回错误，返回空列表", file=sys.stderr)
                return []
            
            # 提取数据（参考Spider_XHS-master的xhs_pc_apis.py第543行）
            data = response_data.get('data', {})
            
            if _SPIDER_DEBUG:
                print(f"API响应状态: code={api_code}, success={api_success}", file=sys.stderr)
                print(f"API返回 data 字段的键: {list(data.keys()) if data else 'data为空'}", file=sys.stderr)
            
            # 检查是否有items字段（参考Spider_XHS-master的xhs_pc_apis.py第543行）
            if "items" not in data:
                # 打印完整响应结构用于调试
                print(f"警告: data中没有items字段", file=sys.stderr)
                print(f"完整响应结构: {json.dumps(response_data, ensure_ascii=False, indent=2)}", file=sys.stderr)
                print(f"提示: API返回成功但没有items字段，可能原因：", file=sys.stderr)
                print(f"  1) 搜索关键词无结果", file=sys.stderr)
                print(f"  2) API结构发生变化", file=sys.stderr)
                print(f"  3) Cookie权限不足或已过期", file=sys.stderr)
                print(f"  4) 请求参数有问题", file=sys.stderr)
                # 检查是否有其他可能包含数据的字段
                for key in data.keys():
                    value = data[key]
                    if isinstance(value, list) and len(value) > 0:
                        print(f"  发现列表字段 {key}，长度: {len(value)}", file=sys.stderr)
                    elif isinstance(value, dict):
                        print(f"  发现字典字段 {key}，键: {list(value.keys())}", file=sys.stderr)
                return []
            
            items = data.get('items', [])
            print(f"API返回 {len(items)} 条数据", file=sys.stderr)
            
            # 如果items为空列表，说明搜索无结果
            if not items:
                print(f"提示: items为空列表，搜索无结果", file=sys.stderr)
                return []
            
            # 视频模式：无直链的项在循环结束后并行请求 /feed，避免逐条 sleep + 串行请求
            pending_video_fetches = []
            
            # 解析每个笔记项
            for item in items:
                try:
                    note_card = item.get('note_card', {})
                    if not note_card:
                        continue
                    
                    # 提取基本信息
                    note_id = item.get('id', '')
                    display_title = note_card.get('display_title', '')
                    
                    # 提取用户信息
                    user = note_card.get('user', {})
                    author = user.get('nickname', user.get('nick_name', ''))
                    author_id = user.get('user_id', '')
                    
                    # 提取交互信息
                    interact_info = note_card.get('interact_info', {})
                    like_count = int(interact_info.get('liked_count', 0))
                    collection_count = int(interact_info.get('collected_count', 0))
                    comment_count = int(interact_info.get('comment_count', 0))
                    shared_count = int(interact_info.get('shared_count', 0))
                    
                    # 提取封面
                    cover = note_card.get('cover', {})
                    cover_url = cover.get('url_default', cover.get('url_pre', ''))
                    
                    # 提取图片列表
                    image_list = note_card.get('image_list', [])
                    images = []
                    for img_item in image_list:
                        info_list = img_item.get('info_list', [])
                        for info in info_list:
                            if info.get('image_scene') == 'WB_DFT':  # 使用默认图片
                                img_url = info.get('url', '')
                                if img_url and img_url not in images:
                                    images.append(img_url)
                    
                    # 如果没有封面，使用第一张图片
                    if not cover_url and images:
                        cover_url = images[0]
                    
                    # 提取视频信息（如果有）
                    video_url = ''
                    is_video_note = False
                    
                    note_type = item.get('model_type', '')
                    if _SPIDER_DEBUG:
                        print(f"调试: 笔记 {note_id}, model_type={note_type}", file=sys.stderr)

                    if note_type == 'video' or note_type == 'note_video':
                        is_video_note = True
                        if _SPIDER_DEBUG:
                            print(f"调试: 通过model_type判断为视频笔记: {note_id}", file=sys.stderr)

                    if _SPIDER_DEBUG and spider_type == 'video':
                        print(f"调试: 笔记 {note_id} 的 note_card 字段: {list(note_card.keys())}", file=sys.stderr)
                        note_card_str = json.dumps(note_card, ensure_ascii=False)
                        print(f"调试: note_card 完整结构（前500字符）: {note_card_str[:500]}", file=sys.stderr)

                    video_info = note_card.get('video') or {}
                    if not video_info:
                        video_info = item.get('video_info') or item.get('video') or {}

                    if video_info:
                        if _SPIDER_DEBUG:
                            print(f"调试: 找到video_info字段: {note_id}", file=sys.stderr)
                            print(f"找到视频信息: {json.dumps(video_info, ensure_ascii=False)[:200]}", file=sys.stderr)
                        is_video_note = True

                        try:
                            consumer = video_info.get('consumer', {})
                            if consumer:
                                origin_video_key = consumer.get('origin_video_key', '')
                                if origin_video_key:
                                    video_url = 'https://sns-video-bd.xhscdn.com/' + origin_video_key
                                    if _SPIDER_DEBUG:
                                        print(f"成功提取视频URL（方式1）: {video_url[:100]}", file=sys.stderr)
                        except Exception as e:
                            if _SPIDER_DEBUG:
                                print(f"提取视频URL方式1失败: {e}", file=sys.stderr)

                        if not video_url:
                            try:
                                media = video_info.get('media', {})
                                if media:
                                    stream = media.get('stream', {})
                                    if stream:
                                        h264_list = stream.get('h264', [])
                                        if h264_list and len(h264_list) > 0:
                                            video_url = h264_list[0].get('master_url', '')
                                            if video_url and _SPIDER_DEBUG:
                                                print(f"成功提取视频URL（方式2）: {video_url[:100]}", file=sys.stderr)
                            except Exception as e:
                                if _SPIDER_DEBUG:
                                    print(f"提取视频URL方式2失败: {e}", file=sys.stderr)

                        if not video_url:
                            video_url = video_info.get('url', '')
                            if video_url and _SPIDER_DEBUG:
                                print(f"成功提取视频URL（方式3）: {video_url[:100]}", file=sys.stderr)

                        if not video_url:
                            try:
                                media = video_info.get('media', {})
                                if media:
                                    video_url = media.get('url', '')
                                    if video_url and _SPIDER_DEBUG:
                                        print(f"成功提取视频URL（方式4）: {video_url[:100]}", file=sys.stderr)
                            except Exception:
                                pass

                        if not video_url:
                            try:
                                vcover = video_info.get('cover', {})
                                if vcover:
                                    video_url = vcover.get('url', '')
                                    if video_url and _SPIDER_DEBUG:
                                        print(f"成功提取视频URL（方式5）: {video_url[:100]}", file=sys.stderr)
                            except Exception:
                                pass

                        if not video_url and _SPIDER_DEBUG:
                            print(f"警告: 找到video_info但无法提取直链，将尝试 feed 详情: {json.dumps(video_info, ensure_ascii=False)[:300]}", file=sys.stderr)
                    elif spider_type == 'video' and _SPIDER_DEBUG:
                        print(f"警告: 搜索项无 video 结构，笔记 {note_id} 将走 feed 详情（并行）", file=sys.stderr)
                    
                    # 根据spider_type过滤笔记类型
                    if spider_type == 'video':
                        if not is_video_note and not video_info:
                            if _SPIDER_DEBUG:
                                print(f"调试: API已过滤视频类型，笔记 {note_id} 无明确视频标识(model_type={note_type})，仍保留", file=sys.stderr)
                            is_video_note = True
                        elif not is_video_note and not video_url:
                            if _SPIDER_DEBUG:
                                print(f"调试: 笔记 {note_id} 有 video 结构但未提取到URL，仍保留", file=sys.stderr)
                            is_video_note = True
                    elif spider_type == 'image':
                        if is_video_note or video_url:
                            if _SPIDER_DEBUG:
                                print(f"跳过视频笔记: {note_id}", file=sys.stderr)
                            continue
                    
                    # 提取时间信息
                    corner_tag_info = note_card.get('corner_tag_info', [])
                    publish_time = ''
                    for tag in corner_tag_info:
                        if tag.get('type') == 'publish_time':
                            publish_time = tag.get('text', '')
                            break
                    
                    # 构建笔记URL
                    note_url = f"https://www.xiaohongshu.com/explore/{note_id}" if note_id else ''
                    
                    result_item = {
                        'id': note_id,
                        'title': display_title,
                        'content': display_title,  # API可能不返回完整内容，使用标题
                        'cover': cover_url,
                        'images': images,
                        'video': video_url,
                        'author': author,
                        'authorId': author_id,
                        'url': note_url,
                        'likeCount': like_count,
                        'collectionCount': collection_count,
                        'commentCount': comment_count,
                        'viewCount': shared_count,  # 使用分享数作为近似值，或保持为0
                        'publishTime': publish_time,
                        'type': spider_type
                    }
                    
                    results.append(result_item)
                    if spider_type == 'video' and cookie and note_id and not (video_url and str(video_url).strip()):
                        pending_video_fetches.append((len(results) - 1, note_id, item.get('xsec_token', '')))
                    if _SPIDER_DEBUG:
                        print(f"成功解析笔记: id={note_id}, title={display_title[:30]}", file=sys.stderr)
                    
                except Exception as e:
                    print(f"解析笔记项失败: {e}", file=sys.stderr)
                    import traceback
                    traceback.print_exc(file=sys.stderr)
                    continue
            
            if pending_video_fetches:
                print(f"视频模式: 并行请求 feed 详情共 {len(pending_video_fetches)} 条（并发 {_VIDEO_DETAIL_CONCURRENCY}，签名已缓存）", file=sys.stderr)

                def _fetch_one_video_detail(tup):
                    res_idx, nid, xtok = tup
                    return res_idx, get_note_detail(nid, cookie, xtok, 'pc_search')

                with ThreadPoolExecutor(max_workers=_VIDEO_DETAIL_CONCURRENCY) as ex:
                    futures = [ex.submit(_fetch_one_video_detail, t) for t in pending_video_fetches]
                    for fut in as_completed(futures):
                        try:
                            res_idx, vurl = fut.result()
                            if vurl and 0 <= res_idx < len(results):
                                results[res_idx]['video'] = vurl
                                if _SPIDER_DEBUG:
                                    print(f"feed 详情写入 video: idx={res_idx} url={vurl[:80]}...", file=sys.stderr)
                        except Exception as ex:
                            print(f"并行 feed 详情任务异常: {ex}", file=sys.stderr)
            
            # 如果没找到数据，返回空列表（不再返回模拟数据）
            if not results:
                print(f"提示: API返回了数据但解析失败或无数据，返回空列表", file=sys.stderr)
                return []
            else:
                # 接口固定 20 条/页；按用户 pageSize 截断返回
                if page_size and page_size > 0 and len(results) > page_size:
                    results = results[:page_size]
                    print(f"✅ 成功获取 {len(results)} 条真实数据（已根据page_size={page_size}截取）", file=sys.stderr)
                else:
                    print(f"✅ 成功获取 {len(results)} 条真实数据", file=sys.stderr)
        
        except requests.exceptions.RequestException as e:
            print(f"API请求失败: {e}", file=sys.stderr)
            return []
        
    except Exception as e:
        print(f"爬取失败: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr)
        return []
    
    return results

def spider_xiaohongshu_old(keyword, spider_type, page, page_size, cookie=None):
    """
    旧版本：通过HTML解析（已废弃，保留作为备用）
    """
    results = []
    
    try:
        # 构建搜索URL
        import urllib.parse
        encoded_keyword = urllib.parse.quote(keyword)
        search_url = f"https://www.xiaohongshu.com/search_result?keyword={encoded_keyword}&page={page}"
        
        # 发送请求
        headers = get_headers()
        session = requests.Session()
        
        # 如果提供了Cookie，添加到请求头
        if cookie and cookie.strip():
            headers['Cookie'] = cookie.strip()
            print(f"使用提供的Cookie进行请求", file=sys.stderr)
        else:
            print(f"警告: 未提供Cookie，可能无法获取真实数据", file=sys.stderr)
        
        # 尝试发送请求
        try:
            response = session.get(search_url, headers=headers, timeout=15, verify=False, allow_redirects=True)
            response.raise_for_status()
            
            # 检查是否被重定向到登录页或验证页
            if 'login' in response.url.lower() or 'verify' in response.url.lower():
                print(f"警告: 需要登录或验证，URL: {response.url}，返回空列表", file=sys.stderr)
                return []
            
            # 解析HTML
            soup = BeautifulSoup(response.text, 'html.parser')
            
            # 方法1: 尝试从页面中提取JSON数据（小红书可能使用JSON格式）
            import re
            json_data = None
            # 查找script标签中的JSON数据
            script_tags = soup.find_all('script')
            for script in script_tags:
                if script.string:
                    # 查找包含笔记数据的JSON
                    json_match = re.search(r'window\.__INITIAL_STATE__\s*=\s*({.+?});', script.string, re.DOTALL)
                    if json_match:
                        try:
                            json_data = json.loads(json_match.group(1))
                            print(f"找到JSON数据", file=sys.stderr)
                            break
                        except:
                            pass
                    # 查找其他可能的JSON格式
                    json_match = re.search(r'"noteList":\s*(\[.+?\])', script.string, re.DOTALL)
                    if json_match:
                        try:
                            json_data = json.loads(json_match.group(1))
                            print(f"找到笔记列表JSON数据", file=sys.stderr)
                            break
                        except:
                            pass
            
            # 方法2: 如果找到JSON数据，直接解析
            if json_data:
                # 这里需要根据实际JSON结构来解析
                # 由于JSON结构可能变化，这里提供一个基础框架
                print(f"尝试解析JSON数据，类型: {type(json_data)}", file=sys.stderr)
                # 如果json_data是列表，直接使用
                if isinstance(json_data, list):
                    for item in json_data[:page_size]:
                        if isinstance(item, dict):
                            result_item = {
                                'id': item.get('id', ''),
                                'title': item.get('title', item.get('desc', '')),
                                'content': item.get('content', item.get('desc', '')),
                                'cover': item.get('cover', item.get('image', '')),
                                'images': item.get('images', []),
                                'video': item.get('video', ''),
                                'author': item.get('author', item.get('user', {}).get('nickname', '')),
                                'authorId': item.get('authorId', item.get('user', {}).get('userId', '')),
                                'url': item.get('url', ''),
                                'likeCount': item.get('likeCount', item.get('likedCount', 0)),
                                'collectionCount': item.get('collectionCount', item.get('collectedCount', 0)),
                                'commentCount': item.get('commentCount', 0),
                                'viewCount': item.get('viewCount', 0),
                                'publishTime': item.get('publishTime', ''),
                                'type': spider_type
                            }
                            if result_item['title'] or result_item['cover'] or result_item['url']:
                                results.append(result_item)
            
            # 方法3: 如果JSON解析失败或没有JSON，使用HTML解析
            if not results:
                # 尝试多种选择器查找笔记项
                note_items = []
                
                # 查找常见的笔记容器
                selectors = [
                    'div[class*="note"]',
                    'div[class*="feed"]',
                    'div[class*="item"]',
                    'article',
                    'div[data-v-]',  # Vue组件
                ]
                
                for selector in selectors:
                    note_items = soup.select(selector)
                    if note_items:
                        print(f"使用选择器找到 {len(note_items)} 个元素: {selector}", file=sys.stderr)
                        break
                
                # 如果还是没找到，尝试查找包含图片的元素
                if not note_items:
                    note_items = soup.find_all('div', class_=lambda x: x and ('img' in x.lower() or 'pic' in x.lower()))
            
                # 解析找到的元素
                print(f"开始解析 {len(note_items)} 个元素", file=sys.stderr)
                for idx, item in enumerate(note_items[:page_size]):
                    try:
                        # 调试：输出元素的部分HTML（前500字符）
                        item_html = str(item)[:500] if hasattr(item, '__str__') else str(item)
                        print(f"元素 {idx+1} HTML预览: {item_html}...", file=sys.stderr)
                        
                        result_item = {
                            'id': '',
                            'title': '',
                            'content': '',
                            'cover': '',
                            'images': [],
                            'video': '',
                            'author': '',
                            'authorId': '',
                            'url': '',
                            'likeCount': 0,
                            'collectionCount': 0,
                            'commentCount': 0,
                            'viewCount': 0,
                            'publishTime': '',
                            'type': spider_type
                        }
                        
                        # 提取标题 - 尝试多种方式
                        title_selectors = ['h3', 'h2', 'h1', '.title', '[class*="title"]', 'a[title]', 'span[class*="title"]', 'div[class*="title"]']
                        title_found = False
                        for selector in title_selectors:
                            title_elem = item.select_one(selector)
                            if title_elem:
                                title_text = title_elem.get_text(strip=True) or title_elem.get('title', '') or title_elem.get('alt', '')
                                if title_text:
                                    result_item['title'] = title_text
                                    title_found = True
                                    print(f"元素 {idx+1} 找到标题: {title_text[:50]}", file=sys.stderr)
                                    break
                        
                        if not title_found:
                            # 尝试从所有文本中提取
                            all_text = item.get_text(strip=True)
                            if all_text and len(all_text) > 10:
                                result_item['title'] = all_text[:100]  # 取前100字符作为标题
                                print(f"元素 {idx+1} 使用文本作为标题: {result_item['title'][:50]}", file=sys.stderr)
                        
                        # 提取封面 - 查找所有图片
                        img_elem = item.find('img')
                        if img_elem:
                            cover_url = img_elem.get('src', '') or img_elem.get('data-src', '') or img_elem.get('data-original', '') or img_elem.get('data-url', '')
                            if cover_url:
                                if not cover_url.startswith('http'):
                                    if cover_url.startswith('//'):
                                        cover_url = 'https:' + cover_url
                                    else:
                                        cover_url = 'https://www.xiaohongshu.com' + cover_url
                                result_item['cover'] = cover_url
                                print(f"元素 {idx+1} 找到封面: {cover_url[:50]}", file=sys.stderr)
                        
                        # 提取所有图片
                        all_imgs = item.find_all('img')
                        print(f"元素 {idx+1} 找到 {len(all_imgs)} 个图片标签", file=sys.stderr)
                        for img in all_imgs:
                            img_url = img.get('src', '') or img.get('data-src', '') or img.get('data-original', '') or img.get('data-url', '')
                            if img_url:
                                if not img_url.startswith('http'):
                                    if img_url.startswith('//'):
                                        img_url = 'https:' + img_url
                                    else:
                                        img_url = 'https://www.xiaohongshu.com' + img_url
                                if img_url not in result_item['images']:
                                    result_item['images'].append(img_url)
                        
                        # 提取链接 - 尝试多种方式
                        link_elem = item.find('a', href=True)
                        if not link_elem:
                            # 尝试查找包含链接的元素
                            link_elem = item.find(attrs={'href': True})
                        
                        if link_elem:
                            href = link_elem.get('href', '')
                            if href:
                                if not href.startswith('http'):
                                    if href.startswith('//'):
                                        href = 'https:' + href
                                    else:
                                        href = 'https://www.xiaohongshu.com' + href
                                result_item['url'] = href
                                print(f"元素 {idx+1} 找到链接: {href[:50]}", file=sys.stderr)
                                # 从URL中提取ID
                                if '/explore/' in href:
                                    result_item['id'] = href.split('/explore/')[-1].split('?')[0]
                                elif '/discovery/item/' in href:
                                    result_item['id'] = href.split('/discovery/item/')[-1].split('?')[0]
                        
                        # 提取作者
                        author_selectors = ['.author', '[class*="author"]', '[class*="user"]', '.username']
                        for selector in author_selectors:
                            author_elem = item.select_one(selector)
                            if author_elem:
                                result_item['author'] = author_elem.get_text(strip=True)
                                if result_item['author']:
                                    break
                        
                        # 提取统计数据
                        stats_selectors = [
                            ('like', ['like', '点赞']),
                            ('collection', ['collection', '收藏']),
                            ('comment', ['comment', '评论']),
                            ('view', ['view', '浏览', '阅读'])
                        ]
                        
                        for stat_name, keywords in stats_selectors:
                            for keyword in keywords:
                                stat_elem = item.find(string=lambda text: text and keyword in text.lower())
                                if stat_elem:
                                    parent = stat_elem.parent
                                    stat_text = parent.get_text(strip=True) if parent else ''
                                    count = parse_count(stat_text)
                                    if count > 0:
                                        if stat_name == 'like':
                                            result_item['likeCount'] = count
                                        elif stat_name == 'collection':
                                            result_item['collectionCount'] = count
                                        elif stat_name == 'comment':
                                            result_item['commentCount'] = count
                                        elif stat_name == 'view':
                                            result_item['viewCount'] = count
                                        break
                        
                        # 只有找到有效数据才添加
                        if result_item['title'] or result_item['cover'] or result_item['url']:
                            results.append(result_item)
                            print(f"成功解析第 {idx+1} 条数据: title={result_item['title'][:20] if result_item['title'] else 'N/A'}, cover={bool(result_item['cover'])}, url={bool(result_item['url'])}", file=sys.stderr)
                        else:
                            print(f"第 {idx+1} 条数据无效，跳过 - title={bool(result_item['title'])}, cover={bool(result_item['cover'])}, url={bool(result_item['url'])}", file=sys.stderr)
                            # 输出更多调试信息
                            print(f"元素 {idx+1} 的类名: {item.get('class', [])}", file=sys.stderr)
                            print(f"元素 {idx+1} 的ID: {item.get('id', 'N/A')}", file=sys.stderr)
                        
                    except Exception as e:
                        print(f"解析笔记项 {idx+1} 失败: {e}", file=sys.stderr)
                        import traceback
                        traceback.print_exc(file=sys.stderr)
                        continue
            
            # 如果没找到数据，返回空列表
            if not results:
                print(f"警告: 未找到有效数据（尝试解析了 {len(note_items) if 'note_items' in locals() else 0} 个元素），可能原因：1) 需要登录 2) 页面结构变化 3) 反爬虫机制。返回空列表", file=sys.stderr)
            else:
                print(f"成功解析 {len(results)} 条真实数据", file=sys.stderr)
        
        except requests.exceptions.RequestException as e:
            print(f"请求失败: {e}，返回空列表", file=sys.stderr)
            return []
        
    except Exception as e:
        print(f"爬取失败: {e}，返回空列表", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr)
        return []
    
    return results

def parse_count(text):
    """解析数量文本（如 "1.2万" -> 12000）"""
    if not text:
        return 0
    try:
        text = text.strip().replace(',', '')
        if '万' in text:
            value = float(text.replace('万', ''))
            return int(value * 10000)
        elif 'k' in text.lower():
            value = float(text.lower().replace('k', ''))
            return int(value * 1000)
        else:
            return int(text)
    except:
        return 0

def main():
    """主函数"""
    args = parse_args()
    
    # 禁用SSL警告
    import urllib3
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    
    # 执行爬取
    results = spider_xiaohongshu(
        args.keyword,
        args.type,
        args.page,
        args.pageSize,
        args.cookie if args.cookie else None,
        args.sortType if hasattr(args, 'sortType') else 0,
        args.noteTime if hasattr(args, 'noteTime') else 0
    )
    
    # 输出JSON结果到stdout（确保只有JSON输出，没有其他内容）
    import sys
    json_output = json.dumps(results, ensure_ascii=False, indent=2)
    print(json_output, file=sys.stdout, flush=True)

if __name__ == '__main__':
    main()

