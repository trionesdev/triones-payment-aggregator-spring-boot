package com.trionesdev.payment.aggregator.alipay.autoconfigure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "triones.payment.alipay")
class AlipayPayProperties {
     val enabled: Boolean? = null
     val appId: String? = null
     val privateKey: String? = null
     val alipayPublicKey: String? = null
     val appCertPath: String? = null
     val alipayPublicCertPath: String? = null
     val rootCertPath: String? = null
     val appCertContent: String? = null
     val alipayPublicCertContent: String? = null
     val rootCertContent: String? = null

     val notifyUrl: String? = null
     val notifyUrls: MutableMap<String?, String?>? = null
}