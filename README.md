# 네이버 로그인 로직

## 관련 spring 공식 문서
[네이버 로그인 관련 spring security공식 문서](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/core.html)     
[네이버 로그인 관련 문서](https://developers.naver.com/docs/login/api/api.md#3-1--%EB%84%A4%EC%9D%B4%EB%B2%84-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%EC%9D%B8%EC%A6%9D-%EC%9A%94%EC%B2%AD)



## oauth2/authorization/naver로 로그인 하는 이유
[관련 spring security 공식 문서1](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/authorization-grants.html#oauth2-client-authorization-code-authorization-request)  
[관련 spring security 공식 문서2](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/advanced.html#page-title)

<img width="1155" height="64" alt="oauth_direct" src="https://github.com/user-attachments/assets/9d67d2db-cc32-41d9-8716-f4f16e15869f" />       

- Spring Security가 내부적으로 OAuth2AuthorizationRequestRedirectFilter라는 필터를 가지고

  /oauth2/authorization/{registrationId} 패턴으로 요청을 가로채서 시작. 그 중 우리는 NAVER을 사용함으로 registrationId는 naver임. 그래서 로그인 페이지로 이동하기 위한  최종 api가 /oauth2/authorization/naver가 됨.

- Spring Security Oauth2 client를 사용하면 spring security의 미리 약속한 규칙인 (Endpoint)에 따라 이동 → 그래서 별도의 구현 없이도 아래 주소로 연결하면 로그인이 시작됨.

        http://localhost:8080/oauth2/authorization/naver

## 네이버에서 로그인을 시작하는 과정

1. Spring security를 이용해서 만들어진 `/oauth2/authorization/naver` 로 버튼 등을 이용해서 이동하게 됨
2. 이 때 Spring security는 이 api를 보고 `네이버` 로그인을 시작한다는 것을 알아챔!
3. 그리고 `application.yaml`에서 적은 authorization-uri, client-id, state 등을 합쳐서 네이버 개발자센터에서 명세로 표기한 진짜 네이버 인증 페이지인 `https://nid.naver.com/oauth2.0/authorize(네이버가 진짜 원하는 요청 url)`(이 요청 url의 출력 포맷이 url 리다이렉트 )로  리다이렉트함
4. 사용자가 네이버 로그인 및 동의 화면을 통해 로그인하면
5. 콜백(Callback): `localhost:8080/login/oauth2/code/naver?code=인증코드&state=상태값` ,즉 네이버에서 서버로 인증 결과를 틀고 우리의 서버로 복귀함

<img width="1236" height="538" alt="login_application" src="https://github.com/user-attachments/assets/0ec42958-aaec-49d6-b9dd-269221feaf8d" />     


## application.yaml

```
  security:
    oauth2:
      client:
        registration:
          naver:
            #            개발자 센터에서 발급받은 id
            client-id: ${NAVER_CLIENT_ID}
            client-secret: ${NAVER_CLIENT_SECRET}
            client-name: Naver
            #            네이버 명세서 상 response_type를 의미 authorization_code로 네이버가 요구하는 code로 전송
            # 즉, 인가 code를 받아 'access token'으로 바꾸는 과정을 Spring security 내부적으로 수행하도록 지정
            authorization-grant-type: authorization_code
            #            서비스 환경에 등록한 callback url을 의미
            # [콜백 주소] 네이버 로그인이 성공하면 어디로 돌아올지 결정
            redirect-uri: "{baseUrl}/login/oauth2/code/naver"
            #            state: state는 spring security가 내부적으로 무작위의 문자열을 생성하고 나중에 검증까지 자동으로 수행
            #            ? 그래서 스프링 세큐리티는 왜 쓰는가
            #              state는 csrf 공격을 방어하기 위한 필수 장치
            #              직접 구현 시에는 무작위의 문자열을 생성해 세션에 저장하고, 네이버가 다시 보내준 값과 세션이
            #              일치하는지 일일이 비교하는 코드를 짜야 됨
            #              spring security를 사용하면 이 과정을 자동으로 처리해서 state값이 다르면 인증을 즉시 거부해
            #              보안 사고를 예방
            #            scope는 네이버 로그인 시 허용받을 권한 리스트를 의미한다.
            # [권한 범위] 가져올 정보의 종류
            scope:
              - name
              - email
              - nickname
              - profile_image
        provider:
          naver:
          # [1단계] 사용자를 네이버 로그인 화면으로 보낼 주소
            #            네이버 로그인 인증 요청 url (authorization-uri)
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            #            네이버 접큰 토큰/ 갱신/삭제 요청 (출력포맷 json)
            # [2단계] 인가 코드를 가져가서 Access Token으로 바꿔올 주소
            # 자바 예제에서 "String token = "YOUR_ACCESS_TOKEN"을 얻기 위한 과정
            # 이 과정은 Spring Security의 내부 로직에서 확인되서 내가 직관적으로 확인할 수 없었음.
            # Spring Security에서 yaml파일에 token-uri, client-id, client-secret을 이용해서
            # DefaultAuthorizationCodeTokenResponseClient라는 클래스가 이역할을 대힌 해준다고 함.
            token-uri: https://nid.naver.com/oauth2.0/token
            
            #[3단계] JAVA 예제에서 apiURL을 의미
            # 얻어온 Access Token을 Bearer 헤더에 담아 실제로 유저 정보를 요청하는 주소
            #            네이버 회원 프로필 조회 (출력포맷 JSON)
            user-info-uri: https://openapi.naver.com/v1/nid/me
            
            #[4단계] 데이터 파싱
            # 실제 데이터가 'response'라는 키 안에 들어있다고 Spring에게 알려주는 설정
            user-name-attribute: response
            
            
  #  전체 동작 흐름 즉 yaml설정을 읽어 명세서대로
  #  authorization-uri를 읽어서 response_type=code&client_id=..&state-...&redirect_uri=..의
  #  형태를 만들어서 사용자 브라우저를 rediredct
  #  사용자가 네이버 로그인을 완료하면 네이버가 state, code(인증코드)를 가지고 설정된 redirect_uri로 돌아옴
  #
  #  spring security가 state를 검증하고, 문제가 없으면 code를 사용해 네이버에서 access token을 받아옴
  #그래서 yaml을 통해 네이버 로그인 버튼을 누르면 아래의 정보로 전체 url을 자동으로 생성해버림
  #https://nid.naver.com/oauth2.0/authorize?client_id=YOUR_ID&response_type=code&redirect_uri=YOUR_URL&state=RANDOM_STRING
  #사용자가 로그인 클릭 -> spring boot가 yaml을 이용해서 로그인 요청 후 redirect -> 네이버: 로그인창 출력 및 인증 진행  
  ```

## 네이버 개발자 센터에서 제공하는 예시
- 위와 같은 로직을 spring security가 대신 수행해줌



```
// 네이버 API 예제 - 회원프로필 조회
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class ApiExamMemberProfile {


    public static void main(String[] args) {
		    // [인증 주체 설정] Spring이 내부적으로 보관중인 토큰을 꺼내 접두사를 붙여줌. 자동 처리됨
		    // application.yaml: client-id, client-secret, token-url
		    // Bearer은 OAuth2Authrizedclient가 붙여줌
        String token = "YOUR_ACCESS_TOKEN"; // 네이버 로그인 접근 토큰;
        String header = "Bearer " + token; // Bearer 다음에 공백 추가
				

        String apiURL = "https://openapi.naver.com/v1/nid/me";
				//application.yaml -> user-info-uri: https://openapi.naver.com/v1/nid/me

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("Authorization", header);
        String responseBody = get(apiURL,requestHeaders);


        System.out.println(responseBody);
    }


    private static String get(String apiUrl, Map<String, String> requestHeaders){
	    // spring security 내부 로직으로 자동 처리.
        HttpURLConnection con = connect(apiUrl);
        try {
        // Spring은 OAuth2 표준에 따라 유저 조회는 기본값이 GET
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
            // Spring security 내부적적으로 Authorization 헤더 자동 주입
                con.setRequestProperty(header.getKey(), header.getValue());
            }

						//application.yaml의 내부 예외 로직
						// Spring 에서 연결 실패 시 전영 예외인 OAuth2AuthenticationException을 발생시킴으로
						// 따로 if - else로 응답 코드를 검사할 필요가 없음
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }


    private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }


    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);


        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();
            

						// application.yaml: user-name-attribute: response + Jackson 라이브러리
						//  자바는 한 줄씩 읽어 긴 문자열로 만들지만 Spring은 이걸 읽어서 바로 Map객체로 변환함
						// 네이버는 네이버 개발자센서 문서에서 확인한 결과 response/id 이런 식으로 유저 정보를 response라는 주머니에 담아줌(
						// 근데 YAML에 user-name-atrribute: response라고 적어주면 Spring은 그 주머니를 알아서 알맹이만 꺼내줌.
            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }


            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }
}
```

## CustomOAuth2UserService
[Spring Security 관련 공식 문서](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/advanced.html#oauth2login-advanced-oauth2-user-service)


<img width="1172" height="87" alt="customOauth2userservice" src="https://github.com/user-attachments/assets/d14abf3c-5c67-4b11-bd14-f9ec8249dc5f" />        

- 관련 설명은 CustomOAuth2UserService 파일에 주석으로 자세히 적어놓았습니다.


## HttpCookieOAuth2AuthorizationRequestRepository


[spring Security 관련 공식 문서](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/authorization-grants.html#oauth2-client-authorization-code-authorization-request-repository)

<img width="1226" height="1072" alt="cookOauth2repository" src="https://github.com/user-attachments/assets/e38d50e8-77ca-429e-9ce6-342748f7dfd2" />

- 관련 설명은 HttpCookieOAuth2AuthorizationRequestRepository 파일에 주석으로 자세히 적어놓았습니다.


## OAuth2SuccessHandler
기본적으로 spring seucrity는 Oauth2 login 성공 후 세션(session)을 생성하고 기본 페이지로 리다이렉트함.
하지만 지금처럼 JWT를 발급하여 , 쿠키를 굽고, 프론트엔드 특정 url로 보내야된는 상황에서는 기본 설정 및 동작을 바꿔야됨. 
      