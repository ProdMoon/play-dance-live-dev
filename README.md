# PLAY DANCE LIVE!

![JS badge](https://img.shields.io/badge/-Javascript-grey?logo=javascript)
![React badge](https://img.shields.io/badge/-React-grey?logo=react)
![Springboot badge](https://img.shields.io/badge/-Springboot-grey?logo=springboot)
![Redis badge](https://img.shields.io/badge/-Redis-grey?logo=redis)
![MySQL badge](https://img.shields.io/badge/-MySQL-grey?logo=mysql)

SW사관학교 정글 5기 **Team Project** *(2022.12.22 ~ 2023.1.28)*

---LOGO---

<br>

# 서비스 소개

- 기본 소개
- 진행 방식
- 시연 영상
- 포스터

<br>

# 사용된 기술

- 아키텍쳐

<br>

# How To Deploy

## 배포 환경

AWS EC2 하나에 Frontend, Backend, Openvidu Deployment를 모두 올렸습니다.

```
서버 사양:

    AWS EC2 t2.medium (2 vcpu, 4 GiB)

    ubuntu 22.04 LTS (amd64)

    22GiB storage

Openvidu v2.25.0:

    Installation Mode: On Premises

Front-end:

    React v17.0.1 created by create-react-app

Back-end:

    Java SpringBoot v2.7.7 (Java 11)
```

기본적으로 openvidu 공식 문서의 [Deployment on premises](https://docs.openvidu.io/en/stable/deployment/ce/on-premises)의 내용대로 진행한 후에 다음 내용을 참고합니다.

- 여러분이 공식문서를 잘 따라왔다면, 다음 항목들이 완료되었을 것입니다.

    - Openvidu를 EC2에 잘 설치했습니다.

    - /opt/openvidu/.env 파일을 올바르게 설정했습니다.
    (아래의 [도메인과 SSL 환경설정](https://github.com/ProdMoon/play-dance-live-dev#%EB%8F%84%EB%A9%94%EC%9D%B8%EA%B3%BC-ssl-%ED%99%98%EA%B2%BD%EC%84%A4%EC%A0%95) 부분을 참고하세요)

- 그리고 여러분의 frontend 코드와 backend(application server) 코드를 약간 수정해주어야 합니다.

    - frontend/.env.production

        ```
        REACT_APP_HOST=domain.com
        ```
        
        `domain.com` 대신 여러분의 도메인 주소를 넣으세요.
        
    - backend에서 openvidu 관련 설정과 서버 포트 설정이 있을 겁니다. 서버 포트는 `5000`번으로 수정해주시고, `OPENVIDU_URL`은 다음과 같이 설정해 주세요.
        
        ```
        OPENVIDU_URL: http://localhost:5443/
        ```

        > **Note**
        >
        > 여러분이 코드를 이해했다면, 서버 포트를 `5000`번이 아닌 다른 포트로 매핑해도 상관없습니다.
        
- 그리고 이제 nginx 환경설정을 ~~약간~~ 수정해야 합니다.

    - [Openvidu 공식 문서: Modify nginx configuration](https://docs.openvidu.io/en/2.25.0/troubleshooting/#162-modify-openvidu-nginx-configuration)의 내용을 참고하며 진행했습니다.

    - `./openvidu start` 를 해서 openvidu와 nginx가 실행되고 있는 상태로 만듭니다.
    - 아래 코드를 실행하면 `custom-nginx.conf`, `nginx.conf` 파일이 `/opt/openvidu` 경로에 만들어집니다.
        
        ```bash
        sudo su
        cd /opt/openvidu
        docker-compose exec nginx cat /etc/nginx/conf.d/default.conf > custom-nginx.conf
        docker-compose exec nginx cat /etc/nginx/nginx.conf > nginx.conf
        ```
        
    - 이제 vi로 `custom-nginx.conf`를 수정해봅시다. (sudo 권한이 필요할 겁니다)
        
        많은 코드들이 있을텐데, yourapp과 관련된 설정만 수정해 보겠습니다.
        
        ```bash
        # Your App
        upstream yourapp {
            server localhost:5442;
        }
        
        upstream openviduserver {
            server localhost:5443;
        }
        
        # ...생략
        
        server {
            listen 443 ssl;
            listen [::]:443 ssl;
            server_name domain.com;
        
            # ...생략
        
            # Your App
            location / {
                proxy_pass http://yourapp;  #your app
            }
        
            # ...생략
        }
        ```
        
        기본적으로 `location /` 에 대해 `5442` 포트로 proxy_pass 되어 있습니다.
        
        저희는 react의 빌드된 파일을 node 서버가 5442번 포트에서 듣도록 하고, springboot 백엔드 서버를 tomcat이 5000번 포트에서 듣도록 설정했습니다.
        
        그리고 백엔드로의 요청은 모두 `$host/api/…` 형태가 되도록 작성했습니다.
        
        이에 맞게 파일을 수정해 보겠습니다.
        
        ```bash
        # Your App
        upstream frontendserver {
            server localhost:5442;
        }
        
        upstream openviduserver {
            server localhost:5443;
        }
        
        upstream backendserver {
            server localhost:5000;
        }
        
        # ...생략
        
        server {
            listen 443 ssl;
            listen [::]:443 ssl;
            server_name domain.com;
        
            # ...생략
        
            # Your App
            location / {
                proxy_pass http://frontendserver;
            }
        
            location /api/ {
                proxy_pass http://backendserver;
            }
        
            # ...생략
        }
        ```
        
    - 이제 `/opt/openvidu/docker-compose.yml` 파일에 다음 라인들을 추가해주고 openvidu를 재시작하면 됩니다.
        
        ```yaml
        nginx:
                ...
                volumes:
                    ...
                    - ./custom-nginx.conf:/custom-nginx/custom-nginx.conf
                    - ./nginx.conf:/etc/nginx/nginx.conf
        ```

## 도메인과 SSL 환경설정

### Let’s Encrypt 인증 방식

다음과 같이 구현하고 싶을 때 사용할 수 있는 방식입니다.

- 상업 서비스를 위해 openvidu를 사용하고 싶을 때 (물론 개발 목적으로도 가능)
- Fully Qualified Domain Name(FQDN)으로 사용하고 싶을 때
- 유효한 SSL 인증을 사용하고 싶을 때

구현을 위해서는 아래 내용에 따르세요.

- 서버의 Public IP를 가리키는 FQDN을 등록하기
    - Type A 방식으로 public IP를 가리키는 DNS 등록을 하면 됩니다.
    (gabia 같은 곳에서 도메인 DNS 등록하는 것을 말함)
- `/opt/openvidu/.env` 파일 수정하기
    - 파일을 다음 내용과 같이 수정하세요.
    
    ```bash
    DOMAIN_OR_PUBLIC_IP=example.openvidu.io  # 위에서 등록한 도메인을 적으면 됩니다.
    
    CERTIFICATE_TYPE=letsencrypt
    
    OPENVIDU_SECRET=YOUR_SECRET  # 임의의 비밀번호. 튜토리얼에서는 MY_SECRET 이었습니다.
    
    LETSENCRYPT_EMAIL=youremail@youremail.com  # let's encrypt를 활용하기 위해 이메일 주소를 적습니다.
    ```
    

### tip: nginx 커스터마이징

location 사용법

NGINX의 proxy_pass 기능은 들어오는 요청을 받아서 특정 위치로 보내고, 다시 돌아오는 응답을 받아서 클라이언트에게 돌려주는 것까지 수행해줍니다. (정말 좋네요)

HTTP 서버에 요청을 전달하기 위해, location 블록 안에 proxy_pass를 지정해보세요.

```bash
location /some/path/ {
    proxy_pass http://www.example.com/link/;
}
```

자세한 사용 방법은 [nginx 공식 문서](http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_pass)를 참고하세요.

<br>

# 기술적 챌린지

<br>

# 팀원

### 문준호
![junho github badge](https://img.shields.io/badge/GitHub-grey?logo=github&link=https://github.com/ProdMoon)

Team Leader, Frontend

<br>

# Off the Record
- 고민했던 부분
- 아쉬웠던 부분

<br>

---

MIT License. 2023. codeEATERS. All rights reserved.
