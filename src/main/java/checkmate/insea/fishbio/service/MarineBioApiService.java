package checkmate.insea.fishbio.service;

import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import checkmate.insea.fishbio.dto.MarineBioApiResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MarineBioApiService {
    
    @Value("${marine.bio.api.url:https://apis.data.go.kr/B553482/mbrisdataview2/taxonlist2}")
    private String apiUrl;
    
    @Value("${marine.bio.api.key}")
    private String serviceKey;
    
    private final RestTemplate restTemplate;
    
    public MarineBioApiService() {
        this.restTemplate = new RestTemplate();
    }
    
    public MarineBioApiResponse fetchMarineBioData(int pageNo, int numOfRows) {
        try {
            // URL 직접 구성 (serviceKey는 원본 그대로 사용)
            String url = String.format("%s?serviceKey=%s&pageNo=%d&numOfRows=%d", 
                    apiUrl, serviceKey, pageNo, numOfRows);

            log.info("API 호출 URL: {}", url);

            // URI 객체를 만들어 RestTemplate에 전달 → 추가 인코딩 방지
            
            String xmlResponse = restTemplate.getForObject(new URI(url), String.class);
            
            if (xmlResponse == null || xmlResponse.isEmpty()) {
                log.error("API 응답이 비어있습니다.");
                return null;
            }
            // UTF-8 강제 변환
            if (xmlResponse != null) {
                xmlResponse = new String(xmlResponse.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            }
            
            return parseXmlResponse(xmlResponse);
            
        } catch (Exception e) {
            log.error("API 호출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    public MarineBioApiResponse searchMarineBioData(String scientificName, String koreanName, 
                                                   String family, int pageNo, int numOfRows) {
        try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(apiUrl)
                     .append("?serviceKey=").append(serviceKey)
                     .append("&pageNo=").append(pageNo)
                     .append("&numOfRows=").append(numOfRows);
            
            if (scientificName != null && !scientificName.isEmpty()) {
                // 학명 공백만 %20 처리
                String encodedScientificName = scientificName.replace(" ", "%20");
                urlBuilder.append("&spcScitfNm=").append(encodedScientificName);
            }
            if (koreanName != null && !koreanName.isEmpty()) {
                // 한글 이름은 UTF-8 인코딩
                try {
                    String encodedKoreanName = java.net.URLEncoder.encode(koreanName, "UTF-8");
                    urlBuilder.append("&commKorNm=").append(encodedKoreanName);
                } catch (Exception e) {
                    log.warn("한글 이름 인코딩 실패: {}", koreanName);
                }
            }
            if (family != null && !family.isEmpty()) {
                urlBuilder.append("&Family=").append(family);
            }
            
            String url = urlBuilder.toString();
            log.info("검색 API 호출 URL: {}", url);
            
            // 여기서도 URI 직접 생성
            String xmlResponse = restTemplate.getForObject(new URI(url), String.class);
            
            if (xmlResponse == null || xmlResponse.isEmpty()) {
                log.error("API 응답이 비어있습니다.");
                return null;
            }
            
            return parseXmlResponse(xmlResponse);
            
        } catch (Exception e) {
            log.error("검색 API 호출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private MarineBioApiResponse parseXmlResponse(String xmlResponse) {
        try {
            JAXBContext context = JAXBContext.newInstance(MarineBioApiResponse.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (MarineBioApiResponse) unmarshaller.unmarshal(new StringReader(xmlResponse));
        } catch (JAXBException e) {
            log.error("XML 파싱 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}
