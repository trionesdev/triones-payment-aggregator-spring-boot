package com.trionesdev.payment.aggregated.wechatpay.autoconfigure

class TransactionNotifyVO {
    var code: String? = null
    var message: String? = null

    constructor()

    constructor(code: String, message: String?) {
        this.code = code
        this.message = message
    }
}