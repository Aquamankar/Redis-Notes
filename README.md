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
