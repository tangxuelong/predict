package com.mojieai.predict.entity.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class JDWithdrawCallBackParam implements Serializable {
    private static final long serialVersionUID = -3884305441846527220L;

    private String sign_type;
    private String sign_data;
    private String trade_no;
    private String merchant_no;
    private String notify_datetime;
    private String bank_code;
    private String customer_no;
    private String out_trade_no;
    private String trade_class;
    private String trade_status;
    private String pay_tool;
    private String trade_pay_time;
    private String is_success;
    private String card_type;
    private String buyer_info;
    private String trade_subject;
    private String trade_pay_date;
    private String trade_finish_time;
    private String seller_info;
    private String trade_amount;
    private String trade_finish_date;
    private String refund_amount;
    private String category_code;
    private String trade_currency;

    @Override
    public String toString() {
        return "JDWithdrawCallBackParam{" +
                "sign_type='" + sign_type + '\'' +
                ", sign_data='" + sign_data + '\'' +
                ", trade_no='" + trade_no + '\'' +
                ", merchant_no='" + merchant_no + '\'' +
                ", notify_datetime='" + notify_datetime + '\'' +
                ", bank_code='" + bank_code + '\'' +
                ", customer_no='" + customer_no + '\'' +
                ", out_trade_no='" + out_trade_no + '\'' +
                ", trade_class='" + trade_class + '\'' +
                ", trade_status='" + trade_status + '\'' +
                ", pay_tool='" + pay_tool + '\'' +
                ", trade_pay_time='" + trade_pay_time + '\'' +
                ", is_success='" + is_success + '\'' +
                ", card_type='" + card_type + '\'' +
                ", buyer_info='" + buyer_info + '\'' +
                ", trade_subject='" + trade_subject + '\'' +
                ", trade_pay_date='" + trade_pay_date + '\'' +
                ", trade_finish_time='" + trade_finish_time + '\'' +
                ", seller_info='" + seller_info + '\'' +
                ", trade_amount='" + trade_amount + '\'' +
                ", trade_finish_date='" + trade_finish_date + '\'' +
                ", refund_amount='" + refund_amount + '\'' +
                ", category_code='" + category_code + '\'' +
                ", trade_currency='" + trade_currency + '\'' +
                '}';
    }
}
