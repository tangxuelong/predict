package com.mojieai.predict.redis.refresh.handler.sub;

public abstract class RedisSubscribeHandler
{
	public abstract void handle(String message);
}
