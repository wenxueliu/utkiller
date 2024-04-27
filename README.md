## UT-Killer

写单元测试是一个费时费力的工作，我们希望写单元测试可以快速完成。因此，我们开发了一个工具来帮助开发人员快速写单元测试。
UT-Killer是一个基于字节码工具，通过自动拦截Java方法，记录方法执行的参数和返回值，之后通过大模型生成单元测试代码。

## 感谢

[arthas](https://github.com/alibaba/arthas): Alibaba开源的Java诊断工具
[bytekit](https://github.com/alibaba/bytekit): Java Bytecode Kit，Arthas 里字节码增强的内核。
[byte-buddy](https://github.com/raphw/byte-buddy): 字节码工具，用于修改字节码。

