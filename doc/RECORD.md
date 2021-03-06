1.什么是Akka?
----------------
Akka是一款高性能，高容错性的分布式和并行应用框架，由Scala实现，它也是基于经典Actor并发模型的一个工具集。

2.特点
----------------
1.并行与并发
  提供对并行和并发的高度抽象
2.异步费阻塞
  Akka-Actor消息通信都是基于异步非阻塞
3.高容错性
4.持久化
  Actor携带的状态或消息可以被持久化，以便于在JVM崩溃后能恢复状态
5.轻量级
  每个Actor大约只占300byte，1G内存可容纳接近300W个Actor

3.场景
----------------
服务后端：比如rest web，websocket服务，分布式消息处理等。
并发和并行：比如日志异步处理，秘籍数据计算等

Akka的架构体系以及周边生态
ppt图1-4

1.Actor是什么鬼？
  1.1 Actor模型
      (1)Akka最基本的执行单元
	  (2)在分布式框架中是并行计算的最小单元(实例)，拥有自己状态和行为
	  (3)Actor之间的通信以消息为载体，提供"问答"式API
	  (4)异步
      (5)线程安全，Actor运行在线程之上，从底层屏蔽了线程和锁的管理，对于开发者不用考虑安全性问题。
	  (6)轻量级
  1.2 ActorSystem
      在一个应用中，所有的Actor共同构成了Actor系统，即ActorSystem。一个应用中只能有一个ActorSystem
	  ActorSystem是一个层级结构，如图2-1

	  如何构建父子关系？Actor被谁创建，谁就是父级
	  ```
	  ActorSystem创建会默认启动三个顶级的Actor
	  我们应用程序里面创建的Actor只是一部分
	  ```

	  /是整个ActorSystem的根
	  /user分支是我们在应用程序中通过ActorSystem.actorOf()方法创建的，这个也是我们能手动创建的最高级别的Actor
	  /system分支都是系统层面Actor(开发者无需关心)

  1.3 Actor生命周期
      图2-10

	  创建并启动(Start)
	      actorOf()方法创建，启动后会默认调用preStart方法(可以做一系列资源的初始化)
	  恢复(Resume)
	      当出现异常，通过容错机制让Actor恢复并继续运行，延用之前的实例，状态也会保留下来。
	  重启(Restart)
	      当出现异常，通过容错机制让Actor执行重启
		  调用旧实例的preRestart方法，默认停止所有子级Actor
		  创建新实例，调用新实例的postRestart方法
	  停止(Stop)
	      停止会调用postStop方法，同时会发送一条Terminated信息给自己的监控者，告知自己已经终结

   1.4 引用和路径
        Actor可能存在于本地或者远程，对开发者来说没有明显区分，仅仅需要操作应用即可
		Actor的引用是ActorRef，它就是Actor的一种代理

		图2-2

		Actor创建之后会有自己路径：
		akka://mysys/user/parentActor/childActor
		akka:tcp://mysys@127.0.0.1:2554/user/parentActor/childActor
		通过这个路径可以方便的定位一个Actor


2 Actor编程
  1.定义Actor
        继承UntypedActor
  2.创建Actor示例
       注意：禁止new对象,Actor作为核心执行的单元，具有并行运行和分布式的特点，为了屏蔽实现细节，简化调用方式，统一资源调配和层级管理，需要通过Akka提供的API来创建Actor
       【示例】：UntypedActorCreator.java
  3.发送接受消息
        tell：发送消息立即结束
		ask：发送消息异步获得响应  示例：ActorInterlocution.java
		forward：消息转发        示例：ActorForwardMessage.java
  4.查找Actor
        图2-9
        示例：ActorFind.java
  5.停止Actor
        当我们不在需要某个Actor时，可以采取下面的方式进行停止：
        （1）调用ActorSystem或者getContext()的stop方法。
        （2）给Actor发送一个PoisonPill（毒丸）消息。
        （3）给Actor发送一个Kill消息，此时会抛出ActorKilledException异常，并上报到父级supervise处理。
        示例：ActorStop

        靠谱的流程：
        (1)当停止Actor时，正在处理的消息会在完全停止之前处理完成，后续信息将不再进行处理，邮箱也会被挂起
        (2)给所有子级Actor发送终止命令，当子级都停止后，在停自己，停止会调用postStop方法，在这里可以清理或释放资源
        (3)向生命周期监控着(DeathWatch)发送Terminated消息，以便监控者做响应的处理

        注意：为了能让子Actor失败之后能响应给自己的父级，需要在父Actor中对自己的子级进行监控，只需要在父级的preStart方法中调用watch方法即可
             当子Actor失败时会自动向自己监控着发送Terminated消息。
        示例：ActorTerminate

3.MailBox
    提示疑问：Akka以消息为载体入参和SpringMVC中一个Controller中的Method接受入参有区别吗？

    在给一个Actor发送消息时，并不是依赖API之间的传参，而会先把消息发送到Actor的邮箱中，然后Actor会从自己的邮箱中读取这些消息。

    Actor中的邮箱本质是一个队列结构，所有到达Actor的消息都会在队列里面进行排队，默认的方式是FIFO，当然也可以自定义邮箱和消息队列的处理方式。

    3.1 消息的顺序性
        （1）当一个Actor接受来自不同Actor的消息，Actor不能保证某个消息一定排在另一个的前面或者后面。
        （2）从同一个Actor发送过来的多个消息，可以保证是串行的顺序。
    3.2 邮箱默认配置
        akka.actor.default-mailbox {
            mailbox-type = "akka.dispatch.UnboundeMailbox"
            mailbox-capacity = 1000
            mailbox-push-timeout-time = 10s
        }

        参数说明：
        [mailbox-type] 邮箱类型，分为有界和无界，Akka默认为UnboundMailbox，不限制邮箱大小
        [mailbox-capacity] 邮箱容量，只对于有界邮箱有效
        [mailbox-push-timeout-time] 如队列超时时间，指push一个消息到有界邮箱的队列超时时间。配置为复负数表示无线超时

     3.3 内置邮箱
         表4-1

         例子：（1）自定义优先级
                   继承PriorityMailbox重写优先级规则
                   示例：MailboxPriorityCustom

               (2) 控制指令优先，如何让消息具有更高的优先级？
                   ①配置邮箱类型为：UnboundedControlAwareMailbox
                   ②将消息实现ControlMessage接口
                   示例：MailboxControlWare

     3.4 自定义邮箱类型
         步骤：1.自定义邮箱队列（实现MessageQueue接口）
              2.自定义邮箱类型（实现MailboxType接口），并指定邮箱队列
              3.配置邮箱类型，绑定到Actor中
         示例：MailboxTypeCustom

4.Router
   tell ask forward只能实现简单的消息投机逻辑，但是如果想实现复杂的消息投机逻辑怎么办呢？

   Router：路由器，消息会进入路由器中然后在发送出去，相当于消息的中转站。
   Routee：路由目标，从路由器中发送出去的消息会到达对应的路由目标
   投递策略：轮询，随机，广播

   图例5-1

   示例：实现轮询投递

   4.1 路由Actor
   路由器可以是一个自包含的Actor，它通常管理者自己所有的Routee。
   一般来说，我们会把这类路由配置在*.conf文件中，然后通过编码的方式加载并创建路由器。

   创建路由Actor的两种方式：
   （1）Pool
       路由器Actor会创建子Actor作为其Routee并对它们进行监督和监控，当某个Routee终止时将其移除出去。

       示例：RoutingPool

   （2）Group
       可以将Routee的产生方式放在外部，然后路由器Actor通过路径path对这些路由目标发送消息。

       示例：RoutingGroup

   4.2 常见路由类型：表5-1

5.Akka Stream
  Akka Stream是建立在Actor模型之上的，它可以将所有的处理过程抽象成异步+并行执行的函数，数据将会在函数内流动并得到处理。

  1.Stream组件
    （1）Source:产生一个输出，它的下游在处理时会接受它的数据。
    （2）Sink：需要一个输入，它通常是流动处理的左后一个阶段。
    （3）Flow：拥有一个输入和输出，它包含一些类似集合的操作，可以做数据转换加载等操作。
    （4）RunnableGraph：当你拥有Source和Sink等结构后，还需要将它们链接在一起形成一个pipeline，需要这样一个对象执行计算图。

     一个简单的例子：StreamExample

  2.构建Source
    例子：StreamSourceExample
  3.构建Sink
    例子：StreamSinkExample
  3.构建Flow
    图6-2
    例子：StreamFlowExample

6.Akka HTTP
  1.相关组件
    (1)HttpRequest:
       表示一个HTTP请求，可以通过HttpRequest.create静态方法创建
    (2)HttpResponse:
       表示一个HTTP响应，可以通过HttpResponse.create静态方法创建
    (3)HttpEntity:
       表示请求和响应中携带的数据，比如：HttpEntity.Strict(简单类型，一般在已知Content-Length,并且数据量不大的时候使用)，
       HttpEntity.Chunked(使用分块的方式进行数据传输，它会将header中的Transfer-Encoding设置为chunked)等
    (4)Header:
       当Akka HTTP服务器接受到请求后，会解析所有的header，例如Conten-Length，Content-Type，User-Agent。

  2.HTTP服务端
    Akka HTTP是建立在Stream基础之上，很多操作都被抽象成Stream中的组件的数据流操作。
    基本思路：启动Akka HTTP服务之后，他会从Source中读取链接对象并交给Sink进行处理，得到链接对象之后
             将其分配给Flow中去进行HttpRequest和HttpResponse相关操作。
    例子：HttpServer

  3.请求和响应
    Sink.foreach里面得到了每个来访的IncomingConnection对象，该对象拥有一系列方法可以用于处理请求。
    比较重要两个是下面两个：
    (1)handleWith:需要一个Flow<HttpRequest,HttpResponse,Mat>组件来定义处理逻辑
    (2)handleWithSyncHandler:需要一个Flow<HttpRequest,HttpResponse>组件来定义处理逻辑
    例子：HttpRequestHandle

  4.HTTP客户端
    Akka HTTP也提供了API用于向服务端发送请求，相当于http-client。
    例子：HttpClient

  5.Routing DSL
    两个核心组件：
    (1)Route:用于匹配和处理请求的函数，它会返回一个RouteResult用来描述可能的结果
    (2)Directive:用于创建和组装多个Route。
    例子：RoutingDSLExample

  6.Directive
    (1)HeaderDirectives
       HeaderDirectives.headerValueByName可以通过名字获取header信息，比如浏览器相关信息：
       path("category", () -> headerValueByName("User-Agent", u -> comlete("获取header" + u)))
    (2)PathDirectives
       PathDirectives.pathSingleSlash可以匹配首页路径，即"/"
       pathSingleSlas(() -> complete("默认首页"))

       PathDirectives.path 可以匹配指定路径，比如"/index"
       path("index", () -> complete("欢迎来到首页"))

    (3)ParameterDirectives
       ParameterDirectives.parameter可以获取查询字符串中的参数，第一个参数（name）表示字符串参数名
       path("searchBook", () -> parameter("name", name -> complete("查询书名:" + name)))

       ParameterDirectives.parameterMap以Map的格式获取所有查询字符串参数
       path("searchBookBy", () -> parameterMap(p -> {
           return omplete("书名:" + p.get("name") + ", 价格:" + p.get("price"));
       }))

       测试URL：searchBookByname=akka&price=100

    (4)FormFieldDirectives
       FormFieldDirectives.formFieldMap会以map格式获取所有method为post的form表单值
       path("addBook", () -> formFieldMap(form -> {
           return complete("提交参数："+form);
       }))

    (5)RouteDirectives
       RouteDirectives.redirect可以产生一个重定向响应
       path("redirect_to", () -> redirect(Uri.create("/index"), StatusCodes.FOUND))





