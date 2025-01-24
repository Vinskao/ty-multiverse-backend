這是我的個人網站的 Backend。

Astro -> Axios -> Spring Boot & SQL Server

## swagger ui

```bash
https://localhost:8443/tymb/swagger-ui/index.html#/
https://peoplesystem.tatdvsonorth.com/tymb/swagger-ui/index.html#/
```

## image

```bash
mvn clean package -DskipTests
mvn -P platform install
docker build -t papakao/ty-multiverse-backend:latest .
docker push papakao/ty-multiverse-backend:latest

mvn -P platform install
docker build -t ty-multiverse-backend .
docker run -e SPRING_DATASOURCE_URL="jdbc:postgresql://peoplesystem.tatdvsonorth.com:30000/peoplesystem" \
           -e SPRING_DATASOURCE_USERNAME="wavo" \
           -e SPRING_DATASOURCE_PASSWORD="Wawi247525=" \
           -p 8080:8080 ty-multiverse-backend

```

## env

```bash
source /Users/vinskao/001-project/TY-Multiverse-Backend/src/main/resources/env/env.local
source /Users/vinskao/001-project/TY-Multiverse-Backend/src/main/resources/env/env.platform
mvn -P platform package
```

## https
```bash
keytool -genkeypair -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 3650 -alias palais
```