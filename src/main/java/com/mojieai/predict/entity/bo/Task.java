package com.mojieai.predict.entity.bo;

import com.mojieai.predict.entity.po.GamePeriod;
import com.mojieai.predict.redis.PeriodRedis;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by Singal
 */
@Data
@NoArgsConstructor
public class Task implements Delayed {
    private Long gameId;
    private String periodId;
    private long trigger;// 时间触发器

    public Task(Long gameId, String periodId) {
        this.gameId = gameId;
        this.periodId = periodId;
        GamePeriod period = PeriodRedis.getPeriodByGameIdAndPeriod(gameId, periodId);
        long delay = period.getAwardTime().getTime() - System.currentTimeMillis();
        delay = delay < 0 ? 0L : delay;
        setDelay(delay);
    }

    public void setDelay(long delay) {
        trigger = System.nanoTime() + TimeUnit.NANOSECONDS.convert(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(trigger - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        Task that = (Task) o;
        if (trigger < that.trigger) {
            return -1;
        }
        if (trigger > that.trigger) {
            return 1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 59;
        result = prime * result + ((gameId == null) ? 0 : gameId.hashCode());
        result = prime * result + ((periodId == null) ? 0 : periodId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof Task) {
            Task otherTask = (Task) anObject;
            if (Objects.equals(gameId, otherTask.getGameId()) && Objects.equals(periodId, otherTask.getPeriodId())) {
                return true;
            }
        }
        return false;
    }
}
