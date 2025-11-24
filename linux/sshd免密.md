<!-- more -->

# 1. 生成SSH密钥对
首先，在本地生成SSH密钥对（如果还没有的话）：

```
ssh-keygen -t rsa -b 4096 -C "your_email@example.com"
```

# 2. 准备服务器列表和密码
创建一个文件servers.txt

```
vim servers.txt
```
内容如下：
```
10.145.114.1 password1
10.145.114.2 password2
10.145.114.3 password3
...
```
# 3. 编写Shell脚本进行分发

```
vim distribute_keys.sh
```


```
#!/bin/bash

# 本地公钥文件
PUBLIC_KEY_FILE=~/.ssh/id_rsa.pub

# 检查公钥文件是否存在
if [ ! -f "$PUBLIC_KEY_FILE" ]; then
    echo "公钥文件不存在，请先生成SSH密钥对"
    exit 1
fi

# 读取服务器列表
while IFS=' ' read -r HOST PASSWORD; do
    echo "正在处理 $HOST"

    # 使用expect自动输入密码
    /usr/bin/expect << EOF
set timeout 10
spawn ssh $HOST "mkdir -p ~/.ssh && chmod 700 ~/.ssh"
expect {
    "*yes/no*" { send "yes\r"; exp_continue }
    "*assword:*" { send "$PASSWORD\r" }
    timeout { exit 1 }
    eof { exit 2 }
}
expect {
    "*assword:*" { exit 1 }
    timeout { exit 1 }
    eof { exit 0 }
}

spawn ssh $HOST "echo '$(cat $PUBLIC_KEY_FILE)' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"
expect {
    "*assword:*" { send "$PASSWORD\r" }
    timeout { exit 1 }
    eof { exit 2 }
}
expect {
    "*assword:*" { exit 1 }
    timeout { exit 1 }
    eof { exit 0 }
}
EOF

    if [ $? -eq 0 ]; then
        echo "成功分发密钥到 $HOST"
    else
        echo "分发密钥到 $HOST 失败"
    fi
done < servers.txt

```

# 执行sh脚本

```
sh distribute_keys.sh
```
