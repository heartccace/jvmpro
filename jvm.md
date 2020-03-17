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