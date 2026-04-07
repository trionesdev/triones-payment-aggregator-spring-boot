package com.trionesdev.payment.aggregate.autoconfigure

import com.trionesdev.payment.aggregate.AggregatePaymentChannel
import com.trionesdev.payment.aggregate.PaymentComponent
import com.trionesdev.payment.aggregate.shared.model.*
import jakarta.annotation.PostConstruct
import org.apache.commons.collections4.CollectionUtils
import org.springframework.core.annotation.AnnotationUtils


class AggregatePaymentFactory(var aggregatedPaymentChannels: MutableList<AggregatePaymentChannel>) {

    var channelsMap: MutableMap<String, AggregatePaymentChannel> = HashMap()

    @PostConstruct
    fun init() {
        if (CollectionUtils.isNotEmpty(aggregatedPaymentChannels)) {
            for (payment in aggregatedPaymentChannels) {
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