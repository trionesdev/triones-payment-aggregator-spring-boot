package com.trionesdev.payment.aggregated.wechatpay.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "triones.payment.wechatpay")
class WechatPayProperties {
     val enabled: Boolean? = null
     val appId: String? = null
     val mchId: String? = null
     val apiV3Key: String? = null
     val privateKey: String? = null
     val privateCert: String? = null
     val privateKeyBase64: String? = null
     val privateCertBase64: String? = null
     val privateKeyPath: String? = null
     val privateCertPath: String? = null
     val transactionNotifyUrl: String? = null
     val refundNotifyUrl: String? = null
     val transferNotifyUrl: String? = null
     val transactionNotifyUrls: MutableMap<String?, String?>? = null
     val refundNotifyUrls: MutableMap<String?, String?>? = null
     val transferNotifyUrls: MutableMap<String?, String?>? = null
}