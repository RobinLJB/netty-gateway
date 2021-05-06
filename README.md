# netty-gateway
---

<p align="center">
  简体中文 
</p>

### 描述
基于netty的tcpip socket代理的Java应用，具有负载均衡功能。

### 初衷
作者查看了很多开源第三方负载均衡的tcpip连接的网关应用。
相同的功能nginx可以做到，用nodejs或者go代码上可能更加容易实现。

然而当查找基于java语言的tcpip连接网关应用却一直找不到，只有想spring-gateway或者zuul等的基于http连接的网关应用。

所以作者用netty实现了一个基于tcpip连接网关应用。

### 设计
首先程序通过建立netty服务监听服务器tcpip端口。用户访问服务器后，建立链接channel。服务端会维护住一份上有上游服务IP地址与端口。在用户建立连接后会随机往其中一个上游服务器发起连接，建立netty用户端。所有netty服务端收到的信息会通过netty用户端转发到真实的上有服务器。所有netty客户端收到的信息会通过netty服务端返回给用户。

### 细节
应用程序默认读取resources下个application.yml
 
  * server.port 代表服务端监听端口 （tcpip端口监听和unix socket文件监听只能二选一）
  * server.path 代表服务端unix socket文件 （tcpip端口监听和unix socket文件监听只能二选一）
  * proxy 代表上游服务器代理配置
  * proxy.serverXXX 代表具体某个上游服务器
  * proxy.serverXXX.host 代表具体某个上游服务器host
  * proxy.serverXXX.port 代表具体某个上游服务器所在服务port  （tcpip地址端口监听和unix socket文件监听只能二选一）
  * proxy.serverXXX.path 代表具体某个上游服务器服务端unix socket文件，此时默认上游动服务器就是本机 （tcpip端口监听和unix socket文件监听只能二选一）
  * proxy.serverXXX.weight 代表具体某个上游服务器所在服务接受服务的权重 默认是1 值越大 执行服务的概率越大

当应用启动后在启动目录下会生成.netty-gateway/application.yml文件
通过覆盖上述属性可以修改服务配置信息


