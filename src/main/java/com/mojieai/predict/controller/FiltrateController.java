package com.mojieai.predict.controller;

/**
 * Created by tangxuelong on 2017/8/7.
 */

import com.mojieai.predict.constant.CommonConstant;
import com.mojieai.predict.service.FiltrateService;
import com.mojieai.predict.service.filter.Filter;
import com.mojieai.predict.service.filter.FilterFactory;
import com.mojieai.predict.service.game.GameFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@RequestMapping("/filter")
@Controller
public class FiltrateController extends BaseController {
    @Autowired
    private FiltrateService filtrateService;

    @RequestMapping("/index")
    @ResponseBody
    public Object index(@RequestParam String gameEn) throws Exception {
        /* 得到一个过滤条件结果集*/
        Filter filter = FilterFactory.getInstance().getFilter(gameEn);
        Map<String, Object> filterIndexShow = filter.getFilterIndexShow(gameEn);
        return buildSuccJson(filterIndexShow);
    }

    @RequestMapping("/")
    @ResponseBody
    public Object index(@RequestParam String gameEn, @RequestParam String lotteryNumber, @RequestParam(required =
            false) String action, @RequestParam(required = false) String matrixAction, @RequestParam(required =
            false) Integer pageIndex) throws Exception {
        /* 过滤，得到一个结果集*/
        if (!checkLotteryNumber(lotteryNumber, gameEn)) {
            return buildErrJson("号码格式错误！");
        }
        Filter filter = FilterFactory.getInstance().getFilter(gameEn);
        if (null != matrixAction) {
            String errMsg = filter.checkMatrixAction(matrixAction, lotteryNumber);
            if (StringUtils.isNotBlank(errMsg)) {
                return buildErrJson(errMsg);
            }
        }
        Map<String, Object> resultMap = filter.getFilterResult(gameEn, lotteryNumber, action, matrixAction, pageIndex);
        return buildSuccJson(resultMap);
    }

    private boolean checkLotteryNumber(String lotteryNumberStr, String gameEn) {
        String[] numArray = lotteryNumberStr.split(CommonConstant.COMMON_ESCAPE_STR + CommonConstant.COMMA_SPLIT_STR);
        for (String lotteryNumber : numArray) {
            boolean result = GameFactory.getInstance().getGameBean(gameEn).checkLotteryNumberIfValid(lotteryNumber);
            if (!result) {
                return result;
            }
        }
        return Boolean.TRUE;
    }
}
