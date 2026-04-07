package com.trionesdev.payment.aggregate.autoconfigure

import com.trionesdev.payment.aggregate.AggregatePaymentChannel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AggregatePaymentAutoConfiguration {

    @Bean
    fun aggregatedPaymentFactory(aggregatedPaymentChannels: MutableList<AggregatePaymentChannel>): AggregatePaymentFactory {
        return AggregatePaymentFactory(aggregatedPaymentChannels)
    }

}