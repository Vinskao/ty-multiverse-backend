這是我的個人網站的 Backend。

Astro -> Axios -> Spring Boot & SQL Server

## swagger ui

```bash
http://localhost:8080/tymb/swagger-ui/index.html#/
https://peoplesystem.tatdvsonorth.com/tymb/swagger-ui/index.html#/
```

## image

```bash
mvn clean package -DskipTests
docker buildx build --platform linux/arm64 -t papakao/ty-multiverse-backend:latest --push .
mvn -P platform install
docker build -t papakao/ty-multiverse-backend:latest .
docker push papakao/ty-multiverse-backend:latest


mvn -P platform install
docker build -t ty-multiverse-backend .
docker run -d --name ty-multiverse-backend `
  -e "SPRING_PROFILES_ACTIVE=platform" `
  -e "URL_BACKEND=http://localhost:8080/tymb" `
  -e "SPRING_DATASOURCE_URL=jdbc:postgresql://peoplesystem.tatdvsonorth.com:30000/peoplesystem" `
  -e "SPRING_DATASOURCE_USERNAME=wavo" `
  -e "SPRING_DATASOURCE_PASSWORD=Wawi247525=" `
  -p 8080:8080 `
  ty-multiverse-backend

```