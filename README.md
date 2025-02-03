# Secured REST API with Spring Boot

This guide will walk you through setting up Spring Boot based security for your REST API endpoints.
It will only focus on the security part, all others are assumed to be known by the reader.
## Prerequisites

- A basic understanding of Spring Boot and REST APIs.
- Java 11+
- Spring Boot 3+
- Maven for dependency management

## 1. Create a basic Spring Boot WEB Application using [Spring Initializr](https://start.spring.io/)

## 2. Add the following dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```
These are the latest versions at the time of creating this repository. 
You can check them when implementing if there are new versions released for some dependencies.


## 3. Implement Security Configuration

### 3.1 Define SecurityConfiguration
Create a Java configuration class, preferably to name it SecurityConfiguration. You should annotate this with `@EnableWebSecurity`.
Inside of this class you should declare a @Bean of type `SecurityFilterChain`, which will have as a parameter an object of type `HttpSecurity`.
You can define your security config inside this bean.

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register").permitAll()    // Public endpoints
                        .anyRequest().authenticated())                         // All other endpoints should be secured
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
```
Here we used a custom filter `JwtAuthenticationFilter` because we want to use JWT in our request.
Now let's see how to create this custom filter.

### 3.2 Create custom Security Filter
In order to create a custom filter, our class needs to extend `OncePerRequestFilter` and override `void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)`
Inside this method we should specify what the filter should do. We want to check if there is a token in every request and to validate it.
So we will look in the request header for the key: `Authorization` and this should have as a value something like `Bearer *token*`.
We will extract the token from the header and we will check if the token is valid. If it is valid we will autheticate the user, in any other case we will forbid the request.
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        var jwt = authHeader.substring(7);
        var email = jwtService.extractUsername(jwt);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var userDetails = userDetailsService.loadUserByUsername(email);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```
### 3.3 Define a JWT service
You can define a JWTService class which should handle the tokens. By making use of the library `io.jsonwebtoken`
you can create some helper methods for decoding, encoding, generation and checking a jwt token. You can take a
look here [JWTService](src/main/java/com/toie/securedRestApiTutorial/service/JwtService.java)

### 3.4 Define some application config
You should declare a configuration class where you should override some beans:
```java
 @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
```
We want to encode the password when storing them, and in this case we will use a `BCryptPasswordEncoder`.

---

```java
    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
```
By default spring will use the UserDetailsService inside its security configuration
and in our case we want to override the `loadByUsername` method to return our user stored in our repository.
The repository used there can be a repository created by yourself where you can define 
how to store the user data (in a db, in memory, etc.)

---

```java
    @Bean
    public AuthenticationProvider authenticationProvider(){
        var authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
```
You need to specify in the `AuthenticationProvider` the `userDetailsService` used and the `passwordEncoder`.



## 4. Create the controller
Define a basic REST controller with 3 endpoints:
- register (Based on an email and password return a token)
- login (Based on an email and password, check them if they already exist, if yes return the token)
- demo (For demo purposes, return just a string if the token is correct)

## 4. Create the service
- For the register method you should just store the email and password you received from the request.
Don't forget to make use of the `PasswordEncoder` before storing the password (eg: `passwordEncoder.encode(password)`).
Generate a new token using the JWTService and return it.
- For the login method you just need to tell Spring to authenticate the user and generate the token to return it as a response. 
You know for sure that the token is correct because the `JwtAuthenticationFilter` will check this before
the code from your controller will be executed.

## 9. Set the JWT secret

You should define a secret for your JWT generation. That one should be kept secret and not pushed on git.
I have defined it as example in plain text in application.yaml, but it is strongly recommended to NOT do the same.

## Conclusion

You have successfully set up a secured REST API with Spring Boot! 🚀
