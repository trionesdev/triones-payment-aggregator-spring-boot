package com.trionesdev.payment.aggregator.alipay.autoconfigure

import com.trionesdev.payment.aggregator.PaymentNotifyCallback
import com.trionesdev.payment.aggregator.alipay.AlipayChannel
import com.trionesdev.payment.alipay.v3.Alipay
import com.trionesdev.payment.alipay.v3.AlipayConfig
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "triones.payment.alipay", value = ["enabled"], havingValue = "true")
@EnableConfigurationProperties(value = [AlipayPayProperties::class])
@Import(value = [AlipayResource::class])
class AlipayAutoConfiguration(
    val alipayPayProperties: AlipayPayProperties
) {
    private fun alipayIntegrationConfig(): AlipayConfig {
        val config = AlipayConfig()
        config.appId = alipayPayProperties.appId
        config.privateKey = alipayPayProperties.privateKey
        config.alipayPublicKey = alipayPayProperties.alipayPublicKey
        config.appCertContent = alipayPayProperties.appCertContent
        config.alipayPublicCertContent = alipayPayProperties.alipayPublicCertContent
        config.rootCertContent = alipayPayProperties.rootCertContent
        config.appCertPath = alipayPayProperties.appCertPath
        config.alipayPublicCertPath = alipayPayProperties.alipayPublicCertPath
        config.rootCertPath = alipayPayProperties.rootCertPath

        config.notifyUrl = alipayPayProperties.notifyUrl
        return config
    }

    @Bean
    fun alipay(): Alipay {
        return Alipay(alipayIntegrationConfig())
    }


    @Bean
    fun alipayService(
        alipay: ObjectProvider<Alipay>,
        aggregatedPaymentNotify: ObjectProvider<PaymentNotifyCallback>,
    ): AlipayChannel {
        return AlipayChannel(alipay.getIfAvailable(), aggregatedPaymentNotify.getIfAvailable())
    }
}