 家人们，最近我一直在寻找一个Jdk多版本管理方案，今天它来了👇👇！**SDKMAN**：一款支持Linux、Windows、MacOS的跨平台管理工具，它不仅支持Java版本管理，同时还支持Maven、Gradle等其他管理工具的版本管理。

**1.1 安装SDKMAN**



-  在Linux Terminal中执行以下命令：

- 

```
curl -s "https://get.sdkman.io" | bash
```

▼ 图一

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/GpX8WJopZeEriaYKw7NCypgMKY4puNgsUnfNPGEZjb1icMmRICiavicTjEB2lXANtXicQIkgWEXNIphOxl2oDEB6kfg/640?wx_fmt=png&from=appmsg&random=0.35036640497428273&random=0.34849539404852714&random=0.45860187372088124&random=0.9768616794499381&random=0.646795601982026&random=0.22357564165250032&random=0.9084306539934535&random=0.18328988149764802&random=0.7916680990845344)

- 新开Linux Terminal窗口或者在当前窗口执行命令：

- 

```
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

- 检测是否安装成功：

- 

```
sdk version
```

▼ 图二

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/GpX8WJopZeEriaYKw7NCypgMKY4puNgsUKuUpgSLawH8Kw5tia7JZtjKVOgVNW3ljd3fqL12rX9qiaHibHuHibk9K8g/640?wx_fmt=png&from=appmsg&random=0.2523222468937283&random=0.4553524764473329&random=0.24042787984168212&random=0.07496807942207262&random=0.9593989376836405&random=0.9853630884956643&random=0.5676103023689583&random=0.21349723529551956&random=0.5198921536226819)

- 卸载SDKMAN命令：

- 

```
sudo rm -rf ~/.sdkman
```

 

**1.2 常用命令**



-  **help:**

查看命令帮助文档，命令语法：

- 

```
sdk help [subcommand]
```

subcommand为可选参数，如果省略则查看sdk的帮助文档，如果指定则查看指定子命令的帮助文档。

- **install：**

安装软件，命令语法：

- 

```
sdk install <candidate> [version] [path]
```

candidate：需要安装的软件的名称；

version：要安装软件的版本号，可选参数；

path：要安装软件的路径，**一般用于将本地已安装的软件导入到sdkman中时使用**，可选参数； 

 示例代码：

- 

```
sdk install java 1.8 $JAVA_HOME
```

- 

```
sdk install java 21 $JDK21
```

▼ 图三

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/GpX8WJopZeEriaYKw7NCypgMKY4puNgsULmIW0otHDszbHKia0nC4CYP1ic1WngW1xmomfzOl0ia59cSZ9Mt2LXDMQ/640?wx_fmt=png&from=appmsg&random=0.08512425278655389&random=0.22657983473303278&random=0.5514780619728981&random=0.02889054950608605&random=0.40695636737940855&random=0.5922075324481997)

- **list：**

查看可用的软件版本，包括已经安装的，命令语法：

- 

```
sdk list [candidate]
```

示例代码：

- 

```
sdk list java
```

▼ 图四

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/GpX8WJopZeEriaYKw7NCypgMKY4puNgsUjs5FwAianFmAbbIicTicrYRfqsdykjPNaeF1Ktq0LrMG45FY3libuOPRLg/640?wx_fmt=png&from=appmsg&random=0.8546408248113733&random=0.22975229718467305&random=0.6431098979947687&random=0.07948929185400488&random=0.8965782798177595&random=0.10168707767556517) 

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/GpX8WJopZeEriaYKw7NCypgMKY4puNgsUIvMBibC8UjgWvROy3QVUtHA2LUJfR5GjZWmakJOMVUsIEicIlgAqIdeA/640?wx_fmt=png&from=appmsg&random=0.8597640827235216&random=0.34464328815725187&random=0.16596582217700062&random=0.7657261237883113&random=0.33151803598747764)

*图四只是展示了本地安装版本，如果要实现当前功能，可以先运行**离线模式**，命令为：**sdk offline enable | disable**；*

*
*

*命令一般**用于安装前获取支持的软件命令和版本，或者查看已安装的软件及版本**。
*

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/GpX8WJopZeEriaYKw7NCypgMKY4puNgsUnyZicWG2LXunyiaX4SibNy3MdWrXqFWzbF7OnMBEKXNBGAmkZM0Rh6enA/640?wx_fmt=png&from=appmsg&random=0.03787016099133811&random=0.3971919534786972&random=0.13216497001185923&random=0.18163765221715544&random=0.24617152746083248)

-  **use：**

  设置当前shell的使用的软件版本号，命令语法：

- 

```
sdk use <candidate> <version>
```

示例代码：

- 

```
sdk use java 1.8
```

- 

```
sdk use java 21
```

▼ 图五

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/GpX8WJopZeEriaYKw7NCypgMKY4puNgsUibwZEtopTa2We9AgpahGC0uxNuE7bKKjFIMAvTCw8orMucVRNlbbvQg/640?wx_fmt=png&from=appmsg&random=0.0080654433556695&random=0.5222949647028687&random=0.8143334220240925&random=0.8199862519168575&random=0.646952605933387) 

![img](https://mmbiz.qpic.cn/sz_mmbiz_png/GpX8WJopZeEriaYKw7NCypgMKY4puNgsUT9iayKicT4Q0GK1JSyp1S7mrsLIeibL5Ocgia2OhjC2ATyfe5icvvicAxKGg/640?wx_fmt=png&from=appmsg&random=0.8215474124515227&random=0.324318246296188&random=0.464226071378409&random=0.5646132277643405)

小结

通过图五我们可以到通过命令：**sdk use java <version>**设置版本后，当使用相关的java命令时其版本号已改变，我们也可以使用命令：**sdk current java**来检测当前所使用的软件版本号，**这里的java可以替换成你通过sdkman安装的任意软件名称**。

- **default：**

设置全局默认的软件版本号，命令语法：

- 

```
sdk default <candidate> [version]
```

- **uninstall：**

卸载指定版本的软件，命令语法：

- 

```
sdk uninstall <candidate> <version>
```