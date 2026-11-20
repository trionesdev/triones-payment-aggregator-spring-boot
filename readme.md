# TrionesDev Payment Aggregator Spring Boot

TrionesDev 聚合支付 Spring Boot 集成，为支付宝、微信支付提供渠道装配和异步通知入口，并通过 `PaymentAggregatorFactory` 统一调用支付渠道。

本文对应当前源码版本 `3.0.0-SNAPSHOT`。当前分支仍有接入限制，使用前请阅读[当前实现注意事项](#当前实现注意事项)。

## 模块说明

| 模块 | 用途 |
| --- | --- |
| [payment-aggregator-spring-boot-dependencies](payment-aggregator-spring-boot-dependencies) | BOM，统一管理 Starter 和底层聚合支付依赖版本 |
| [spring-boot-starter-payment-aggregator](payment-aggregator-spring-boot-starters/spring-boot-starter-payment-aggregator) | 注册统一调用入口 `PaymentAggregatorFactory` |
| [spring-boot-starter-payment-aggregator-alipay](payment-aggregator-spring-boot-starters/spring-boot-starter-payment-aggregator-alipay) | 支付宝渠道、客户端配置和通知入口 |
| [spring-boot-starter-payment-aggregator-wechatpay](payment-aggregator-spring-boot-starters/spring-boot-starter-payment-aggregator-wechatpay) | 微信支付渠道、配置转换和支付／退款／转账通知入口 |

统一入口提供创建订单、关闭订单、创建退款和创建转账四种操作；具体渠道支持的业务场景及请求字段由底层 `payment-aggregator-*` 依赖决定。

## 环境要求

- JDK 17。
- Maven（仓库未提供 Maven Wrapper）。
- 项目 POM 管理的 Spring Boot 版本为 `3.3.5`，Kotlin 版本为 `2.1.21`。
- 渠道通知入口基于 Spring MVC 和 `jakarta.servlet`；本文按 Spring Boot 3 的项目配置编写。

## 引入依赖

在业务项目的 `pom.xml` 中导入 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.trionesdev.payment</groupId>
            <artifactId>payment-aggregator-spring-boot-dependencies</artifactId>
            <version>3.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

添加统一入口及需要的渠道。渠道 Starter 不会传递引入统一入口 Starter；使用工厂时需要单独添加：

```xml
<dependencies>
    <dependency>
        <groupId>com.trionesdev.payment</groupId>
        <artifactId>spring-boot-starter-payment-aggregator</artifactId>
    </dependency>
    <dependency>
        <groupId>com.trionesdev.payment</groupId>
        <artifactId>spring-boot-starter-payment-aggregator-alipay</artifactId>
    </dependency>
    <dependency>
        <groupId>com.trionesdev.payment</groupId>
        <artifactId>spring-boot-starter-payment-aggregator-wechatpay</artifactId>
    </dependency>
</dependencies>
```

可以删除不使用的渠道依赖。当前 BOM 引入的底层 BOM 为 `payment-aggregator-dependencies:0.0.2-SNAPSHOT`。若需要解析快照依赖，可使用项目 POM 中配置的仓库；以下配置应放入业务项目的 `<project>` 下：

```xml
<repositories>
    <repository>
        <id>trionesdev</id>
        <url>https://maven.cnb.cool/trionesdev/mvn/-/packages/</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```

版本号来自当前源码，不代表已验证远程仓库存在对应制品。也可以先在本地执行 `mvn clean install` 安装本项目模块；底层依赖仍需能够解析。

## 自动配置

三个 Starter 均在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中声明自动配置入口，并保留 `META-INF/spring.factories` 注册信息。当前入口已与源码包名同步：

| Starter | 自动配置类 | 装配条件与内容 |
| --- | --- | --- |
| 统一入口 | `com.trionesdev.payment.aggregator.autoconfigure.PaymentAggregatorAutoConfiguration` | 收集 `PaymentChannel` Bean，注册 `PaymentAggregatorFactory` |
| 支付宝 | `com.trionesdev.payment.aggregator.alipay.autoconfigure.AlipayAutoConfiguration` | `triones.payment.alipay.enabled=true` 时注册 `Alipay`、`AlipayChannel` 并导入通知 Controller |
| 微信支付 | `com.trionesdev.payment.aggregator.wechatpay.autoconfigure.WechatPayAutoConfiguration` | `triones.payment.wechatpay.enabled=true` 时注册 `WechatPayChannel` 并导入通知 Controller |

业务项目通过 Spring Boot 自动配置加载这些入口，无需手动扫描 Starter 包。统一工厂的 Bean 名称为 `aggregatedPaymentFactory`，可以按 `PaymentAggregatorFactory` 类型注入。微信客户端装配仍需补齐，见文末说明。

## 渠道配置

以下为 `application.yml` 配置模板，环境变量需替换为实际商户参数。渠道仅在对应 `enabled: true` 时装配。配置字段来自当前属性类，绑定限制见文末说明。

### 支付宝

```yaml
triones:
  payment:
    alipay:
      enabled: true
      app-id: ${ALIPAY_APP_ID}
      private-key: ${ALIPAY_PRIVATE_KEY}
      alipay-public-key: ${ALIPAY_PUBLIC_KEY}
      notify-url: ${PAYMENT_PUBLIC_BASE_URL}/rest-api/payment/alipay/notify
```

| 配置项（前缀 `triones.payment.alipay`） | 说明 |
| --- | --- |
| `enabled` | 渠道装配开关，需显式设置为 `true` |
| `app-id` | 支付宝应用 ID |
| `private-key` | 应用私钥内容 |
| `alipay-public-key` | 支付宝公钥内容 |
| `app-cert-path` / `app-cert-content` | 应用证书路径／内容 |
| `alipay-public-cert-path` / `alipay-public-cert-content` | 支付宝公钥证书路径／内容 |
| `root-cert-path` / `root-cert-content` | 根证书路径／内容 |
| `notify-url` | 默认异步通知地址 |
| `notify-urls` | 属性类声明了该映射，当前自动配置未向客户端传递 |

证书模式下可配置相应证书字段；路径、内容的选择规则由底层支付宝客户端决定。

### 微信支付

```yaml
triones:
  payment:
    wechatpay:
      enabled: true
      app-id: ${WECHATPAY_APP_ID}
      mch-id: ${WECHATPAY_MCH_ID}
      api-v3-key: ${WECHATPAY_API_V3_KEY}
      private-key-base64: ${WECHATPAY_PRIVATE_KEY_BASE64}
      private-cert-base64: ${WECHATPAY_PRIVATE_CERT_BASE64}
      transaction-notify-url: ${PAYMENT_PUBLIC_BASE_URL}/rest-api/payment/wechatpay/transaction-notify
      refund-notify-url: ${PAYMENT_PUBLIC_BASE_URL}/rest-api/payment/wechatpay/refund-notify
      transfer-notify-url: ${PAYMENT_PUBLIC_BASE_URL}/rest-api/payment/wechatpay/transfer-notify
```

| 配置项（前缀 `triones.payment.wechatpay`） | 说明 |
| --- | --- |
| `enabled` | 渠道装配开关，需显式设置为 `true` |
| `app-id` / `mch-id` | 应用 ID／商户号 |
| `api-v3-key` | API v3 密钥 |
| `private-key` / `private-cert` | 商户私钥／证书内容 |
| `private-key-base64` / `private-cert-base64` | 私钥／证书内容的 Base64 编码 |
| `private-key-path` / `private-cert-path` | 私钥／证书文件路径 |
| `transaction-notify-url` | 默认支付通知地址 |
| `refund-notify-url` | 默认退款通知地址 |
| `transfer-notify-url` | 默认转账通知地址 |
| `transaction-notify-urls` / `refund-notify-urls` | 传递至底层配置的通知地址映射 |
| `transfer-notify-urls` | 属性类声明了该映射，当前自动配置未向客户端传递 |

私钥或证书的直接内容为空时，配置转换方法才会解码对应的 Base64 字段；文件路径会另外传入底层配置。Base64 只用于编码，密钥仍应通过环境变量或密钥管理服务注入。

## 统一调用

业务服务可以通过构造器注入工厂。以下 Java 示例展示调用方式，请先根据底层依赖的模型构造完整请求：

```java
import com.trionesdev.payment.aggregator.autoconfigure.PaymentAggregatorFactory;
import com.trionesdev.payment.aggregator.shared.model.CreateOrderRequest;
import com.trionesdev.payment.aggregator.shared.model.CreateOrderResponse;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private final PaymentAggregatorFactory paymentFactory;

    public PaymentService(PaymentAggregatorFactory paymentFactory) {
        this.paymentFactory = paymentFactory;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        // 调用前填入有效 channel，以及该渠道要求的订单和支付场景参数。
        return paymentFactory.createOrder(request);
    }
}
```

| 方法 | 请求 | 返回值 |
| --- | --- | --- |
| `createOrder` | `CreateOrderRequest` | `CreateOrderResponse` |
| `closeOrder` | `CloseOrderRequest` | 无 |
| `createRefund` | `CreateRefundRequest` | `CreateRefundResponse` |
| `createTransfer` | `CreateTransferRequest` | `CreateTransferResponse` |

请求／响应模型位于 `com.trionesdev.payment.aggregator.shared.model`。工厂收集 Spring 容器中的 `PaymentChannel`，以渠道类上 `@PaymentComponent` 的 `channel` 值建立映射，并按请求的 `channel` 路由。渠道标识请以底层实现的注解值为准。

当前实现对请求渠道和查找结果使用非空断言；`channel` 为空或渠道未注册会导致异常。业务层应在调用前校验渠道与参数。

## 异步通知

渠道启用并成功装配后提供以下 POST 入口。若应用设置了 context path 或网关前缀，外部通知地址需包含对应前缀。

| 渠道 | 路径 | 当前行为 |
| --- | --- | --- |
| 支付宝 | `/rest-api/payment/alipay/notify` | 仅在 `trade_status=TRADE_SUCCESS` 时调用渠道通知方法并返回 `success`，其他状态返回 `fail` |
| 支付宝 | `/rest-api/payment/alipay/app-gateway` | 预留入口，方法体为空 |
| 微信支付 | `/rest-api/payment/wechatpay/transaction-notify` | 调用渠道支付通知方法 |
| 微信支付 | `/rest-api/payment/wechatpay/refund-notify` | 调用渠道退款通知方法 |
| 微信支付 | `/rest-api/payment/wechatpay/transfer-notify` | 调用渠道转账通知方法 |

微信通知入口接收原始请求体和 `Wechatpay-Nonce`、`Wechatpay-Signature`、`Wechatpay-Timestamp`、`Wechatpay-Serial` 四个请求头，传给底层渠道处理。正常返回包含 `code: SUCCESS` 的对象；处理异常时调用 `sendError` 返回 HTTP 500。

业务通知扩展点为 `com.trionesdev.payment.aggregator.PaymentNotifyCallback`。如需更新业务订单，实现底层依赖中该接口并注册为 Spring Bean；渠道装配时通过 `ObjectProvider` 获取回调实现。接口方法签名以实际依赖版本为准。

通知地址应允许支付平台访问，应用鉴权和 CSRF 配置需适配这些入口。业务处理应校验订单信息并保证幂等，验签及解密行为应结合底层渠道实现确认。

## 当前实现注意事项

当前源码存在以下限制，以上配置模板尚不能视为已通过端到端验证的接入方案：

1. `AlipayPayProperties`、`WechatPayProperties` 使用初始化为 `null` 的只读 `val` 属性，未提供构造绑定参数或 setter，需完善属性绑定后验证 YAML 配置能正确注入。
2. 微信模块提供 `wxPayConfig()` 配置转换方法，但当前未将其声明为 Bean，也未声明创建 `WechatPay` 客户端的 Bean。接入时需补齐客户端装配，确保渠道获取到可用客户端。
3. 支付宝 `notify-urls` 和微信 `transfer-notify-urls` 当前未在自动配置中传递；支付宝应用网关入口尚未实现业务处理。

## 本地构建

在项目根目录执行：

```shell
# 检查开发环境
java -version
mvn -version

# 构建所有模块并执行测试阶段
mvn clean verify

# 安装到本地 Maven 仓库，供其他项目引用
mvn clean install
```

本仓库是多模块 Starter 工程，没有可独立运行的 Spring Boot 应用入口，也未包含测试源码。构建成功仅验证构建流程；真实支付与回调需在接入项目中完成联调。

## 交流

互相吹捧，共同进步。沟通不及时，可以关注公众号，直接联系。

<div style="width: 100%;text-align: center">
<img src="images/shuque_wx.jpg" width="200px" alt="TrionesDev 交流公众号二维码">
</div>
