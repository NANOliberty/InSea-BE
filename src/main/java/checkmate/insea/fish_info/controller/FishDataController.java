package checkmate.insea.fish_info.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import checkmate.insea.fish_info.service.FishDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/fish-data")
@RequiredArgsConstructor
@Slf4j
public class FishDataController {
    
    private final FishDataService fishDataService;
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Fish API is working!");
    }
    /**
     * 엑셀 파일에서 어류 데이터 임포트
     */
    @PostMapping("/import")
    public ResponseEntity<String> importFishData(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어있습니다.");
            }
            
            if (!isExcelFile(file)) {
                return ResponseEntity.badRequest().body("엑셀 파일(.xlsx)만 업로드 가능합니다.");
            }
            
            fishDataService.importFishDataFromExcel(file);
            return ResponseEntity.ok("어류 데이터 임포트가 완료되었습니다.");
            
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("데이터 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 중복되지 않는 새로운 데이터만 추가
     */
    @PostMapping("/import-new")
    public ResponseEntity<String> importNewFishDataOnly(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 비어있습니다.");
            }
            
            if (!isExcelFile(file)) {
                return ResponseEntity.badRequest().body("엑셀 파일(.xlsx)만 업로드 가능합니다.");
            }
            
            fishDataService.importNewFishDataOnly(file);
            return ResponseEntity.ok("새로운 어류 데이터만 추가되었습니다.");
            
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("파일 처리 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("데이터 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 엑셀 파일 확장자 검증
     */
    private boolean isExcelFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".xlsx");
    }
}
