package com.trionesdev.payment.aggregator.autoconfigure

import com.trionesdev.payment.aggregate.AggregatePaymentChannel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PaymentAggregatorAutoConfiguration {

    @Bean
    fun aggregatedPaymentFactory(aggregatedPaymentChannels: MutableList<AggregatePaymentChannel>): PaymentAggregatorFactory {
        return PaymentAggregatorFactory(aggregatedPaymentChannels)
    }

}