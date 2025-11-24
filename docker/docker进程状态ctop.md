# 类似htop的容器ctop
<!-- more -->
Github地址：
https://github.com/bcicen/ctopctop

可以像top一样提供单台主机下所有容器的指标信息或者单独一个容器的指标信息。


![20241129175008](https://liu-fu-gui.github.io/myimg/halo/20241129175008.png)

## 一、安装Linux平台：
需要github代理，去看我的

https://www.xiaoliu.xn--6qq986b3xl/post/33#%E4%BA%B2%E6%B5%8B
```
wget https://github.com/bcicen/ctop/releases/download/v0.7.3/ctop-0.7.3-linux-amd64 -O /usr/local/bin/ctop
chmod +x /usr/local/bin/ctop
```
或者docker直接运行：
```
docker run --rm -ti \
  --name=ctop \
  --volume /var/run/docker.sock:/var/run/docker.sock:ro \
  quay.io/vektorlab/ctop:latest
```
## 二、使用我们先查看下ctop有哪些参数

```
ctop -h
ctop - interactive container viewer
usage: ctop [options]
options:
  -a  show active containers only   #只查当前活动的容器  
  -connector string      
      container connector to use (default "docker")  
  -f string        # 搜索容器，比如包含kafka前缀的容器名      
      filter containers  
  -h  display this help dialog   # 帮助信息  
  -i  invert default colors   # 翻转当前终端颜色  
  -r  reverse container sort order # 排序，可以选择cpu，内存，状态等信息  
  -s string      
      select container sort field  # 反排序  
  -scale-cpu      
      show cpu as % of system total  # cpu百分比  
  -shell string                                         
      default shell  -v  output version information and exit
```
和使用top一样，我们可能会更喜欢在命令打开的情况下使用快捷键进行操作，ctop也提供了对应的按键设置。
这里我选择几个常用的截图示例让大家看看效果如何

回车键——显示容器的菜单，具体如下图
- o 是查看单独的一个容器信息
- l 是查看容器的日志
- s 是关闭容器
- p 暂停该容器
- r 重启该容器
- e 是进行容器内部，默认是sh模式


![20241129175044](https://liu-fu-gui.github.io/myimg/halo/20241129175044.png)
1、f 搜索容器名

![20241129175052](https://liu-fu-gui.github.io/myimg/halo/20241129175052.png)
2、o 单容器信息模式

![20241129175103](https://liu-fu-gui.github.io/myimg/halo/20241129175103.png)
3、s 选择容器排序的字段，排序字段包含cpu，io，mem，mem %，name，pids，net，state

![20241129175113](https://liu-fu-gui.github.io/myimg/halo/20241129175113.png)
比如我选择按mem %排序（PS:能够识别添加的内存limit限制）

![20241129175128](https://liu-fu-gui.github.io/myimg/halo/20241129175128.png)

4、l 查看容器的日志，如下图查看容器名 archery的日志

![20241129175138](https://liu-fu-gui.github.io/myimg/halo/20241129175138.png)
5、e 进入容器内部，默认是sh，若容器支持bash，可以切换到bash下

![20241129175146](https://liu-fu-gui.github.io/myimg/halo/20241129175146.png)

![20241129175154](https://liu-fu-gui.github.io/myimg/halo/20241129175154.png)