package com.mojieai.predict.service;

import com.mojieai.predict.entity.bo.GoldTask;
import com.mojieai.predict.entity.bo.JDWithdrawCallBackParam;
import com.mojieai.predict.entity.bo.PrePayInfo;
import com.mojieai.predict.entity.po.UserAccount;
import com.mojieai.predict.entity.po.UserAccountFlow;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by tangxuelong on 2017/12/11.
 */
public interface PayService {

    Map<String, Object> payCreateFlow(Long userId, String payId, Long totalAmount, Integer payType, Integer
            channel, Long payAmount, String payDesc, String clientIp, Integer clientId, String clazzMethodName,
                                      Integer operateType, Integer bankId);

    // 创建支付流水
    Map<String, Object> payCreateFlow(Long userId, String payId, Long totalAmount, Integer payType, Integer
            channel, Long payAmount, String payDesc, String clientIp, Integer clientId, String clazzMethodName,
                                      Integer operateType, Integer bankId, String wxCode);

    UserAccountFlow transCreatePayFlow(Long userId, String payId, Long totalAmount, Integer payType, Integer
            channel, Long payAmount, String payDesc, String clientIp, Integer clientId, String clazzMethodName,
                                       Map<String, Object> payInfo, Integer operateType, Integer bankId, String openId);

    Map<String, Object> fillAccount(Long userId, String payId, Long totalAmount, Integer payType, Integer
            channel, Long payAmount, String payDesc, String clientIp, Integer clientId);

    // 微信支付回调
    String wxPayCallBack(HttpServletRequest request);

    String aliPayCallBack(HttpServletRequest request);

    String yopPayCallBack(HttpServletRequest request);

    String jdPayCallBack(HttpServletRequest request);

    String jdWithdrawCallBack(JDWithdrawCallBackParam request);

    String haoDianCallBack(HttpServletRequest request);

    LinkedBlockingQueue<UserAccountFlow> FLOW_LIST_QUEUE = new LinkedBlockingQueue<>();

    // 微信订单查询
    void wxOrderQuery(UserAccountFlow userAccountFlow);

    // 苹果支付验证
    Map applePayValidate(String flowId, String receipt);

    // 获取支付渠道列表
    Map<String, Object> getChannelList(Long userId, Integer clientId, Integer versionCode);

    // 兑换商城
    Map<String, Object> getExchangeMall(Long userId);

    // 金币流水
    Map<String, Object> flowList(Long userId, Integer payType, Integer page);

    // 金币兑换
    Map<String, Object> exchangeItem(Long userId, Integer itemId, String clientIp, Integer clientId);

    Map<String, Object> getAccessIdByType(String type, Long gameId);

    // 如何赚金币
    Map<String, Object> taskList(Long userId, Integer versionCode, Integer clientType);

    GoldTask getTaskMap(String type);

    Boolean checkUserAccess(Long userId, Long gameId, String periodId, Integer accessId);

    // 金币余额事务
    void operateAccountDec(Long userId, Long payAmount, Map<String, Object> payInfo, UserAccountFlow userAccountFlow,
                           Integer payType);

    void operateAccountAdd(Long userId, Long payAmount, Map<String, Object> payInfo, UserAccountFlow userAccountFlow);

    UserAccount getUserAccount(Long userId, Integer accountType, Boolean isLock);

    void handledFlow(String flowId);

    String generateFlowId(Long userId);

    void wxNotifySuccessBusinessErrorCompensateTiming();

    void paySuccessDo(UserAccountFlow userAccountFlow) throws Exception;

    Map<String, Object> checkFlowIdStatus(String flowId);

    // 支付弹窗
    Map<String, Object> getConfirmPayPopInfo(Long userId, PrePayInfo prePayInfo, Integer clientId, Integer versionCode);

    Integer checkOrderOutTradeStatus(String subscribeId, Integer payType, Long userId);

    Map<String, Object> manualWithdraw(Long userId, Long withdrawAmount, Long operateUserId);
}
