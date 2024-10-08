## UT-Killer

写单元测试是一个费时费力的工作，我们希望写单元测试可以快速完成。因此，我们开发了一个工具来帮助开发人员快速写单元测试。
UT-Killer是一个基于字节码工具，通过自动拦截Java方法，记录方法执行的参数和返回值，之后通过大模型生成单元测试代码。


## 使用方式

### 添加依赖
```xml
<dependency>
    <groupId>ut.killer</groupId>
    <artifactId>utkiller-core</artifactId>
    <version>1.0.2-SNAPSHOT</version>
</dependency>
```

### main 方法增加初始化代码

执行 as.bat xxxx 或 as.sh xxxx 附加到某个进程

## 案例

针对某个被测方法
1. start: 开始录制某个方法
2. exec: 多次执行这个方法的各种参数组合
3. stop: 停止录制该方法

### 打印类的依赖关系

URL: http://127.0.0.1:8888/rest/v1/tree

#### 方法
POST

#### 请求头

```
"Content-Type": "application/json"
```

#### 请求体

```json
{
    "classPath": "com.imagedance.zpai.controller.ImageController",
    "methodName": "queryCollectImages",
    "excludeClassPaths": [],
    "includeClassPaths": ["com.imagedance.zpai.service", "com.imagedance.zpai.dao"]
}
```

#### 应答体

```json
{
    "code": "0",
    "msg": "success",
    "data": {
        "className": "com.imagedance.zpai.controller.ImageController",
        "methodDependencies": [
            {
                "className": "com.imagedance.zpai.controller.ImageController",
                "methodName": "queryCollectImages",
                "dependencies": [
                    {
                        "className": "com.imagedance.zpai.service.impl.ImageServiceImpl",
                        "methodName": "queryCollectImages",
                        "dependencies": [
                            {
                                "className": "com.imagedance.zpai.service.impl.ImageMetaServiceImpl",
                                "methodName": "queryCollectImages",
                                "dependencies": []
                            },
                            {
                                "className": "com.imagedance.zpai.service.impl.ImageMetaServiceImpl",
                                "methodName": "queryImages",
                                "dependencies": []
                            }
                        ]
                    }
                ]
            }
        ],
        "classDependencies": []
    }
}
```

### 启动

URL: http://127.0.0.1:8888/rest/v1/start

#### 方法
POST

#### 请求头
```
"Content-Type": "application/json"
```

#### 请求体
```json
{
  "execRequest" :{
    "className": "com.imagedance.zpai.controller.ImageController",
    "methodName":"deleteCollectImage",
    "methodSignature": "(Lcom/imagedance/zpai/model/vo/ImageCollectDeleteVo;)Lcom/imagedance/zpai/model/ResultVo;",
    "parameterJsonString": [
      "{\"imageId\": \"123\",\"userId\": \"John Doe\"}"
    ],
    "parameterTypeSignature": [
      "Lcom/imagedance/zpai/model/vo/ImageCollectDeleteVo"
    ]
  },
  "mockRequests": [{
    "className": "com.imagedance.zpai.service.ImageService",
    "methodName":"deleteCollectImage",
    "methodSignature": "(Ljava/lang/String;Ljava/lang/String;)V"
  }]
}

```

#### 应答

```json
{
  "jobId" : 0,
  "root" : {
    "children" : [ {
      "children" : [ {
        "children" : null,
        "type" : "method",
        "className" : "com.imagedance.zpai.service.impl.ImageServiceImpl",
        "methodName" : "deleteCollectImage",
        "lineNumber" : -1,
        "args" : [ {
          "type" : "java.lang.String",
          "name" : "imageId",
          "value" : "aaaaaa"
        }, {
          "type" : "java.lang.String",
          "name" : "userId",
          "value" : "bbbbbb"
        } ],
        "returnInfo" : null,
        "throwExp" : null,
        "invoking" : false,
        "mock" : true,
        "throw" : null
      } ],
      "type" : "method",
      "className" : "com.imagedance.zpai.controller.ImageController",
      "methodName" : "deleteCollectImage",
      "lineNumber" : -1,
      "args" : [ {
        "type" : "com.imagedance.zpai.model.vo.ImageCollectDeleteVo",
        "name" : "imageCollectDeleteVo",
        "value" : {
          "imageId" : "aaaaaa",
          "userId" : "bbbbbb"
        }
      } ],
      "returnInfo" : null,
      "throwExp" : null,
      "invoking" : false,
      "mock" : false,
      "throw" : null
    } ],
    "type" : "thread",
    "threadName" : "NanoHttpd Request Processor (#1)",
    "threadId" : 47,
    "daemon" : true,
    "priority" : 5,
    "classloader" : "sun.misc.Launcher$AppClassLoader@18b4aac2"
  },
  "nodeCount" : 2,
  "type" : "trace"
}

```

### 执行

URL: http://127.0.0.1:8888/rest/v1/exec

#### 方法
POST

#### 请求头
```
"Content-Type": "application/json"
```

#### 请求体

```json
{
    "className": "com.imagedance.zpai.controller.ImageController",
    "methodName":"deleteCollectImage",
    "methodSignature": "(Lcom/imagedance/zpai/model/vo/ImageCollectDeleteVo;)Lcom/imagedance/zpai/model/ResultVo;",
    "parameterJsonString": [
      "{\"imageId\": \"123\",\"userId\": \"John Doe\"}"
    ],
    "parameterTypeSignature": [
      "Lcom/imagedance/zpai/model/vo/ImageCollectDeleteVo"
    ]
  }
```

#### 应答

```json
{
"code": "0",
"msg": "success",
"data": "{\"code\":\"0\",\"message\":\"ok\",\"data\":\"\"}"
}
```

### 停止

URL: http://127.0.0.1:8888/rest/v1/stop

#### 方法
POST

#### 请求头
```
"Content-Type": "application/json"
```

#### 请求体

```json
{
  "execRequest" :{
    "className": "com.imagedance.zpai.controller.ImageController",
    "methodName":"deleteCollectImage",
    "methodSignature": "(Lcom/imagedance/zpai/model/vo/ImageCollectDeleteVo;)Lcom/imagedance/zpai/model/ResultVo;",
    "parameterJsonString": [
      "{\"imageId\": \"aaaaaa\",\"userId\": \"bbbbbb\"}"
    ],
    "parameterTypeSignature": [
      "Lcom/imagedance/zpai/model/vo/ImageCollectDeleteVo"
    ]
  },
  "mockRequests": [{
    "className": "com.imagedance.zpai.service.ImageService",
    "methodName":"deleteCollectImage",
    "methodSignature": "(Ljava/lang/String;Ljava/lang/String;)V"
  }]
}
```

#### 应答

```json
{
    "code": "0",
    "msg": "success",
    "data": "ok"
}
```


## 感谢

[arthas](https://github.com/alibaba/arthas): Alibaba开源的Java诊断工具
[bytekit](https://github.com/alibaba/bytekit): Java Bytecode Kit，Arthas 里字节码增强的内核。
[byte-buddy](https://github.com/raphw/byte-buddy): 字节码工具，用于修改字节码。

## 

你是一名 Java 技术专家
```java
public abstract class ResultModel {

    private int jobId;

    /**
     * Command type (name)
     *
     * @return
     */
    public abstract String getType();


    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}

public class TraceModel extends ResultModel {
    private TraceNode root;
    private int nodeCount;

    public TraceModel() {
    }

    public TraceModel(TraceNode root, int nodeCount) {
        this.root = root;
        this.nodeCount = nodeCount;
    }

    @Override
    public String getType() {
        return "trace";
    }

    public TraceNode getRoot() {
        return root;
    }

    public void setRoot(TraceNode root) {
        this.root = root;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }
}
public abstract class TraceNode {
    protected TraceNode parent;
    protected List<TraceNode> children;

    /**
     * node type: method,
     */
    private String type;

    public TraceNode(String type) {
        this.type = type;
    }

    public void addChild(TraceNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        this.children.add(child);
        child.setParent(this);
    }

    public void begin() {
    }

    public void end() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public TraceNode parent() {
        return parent;
    }

    public void setParent(TraceNode parent) {
        this.parent = parent;
    }

    public List<TraceNode> getChildren() {
        return children;
    }
}
public class MethodNode extends TraceNode {
    private String className;
    private String methodName;
    private int lineNumber;

    private List<ArgumentInfo> args;

    private ReturnInfo returnInfo;

    private Boolean isThrow;
    private String throwExp;

    private boolean isMock;

    /**
     * 是否为invoke方法，true为beforeInvoke，false为方法体入口的onBefore
     */
    private boolean isInvoking;

    public MethodNode(String className, String methodName, List<ArgumentInfo> args, int lineNumber, boolean isInvoking) {
        super("method");
        this.className = className;
        this.methodName = methodName;
        this.args = args;
        this.lineNumber = lineNumber;
        this.isInvoking = isInvoking;
        this.isMock = false;
    }

    public void begin() {
    }

    public void end() {
    }

    public void setArgs(List<ArgumentInfo> args) {
        this.args = args;
    }

    public List<ArgumentInfo> getArgs() {
        return args;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Boolean getThrow() {
        return isThrow;
    }

    public void setThrow(Boolean aThrow) {
        isThrow = aThrow;
    }

    public String getThrowExp() {
        return throwExp;
    }

    public void setThrowExp(String throwExp) {
        this.throwExp = throwExp;
    }

    public boolean isInvoking() {
        return isInvoking;
    }

    public void setInvoking(boolean invoking) {
        isInvoking = invoking;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setReturnInfo(ReturnInfo returnInfo) {
        this.returnInfo = returnInfo;
    }

    public ReturnInfo getReturnInfo() {
        return returnInfo;
    }
}
public class ThrowNode extends TraceNode {
    private String exception;
    private String message;
    private int lineNumber;

    public ThrowNode() {
        super("throw");
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
public class ThreadNode extends TraceNode {
    private String threadName;
    private long threadId;
    private boolean daemon;
    private int priority;
    private String classloader;

    public ThreadNode() {
        super("thread");
    }

    public ThreadNode(String threadName, long threadId, boolean daemon, int priority, String classloader) {
        super("thread");
        this.threadName = threadName;
        this.threadId = threadId;
        this.daemon = daemon;
        this.priority = priority;
        this.classloader = classloader;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getClassloader() {
        return classloader;
    }

    public void setClassloader(String classloader) {
        this.classloader = classloader;
    }
}
```
基于以上代码，和如下 json 格式，使用 freemarker 写一个 Java UT代码生成器，
将 json 格式转换为 Java UT 用例，并生成到 src/main/resource 文件夹。以被测类为名称
```json
{
  "jobId" : 0,
  "root" : {
    "children" : [ {
      "children" : [ {
        "children" : null,
        "type" : "method",
        "className" : "com.imagedance.zpai.service.impl.ImageServiceImpl",
        "methodName" : "deleteCollectImage",
        "lineNumber" : -1,
        "args" : [ {
          "type" : "java.lang.String",
          "name" : "imageId",
          "value" : "aaaaaa"
        }, {
          "type" : "java.lang.String",
          "name" : "userId",
          "value" : "bbbbbb"
        } ],
        "returnInfo" : null,
        "throwExp" : null,
        "invoking" : false,
        "mock" : true,
        "throw" : null
      } ],
      "type" : "method",
      "className" : "com.imagedance.zpai.controller.ImageController",
      "methodName" : "deleteCollectImage",
      "lineNumber" : -1,
      "args" : [ {
        "type" : "com.imagedance.zpai.model.vo.ImageCollectDeleteVo",
        "name" : "imageCollectDeleteVo",
        "value" : {
          "imageId" : "aaaaaa",
          "userId" : "bbbbbb"
        }
      } ],
      "returnInfo" : null,
      "throwExp" : null,
      "invoking" : false,
      "mock" : false,
      "throw" : null
    } ],
    "type" : "thread",
    "threadName" : "NanoHttpd Request Processor (#1)",
    "threadId" : 47,
    "daemon" : true,
    "priority" : 5,
    "classloader" : "sun.misc.Launcher$AppClassLoader@18b4aac2"
  },
  "nodeCount" : 2,
  "type" : "trace"
}
```

## 调试

1、主程序（被 agentmain attach 的进程）增加如下 JVM 参数

-Xrunjdwp:transport=dt_socket,server=y,address=8001

主程序正常启动（不要调试启动），将会被阻塞，直到远程调试程序连接上 8001 端口。输出如下内容，并阻塞

Listening for transport dt_socket at address: 8001

2、在有 agentmain 工程增加一个 Remote JVM Debug，端口为 8001。如下配置

-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8001

3、运行远程调试， 主程序将会被启动。

4、在 agentmain 工程打上断点， 然后正常运行 Agent-main 程序

5、远程调试将会暂停到相应断点处，接下来调试就跟普通 Debug 模式一样