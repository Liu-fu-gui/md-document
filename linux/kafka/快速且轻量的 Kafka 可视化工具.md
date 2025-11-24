Kafka 以其高吞吐量和低延迟的数据传输能力而闻名，作为一个分布式流处理平台，广泛应用于大数据、实时流处理等领域。许多大型企业和开源项目都建立在Kafka之上，在互联网公司、金融行业、物联网（IoT）等多个领域都有广泛应用，比如：



- Kafka 在美团的数据平台中，集群规模已经超过 15000 台机器，单集群的最大机器数达到 2000 台。在数据规模上，天级消息量超过 30P，天级消息量峰值达到 4 亿/秒。（数据来源：https://tech.meituan.com/2022/08/04/the-practice-of-kafka-in-the-meituan-data-platform.html）



- 某金融公司的行情计算、交易回报、量化分析等核心系统采用Kafka服务，日均消息吞吐量达到 2.3TB，峰值流量超过 4.8Gb/s，TOPIC 数量超过 190 个，服务于 30 个以上的应用系统。



- 在IOT场景中，通过Kafka能够实时收集和处理来自生产线的大量数据，实现设备的智能监控和故障预测，显著提高了生产效率和设备利用率。

但是，就因为Kafka可通过集群的方式进行大规模的拓展，部署Kafka集群需要处理许多配置参数，对于很多运维和开发同学在管理的时候都会比较头疼。而今天介绍的就是一个快速且轻量的Kafka可视化工具Kafka-UI



**01** 

**—** 

 **Kafka-UI** **介绍** 



# **一个多功能、快速且轻量级的开源Web UI工具，专为管理和监控Apache Kafka®集群而设计，****提供了一个直观的界面，使得用户能够轻松地执行****Kafka****日常运维工作，如主题管理、消费者组查看和生产者测试等。并且支持****跟踪Kafka集群的关键指标，包括Broker、Topics、Partitions、生产和消费情况。****此外，还支持多集群管理、性能监控、访问控制等功能。**

![img](https://mmbiz.qpic.cn/mmbiz_png/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqad4u3EXLt2XjziaicPTiaxJSB7r7zicQ1qNB9dmF9Ij5JI8jSfh58piaTsDA/640?wx_fmt=png&from=appmsg)

***\*🏠 项目信息\****

- 
- 
- 
- 

```
#github地址https://github.com/provectus/kafka-ui#产品详细文档地址https://docs.kafka-ui.provectus.io/
```

![img](https://mmbiz.qpic.cn/mmbiz_png/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqamUKTlz3cia3CRjmfy0t4xXiamjFEr2NVFIWia76vq0iau0iacqf945cn8lg/640?wx_fmt=png&from=appmsg)

## 🚀**功能特性**

- **多集群管理：**能够在一个界面上监控和管理所有的Kafka集群
- **性能监控与指标仪表板：**通过轻量级的仪表板跟踪关键的Kafka指标
- **查看Kafka代理：**查看主题和分区分配、控制器状态
- **查看Kafka主题：**查看分区计数、复制状态和自定义配置
- **查看消费者组：**查看每个分区的停放偏移量、组合滞后和每个分区滞后
- **浏览消息：**使用JSON、纯文本和Avro编码浏览消息
- **动态主题配置：**使用动态配置创建和配置新主题
- **可配置的身份验证：**使用可选的Github/Gitlab/Google OAuth 2.0登录认证
- **自定义序列化/反序列化插件：**使用现成的SerDe（如AWS Glue或Smile），或者自行编写代码
- **基于角色的访问控制：**精确管理访问UI的权限
- **数据脱敏：**混淆主题消息中的敏感数据

![img](https://mmbiz.qpic.cn/mmbiz_gif/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqaTe5dpPalJ7zUEaj40uZk4aIrCtG5gtAcowa8g5w9L5ibxJYgAsWtlSQ/640?wx_fmt=gif&from=appmsg)

**
**

**02**

**—**

 ***\*Kafka-UI\** 安装** 

- **Docker 快速部署**

- 

```
docker run -it -p 8080:8080 -e DYNAMIC_CONFIG_ENABLED=true provectuslabs/kafka-ui
```

- **Docker-compose 部署**

- 
- 
- 
- 
- 
- 
- 
- 
- 
- 

```
services:  kafka-ui:    container_name: kafka-ui    image: provectuslabs/kafka-ui:latest    ports:      - 8080:8080    environment:      DYNAMIC_CONFIG_ENABLED: true    volumes:      - ~/kui/config.yml:/etc/kafkaui/dynamic_config.yaml
```

- **K8S 部署**

- 
- 

```
helm repo add kafka-ui https://provectus.github.io/kafka-ui-chartshelm install kafka-ui kafka-ui/kafka-ui
```

部署完成后，通过http://hostip:8080访问界面

![img](https://mmbiz.qpic.cn/mmbiz_png/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqabu40ibWSypKFyjpdPfibnmCufCjKeDiaITg2FAdmpm1NCunOKnGAxRaWw/640?wx_fmt=png&from=appmsg)

**03**

**—**

 ***\*Kafka-UI\** 使用** 

- **添加Kafak集群**

![img](https://mmbiz.qpic.cn/mmbiz_png/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqa0dy8AuB0ltibOCFKCfnfDsWic4cJIf1GicDf3rl1cMaw6YoBrx8OKET1Q/640?wx_fmt=png&from=appmsg)

- **主题管理**

通过在浏览器中进行几次点击、粘贴自己的参数，以及在列表中查看主题，从而轻松地创建主题。

![img](https://mmbiz.qpic.cn/mmbiz_gif/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqag3CKnhGiadArTFMiaicex5Ot0sN5QB7cYPR6jic7nz84KiaZ19v8tDZ0dfg/640?wx_fmt=gif&from=appmsg)



可以方便地在连接器视图和相应的主题之间跳转，以及从主题跳转到消费者，便捷的导航。此外，还可以查看和设置连接器和主题的概览设置。

![img](https://mmbiz.qpic.cn/mmbiz_gif/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqaAkxVIsLds8FdjnBLgvNKW5dxkG33wurPOnI22Dj8KelNjVzGvic0I9A/640?wx_fmt=gif&from=appmsg)



- **消息管理**

可以轻松地将数据或消息发送到Kafka主题，只需指定相关参数，并且可以在列表中查看这些消息。

![img](https://mmbiz.qpic.cn/mmbiz_gif/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqahOx8zT8iaAx3NTZVp9ibso0gswTtkhRicluMrDeGuQGpjrXMHguyNfHGw/640?wx_fmt=gif&from=appmsg)



![img](https://mmbiz.qpic.cn/mmbiz_png/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqaLEEn3C6dibz1biaib55ToCVHVAYM8icHic6dfic7kck75iahvF44qsmzP0YhA/640?wx_fmt=png&from=appmsg)

**
**

- **Schema registry**

支持三种模式类型：Avro®、JSON Schema和Protobuf模式。

![img](https://mmbiz.qpic.cn/mmbiz_gif/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqaQaiaDs1FHymdWCGKfDlkPpWP5Ggk3MOumkQuVJ1ICjrfEX7lXZA5ucg/640?wx_fmt=gif&from=appmsg)

在生成Avro或Protobuf编码的消息之前，必须在Schema Registry中为该主题添加一个模式。现在，只需几次点击就能轻松完成所有这些步骤。

![img](https://mmbiz.qpic.cn/mmbiz_gif/kgXibFxsv0e1CL7G6x7fKGqkRE2ZPoicqaBVwvP9ZHAsZSGIsy264Hf36TSic8Ej7O8SvnLjEDCnKibJENEITmXWaA/640?wx_fmt=gif&from=appmsg)



**04**

**—**

 **最后** 

综上所述，如果您正在寻找一个高效、直观且完全免费的开源工具来管理 Kafka集群，那么Kafka-UI绝对是不二之选。它提供了一个轻量级的Web界面，能够轻松地监控和操作Kafka的各个方面，包括多集群管理、性能监控、主题和分区配置、消费者组状态以及消息浏览等，绝对能够帮助简化日常运维工作，提高 Kafak 的管理效率。