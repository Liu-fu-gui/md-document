# 常用python脚本
<!-- more -->

## 天翼云obs 脚本拉取
### 依赖安装

```
pip3 install esdk-obs-python --trusted-host mirrors.huaweicloud.com -i https://mirrors.huaweicloud.com/repository/pypi/simple
```
### win版本
python3 
```
from obs import ObsClient
import os
import time
import traceback

# 硬编码AKSK
ak = "你的ak"
sk = "你的sk"
server = "https://obs.cn-hz1.ctyun.cn"

# 创建obsClient实例
obsClient = ObsClient(access_key_id=ak, secret_access_key=sk, server=server)

# 用来控制下载进度打印频率
last_print_time = 0

# 获取下载对象的进度
def callback(transferredAmount, totalAmount, totalSeconds):
    global last_print_time
    try:
        current_time = time.time()

        # 每1秒打印一次下载进度
        if current_time - last_print_time >= 10:
            download_speed = transferredAmount * 1.0 / totalSeconds / 1024  # KB/s
            download_percentage = transferredAmount * 100.0 / totalAmount  # %
            print(f"下载速度: {download_speed:.2f} KB/s, 下载进度: {download_percentage:.2f}%")
            last_print_time = current_time
    except Exception as e:
        print(f"进度回调出错: {e}")

# 递归下载文件
def download_files(bucketName, prefix, localDownloadPath):
    try:
        # 获取目录下的对象列表
        resp = obsClient.listObjects(bucketName=bucketName, prefix=prefix, delimiter='/')

        # 检查是否成功返回文件或子目录
        if resp.status < 300:
            if resp.body.contents:
                for content in resp.body.contents:
                    file_key = content['key']
                    file_name = os.path.basename(file_key)

                    # 如果是文件夹，则跳过
                    if file_name == '':
                        print(f"跳过文件夹: {file_key}")
                        continue

                    print(f"准备下载文件: {file_key}")  # 打印出文件名
                    local_file_path = os.path.join(localDownloadPath, file_key.replace('/', '\\'))
                    local_dir = os.path.dirname(local_file_path)

                    # 确保目录存在
                    if not os.path.exists(local_dir):
                        os.makedirs(local_dir)

                    # 单文件下载，不使用断点续传
                    print(f"开始下载文件: {file_key} 到 {local_file_path}")
                    resp = obsClient.getObject(bucketName, file_key, downloadPath=local_file_path, progressCallback=callback)

                    if resp.status < 300:
                        print(f"文件已下载到: {local_file_path}")
                    else:
                        print(f"文件下载失败: {file_key}")
                        print(f"错误信息: {resp.errorMessage}")

            # 如果有子目录，则递归处理子目录
            if resp.body.commonPrefixs:
                for commonPrefix in resp.body.commonPrefixs:
                    subdir_prefix = commonPrefix['prefix']
                    print(f"处理子目录: {subdir_prefix}")
                    download_files(bucketName, subdir_prefix, localDownloadPath)
        else:
            print('获取对象列表失败')
            print(f"错误信息: {resp.errorMessage}")

    except Exception as e:
        print('下载过程中发生错误')
        print('错误详情:', str(e))
        print(traceback.format_exc())
try:
    bucketName = ""  # 要操作的桶名称
    prefix = ""  # 要开始递归下载的父目录
    localDownloadPath = r"D:\pycharm\常用脚本整合\tianyiyun-obs"  # 本地下载路径

    print("开始下载...")
    download_files(bucketName, prefix, localDownloadPath)
    print("下载完成！")
except Exception as e:
    print('下载过程中发生错误')
    print('错误详情:', str(e))
    print(traceback.format_exc())
```
### linux脚本

```
[root@ecs-564e /bak]$ cat tianyiyun-obs.py 
from obs import ObsClient
import os
import time
import traceback

# 硬编码AKSK
ak = "你的ak"
sk = "你的sk"
server = "https://obs.cn-hz1.ctyun.cn"

# 创建obsClient实例
obsClient = ObsClient(access_key_id=ak, secret_access_key=sk, server=server)

# 用来控制下载进度打印频率
last_print_time = 0

# 获取下载对象的进度
def callback(transferredAmount, totalAmount, totalSeconds):
    global last_print_time
    try:
        current_time = time.time()

        # 每1秒打印一次下载进度
        if current_time - last_print_time >= 10:
            download_speed = transferredAmount * 1.0 / totalSeconds / 1024  # KB/s
            download_percentage = transferredAmount * 100.0 / totalAmount  # %
            print(f"下载速度: {download_speed:.2f} KB/s, 下载进度: {download_percentage:.2f}%")
            last_print_time = current_time
    except Exception as e:
        print(f"进度回调出错: {e}")

# 递归下载文件
def download_files(bucketName, prefix, localDownloadPath):
    try:
        # 获取目录下的对象列表
        resp = obsClient.listObjects(bucketName=bucketName, prefix=prefix, delimiter='/')

        # 检查是否成功返回文件或子目录
        if resp.status < 300:
            if resp.body.contents:
                for content in resp.body.contents:
                    file_key = content['key']
                    file_name = os.path.basename(file_key)

                    # 如果是文件夹，则跳过
                    if file_name == '':
                        print(f"跳过文件夹: {file_key}")
                        continue

                    print(f"准备下载文件: {file_key}")  # 打印出文件名
                    local_file_path = os.path.join(localDownloadPath, file_key.replace('/', os.sep))
                    local_dir = os.path.dirname(local_file_path)

                    # 确保目录存在
                    if not os.path.exists(local_dir):
                        os.makedirs(local_dir)

                    # 单文件下载，不使用断点续传
                    print(f"开始下载文件: {file_key} 到 {local_file_path}")
                    resp = obsClient.getObject(bucketName, file_key, downloadPath=local_file_path, progressCallback=callback)

                    if resp.status < 300:
                        print(f"文件已下载到: {local_file_path}")
                    else:
                        print(f"文件下载失败: {file_key}")
                        print(f"错误信息: {resp.errorMessage}")

            # 如果有子目录，则递归处理子目录
            if resp.body.commonPrefixs:
                for commonPrefix in resp.body.commonPrefixs:
                    subdir_prefix = commonPrefix['prefix']
                    print(f"处理子目录: {subdir_prefix}")
                    download_files(bucketName, subdir_prefix, localDownloadPath)
        else:
            print('获取对象列表失败')
            print(f"错误信息: {resp.errorMessage}")

    except Exception as e:
        print('下载过程中发生错误')
        print('错误详情:', str(e))
        print(traceback.format_exc())


try:
    bucketName = ""  # 要操作的桶名称
    prefix = ""  # 要开始递归下载的父目录
    localDownloadPath = "/bak"
    print("开始下载...")
    download_files(bucketName, prefix, localDownloadPath)
    print("下载完成！")
except Exception as e:
    print('下载过程中发生错误')
    print('错误详情:', str(e))
    print(traceback.format_exc())

```
