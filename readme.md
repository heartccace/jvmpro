<center><h1>jvm性能调优(基于java1.8)</h1></center>

### 1、JVM的运行参数（https://docs.oracle.com/javase/8/docs/index.html）

在jvm中有很多参数可以进行设置，这样可以让jvm在各种环境中能够高效的运行，绝大多数参数保持默认即可。

#### 1.1、三种参数类型

jvm的参数类型分为三类，分别是：

- 标准参数
  - -help
  - -version

- -X参数（非标准参数）
  - -Xint
  - -Xcomp

- -XX参数（使用率较高）
  - -XX:newSize
  - -XX:+UseSerialGC

#### 1.2、标准参数

jvm标准参数一般都是很稳定的，在未来的jvm版本中不会改变，可以使用java -help检索出所有的标准参数。

> C:\Users\admin>java -help
> 用法: java [-options] class [args...]
>            (执行类)
>    或  java [-options] -jar jarfile [args...]

> C:\Users\admin>java -version
> java version "1.8.0_221"
> Java(TM) SE Runtime Environment (build 1.8.0_221-b11)
> Java HotSpot(TM) 64-Bit Server VM (build 25.221-b11, mixed mode)
>
> #java -showversion 可以用于输出产品版本，并继续执行

##### 1.2.1、-server和-client参数

可以通过-server和-client设置jvm运行参数

- 他们的区别是Server vm的初始堆空间会大一些，默认使用的是并行垃圾回收，启动慢运行快
- Client vm相对来讲保守一些，初始堆空间会小一些，使用串行垃圾回收机制，它的目标是为了让jvm的启动速度快，但运行速度会比servervm模式慢。
- jvm在启动时会根据硬件和操作系统自动选择client类型还是server类型的jvm
- 32位操作系统
  - 如果是window系统，不论硬件如何配置，都默认使用client类型的jvm
  - 如果其他操作系统上，机器配置有2GB以上的内存同时有2个以上的CPU的话默认使用的server模式，否则使用client模式

- 64位操作系统
  - 只有server类型，不支持client类型

#### 1.3、-X参数

jvm的-X参数是非标准参数，在不同版本的jvm中，参数可能会有所不同，可以通过java -X参看非标准参数。

> C:\Users\admin>java -X
>     -Xmixed           混合模式执行 (默认)
>     -Xint             仅解释模式执行
>     -Xbootclasspath:<用 ; 分隔的目录和 zip/jar 文件>
>                       设置搜索路径以引导类和资源
>     -Xbootclasspath/a:<用 ; 分隔的目录和 zip/jar 文件>
>                       附加在引导类路径末尾
>     -Xbootclasspath/p:<用 ; 分隔的目录和 zip/jar 文件>
>                       置于引导类路径之前
>     -Xdiag            显示附加诊断消息
>     -Xnoclassgc       禁用类垃圾收集
>     -Xincgc           启用增量垃圾收集
>     -Xloggc:<file>    将 GC 状态记录在文件中 (带时间戳)
>     -Xbatch           禁用后台编译
>     -Xms<size>        设置初始 Java 堆大小
>     -Xmx<size>        设置最大 Java 堆大小
>     -Xss<size>        设置 Java 线程堆栈大小
>     -Xprof            输出 cpu 配置文件数据
>     -Xfuture          启用最严格的检查, 预期将来的默认值
>     -Xrs              减少 Java/VM 对操作系统信号的使用 (请参阅文档)
>     -Xcheck:jni       对 JNI 函数执行其他检查
>     -Xshare:off       不尝试使用共享类数据
>     -Xshare:auto      在可能的情况下使用共享类数据 (默认)
>     -Xshare:on        要求使用共享类数据, 否则将失败。
>     -XshowSettings    显示所有设置并继续
>     -XshowSettings:all
>                       显示所有设置并继续
>     -XshowSettings:vm 显示所有与 vm 相关的设置并继续
>     -XshowSettings:properties
>                       显示所有属性设置并继续
>     -XshowSettings:locale
>                       显示所有与区域设置相关的设置并继续
>
> -X 选项是非标准选项, 如有更改, 恕不另行通知。

​	1.3.1、-Xint、-Xcomp、-Xmixed

- -Xint表示解释模式(interpreted mode)下，-Xint标记会强制执行所有的字节码，当然会降低运行速率，通常低10倍或更多。
- -Xcomp与-Xint相反，jvm在第一次使用的时候会把所有的字节码编译成本地代码，从而带来最大程度的优化
  
- 然而，很多应用在-Xcomp也会有一些性能损失，当然这比-Xint损失少，原因是-Xcomp没有让jvm启用jit编译器的全部功能，jit编译器可以对是否需要编译做判断，如果所有代码都进行编译的话，对于一些只执行一次的代码就没意义。
  
- -Xmixed是混合模式，将解释模式和编译模式混合使用，由jvm自己决定，这是java虚拟机默认的模式，也是推荐使用的模式

  > // 采用-Xint方式（解释模式）
  >
  > PS C:\Users\admin\Desktop\新建文件夹> java -showversion -Xint TestJVM
  > java version "1.8.0_221"
  > Java(TM) SE Runtime Environment (build 1.8.0_221-b11)
  > Java HotSpot(TM) 64-Bit Server VM (build 25.221-b11, interpreted mode)

  > // 采用-Xcomp方式执行 (编译模式)
  >
  > PS C:\Users\admin\Desktop\新建文件夹> java -showversion -Xcomp TestJVM
  > java version "1.8.0_221"
  > Java(TM) SE Runtime Environment (build 1.8.0_221-b11)
  > Java HotSpot(TM) 64-Bit Server VM (build 25.221-b11, compiled mode)

#### 1.4、-XX参数

-XX参数也是非标准参数，主要用于jvm调优和debug操作。

-XX的使用方式有两种，一种是boolean类型，一种是非boolean类型

- boolean类型
  - 格式： -XX：[+-]<name>表示启用或者禁用name属性
  - 如：-XX：+DisableExplictGC表示禁用手动调用GC操作，也就是说System.gc()无效。

- 非boolean类型
  - 格式： -XX：<name>=<value> 表示name的属性值为value
  - 如：-XX：NewRatio=1表示新生代和老年代的比值

#### 1.5、-Xms与-Xmx参数

-Xms与-Xmx分别是设置jvm的堆内存的初始大小和最大大小。

-Xmx2048m： 等价于-XX：MaxHeapSize设置jvm最大堆内存为2048m。

-Xms512m：等价于-XX：InitialHeapSize，设置jvm初始堆内存为512m

适当的调整jvm的内存大小，可以充分利用服务器资源，让程序跑的更快。

> C:\Users\admin\Desktop\新建文件夹>java -Xms512m -Xmx2048m TestJVM
> hello world

> C:\Users\admin\Desktop\新建文件夹>java -XX:InitialHeapSize=512m -XX:MaxHeapSize=2048m TestJVM
> hello world

#### 1.6、查看jvm的运行参数

有些时候我们想查看jvm的运行参数，这种需求可能会存在两种情况：

第一：运行java命令时打印出运行参数

第二：查看正在运行的java进程参数

##### 1.6.1、运行java命令时打印参数

运行Java命令时打印参数，需要添加-XX:+PrintFlagsFinal参数即可

> C:\Users\admin\Desktop\新建文件夹> java -XX:+PrintFlagsFinal TestJVM
>
>   bool C1UpdateMethodData                        = true                                {C1 product}
>      intx CICompilerCount                          := 3                                   {product}
>
> #######    =表示默认值，:=表示值被修改 #######

##### 1.6.2、查看正在运行的java进程参数

使用jps或者jps -l查看java进程

> C:\Users\admin>jps -l
> 27312 sun.tools.jps.Jps
> 928 org.jetbrains.idea.maven.server.RemoteMavenServer36
> 32100 springmvc-1.0-SNAPSHOT-war-exec.jar
> 13676

查看所有的参数，用法： jinfo -flags <进程id>

> C:\Users\admin>jinfo -flags 32100
> Attaching to process ID 32100, please wait...
> Debugger attached successfully.
> Server compiler detected.
> JVM version is 25.221-b11
> Non-default VM flags: -XX:CICompilerCount=3 -XX:InitialHeapSize=134217728 -XX:MaxHeapSize=2139095040 -XX:MaxNewSize=713031680 -XX:MinHeapDeltaBytes=524288 -XX:NewSize=44564480 -XX:OldSize=89653248 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:-UseLargePagesIndividualAllocation -XX:+UseParallelGC
> Command line:

查看某个参数值，用法：jinfo -flag <参数名> <进程id>

> C:\Users\admin>jinfo -flag MaxHeapSize 32100
> -XX:MaxHeapSize=2139095040

### 2、jvm的内存模型

jvm的内存模型在1.7和1.8有较大的区别。

#### 2.1、jdk1.7的内存模型

- yuong年轻区（代）

  Yuong区被划分为三个部份，Eden区和两个严格相同的Survivor区，其中，Survivor区间中，某一时刻只有一个被使用，另外一个留作垃圾收集是复制对象用，在Eden区间变满时，GC就会将存活的对象一道空闲的Survivor区间中，根据jvm的策略，在经过几次垃圾收集后，任然存活的对象将从Survivor区中一道Tenured区间中。

- Tenured年老区

  Tenured区主要保存生命周期长的对象，一般是一些老的对象（或者大对象），当一些对象在yuong复制转移一定的次数以后，对象就会被转移到Tenured区，一般系统使用application级别的缓存，缓存中的对象往往会被转移带这一区间。

- Perm永久区

  Perm代主要保存class、method、filed对象，这部分的空间一般不会溢出，除非一次性加载很多的类不过在涉及到热部署的应用服务器的时候，有时候会遇到java.lang.OutOfMemoryError:PermGen space的错误，造成这个错误的很大原因就有可能每次都重新部署，但是重新部署后，类的class没有被卸载掉，这样就造成了大量class对象保存在perm中，这种情况一般可以重启应用服务器可以解决。

- Virtual区

  最大内存和初始内存的差值，就是Virtual区

#### 2.2、jdk1.8的堆内存模型

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/jvm8.png)

jdk1.8内存模型由两部分组成，年轻代 + 老年代

年轻代：Eden + 2 * Survivor

老年代： OldGen

在jdk1.8中变化最大的Perm代，用Metaspace(元数据空间)进行替换

需要特别说明的是：Metaspace所占用的内存空间不是虚拟内存，而是在本地内存空间中，这也是与1.7永久代最大的区别所在。

#### 2.3、为什么要废弃1.7中的永久区

配合JRockit Rockit ，因为JRockit 里面没有永久区。

> #http://openjdk.java.net/jeps/122
>
> This is part of the JRockit and Hotspot convergence effort. JRockit customers do not need to configure the permanent generation (since JRockit does not have a permanent generation) and are accustomed to not configuring the permanent generation.

#### 2.4、通过jstat命令进行查看堆内存使用情况

jstat命令可以查看堆内存各部分的使用量，以及加载类的数量。命令格式如下：

jstat [命令选项] [vmid] [间隔时间/毫秒] [查阅次数]

##### 2.4.1、查看class加载统计

> E:\apache-tomcat-9.0.22\bin>jstat -class 32100
> Loaded  Bytes  Unloaded  Bytes     Time
>   4505  9139.5        0     0.0       3.50

说明：

- loaded：加载class数量
- Byte：占用空间大小
- Unloaded：未加载class数量
- Bytes：未加载占用空间
- Time：加载时间

##### 2.4.2、查看编译统计

> Compiled Failed Invalid   Time   FailedType 	FailedMethod
>     2531      2       0    		16.26          1 			org/apache/catalina/loader/WebappClassLoader findResourceInternal

说明：

- Compiled：编译数量
- Failed：失败数量
- Invalid：不可用数量
- Time ：编译时间
- FailedType：失败类型
- FailedMethod：失败方法

##### 2.4.3、查看垃圾统计情况

> E:\apache-tomcat-9.0.22\bin>jstat -gc 32100
>  S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT
> 29696.0 25600.0  0.0    0.0   260096.0 49981.6   75264.0    28419.2   21296.0 20703.3 2432.0 2353.7      9    0.138   1      0.053    0.191

> ######  ######每隔一秒，总共打印5次
>
> E:\apache-tomcat-9.0.22\bin>jstat -gc 32100 1000 5
>  S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT
> 29696.0 25600.0  0.0    0.0   260096.0 49981.6   75264.0    28419.2   21296.0 20703.3 2432.0 2353.7      9    0.138   1      0.053    0.191
> 29696.0 25600.0  0.0    0.0   260096.0 49981.6   75264.0    28419.2   21296.0 20703.3 2432.0 2353.7      9    0.138   1      0.053    0.191
> 29696.0 25600.0  0.0    0.0   260096.0 49981.6   75264.0    28419.2   21296.0 20703.3 2432.0 2353.7      9    0.138   1      0.053    0.191
> 29696.0 25600.0  0.0    0.0   260096.0 49981.6   75264.0    28419.2   21296.0 20703.3 2432.0 2353.7      9    0.138   1      0.053    0.191
> 29696.0 25600.0  0.0    0.0   260096.0 49981.6   75264.0    28419.2   21296.0 20703.3 2432.0 2353.7      9    0.138   1      0.053    0.191

说明：

- S0C：第一个Survivor区的大小（KB）
- S1C：第二个Survivor区的大小（KB）
- S0U：第一个Survivor区的使用大小（KB）
- S1U：第二个Survivor区的使用大小（KB）
- EC：Eden区的大小（KB）
- EU：Enden区的使用大小（KB）
- OC：Old区的大小（KB）
- OU：Old区的使用大小（KB）
- MC：方法区的大小（KB）
- MU：方法区使用大小（KB）
- CCSC：压缩类空间大小（KB）
- CCSU：压缩类使用空间大小（KB）
- YGC：年轻代垃圾回收次数
- YGCT：年轻代垃圾回收消耗时间
- FGC：老年代回收次数
- FGCT：老年代回收次数消耗时间
- GCT：垃圾回收消耗总时间

### 3、jmap的使用以及内存溢出的分析

通过jstat可以对jvm堆内存进行分析，而jmap可以获得更加写详细的内容，如内存使用情况汇总，对内存定位与分析。

#### 3.1、查看内存使用情况

使用方法 jamp -heap <进程id>

详细使用请参考 jmap -help

> E:\apache-tomcat-9.0.22\bin>jmap -heap 32100
> Attaching to process ID 32100, please wait...
> Debugger attached successfully.
> Server compiler detected.
> JVM version is 25.221-b11
>
> using thread-local object allocation.
> Parallel GC with 4 thread(s)
>
> Heap Configuration:
>    MinHeapFreeRatio         = 0
>    MaxHeapFreeRatio         = 100
>    MaxHeapSize              = 2139095040 (2040.0MB)
>    NewSize                  = 44564480 (42.5MB)
>    MaxNewSize               = 713031680 (680.0MB)
>    OldSize                  = 89653248 (85.5MB)
>    NewRatio                 = 2
>    SurvivorRatio            = 8
>    MetaspaceSize            = 21807104 (20.796875MB)
>    CompressedClassSpaceSize = 1073741824 (1024.0MB)
>    MaxMetaspaceSize         = 17592186044415 MB
>    G1HeapRegionSize         = 0 (0.0MB)
>
> Heap Usage:
> PS Young Generation
> Eden Space:
>    capacity = 266338304 (254.0MB)
>    used     = 51181184 (48.8101806640625MB)
>    free     = 215157120 (205.1898193359375MB)
>    19.216606560654526% used
> From Space:
>    capacity = 26214400 (25.0MB)
>    used     = 0 (0.0MB)
>    free     = 26214400 (25.0MB)
>    0.0% used
> To Space:
>    capacity = 30408704 (29.0MB)
>    used     = 0 (0.0MB)
>    free     = 30408704 (29.0MB)
>    0.0% used
> PS Old Generation
>    capacity = 77070336 (73.5MB)
>    used     = 29101304 (27.75316619873047MB)
>    free     = 47969032 (45.74683380126953MB)
>    37.759409794191114% used
>
> 14031 interned Strings occupying 1831104 bytes.

#### 3.2、查看内存中对象的数量及大小

> ```
> #查看活跃以及不活跃对象
> jmap -histo 32100 
> 
> #只查看活跃对象
> jmap -histo:live 32100
> E:\apache-tomcat-9.0.22\bin>jmap -histo 32100 | more
> 
>  num     #instances         #bytes  class name
> ----------------------------------------------
>    1:         30039       37356920  [B
>    2:        129204       17351640  [C
>    3:          9009       12427608  [I
>    4:         88660        2127840  java.lang.String
>    5:         17005        1398104  [Ljava.lang.Object;
>    6:          9246         742432  [S
>    7:          5016         572704  java.lang.Class
>    8:         15730         503360  java.util.HashMap$Node
>    9:          5067         445896  java.lang.reflect.Method
>   10:          2386         399520  [Ljava.util.HashMap$Node;
>   11:          9447         377880  java.lang.ref.Finalizer
>   12:          4699         263144  java.util.zip.ZipFile$ZipFileInputStream
>   13:          4188         234528  java.util.zip.ZipFile$ZipFileInflaterInputStream
>   14:          5659         226360  com.sun.org.apache.xerces.internal.dom.DeferredTextImpl
>   15:          3358         214912  java.net.URL
>   16:          8568         205632  java.lang.StringBuilder
>   17:          2830         203760  com.sun.org.apache.xerces.internal.dom.DeferredElementNSImpl
>   18:          6367         203744  java.util.concurrent.ConcurrentHashMap$Node
>   19:          9189         198464  [Ljava.lang.Class;
>   20:          2590         145040  jdk.internal.org.objectweb.asm.Item
>   21:          2729         144568  [Ljava.lang.String;
>   22:          2353         126800  [[C
>   23:          2386         114528  java.util.HashMap
>   24:          2743         109720  java.util.LinkedHashMap$Entry
>   25:          1020          97920  java.util.jar.JarFile$JarFileEntry
>   26:          2845          91040  java.util.concurrent.ConcurrentLinkedQueue$Itr
> -- More  --
> #对象说明
> B	byte
> C	char
> D	double
> F	float
> I	int
> Z	boolean
> [	数组：如[I表示int[]
> [L+类名 其他对象
> ```
>
> 

#### 3.3、将内存使用情况dump到文件中

有些时候我们需要将内存使用情况dump到文件中，jmap支持dump到文件的操作。

```
#用法 b代表二进制
C:\Users\admin>jmap -dump:format=b,file=dumpfile 32100
Dumping heap to C:\Users\admin\dumpfile ...
Heap dump file created
```

#### 3.4、通过jhat对dump文件进行分析

通过jmap将jvm内存dump到文件中，导出的是一个二进制文件，不方便查看，这时候可以借助jhat工具进行查看

```
#用法 端口可以不指定，默认未7000，启动成功后可以通过浏览器访问
jhat -port <port> <file>

C:\Users\admin\Desktop\java>jhat -port 9900 dump.dat
Reading from dump.dat...
Dump file created Sun Mar 15 15:48:04 CST 2020
Snapshot read, resolving...，启动成功后可以通过浏览器访问
Resolving 170626 objects...
Chasing references, expect 34 dots..................................
Eliminating duplicate references..................................
Snapshot resolved.
Started HTTP server on port 9900
Server is ready.
```

#### 3.5、使用mat对内存溢出进行分析

使用参数，当在内存溢出时自动dump出内存情况

```
#参数如下
-Xms8m -Xmx8m -XX:+HeapDumpOnOutoFMemoryError
```

### 4、jstack的使用

有些时候我们需要查看下jvm中的线程执行情况，比如，发现cpu的负载突然增高了、痴线死锁、死循环等，我们该如何分析？

由于程序时正常运行的，没有任何输出，从日志方面也看不出问题，所以就需要看下jvm的内部线程的执行情况，然后再进行分析查找出原因。

这个时候就需要借助jstack，jstack的作用时将正在运行的jvm线程情况进行快照，并且打印出来。

```
#用法
jstack <pid>

C:\Users\admin>jstack 74908
2020-03-17 13:56:10
Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.221-b11 mixed mode):

"http-bio-8080-AsyncTimeout" #15 daemon prio=5 os_prio=0 tid=0x0000000018bed000 nid=0xf2a8 waiting on condition [0x000000001f63f000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
        at java.lang.Thread.sleep(Native Method)
        at org.apache.tomcat.util.net.JIoEndpoint$AsyncTimeout.run(JIoEndpoint.java:148)
        at java.lang.Thread.run(Thread.java:748)

"http-bio-8080-Acceptor-0" #14 daemon prio=5 os_prio=0 tid=0x0000000018bec800 nid=0x7e4 runnable [0x000000001f53f000]
   java.lang.Thread.State: RUNNABLE
        at java.net.DualStackPlainSocketImpl.accept0(Native Method)
        at java.net.DualStackPlainSocketImpl.socketAccept(DualStackPlainSocketImpl.java:131)
        at java.net.AbstractPlainSocketImpl.accept(AbstractPlainSocketImpl.java:409)
        at java.net.PlainSocketImpl.accept(PlainSocketImpl.java:199)
        - locked <0x00000000811848d8> (a java.net.SocksSocketImpl)
        at java.net.ServerSocket.implAccept(ServerSocket.java:545)
        at java.net.ServerSocket.accept(ServerSocket.java:513)
        at org.apache.tomcat.util.net.DefaultServerSocketFactory.acceptSocket(DefaultServerSocketFactory.java:60)
        at org.apache.tomcat.util.net.JIoEndpoint$Acceptor.run(JIoEndpoint.java:216)
        at java.lang.Thread.run(Thread.java:748)

"ContainerBackgroundProcessor[StandardEngine[Tomcat]]" #13 daemon prio=5 os_prio=0 tid=0x0000000018beb800 nid=0x120a0 waiting on condition [0x000000001f43e000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
        at java.lang.Thread.sleep(Native Method)
        at org.apache.catalina.core.ContainerBase$ContainerBackgroundProcessor.run(ContainerBase.java:1508)
        at java.lang.Thread.run(Thread.java:748)

"Service Thread" #9 daemon prio=9 os_prio=0 tid=0x00000000188ec000 nid=0x7214 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C1 CompilerThread2" #8 daemon prio=9 os_prio=2 tid=0x000000001888f800 nid=0x12790 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread1" #7 daemon prio=9 os_prio=2 tid=0x000000001888c800 nid=0xf398 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"C2 CompilerThread0" #6 daemon prio=9 os_prio=2 tid=0x000000001755c000 nid=0xee1c waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Attach Listener" #5 daemon prio=5 os_prio=2 tid=0x000000001755a000 nid=0xc3c8 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Signal Dispatcher" #4 daemon prio=9 os_prio=2 tid=0x000000001750f800 nid=0x52b0 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

"Finalizer" #3 daemon prio=8 os_prio=1 tid=0x000000000292b800 nid=0xc1b0 in Object.wait() [0x000000001887f000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x000000008110b700> (a java.lang.ref.ReferenceQueue$Lock)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:144)
        - locked <0x000000008110b700> (a java.lang.ref.ReferenceQueue$Lock)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:165)
        at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:216)

"Reference Handler" #2 daemon prio=10 os_prio=2 tid=0x00000000174ea000 nid=0x30fc in Object.wait() [0x000000001877f000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x00000000812e69e8> (a java.lang.ref.Reference$Lock)
        at java.lang.Object.wait(Object.java:502)
        at java.lang.ref.Reference.tryHandlePending(Reference.java:191)
        - locked <0x00000000812e69e8> (a java.lang.ref.Reference$Lock)
        at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:153)

"main" #1 prio=5 os_prio=0 tid=0x0000000002831000 nid=0x1131c in Object.wait() [0x000000000272e000]
   java.lang.Thread.State: WAITING (on object monitor)
        at java.lang.Object.wait(Native Method)
        - waiting on <0x00000000dd9d13b8> (a java.lang.Object)
        at java.lang.Object.wait(Object.java:502)
        at org.apache.tomcat.maven.runner.Tomcat7Runner.waitIndefinitely(Tomcat7Runner.java:482)
        - locked <0x00000000dd9d13b8> (a java.lang.Object)
        at org.apache.tomcat.maven.runner.Tomcat7Runner.run(Tomcat7Runner.java:410)
        at org.apache.tomcat.maven.runner.Tomcat7RunnerCli.main(Tomcat7RunnerCli.java:204)

"VM Thread" os_prio=2 tid=0x00000000174c7000 nid=0x110a8 runnable

"GC task thread#0 (ParallelGC)" os_prio=0 tid=0x0000000002846800 nid=0x11f2c runnable

"GC task thread#1 (ParallelGC)" os_prio=0 tid=0x0000000002848000 nid=0x9a58 runnable

"GC task thread#2 (ParallelGC)" os_prio=0 tid=0x000000000284a000 nid=0x127b0 runnable

"GC task thread#3 (ParallelGC)" os_prio=0 tid=0x000000000284b800 nid=0x124f4 runnable

"VM Periodic Task Thread" os_prio=2 tid=0x00000000188f0800 nid=0xb918 waiting on condition

JNI global references: 23
```

#### 4.1、线程的状态

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/线程状态.jpg)

在java中线程的状态一共分为6种：

- 初始态(NEW)
  - 创建一个Thread对象，但还未调用start()方法，线程处于初始态。

- 运行态(Runnable)，在java中运行态包括就绪态和运行态
  - 就绪态
    - 该状态下的线程以获取到执行所需的所有资源，只要CPU分配执行权就能执行
    - 所有就绪态的线程放在就绪队列中
  - 运行态
    - 获得CPU执行权正在执行线程
    - 由于一个CPU同一时刻只能执行一条线程，因此每隔CPU每隔时刻只有一条运行态的线程。

- 阻塞态(BLOCKED)
  - 当一条正在执行的线程请求某一资源失败时，就会进入阻塞态
  - 而在java中阻塞态专指请求锁失败时进入的状态
  - 由一个阻塞队列存放所有阻塞态的线程

- 等待状态(WAITING)
  - 当线程调用wait、join、park就会进入等待态
  - 也有一个等待队列存放所有等待态的线程
  - 线程处于等待态，表示他需要等待其他线程的指示才能继续运行
  - 进入等待态的线程会释放CPU的执行权，并释放资源（如：锁）

- 超时等待态(TIMED_WAITING)
  - 当运行中的线程调用sleep(time)、wait、join、parkNanos、parkUtil时，就会进入该状态
  - 它和等待态一样，并不是请求不到资源，而是主动进入，并且进入后需要其他线程唤醒
  - 进入该状态后释放CPU执行权和占有资源
  - 与等待态的区别：到了超时事件后会自动进入阻塞队列，开始竞争锁

- 终止态(TERMINATED)
  - 线程执行结束状态

#### 4.2、死锁实战

```
package com.jvm;

/**
 * @author liushuang
 * @create 2020-03-17 14:31
 */
public class DeathLock {
    private  static Object obj1 = new Object();
    private static Object obj2 = new Object();

    public static void main(String[] args) {
       new Thread(new Thread1()).start();
        new Thread(new Thread2()).start();
    }

    private static class Thread1 implements Runnable {

        public void run() {
            synchronized (obj1) {
                try {
                    System.out.println("will sleep 2 seconds");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (obj2) {
                    System.out.println("block 2");
                }
            }
        }
    }


    private static class Thread2 implements Runnable {

        public void run() {
            synchronized (obj2) {
                try {
                    System.out.println("will sleep 2 seconds");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (obj1) {
                    System.out.println("block 2");
                }
            }
        }
    }

}

```

```
#output
Found one Java-level deadlock:
=============================
"Thread-1":
  waiting to lock monitor 0x00000000034fac28 (object 0x00000000d5945db8, a java.lang.Object),
  which is held by "Thread-0"
"Thread-0":
  waiting to lock monitor 0x00000000034f8398 (object 0x00000000d5945dc8, a java.lang.Object),
  which is held by "Thread-1"

Java stack information for the threads listed above:
===================================================
"Thread-1":
        at com.jvm.DeathLock$Thread2.run(DeathLock.java:45)
        - waiting to lock <0x00000000d5945db8> (a java.lang.Object)
        - locked <0x00000000d5945dc8> (a java.lang.Object)
        at java.lang.Thread.run(Thread.java:748)
"Thread-0":
        at com.jvm.DeathLock$Thread1.run(DeathLock.java:27)
        - waiting to lock <0x00000000d5945dc8> (a java.lang.Object)
        - locked <0x00000000d5945db8> (a java.lang.Object)
        at java.lang.Thread.run(Thread.java:748)

Found 1 deadlock.

```

#### 4.3、VisualVM工具的使用

VisualVM能够监控线程，内存情况查看方法的CPU时间和内存中的对象，已被GC的对象，反向查看分配的堆栈(如100个String对象分别由那几个对象分配出来的)

VisualVM使用简单，几乎0配置，功能丰富，几乎囊括了其他JDK自带命令的所有功能。

- 内存信息
- 线程信息
- Dump堆(本地进程)
- Dump线程(本地进程)
- 打开堆Dump，堆Dump可以由jmap生成
- 打开线程Dump
- 生成应用快照(包含内存、线程信息等)
- 性能分析、CPU分析(各个方法调用时间、检查哪些方法耗时多)、内存分析(检查各对象占用内存，检查哪些类占用内存多）

##### 4.3.1、VisualVM启动

在jdk的安装目录bin下找到visualvm.exe执行即可

### 5、垃圾回收

#### 5.1、什么是垃圾回收

程序运行必然会申请内存资源，无效的对象资源如果不及时处理就会一直占用内存资源，最终导致内存溢出，所以堆内存资源的管理是非常重要的。

##### 5.1.1、C/C++语言垃圾回收

在C/C++语言中，没有自动垃圾回收机制，是通过new关键字申请内存资源，通过delete关键字释放内存资源。如果程序员在某些位置没有写delete进行释放，那么申请的对象将一直占用内存资源，最终导致内存溢出。

##### 5.1.2、java语言的垃圾回收

为了让程序员专注代码实现，而不用过多考虑内存释放问题，所以在java语言中，有了自动垃圾回收机制，也就是GC；

有了垃圾回收机制后，内存释放由系统自动识别完成，自动回收的算法很重要，如果算法不合理，导致内存资源一直未被释放，同样可能造成内存溢出。

#### 5.2、垃圾回收的常用算法

##### 5.2.1、引用计数法

引用计数法历史最悠久的一种算法该算法依然被很多编程语言使用。

##### 5.2.2、原理

假设有一个对象A，任何一个对象对A进行引用，那么A的引用计数器+1，当引用失效时A的引用计数器-1，如果A的计数器的值为0，就说明对象A没有被引用，可以被回收。

##### 5.2.3、优缺点

优点：

- 实时性较高，无需等待内存不够的时候才开始回收，运行时根据对象的计数器是否为0，就可以直接回收
- 在垃圾回收过程中应用无需挂起，如果申请内存时，内存不足，则立即报outofmemerry
- 区域性，更行对象的计数器，只是影响到该对象，不会扫描全部对象

缺点：

- 每次对象被引用时都需要去更新计数器，有时间开销
- 浪费CPU资源，即使内存够用，仍然在运行时进行计数器的统计
- 无法解决循环引用的问题。（最大问题）

#### 5.3、标记清除法

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/标记清除算法.png)

标记清除法将垃圾回收分为两个阶段，分别是标记和清除

- 标记：从根节点开始标记引用的对象
- 清除：未被标记引用的对象就是垃圾对象，可以被清理

所有从root对象可达的对象就被标记为存活对象，此时已完成第一阶段的标记，接下来执行清除。

优缺点：

- 优点：标记清除算法解决了循环引用问题。
- 缺点：
  - 效率较低，标记和清除都需要遍历所有对象，并且在GC时要停止应用程序对于交互性要求比较高的应用而言，这个体验是非常差的。
  - 通过标记清除算法清理出来的内存碎片化严重，因为被回收的对象可能存于内存中的各个角落，所以清理出来的内存是不连贯的。

#### 5.4、标记压缩算法

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/标记压缩算法.png)

标记压缩算法在标记清除算法的基础上做了优化改进的算法。和标记清除算法一样，也是从根节点开始，对对象引用进行标记，在清理阶段，并不是简单的清理未标记对象，而是将存活的对象压缩到内存的另一端，然后清理边界以外的垃圾，从而解决碎片化问题。

使用场景：多用于老年代

#### 5.5、复制算法

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/复制算法.png)

与标记清除算法相比，复制算法是一种相对高效的回收方法。它的核心思想：将现有的内存分为量块，每次使用其中一块，在垃圾回收时，将正在使用内存中或对象复制到未使用的内存中，之后清理正在使用的内存块中的所有对象，交换两内存角色，完成垃圾回收。可以保证回收后的内存空间没有碎片化，代价是将内存折半。

java新生代串行垃圾回收机制中使用了复制算法的思想。新生代分为eden、from、to三个空间，其中from和to像个空间可视为大小相等的两块。from和to也称为survivor空间。

在垃圾回收时，eden控件中存活的对象会被复制到未使用的survivor空间中（假设是to），正在使用的survivor（假设是from）

中的年轻对象也会被复制到to空间中（大对象或老年对象会直接进入老年代，如果to空间已满，则对象也会直接进入老年代），此时eden空间和from空间中剩余的对象就是垃圾对象，可以直接清空，to空间则存放此后回收后的存活对象。

#### 5.6、分代算法(Generational Collecting)

如复制、标记清除、标记压缩等垃圾回收算法没有一种算法能完全替代其他算法，他们都有自己独特的优势和特点，因此根据垃圾回收对象的特性，使用适合的算法回收，才是明智的选择。

以hot spot为例，它将所有的新建对象都放入到年轻代内存区域，年轻代的特点是对象朝生夕灭，大约90%的新建对象会被很快回收，因此年轻代选择效率搞得复制算法。当一个对象经历几次回收后依然存活，对象就会被放入老年代。老年代对象可以认为在一段时间内，甚至整个应用程序的生命周期中存活。

根据老年代的特性可以采用标记-压缩法。

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/分代算法.png)

#### 5.7、垃圾收集器以及内存分配

在jvm中实现了多种垃圾收集器，包括：串行垃圾收集器、并行垃圾收集器、CMS(并发)垃圾收集器、G1垃圾收集器。

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/垃圾收集器的分类.png)

##### 5.7.1、串行垃圾收集器

串行垃圾收集器是指单线程进行垃圾回收，只有一个线程工作，并且java应用中的所有线程都要暂停，等待垃圾回收完成。这种现象称之为STW(Stop-The-World).

对于交互性较强的应用而言，这种垃圾收集器是不能接受的。

```
#指定垃圾回收算法
-XX:UseSerialGC 
#打印垃圾回收信息
-XX:+PrintGCDetails

/**
 * @author heartccace
 * @create 2020-03-18 14:29
 * @Description 测试串行垃圾回收器SerialGC
 * @param 配置虚拟机参数：-XX:+UseSerialGC -XX:+PrintGCDetails -Xms8m -Xmx8m
 * @Version 1.0
 */
public class TestGC {
    private static List<Object> list = new ArrayList<Object>();
    public static void main(String[] args) {
        int time = new Random().nextInt(100);
        while (true) {
            if(time % 2 == 0) {
                list.clear();
            } else {
                for(int i = 0;i < 1000; i++) {
                    Properties pro = new Properties();
                    pro.put("key" + i, "value" + System.currentTimeMillis() + i);
                    list.add(pro);
                }
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}



#OUTPUT GC日志
[GC (Allocation Failure) [DefNew: 2176K->255K(2432K), 0.0038030 secs] 2176K->903K(7936K), 0.0038896 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 
[GC
[GC (Allocation Failure) [DefNew: 2432K->2432K(2432K), 0.0000264 secs][Tenured: 4226K->5504K(5504K), 0.0145113 secs] 6658K->5533K(7936K), [Metaspace: 3754K->3754K(1056768K)], 0.0152593 secs] [Times: user=0.02 sys=0.00, real=0.02 secs] 
[Full GC (Allocation Failure) [Tenured: 5504K->5504K(5504K), 0.0145040 secs] 7936K->6802K(7936K), [Metaspace: 3755K->3755K(1056768K)], 0.0147014 secs] [Times: user=0.00 sys=0.00, real=0.02 secs] 

```



GC日志解读：

年轻代的内存GC前后大小：

- (Allocation Failure)
  - 表示垃圾回收的原因

- DefNew
  - 表示使用的是串行垃圾收集器

-  2176K->255K(2432K)
  - 表示，年轻代GC前占有2176K内存，GC后占有255K内存，总大小2432K

- 0.0038030 secs
  - 表示GC所用的时间，单位：毫秒

- 2176K->903K(7936K)
  - 表示：GC前，堆内存占有2176K，GC后堆内存占有903K，总大小未7936K

- Full GC
  - 表示：内存空间全部进行GC（包括年轻带、老年代、matespace）

##### 5.7.2、并行垃圾回收器

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/新生代并行回收.png)

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/老年代并行回收.png)

并行垃圾回收器在串行垃圾回收器的基础上做了改进，将单线程改为多线程垃圾回收，这样可以缩短垃圾回收时间。

并行垃圾回收器在收集过程中也会暂停应用程序，这和串行垃圾回收器也是一样。只是并行执行速度更快些。

- ParNew垃圾回收器

```
#开启并行垃圾回收
#参数设置年轻代采用ParNew回收器，老年代依然是串行回收器
-XX:+UseParNewGC


#output
[GC (Allocation Failure) [ParNew: 2176K->256K(2432K), 0.0019652 secs] 2176K->925K(7936K), 0.0026358 secs] [Times: user=0.00 sys=0.00, real=0.00 secs] 

```

- ParallelGC垃圾回收器（存在stop the world）

  ParallelGC垃圾回收器工作机制和ParNew收集器一样，只是在此基础上，新增了两个和系统相关的参数，使得其使用起来更加灵活和高效。

  - -XX:+UseParallelGC
    - 年轻代使用ParallelGC，老年代使用穿行回收器

  - -XX:+UseParallelOldGC
    - 年轻代使用ParallelGC，老年代使用ParallelOldGC

  - -XX:MaxGCPauseMills
    - 设置最大垃圾收集的停顿时间，单位为毫秒
    - 需要注意的是ParallelGC为达到设置的停顿时间，可能会调整堆大小或其它参数，如果堆大小设置较小，就会导致GC工作变得频繁，反而可能会影响性能。
    - 该参数使用需谨慎

  - -XX:GCTimeRadio
    - 设置垃圾回收占程序运行时间的百分比，公式为1/（1+n）
    - 它的值为0-100之间的数字，默认值为99，也就是垃圾回收时间不能草果1%

  - -XX:+UseAdaptiveSizePolicy
    - 自适应GC模式，垃圾回收器会自动调整新生代、老年代参数，达到吞吐量、堆大小、停顿时间之间的平衡
    - 一般用于手动调整参数比较困难的场景，让收集器自动调整

##### 5.7.3、CMS垃圾收集器

CMS全称是Concurrent Mark sweep,是一款并发的、使用标记-清除算法的垃圾回收器，该回收器针对老年代垃圾回收，通过-XX:+UseConcMarkSweepGC设置，其中新生代使用并行收集器，老年代使用CMS。

年轻代使用的收集器时ParNew并行收集器

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/cms工作原理.png)

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/CMS执行流程.png)

- 初始化标记（CMS-initial-mark），标记root，会导致stop the world
- 并发标记（CMS-concurrent-mark），与用户线程同时进行
- 预处理（CMS-concurrent-preclean），与用户线程同时进行
- 重新标记（CMS-remark），会导致stw
- 并发清除（CMS-concurrent-sweep），与用户线程同时运行
- 调整大小，设置CMS在清理之后进行内存压缩，目的清理内存中的碎片
- 并发重置状态，等待一次CMS的触发（CMS-concurrent-reset），与用户线程同时进行

-XX:CMSinitiatingOccupancyFraction默认值68，表示当老年代空间使用到达68%时会启动一次CMS;如果应用程序内存使用率增长很快，在CMS的执行过程中，已经出现内存不足，此时CMS就会失效，系统将会启动老年代的串行回收。

##### 5.7.4、G1（Garbage First）收集器

G1垃圾收集器是目前最新的垃圾回收器。

与CMS收集器相比，G1收集器是基于标记-压缩算法，不会产生碎片空间，也不会在收集完成之后进行一次独占式的碎片整理工作，它在吞吐量和停顿上要优于CMS收集器。

原理：

G1收集器相比其他收集器而言，最大的区别在于它取消了年轻代、老年代的划分，取而代之的是将堆分为若干个区域（Region）,而这些区域包含了逻辑上的年轻代和老年代，这样做的好处是，我们再也不用单独对每个空间对每个代进行设置了，不用担心内存是否足够。

G1的设计原则时简化JVM性能调优，开发人员只需完成三步：
1、 第一步：开启G1垃圾回收器

2、 第二步：设置堆大的最大内存

3、 第三步： 设置最大的停顿时间

G1中提供了三种垃圾回收模式，YoungGC、MixedGC和FullGC，它们在不同的条件下触发。



![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/G1垃圾收集器.png)

在G1划分的区域中，年轻代的垃圾收集器依然采用暂停所有应用线程的方式，将存活对象拷贝到Survivor空间或者老年代，G1收集器通过将对象从一个区域复制到另一个区域完成收集工作。

这就意味着在正常的处理过程中，G1完成堆的压缩(至少部份堆的压缩)，这样也就不会有CMS内存碎片的问题了。

###### 在G1中有特殊的一个区域叫Humongous：

- 如果一个对象占用的空间超过了分区容量的50%，G1收集器就认为这是一个巨型对象。
- 这些巨型对象，默认会直接分配到老年代，但如果他是一个短时间存在的巨型对象，就会对垃圾回收器造成负面的影响。
- 为了解决这个问题，G1划分了一个Humongous区，它采用了专门存放巨型对象，如果一个H区装不下一个巨型对象，那么G1就会寻找连续的H区来存储，为了寻找连续的H区，有时候不得不启用Full GC。

###### G1中的YuongGC：

YuongGC主要对Eden区进行GC，它在Eden空间被耗尽时触发

- Eden空间的数据移动到Suvivor空间中，如果Survivor空间不够，Eden空间的部份数据，直接进入到老年代空间
- Survivor区中的数据移动到新的Survivor中，也有部分数据晋升到老年代
- 最终Eden空间的数据为空，GC停止工作，应用线程继续执行。

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/G1工作原理.png)

###### Remember Set(已记忆集合)：

在GC年轻代对象时，我们如何找到年轻对象中的根对象？

跟对象可能在年轻代中，也可能在老年代中，那么老年代中所有对象都是跟对象？

如果全盘扫描老年代，那么这样扫描会消耗大量时间。

于是G1引进了RSet的概念，它的名称时Remember Set，其作用时跟踪指向某个堆内的对象引用。

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/RSet.png)

图中每个Region初始化的时候都会初始化一个RSet，Region中每一格代表一个对象（card）。

Region1和Region3中引用了Region2中的对象，所以在Region2的RSet里面记录了，Region1和Region3中的card



###### G1中的MixedGC

当越来越多的对象晋升到老年代old Region时，为了避免内存被耗尽，虚拟机会触发一次混合垃圾回收，即mixedGC,该算法并不是一次Old GC，除了回收整个Yuong Region，还会回收一部分Old Region，这里需要注意的时一部分Old Region而不是全部，可以选择哪些Old Region进行回收。从而可以对垃圾回收时间进行控制。也需要注意MixedGC并不是FullGC。

MixedGC什么时候触发是由参数-XX:InitialHeapOccupancyPercent=n决定，默认45%，该参数的意思是：当老年代大小占整个堆大小百分比达到该阈值的时候触发。

它的GC步骤分为：

- 全局并发标记(Global concurrent marking)

  全局并发标记分为以下步骤：

  - 初始标记(Initial mark,STW)：标记从根节点直接可到达的对象，这一阶段会执行一次年轻代GC，会产生全局停顿
  - 根区域扫描(root region scan)：G1 GC在初始标记的存活区域扫描对老年代的引用，并标记被引用的对象，该阶段引用程序（非stw）同时运行，并且只有完成该阶段后，才能开始下一次STW年轻代垃圾回收。
  - 并发标记（concurrent marking）：G1 CG在整个堆中查找可访问的(存活对的)对象，该阶段与应用程序同时运行，可以被STW年轻代垃圾回收中断。
  - 重新标记（Remaark Marking，STW）：该阶段时STW的回收，因为程序在运行，针对上一次标记进行修正。
  - 清除垃圾（cleanup，STW）：清点和重置标记状态，该阶段会STW，该阶段并不会实际去做垃圾的回收，等待evacuation的阶段来回收。

- 拷贝存活对象(evacuation)

  Evacuation阶段时全暂停的，该阶段把一部分的Region里的或对象拷贝到另一部分Region中，从而实现垃圾回收清理。

###### G1垃圾收集器的一些参数：

- -XX:+UseG1GC
  - 启用G1垃圾收集器

- -XX:MaxGCPauseMillis
  - 设置期望达到的最大GC停顿时间指标(JVM会尽力实现，但不保证达到)，默认值是200毫秒

- -XX:G1HeapRegionSize=n
  - 设置G1区域的大小，值是2的幂，范围1MB到32M之间，目标是根据最小java堆大小划分出2048个区域
  - 默认堆内存的1/2000

- -XX:ParallelGCThreads=n
  - 设置STW工作线程的值，将n的值设置成逻辑处理器的数量，n的值与逻辑处理器的数量相同。
- -XX:ConcGCThreads=n
  - 设置并行标记的线程数，将n设置为并行垃圾回收线程数（ParallelGCThreads）的1/4左右。

- -XX:InitiatingHeapOccupanccyPercent=n
  - 设置触发标记周期的java堆占用率，默认为整个java堆的45%

###### 对G1垃圾收集器的优化建议

- 年轻代大小
  - 避免使用-Xmn选项和-XX:NewRatio等其他相关选项设置年轻代大小
  - 固定年轻代大小会覆盖暂停时间目标

- 暂停时间(200ms)目标不要太严苛
  - G1 GC的吞吐量为90%的应用程序时间和10%的垃圾回收时间
  - 评估G1 GC的吞吐量时，暂停时间不要太严苛，目标太严苛表示愿意承受更多的垃圾回收开销，而这会直接影响吞吐量

##### 5.7.5、评价GC策略的指标

可以用以下指标评价一个垃圾回收器的好坏：

- 吞吐量：指在应用程序的生命周期内，应用程序所花费的时间和系统运行总时间的比值，系统总运行时间 = 应用程序耗时 + GC耗时。如果系统运行10min，GC耗时1min，那么吞吐量为（100 -1）/100 = 99%

- 垃圾回收器负载：和吞吐量相反，垃圾回收器负载值垃圾回收器于系统运行总时间的比值

- 停顿时间：指垃圾回收器正在运行，应用程序的暂停时间。

- 垃圾回收频率：指垃圾回收器多长时间运行一次。

- 反应时间：当一个对象成为垃圾后，多长时间它锁占用的内存空间会被释放

- 堆分配：不同的垃圾回收器堆堆内存的分配方式可能不同，一个良好的收集器应该有一个合理的堆内存区间的划分

  

### 6.可视化GC分析工具

#### 6.1、GC日志输出参数

```
#输出GC日志
-XX:+PrintGC 
#输出GC的详细日志
-XX:+PrintDetailsGC
#输出GC的时间戳（以基准时间的格式）
-XX:+PrintGCTimeStamps 
#输出GC的时间戳（以日期格式，如2020-03-19T21:29）
-XX:+PrintGCDateStamps
#进行GC前后，打印出堆信息
-XX:+PrintHeapAtGC
#文本文件的输出路径
-Xloggc:../logs/gc.log

```

#### 6.2、GC Easy可以堆日志进行在线分析（gceasy.io）

GC性能图表：

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/g1内存分析.png)



GC吞吐量图表：

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/GC吞吐量.png)



GC分析：



![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/GC详情整体分析.png)



### 7、Tomcat8优化

tomcat服务器在javaEE项目中使用率非常高，所以在生产环境堆tomcat的优化也变得非常重要，对tomcat的优化主要从两个方面入手，1是tomcat自身配置，另一个是tomcat运行时jvm虚拟机的调优。

#### 7.1、tomcat配置

配置tomcat conf目录下的tomcat-users.xml文件

```
#增加以下配置，访问localhost:8080
<role rolename="manager"/>
  <role rolename="manager-gui"/>
  <role rolename="admin"/>
  <role rolename="admin-gui"/>
  <user username="tomcat" password="tomcat" roles="manager,manager-gui,admin,admin-gui"/>
```

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/查看tomcat步骤1.png)

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/查看tomcat步骤2.png)

#### 7.2、禁用AJP连接

在Tomcat的管理页面可以看到AJP默认开启

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/AJP.png)

AJP(Apache JServer Protocol)

AJPv13协议是面向包的，WEB服务器和Server容器通过TCP连接来交互，为了节省SOCKET创建的昂贵代价，WEB服务器会尝试维护一个永久的TCP连接到Servlet容器，并且在多个请求响应周期过程会重用连接。

当未使用nginx等可以禁用AJP协议

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/AJP协议.png)

#### 7.3、执行器（线程池）

在tomcat中每一格用户请求一个线程，所以可以使用线程池来提高性能。修改server.xml

![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/配置连接器.png)



![](https://github.com/heartccace/jvmpro/blob/master/src/main/resources/images/连接器查看.png)

#### 7.4、三种运行方式

tomcat的运行方式有三种：

1、bio

默认的模式，性能非常地下，没有经过任何优化处理和支持

2、nio

nio（new I/O），是java1。4以后版本提供的一种新I/O,java nio是一个基于缓冲区，并能提供阻塞I/O操作的api，因此nio也被堪称non-blocking I/O的缩写，它拥有比传统I/O更好的并发性。

3、apr

安装起来最困难，但是从操作系统界别来解决异步I/O问题，大幅度提高性能。推荐使用nio，tomcat8中有nio2，速度更快：设置nio2

```
 #开启nio2 将protocol配置org.apache.coyote.http11.Http11Nio2Protocol
 <Connector executor="tomcatThreadPool" 
			   port="8080" protocol="org.apache.coyote.http11.Http11Nio2Protocol"
               connectionTimeout="20000"
               redirectPort="8443" />
```

