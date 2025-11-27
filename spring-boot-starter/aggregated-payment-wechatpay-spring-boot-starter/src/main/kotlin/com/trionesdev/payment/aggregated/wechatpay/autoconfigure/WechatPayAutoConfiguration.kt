package com.trionesdev.payment.aggregated.wechatpay.autoconfigure

import com.trionesdev.payment.aggregated.AggregatedPaymentNotifyCallback
import com.trionesdev.payment.aggregated.wechatpay.WechatPayAggregatedPaymentChannel
import com.trionesdev.payment.wechatpay.v3.WechatPay
import com.trionesdev.payment.wechatpay.v3.WechatPayConfig
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Slf4j
@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "triones.payment.wechatpay", value = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(value = [WechatPayProperties::class])
@Import(value = [WechatPayResource::class])
class WechatPayAutoConfiguration(
    private val wxPayProperties: WechatPayProperties? = null
) {

    @OptIn(ExperimentalEncodingApi::class)
    fun wxPayConfig(): WechatPayConfig {
        var privateKey: String? = wxPayProperties?.privateKey
        var privateCert: String? = wxPayProperties?.privateCert
        if (StringUtils.isBlank(privateKey) && StringUtils.isNotBlank(wxPayProperties?.privateKeyBase64)) {
            privateKey = Base64.decode(wxPayProperties?.privateKeyBase64.toString()).toString(Charsets.UTF_8)
        }
        if (StringUtils.isBlank(privateCert) && StringUtils.isNotBlank(wxPayProperties?.privateCertBase64)) {
            privateCert = Base64.decode(wxPayProperties?.privateCertBase64.toString()).toString(Charsets.UTF_8)
        }
        return WechatPayConfig().apply {
            this.appId = wxPayProperties?.appId
            this.mchId = wxPayProperties?.mchId
            this.privateKey = privateKey
            this.privateCert = privateCert
            this.privateKeyPath = wxPayProperties?.privateKeyPath
            this.privateCertPath = wxPayProperties?.privateCertPath
            this.apiV3Key = wxPayProperties?.apiV3Key
            this.transactionNotifyUrl = wxPayProperties?.transactionNotifyUrl
            this.refundNotifyUrl = wxPayProperties?.refundNotifyUrl
            this.transferNotifyUrl = wxPayProperties?.transferNotifyUrl
            this.transactionNotifyUrls = wxPayProperties?.transactionNotifyUrls
            this.refundNotifyUrls = wxPayProperties?.refundNotifyUrls
        }
    }


    @Bean
    fun wechatPayService(
        wechatpay: ObjectProvider<WechatPay>,
        aggregatedPaymentNotify: ObjectProvider<AggregatedPaymentNotifyCallback>,
    ): WechatPayAggregatedPaymentChannel {
        return WechatPayAggregatedPaymentChannel(wechatpay.ifAvailable, aggregatedPaymentNotify.ifAvailable)
    }
}