package com.mojieai.predict.service.impl;

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.constant.LogConstant;
import com.mojieai.predict.dao.AllProductBillDao;
import com.mojieai.predict.dao.UserAccountFlowDao;
import com.mojieai.predict.entity.po.AllProductBill;
import com.mojieai.predict.entity.po.UserAccountFlow;
import com.mojieai.predict.enums.BillEnum;
import com.mojieai.predict.service.AllProductBillService;
import com.mojieai.predict.service.beanself.BeanSelfAware;
import com.mojieai.predict.util.BillUtil;
import com.mojieai.predict.util.CommonUtil;
import com.mojieai.predict.util.DateUtil;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AllProductBillServiceImpl implements AllProductBillService, BeanSelfAware {
    protected Logger log = LogConstant.commonLog;
    @Autowired
    private AllProductBillDao allProductBillDao;
    @Autowired
    private UserAccountFlowDao userAccountFlowDao;

    private AllProductBillService self;


    @Override
    public Map<String, Object> getAllProductBills(Integer beginDate, Integer endDate) {
        Map<String, Object> res = new HashMap<>();
        List<AllProductBill> productBillList = allProductBillDao.getAllProductBills(beginDate, endDate);
        Map<Integer, List<AllProductBill>> dateBills = new HashMap<>();
        for (AllProductBill bill : productBillList) {
            List<AllProductBill> products = null;
            Integer key = bill.getDateNum();
            if (dateBills.containsKey(key)) {
                products = dateBills.get(key);
            } else {
                products = new ArrayList<>();
            }
            products.add(bill);
            dateBills.put(key, products);
        }
        //1.统计每天的数据
        List<Map<String, Object>> products = new ArrayList<>();
        for (Map.Entry<Integer, List<AllProductBill>> enter : dateBills.entrySet()) {
            Map<String, Object> temp = new HashMap<>();
            temp = statisticDateProduct(enter.getKey(), enter.getValue());
            if (!temp.isEmpty()) {
                products.add(temp);
            }
        }

        res.put("orders", products);
        return res;
    }

    private Map<String, Object> statisticDateProduct(Integer dateNum, List<AllProductBill> value) {
        Map<String, Object> res = new HashMap<>();
        //1.统计总金额
        Long totalAmount = 0l;
        Integer payPersonNum = 0;
        Integer cumulatePersonNum = 0;
        Long totalCumulateAmount = 0l;
        Long vipAmount = 0l;
        Long programAmount = 0l;
        Long wisdomAmount = 0l;
        Long subscribeKillAmount = 0l;
        Long resonanceKillAmount = 0l;

        //2.统计总人数
        for (AllProductBill bill : value) {
            Long amount = bill.getAmount() == null ? 0l : bill.getAmount();
            Integer personNum = bill.getPayPersonNum() == null ? 0 : bill.getPayPersonNum();
            Integer cumulatePerson = bill.getCumulatePayPersonNum() == null ? 0 : bill.getCumulatePayPersonNum();
            Long cumulateAmount = bill.getCumulateAmount() == null ? 0l : bill.getCumulateAmount();

            totalAmount += amount;
            payPersonNum += personNum;
            cumulatePersonNum += cumulatePerson;
            totalCumulateAmount += cumulateAmount;

            //2.1
            if (bill.getOrderType().equals(CommonConstant.BILL_PRODUCT_TYPE_VIP)) {
                vipAmount += amount;
            } else if (bill.getOrderType().equals(CommonConstant.BILL_PRODUCT_TYPE_PROGRAM)) {
                programAmount += amount;
            } else if (bill.getOrderType().equals(CommonConstant.BILL_PRODUCT_TYPE_WISDOM)) {
                wisdomAmount += amount;
            } else if (bill.getOrderType().equals(CommonConstant.BILL_PRODUCT_TYPE_SUBSCRIBE_KILL)) {
                subscribeKillAmount += amount;
            } else if (bill.getOrderType().equals(CommonConstant.BILL_PRODUCT_TYPE_RESONANCE_DATA)) {
                resonanceKillAmount += amount;
            }
        }
        String tempPayPersonNum = CommonUtil.multiply(payPersonNum + "", "100").toString();
        String arpu = CommonUtil.divide(totalAmount + "", tempPayPersonNum, 2);
        //3.计算占比
        String vipPercent = "0%";
        String programPercent = "0%";
        String wisdomPercent = "0%";
        String subscribeKillPercent = "0%";
        String resonanceKillPercent = "0%";
        if (totalAmount != 0l) {
            vipPercent = CommonUtil.calculatePercent(vipAmount.intValue(), totalAmount.intValue());
            programPercent = CommonUtil.calculatePercent(programAmount.intValue(), totalAmount.intValue());
            wisdomPercent = CommonUtil.calculatePercent(wisdomAmount.intValue(), totalAmount.intValue());
            subscribeKillPercent = CommonUtil.calculatePercent(subscribeKillAmount.intValue(), totalAmount.intValue());
            resonanceKillPercent = CommonUtil.calculatePercent(resonanceKillAmount.intValue(), totalAmount.intValue());
        }

        res.put("dateNum", dateNum);
        res.put("payPersonNum", payPersonNum);
        res.put("amount", CommonUtil.convertFen2Yuan(totalAmount));
        res.put("cumulatePersonNum", cumulatePersonNum);
        res.put("cumulateAmount", CommonUtil.convertFen2Yuan(totalCumulateAmount));
        res.put("arpu", arpu);
        res.put("vipAmount", CommonUtil.convertFen2Yuan(vipAmount));
        res.put("programAmount", CommonUtil.convertFen2Yuan(programAmount));
        res.put("wisdomAmount", CommonUtil.convertFen2Yuan(wisdomAmount));
        res.put("subscribeKillAmount", CommonUtil.convertFen2Yuan(subscribeKillAmount));
        res.put("resonanceKillAmount", CommonUtil.convertFen2Yuan(resonanceKillAmount));
        res.put("vipPercent", vipPercent);
        res.put("programPercent", programPercent);
        res.put("wisdomPercent", wisdomPercent);
        res.put("subscribeKillPercent", subscribeKillPercent);
        res.put("resonanceKillPercent", resonanceKillPercent);
        return res;
    }

    @Override
    public void statisticCashPurchaseProduct(Long userId, String flowId) {
        //1.获取支付流水信息
        UserAccountFlow flow = userAccountFlowDao.getUserFlowByShardType(flowId, userId, false);

        if (flow == null || flow.getStatus() == null || !flow.getStatus().equals(CommonConstant.PAY_STATUS_HANDLED)) {
            return;
        }
        String method = BillUtil.getCallBackMethodStr(flow.getRemark());
        BillEnum billEnum = BillEnum.getBillEnumByCallBackMethod(method);
        if (billEnum == null) {
            log.error("BillEnum not exist. remark method:" + method);
            return;
        }
        //2.统计购买人数
        boolean personCount = true;
        Timestamp beginTime = DateUtil.getBeginOfOneDay(flow.getCreateTime());
        Integer count = userAccountFlowDao.getUserFlowCountByUserIdAndTime(userId, beginTime, flow.getCreateTime());

        if (count > 0) {
            personCount = false;
        }

        //3.依据方案获取所属天和订单类型
        Integer dateNum = CommonUtil.getIntOfTime(flow.getCreateTime());
        Integer orderType = billEnum.getProductType();
        AllProductBill productBill = allProductBillDao.getProductBillByPk(dateNum, orderType, false);
        if (productBill == null) {
            productBill = new AllProductBill();
            productBill.setOrderType(orderType);
            productBill.setDateNum(dateNum);
            Integer lastDateNum = CommonUtil.getIntOfTime(DateUtil.getIntervalDays(flow.getCreateTime(), -1));
            AllProductBill lastProductBill = allProductBillDao.getProductBillByPk(lastDateNum, orderType, false);
            if (lastProductBill != null) {
                productBill.setCumulateAmount(lastProductBill.getCumulateAmount());
                productBill.setCumulatePayPersonNum(lastProductBill.getCumulatePayPersonNum());
            }
            try {
                allProductBillDao.insert(productBill);
            } catch (DuplicateKeyException e) {
                log.info("dateNum:" + dateNum + " orderType:" + orderType + " has been save");
            }
        }
        //4.更新
        self.updateAllProductBill(personCount, productBill.getDateNum(), productBill.getOrderType(), flow
                .getPayAmount());

    }

    @Transactional
    @Override
    public void updateAllProductBill(boolean personAdd, Integer dateNum, Integer orderType, Long amount) {
        AllProductBill productBill = allProductBillDao.getProductBillByPk(dateNum, orderType, true);

        if (personAdd) {
            Integer payPersonNum = productBill.getPayPersonNum() == null ? 0 : productBill.getPayPersonNum();
            Integer cumulatePayPersonNum = productBill.getCumulatePayPersonNum() == null ? 0 : productBill
                    .getCumulatePayPersonNum();
            productBill.setPayPersonNum(payPersonNum + 1);
            productBill.setCumulatePayPersonNum(cumulatePayPersonNum + 1);
        }

        Long amountAll = productBill.getAmount() == null ? 0 : productBill.getAmount();
        Long cumulateAmountAll = productBill.getCumulateAmount() == null ? 0 : productBill.getCumulateAmount();
        productBill.setAmount(amountAll + amount);
        productBill.setCumulateAmount(cumulateAmountAll + amount);

        allProductBillDao.update(productBill);
    }


    @Override
    public void setSelf(Object proxyBean) {
        self = (AllProductBillService) proxyBean;
    }
}
