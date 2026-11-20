package com.trionesdev.payment.aggregator.autoconfigure

import com.trionesdev.payment.aggregator.PaymentChannel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PaymentAggregatorAutoConfiguration {

    @Bean
    fun aggregatedPaymentFactory(paymentChannels: MutableList<PaymentChannel>): PaymentAggregatorFactory {
        return PaymentAggregatorFactory(paymentChannels)
    }

}