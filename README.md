# 🧠 Redis Caching in Spring Boot (Monolith)

This guide shows how to implement **Redis-based caching** in a monolithic Spring Boot application to reduce database hits and improve performance.

---

## ✅ Why Redis?

- In-memory = blazing fast
- Key-Value store
- Supports automatic expiration (TTL)
- Great for caching DB results, API responses, etc.

---

## ⚙️ Technologies Used

- Spring Boot
- Redis
- Spring Data Redis
- Spring Caching

---

## 🧰 Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```


## Enable Caching 

```
@SpringBootApplication
@EnableCaching
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}


```


## Service layer add @Cache()

```
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "users", key = "#userId")
    public User getUserById(Long userId) {
        System.out.println("Fetching from DB...");
        return userRepository.findById(userId).orElse(null);
    }

    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}

```

## Before starting application run docker with this command
```
docker run -d --name redis-server -p 6379:6379 redis

```



# ✅ Redis as Rate Limiter – Overview
Rate Limiting is used to control the number of requests a client can make to a server in a given time window. Redis is ideal for this due to its:

Fast performance (in-memory)

TTL (Time-To-Live) capabilities

Atomic operations with Lua scripting

🧱 In Monolithic Architecture
🔧 Implementation Logic
Approach: Use an Interceptor or Filter before the Controller.

🚦 Use Case:
Limit: 5 requests per user/IP per minute.

🧪 Key Logic:
java
```
String key = "rate_limit:" + userId;
Long current = redisTemplate.opsForValue().increment(key, 1);
if (current == 1) {
    redisTemplate.expire(key, Duration.ofMinutes(1));
}
if (current > 5) {
    throw new RateLimitExceededException();
}
```
✅ Advantages:
Simple to plug into Spring Boot using RedisTemplate.

Centralized logic inside the monolith.

🧱 In Microservice Architecture
📌 Where to Apply:
At API Gateway or each service (usually better at the Gateway level).

🔧 Implementation Options:
Spring Cloud Gateway + Redis

API Gateway (Kong/Nginx/APIGEE) + Redis

Use Redis Lua script for atomic counter + TTL.

🚦 Gateway Rate Limiting with Redis:
Use Spring Cloud Gateway built-in rate limiter:

yaml


```
spring:
  cloud:
    gateway:
      routes:
        - id: user_route
          uri: http://localhost:8081
          predicates:
            - Path=/api/user/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 5
                redis-rate-limiter.burstCapacity: 10
                key-resolver: "#{@userKeyResolver}"

 ```            
🔍 Key Resolver Bean:
java


```
@Bean
public KeyResolver userKeyResolver() {
    return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst("X-User-ID"));
}
```
💡 Advanced: Use Lua Script in Redis
Ensures atomicity:
lua
```
local current = redis.call("INCR", KEYS[1])
if current == 1 then
  redis.call("EXPIRE", KEYS[1], ARGV[1])
end
return current
```
🔁 Monolithic vs Microservice - Comparison
Feature	Monolith	Microservices / Gateway
Setup Scope	Single app	Per service or centralized at gateway
Redis Role	In-app rate counter	Shared distributed rate counter
Scalability	Limited (single node)	Scales horizontally
TTL Handling	Localized	Centralized in Redis
Best Use Case	Simple apps	Cloud-native apps, multi-client

📦 Tools/Libraries
Tool	Use
RedisTemplate	Java Redis access in Spring Boot
Bucket4j + Redis	Token Bucket implementation
Spring Cloud Gateway	Rate Limiting via Filters
Lua Scripts	Atomic increments with TTL

🛠 Best Practices
Use user ID or IP address as Redis key suffix.

Always expire keys to free memory.

Use Redis Cluster or Sentinel for production.

Use Burst Capacity to allow temporary spikes.

Add retry-after headers in rate limit response.

🧪 Example Redis Key:
makefile
```
rate_limit:user:123     => 5 (expires in 60s)

```
