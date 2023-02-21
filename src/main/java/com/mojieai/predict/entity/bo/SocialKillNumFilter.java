package com.mojieai.predict.entity.bo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
public class SocialKillNumFilter {
    private Integer partakeCount;
    private String encircleCount;
    private String killNumCount;

    private Integer filterNoDataFlag = 0;//1:过滤将数据过滤完了 0:没有围号

    public SocialKillNumFilter(Integer partakeCount, String encircleCount, String killNumCount) {
        this.partakeCount = partakeCount;
        this.encircleCount = encircleCount;
        this.killNumCount = killNumCount;
    }

    public boolean ifNeedFilter() {
        if (this.partakeCount != null || StringUtils.isNotBlank(this.encircleCount) || StringUtils.isNotBlank(this
                .killNumCount)) {
            return true;
        }
        return false;
    }

}
