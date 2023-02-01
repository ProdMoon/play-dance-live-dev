# PLAY DANCE LIVE!

![JS badge](https://img.shields.io/badge/-Javascript-grey?logo=javascript)
![React badge](https://img.shields.io/badge/-React-grey?logo=react)
![Springboot badge](https://img.shields.io/badge/-Springboot-grey?logo=springboot)
![Redis badge](https://img.shields.io/badge/-Redis-grey?logo=redis)
![MySQL badge](https://img.shields.io/badge/-MySQL-grey?logo=mysql)

SW사관학교 정글 5기 **Team Project** _(2022.12.22 ~ 2023.1.28)_

<img src='https://user-images.githubusercontent.com/112556572/216054697-f2d0ef98-4b9a-4e48-9f9b-0a1deb2920b6.png' width='20%' height='20%'>

<br>

# 서비스 소개

## 실시간 댄스 배틀 플랫폼

PLAY DANCE LIVE! 는 실시간으로 1대 1 릴레이 댄스 배틀을 펼치고, 시청할 수 있는 플랫폼입니다.

## [시연 영상](https://youtu.be/SDAnYaX9bks)

[![시연영상](http://img.youtube.com/vi/SDAnYaX9bks/0.jpg)](https://youtu.be/SDAnYaX9bks)

## 서비스 소개

> **랜덤으로 나오는 댄스 챌린지를 소화하고 챔피언의 자리를 차지해라!**

- 랜덤으로 플레이 곡 선정

  ![그림1](https://user-images.githubusercontent.com/112556572/216065139-9f8c004f-1389-4ced-80ec-bc1f59158d49.gif)

- 챌린지 스타트! 실시간으로 응원하자

  ![그림2](https://user-images.githubusercontent.com/112556572/216065137-d4b67734-c797-4518-8d55-1ee52a54e69f.gif)

- 승자는 계속해서 플레이

  ![그림3](https://user-images.githubusercontent.com/112556572/216065133-6f762d05-4dd3-433f-9bce-d53c5000ff64.gif)

## 포스터

![포스터 저용량 복사](https://user-images.githubusercontent.com/112556572/216072534-f2a045aa-17cd-4489-939b-11ee2dd8bf55.jpg)

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

## 춤과 음악의 싱크를 맞추기 위한 방식 고민

### BEFORE : WebSocket을 이용한 Local Audio 재생 타이밍 동기화

<img width="80%" src="https://s3.us-west-2.amazonaws.com/secure.notion-static.com/c6864b9f-388d-479d-8146-c61ea46432f4/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20230201%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20230201T134507Z&X-Amz-Expires=86400&X-Amz-Signature=572ec4cd7fc735ecc231ce205f1fbac7b32076101761335ac26209c27366156a&X-Amz-SignedHeaders=host&response-content-disposition=filename%3D%22Untitled.png%22&x-id=GetObject"/>

<br>

**문제점**

- 음악이 시작하는 타이밍에만 signal이 전달되므로, 중간에 들어오는 시청자는 신호를 받지 못함
- 네트워크 지연으로 영상이 끊기게 되어도 음악은 계속 재생되기 때문에, 영상과 음악의 싱크가 어긋남

<br>

### AFTER : WebAudio API를 이용하여 직접 mp3파일을 AudioSource로 사용

<img width="80%" src="https://s3.us-west-2.amazonaws.com/secure.notion-static.com/73845a00-da93-41d2-a078-4f9abf578921/Untitled.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Content-Sha256=UNSIGNED-PAYLOAD&X-Amz-Credential=AKIAT73L2G45EIPT3X45%2F20230201%2Fus-west-2%2Fs3%2Faws4_request&X-Amz-Date=20230201T134558Z&X-Amz-Expires=86400&X-Amz-Signature=9fc3226aa9422cc0cba46e45b46742a2b25e8a3b5392917fd6a9f65db915bd8a&X-Amz-SignedHeaders=host&response-content-disposition=filename%3D%22Untitled.png%22&x-id=GetObject"/>

<br>

WebRTC 특성 상 mp3 파일을 직접 AudioSource로 사용할 수 없으나, **WebAudio API**를 이용하여 MediaStream을 생성하게 되면 가능해짐

영상과 음성이 한 스트림에 전송되므로 싱크 문제가 **해결됨**

- 중간에 진입하는 시청자도 중간부터 영상과 음성의 동시 수신 가능
- 영상이 끊겨도 음악과 싱크가 어긋나지 않음

<br>

# 팀원

### 문준호
[![junho github badge](https://img.shields.io/badge/GitHub-grey?logo=github)](https://github.com/ProdMoon)

Team Leader / 스트리밍, 진행 로직 설계

<br>

### 김다엘
[![dael github badge](https://img.shields.io/badge/GitHub-grey?logo=github)](https://github.com/Daeell)

Backend / 채팅, 소켓, 방송 데이터 관리

<br>

### 이강욱
[![kangwook github badge](https://img.shields.io/badge/GitHub-grey?logo=github)](https://github.com/rivolt0421)

Full Stack / 투표 시스템, 영상 레이아웃

<br>

### 정성현
[![sunghyun github badge](https://img.shields.io/badge/GitHub-grey?logo=github)](https://github.com/skqls)

Backend / DB, 인증 인가 시스템

<br>

# Off the Record

### 고민했던 부분
- 두 명이 동시에 춤추기
  - 현재 'PLAY DANCE LIVE!' 는 두 명의 참가자가 번갈아가며 춤을 추는 형식. 기획적으로는 두 사람이 동시에 춤 대결을 펼치는 것이 더 흥미로운 지점이 많았지만, 기술적으로는 많은 어려움이 예상되어 일단 번갈아 추는 것을 목표로 구현하고 각자의 춤과 노래의 싱크를 맞추는데 신경을 많이 썼음.
간단하게 생각해본 방안으로는, 춤을 추는 두 사람은 서로 P2P로 연결해 같은 타이밍에 맞춰 춤을 출 수 있게 하고, 중계 서버는 이 둘이 보내는 stream을 실제 음원에 해당하는 하나의 타이밍을 통해 mixing한 후 산출된 stream을 시청자들에게 송출하는 방법을 시도해 볼 수 있음. 다만 이 경우 춤추는 두 사람과 시청자들 간에는 서버의 연산시간 만큼의 지연이 발생하게 됨.

- 여러 개의 채널 중 골라서 입장하기
  - 하나의 배틀을 온전히 진행할 수 있게 된 후, 음악 장르별 채널을 다양화해(e.g. K-pop, 1990s, ...) 각 채널에서 서로 다른 배틀이 이루어질 수 있게 추가 기획을 한 바 있음. 추가적인 구현이 필요함.

### 발전시킬 부분
- 어뷰징 방지
  - 투표 시스템을 악용해 반복문으로 투표 클릭 신호를 서버로 보내는 등의 어뷰징 행위를 방지해야 함. 버튼을 통한 api 요청에 '쿨타임' 같은 딜레이를 적용한다던지, 더 안전하게는 요청을 받는 서버가 같은 로그인 정보를 가진 요청에 대해 일정 시간내 최대 요청의 개수를 제한하는 방법이 있음.
- 배틀 시간 운영
  - 시청자 대기열이 비어있는 상황을 피하고자 배틀 시간대를 지정하여 운영함. (e.g. 오후반은 1시에 시작. 저녁반은 21시에 시작.)

<br>

---

MIT License. 2023. codeEATERS. All rights reserved.
