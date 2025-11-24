```
# Linux 系统资源动态调整脚本提示词
## 概述
此脚本用于动态调整 Linux 系统的 CPU、内存和磁盘利用率，确保资源使用维持在设定的达标范围内。脚本会定期检测系统资源，并根据检测结果自动调整资源使用情况，同时输出详细的日志记录。

## 功能需求
1. 资源检测 ：检测当前系统的 CPU、内存和磁盘利用率。
2. 达标条件判断 ：
   - 达标条件 1：（15% ≤ CPU 平均利用率 ≤ 30% 或 35% ≤ 内存平均利用率 ≤ 70%）且 40% ≤ 存储平均利用率 ≤ 80%。
   - 达标条件 2：若 CPU/内存一个指标达标另一个不达标，则不达标指标需满足：
     - CPU：峰值利用率 ≥ 30% 或当前内存规格下已是最小 CPU 规格。
     - 内存：峰值利用率 ≥ 70% 或当前 CPU 规格下已是最小内存规格。
3. 资源动态调整 ：
   - 如果 CPU 利用率低于 15%，自动增加系统负载以提高 CPU 使用率。
   - 如果 CPU 利用率高于 30%，适当减少系统负载。
   - 如果内存利用率低于 35%，增加内存使用量。
   - 如果内存利用率高于 70%，释放部分内存。
   - 如果存储利用率不在 40% - 80% 范围内，相应调整使用量。
4. 调整方法 ：
   - 通过生成适量的系统负载来提高 CPU 使用率。
   - 通过分配/释放内存来调整内存使用率。
   - 通过创建/删除临时文件来调整磁盘使用率。
5. 日志记录 ：每次检测和调整后，输出清晰的日志，记录当前资源使用情况和采取的调整措施。
6. 自动化执行 ：脚本应定期循环执行以持续维持资源在达标范围内。
```

```
yum install python3-pip python3 python3-devel  -y
pip3 install psutil -i https://mirrors.aliyun.com/pypi/simple/
```



```
# python3 resource_adjustment.py

import psutil
import time
import logging
import tempfile
import os
import multiprocessing  

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('resource_adjustment.log'),
        logging.StreamHandler()
    ]
)

# 全局变量，用于存储临时文件路径
temp_files = []
# 全局变量，用于存储占用内存的对象
memory_objects = []

def get_cpu_usage():
    return psutil.cpu_percent(interval=1)

def get_memory_usage():
    memory = psutil.virtual_memory()
    return memory.percent

def get_disk_usage():
    # 修改为Linux磁盘路径
    disk = psutil.disk_usage('/')
    return disk.percent

def increase_cpu_load():
    # 生成适量的系统负载以提高CPU使用率
    import multiprocessing
    import math
    def stress_cpu():
        # 执行一些计算任务，而不是死循环
        for _ in range(1000000):
            math.sqrt(256)
    processes = []
    # 可以根据需要调整进程数量
    num_processes = multiprocessing.cpu_count() // 2
    for _ in range(num_processes):
        p = multiprocessing.Process(target=stress_cpu)
        p.start()
        processes.append(p)
    for p in processes:
        p.join()
    logging.info("已增加CPU负载")

def decrease_cpu_load():
    # 适当减少系统负载，这里简单记录日志
    logging.info("已减少CPU负载")

def increase_memory_usage():
    # 分配内存以提高内存使用率
    global memory_objects
    try:
        # 创建一个大的列表来占用内存
        large_list = [0] * (100 * 1024 * 1024 // 8)  # 占用约100MB内存
        memory_objects.append(large_list)
        logging.info("已增加内存使用量")
    except Exception as e:
        logging.error(f"增加内存使用量时出错: {e}")

def release_memory():
    # 释放部分内存
    global memory_objects
    if memory_objects:
        memory_objects.pop()
        import gc
        gc.collect()  # 强制垃圾回收
        logging.info("已释放部分内存")
    else:
        logging.info("没有可释放的内存")

def increase_disk_usage():
    # 创建临时文件以提高磁盘使用率
    try:
        temp_file = tempfile.NamedTemporaryFile(delete=False)
        temp_files.append(temp_file.name)
        # 写入100MB数据
        temp_file.write(b'0' * 100 * 1024 * 1024)
        temp_file.close()
        logging.info("已增加磁盘使用量")
    except Exception as e:
        logging.error(f"增加磁盘使用量时出错: {e}")

def decrease_disk_usage():
    # 删除临时文件以降低磁盘使用率
    global temp_files
    if temp_files:
        file_to_delete = temp_files.pop()
        try:
            os.remove(file_to_delete)
            logging.info("已减少磁盘使用量")
        except Exception as e:
            logging.error(f"减少磁盘使用量时出错: {e}")
    else:
        logging.info("没有可删除的临时文件")

def check_resource_usage():
    cpu_usage = get_cpu_usage()
    memory_usage = get_memory_usage()
    disk_usage = get_disk_usage()

    # 达标条件判断
    condition1 = ((15 <= cpu_usage <= 30 or 35 <= memory_usage <= 70) and 40 <= disk_usage <= 80)

    if not condition1:
        logging.info(f"当前CPU利用率: {cpu_usage}%")
        logging.info(f"当前内存利用率: {memory_usage}%")
        logging.info(f"当前磁盘利用率: {disk_usage}%")
    else:
        logging.debug(f"资源使用满足要求，当前CPU利用率: {cpu_usage}%，内存利用率: {memory_usage}%，磁盘利用率: {disk_usage}%")

    # 更激进的CPU调整逻辑
    if cpu_usage < 15:
        # 接近下限，增加更多进程
        num_processes = multiprocessing.cpu_count()
        logging.info(f"即将通过 {num_processes} 个进程增加CPU负载")
        increase_cpu_load()
    elif cpu_usage > 30 and not condition1:
        decrease_cpu_load()
    elif 15 <= cpu_usage < 20:  # 接近下限，适当增加负载
        num_processes = multiprocessing.cpu_count() * 3 // 4
        logging.info(f"即将通过 {num_processes} 个进程增加CPU负载")
        increase_cpu_load()
    elif 25 < cpu_usage <= 30:  # 接近上限，适当减少负载
        decrease_cpu_load()

    # 更激进的内存调整逻辑
    if memory_usage < 35:
        # 接近下限，增加更多内存
        memory_size = 200  # 单位：MB
        logging.info(f"即将增加 {memory_size}MB 内存使用量")
        increase_memory_usage()
    elif memory_usage > 70 and not condition1:
        release_memory()
    elif 35 <= memory_usage < 40:  # 接近下限，适当增加内存
        memory_size = 150  # 单位：MB
        logging.info(f"即将增加 {memory_size}MB 内存使用量")
        increase_memory_usage()
    elif 65 < memory_usage <= 70:  # 接近上限，适当释放内存
        release_memory()

    # 更激进的磁盘调整逻辑
    if disk_usage < 40:
        # 接近下限，创建更大的临时文件
        # 可以修改写入的数据量，这里翻倍
        try:
            temp_file = tempfile.NamedTemporaryFile(delete=False)
            temp_files.append(temp_file.name)
            temp_file.write(b'0' * 200 * 1024 * 1024)  # 写入200MB数据
            temp_file.close()
            logging.info("已增加磁盘使用量")
        except Exception as e:
            logging.error(f"增加磁盘使用量时出错: {e}")
    elif disk_usage > 80:
        decrease_disk_usage()
    elif 40 <= disk_usage < 45:  # 接近下限，适当增加磁盘使用量
        increase_disk_usage()
    elif 75 < disk_usage <= 80:  # 接近上限，适当减少磁盘使用量
        decrease_disk_usage()

if __name__ == "__main__":
    while True:
        check_resource_usage()
        time.sleep(5)  # 每5s执行一次
```

```
echo "[Unit]
Description=Resource Adjustment Service
After=network.target

[Service]
ExecStart=/usr/bin/python3 /opt/resource_adjustment.py
WorkingDirectory=/opt
Restart=always
User=root

[Install]
WantedBy=multi-user.target" | sudo tee /etc/systemd/system/resource_adjustment.service


systemctl daemon-reload
systemctl start resource_adjustment.service
systemctl status resource_adjustment.service
```

