package com.mojieai.predict.constant;

public class PayConstant {

    public static final Boolean SAND_BOX_SWITCH = false;//沙箱开关

    public static final String OUT_TRADE_NO_CN = "transaction_id";//三方交易订单号（支付宝或微信的订单号）
    public static final String TRADE_STATUS_CN = "trade_status";

    /***  支付宝支付相关***/
    public static final String ALI_TRADE_NO_CN = "trade_no";//支付宝订单号

    public static final String ALI_SX_ADDRESS = "https://openapi.alipaydev.com/gateway.do";
    public static final String ALI_ONLINE_ADDRESS = "https://openapi.alipay.com/gateway.do";

    public static final String ALI_SX_APPID = "2016082600315465";
    public static final String ALI_SX_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm6K9jcrz5PiGypyi0+Av3HtBj5QCt3/2wq9S" +
                    "/WtuC1FV7pRWCF6C39tlZQh0xGkEsWvdIBQ2KZf9zyzG5mLcPECZg9J27axbchlzUKDN8UeHtCJIlDSJsaIAJBURJtzpzusDUbctak9FaU+ZTmeSloJcAQAlaJ7hZOLWidLxG7iO5Nx/jGeWjg9K5HrT/mxBeKtK7y5+oC3kAcsxxf3k2Hi65FPQoV0bsqWiajgnj6Dv5NJnpNXF2Bw2/3FyvOYLG9RRnFbtU56tDVYqrkg/iw2qygOqXtuvO2KN/senT27kxE9nzQ+cBSsQLqNxdbIyNtLwSdatZLvgNyl2ZRP7VwIDAQAB";
    public static final String ALI_SX_PRIVATE_KEY =
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCbor2NyvPk+IbKnKLT4C/ce0GPlAK3f" +
                    "/bCr1L9a24LUVXulFYIXoLf22VlCHTEaQSxa90gFDYpl" +
                    "/3PLMbmYtw8QJmD0nbtrFtyGXNQoM3xR4e0IkiUNImxogAkFREm3OnO6wNRty1qT0VpT5lOZ5KWglwBACVonuFk4taJ0vEbuI7k3H+MZ5aOD0rketP+bEF4q0rvLn6gLeQByzHF/eTYeLrkU9ChXRuypaJqOCePoO/k0mek1cXYHDb/cXK85gsb1FGcVu1Tnq0NViquSD+LDarKA6pe2687Yo3+x6dPbuTET2fND5wFKxAuo3F1sjI20vBJ1q1ku+A3KXZlE/tXAgMBAAECggEAXpqc+Hzeplc/sBdojrweu7ocjwccY6igOZVTMAJGgRCu2HhIl4vDqAl35+Ms/1sK5EI5xs8iYUQnnvCL8u0zDFkEN/IOIsj7SS8ZPnCQ3tJpNeEFFzmcXWKpDUXVCDeO1B1dBiX/sDiBa9Nb0CH7JAwXmvAYzTCOt8F/QYX806j2wB+xGHVhG8HmFC6IFQqHvoIurYZG1OxINCyzdPXdvQvRFHUOF9L1x3QgWJEsQSSp1ayt7vFznt1LoU8kZyncqJ/szWZGNO2ySMjsJqNd5Goy2aig5q+EnVwPiXIomFr9RbXRdIIgDK5dBclrWXhxoIAk0MpR/n26OHMnXxXJeQKBgQDgFyZvlHZaeobSsI2E/MqEUaddiZTEmbghBFYPDcu+wPgOjmQ+wrB1h0POqEzA8jn6hWZUZLpG20124DmIHQYQdeK4OEIVyaDRRwm+rOVWak5nWAopfWcGdQPJJQrI9I0FWqd8tkibrDMzDve2gazemgTYjJOouh3wtBR7kW9aswKBgQCxzC9x+SxQOlqHoAstA7xTTM1CMTo0ncbVaTgJrciMmLKSW/m+zxWKNNrdngNaNdT0pp1VnbcW9VjsVIm3n9QHDDGH03qXHX4V01/eMfVmCqJP0Qi8gDZCUq5/0tgsVV8Opam/q+tCwCT4mD4V986h+UrNv6Ug0iB96kpgScs+zQKBgQCF9hDqHyxphlnW1eikGaLPWyjcSAUBSovCYR85PJwSWrVvrjvLMQPgHo6wmffYWHXEh77WsKiS90Llz8FU11Tqvi5cxnTrJW/X68mtjRpEgKSCLyVUDD1spMMwmc5Kt7cd/kWlziq1tUmeOlJnbjnzkMtY0SDseuavIFgmTpjs9QKBgG1/3TCKTxpR9fNVQ8zDYjbDNB6yBrDlAtHobVS4rKy6VjABaTx0eUWW67xVVqc2RCMwgfF6oooHLG7QWci3LHPytIZ8XiG3TjHO7ln/1qyzm+cgTAsbw07xcPrUNcKICXUR3gtnrAp+H8birEoHRkFHMSSqBN7DyrMBmjDXAc95AoGBALliaX70tkA2h1pVw4UT+sD1DCvqWnESs/LjeFGRmZl24Or8iY9pVKXwRE1WeiEC46zI2KEhkLi5Ry/XLSimVemR5uoYveJ9j4di16bImTWfjuqBoaQ/0cH1ucF9xUSbaLau6MWdh1J+Klf2E7CXsDQrvcW+1gZrSrNPez7gXOou";


    public static final String ALI_APP_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA21DXgCCZmy58CLA0whuQLmvTIRH" +
                    "/A63zOJ9W6G4NhAAwuow5K2XijnygIOVAKilWmbQIqB9cLfngX0vV1jtR5luEcwX0" +
                    "/VAAUX20SZu5WlNHB40zdHPiUeLcIQBlaoPP2By1Hw2trApOFwP4913ToMpyBJxB1MFnxjVcbEJaIE8KQo+riRLGSC+S2" +
                    "+tR1xvuu/u6LttIrWmli/CNsytp+x5ugDQMk7lK4eB5zoZjer4bdS04D1d" +
                    "//xJ3s4OYicPNZ2GKBUYk2yEHsXE0MrhkeZFjQagC+u4jXgtnypcNBAm4B67lMP450uWykhHtctHekMJu697eiRMcr" +
                    "+o3bm7pIwIDAQAB";

    public static final String GOODS_TYPE_SUBSCRIBE_PROGRAM = "killProgram";
    public static final String ABSTRACT_PURCHASE_AFTER = "Purchase";

    //订阅10期类的组包类商品 需要主动查询一次
    public static final Integer OUT_TRADE_ORDER_CONFLICT_INIT = 0;//特殊冲突订单状态 0:初始
    public static final Integer OUT_TRADE_ORDER_CONFLICT_CLOSE = 1;//特殊冲突订单状态 1:关闭订单

    public static final Integer OUT_TRADE_ORDER_STATUS_NO_PAY = 0;//未付款
    public static final Integer OUT_TRADE_ORDER_STATUS_PAY_SUCCESS = 1;//成功
    public static final Integer OUT_TRADE_ORDER_STATUS_TRADING = 2;//交易中

    public static final Integer PAY_CHECK_ERROR_CODE = -1;//支付前check失败
    public static final Integer PAY_CHECK_SUCCESS_CODE = 0;//支付前check成功

    public static final Integer CHANNEL_AUTHENTICATE_NO_NEED = 0;
    public static final Integer CHANNEL_AUTHENTICATE_REAL_NAME = 1;
    public static final Integer CHANNEL_AUTHENTICATE_BIND_BANK = 2;
    public static final Integer CHANNEL_AUTHENTICATE_SUCCESS = 3;

    public static final String H5_PAY_FORM_FORM_URL = "https://predictapi.mojieai.com/web/jdPay/index.html";
}
