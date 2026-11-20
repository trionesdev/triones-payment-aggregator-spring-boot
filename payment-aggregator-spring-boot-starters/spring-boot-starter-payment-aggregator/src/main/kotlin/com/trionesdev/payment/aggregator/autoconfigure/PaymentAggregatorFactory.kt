package com.trionesdev.payment.aggregator.autoconfigure

import com.trionesdev.payment.aggregator.PaymentChannel
import com.trionesdev.payment.aggregator.PaymentComponent
import com.trionesdev.payment.aggregator.shared.model.*
import jakarta.annotation.PostConstruct
import org.apache.commons.collections4.CollectionUtils
import org.springframework.core.annotation.AnnotationUtils


class PaymentAggregatorFactory(var paymentChannels: MutableList<PaymentChannel>) {

    var channelsMap: MutableMap<String, PaymentChannel> = HashMap()

    @PostConstruct
    fun init() {
        if (CollectionUtils.isNotEmpty(paymentChannels)) {
            for (payment in paymentChannels) {
                val paymentComponent =
                    AnnotationUtils.getAnnotation(payment.javaClass, PaymentComponent::class.java)
                paymentComponent?.let { component ->
                    channelsMap.put(component.channel, payment)
                }
            }
        }
    }

    fun createOrder(request: CreateOrderRequest): CreateOrderResponse {
        return channelsMap[request.channel!!]!!.createOrder(request)
    }

    fun closeOrder(request: CloseOrderRequest) {
        channelsMap[request.channel!!]!!.closeOrder(request)
    }

    fun createRefund(request: CreateRefundRequest): CreateRefundResponse {
        return channelsMap[request.channel!!]!!.createRefund(request)
    }

    fun createTransfer(request: CreateTransferRequest): CreateTransferResponse {
        return channelsMap[request.channel!!]!!.createTransfer(request)
    }
}