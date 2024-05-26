## UT-Killer

写单元测试是一个费时费力的工作，我们希望写单元测试可以快速完成。因此，我们开发了一个工具来帮助开发人员快速写单元测试。
UT-Killer是一个基于字节码工具，通过自动拦截Java方法，记录方法执行的参数和返回值，之后通过大模型生成单元测试代码。


## 使用方式

### 添加依赖
```xml
<dependency>
    <groupId>ut.killer</groupId>
    <artifactId>ut-killer</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### main 方法增加初始化代码

```java
    HttpAgentServer.init(8888);
```

### 示例

URL: http://127.0.0.1:8888/rest/v2/exec

method: POST 

header:
```
"Content-Type": "application/json"
```

body:
```json
{
  "execRequest": {
    "className": "com.imagedance.zpai.utils.JsonUtils",
    "methodName": "toUserInfo",
    "methodSignature": "(Lcom/imagedance/zpai/model/UserInfo;)Ljava/lang/String;",
    "parameterJsonString": [
      "{\"userId\": \"123\",\"userName\": \"John Doe\"}"
    ],
    "parameterTypeSignature": [
      "Lcom/imagedance/zpai/model/UserInfo"
    ]
  },
  "mockRequests": []
}
```

URL: http://127.0.0.1:8888/rest/v2/exec

method: POST

header:
```
"Content-Type": "application/json"
```

body:
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

## 感谢

[arthas](https://github.com/alibaba/arthas): Alibaba开源的Java诊断工具
[bytekit](https://github.com/alibaba/bytekit): Java Bytecode Kit，Arthas 里字节码增强的内核。
[byte-buddy](https://github.com/raphw/byte-buddy): 字节码工具，用于修改字节码。

