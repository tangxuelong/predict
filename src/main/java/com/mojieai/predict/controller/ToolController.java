package com.mojieai.predict.controller;

import com.mojieai.predict.cache.GameCache;
import com.mojieai.predict.entity.po.Game;
import com.mojieai.predict.service.ButtonOrderedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/tools")
public class ToolController extends BaseController {

    @Autowired
    private ButtonOrderedService buttonOrderedService;

    @RequestMapping("/index")
    @ResponseBody
    public Object index(@RequestParam String gameEn, @RequestAttribute(required = false) String versionCode,
                        @RequestAttribute Integer clientType) {

        Game game = GameCache.getGame(gameEn);
        if (game == null) {
            return buildErrJson("彩种不存在");
        }

        Map<String, Object> result = buttonOrderedService.getToolsIndexButtons(game.getGameId(), versionCode, clientType);
        return buildSuccJson(result);
    }
}
