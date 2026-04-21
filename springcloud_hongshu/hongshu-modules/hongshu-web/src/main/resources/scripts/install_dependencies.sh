#!/bin/bash
# 安装Python依赖脚本

echo "正在安装Python依赖..."

# 检查Python是否安装
if ! command -v python3 &> /dev/null; then
    echo "错误: 未找到 python3，请先安装 Python 3.7+"
    exit 1
fi

# 安装依赖
pip3 install -r requirements.txt

if [ $? -eq 0 ]; then
    echo "依赖安装成功！"
else
    echo "依赖安装失败，请检查网络连接或使用以下命令手动安装："
    echo "pip3 install requests beautifulsoup4 urllib3"
    exit 1
fi


